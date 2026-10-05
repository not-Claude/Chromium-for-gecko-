package com.example.engine

import android.content.Context
import android.content.pm.ApplicationInfo
import android.net.Uri
import android.os.Handler
import android.os.Looper
import com.example.model.ExtensionItem
import org.mozilla.geckoview.AllowOrDeny
import org.mozilla.geckoview.GeckoResult
import org.mozilla.geckoview.GeckoRuntime
import org.mozilla.geckoview.GeckoRuntimeSettings
import org.mozilla.geckoview.GeckoSession
import org.mozilla.geckoview.GeckoSessionSettings
import org.mozilla.geckoview.WebExtension
import org.mozilla.geckoview.WebExtensionController
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.Executors

/**
 * High-performance GeckoView manager with Chrome Web Store extension pipeline.
 */
class GeckoManager private constructor(private val appContext: Context) {

    companion object {
        const val HOME_URL = "about:blank"
        const val CHROME_WEB_STORE_URL = "https://chromewebstore.google.com"

        const val CHROME_MOBILE_UA =
            "Mozilla/5.0 (Linux; Android 14; Pixel 8) AppleWebKit/537.36 (KHTML, like Gecko) " +
                "Chrome/126.0.0.0 Mobile Safari/537.36"

        const val CHROME_DESKTOP_UA =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) " +
                "Chrome/126.0.0.0 Safari/537.36"

        /** Matches both new and legacy Chrome Web Store URLs */
        val STORE_REGEX = Regex(
            """(?:chromewebstore\.google\.com|chrome\.google\.com/webstore)/detail/(?:[^/?#]+/)?([a-p]{32})"""
        )

        @Volatile
        private var instance: GeckoManager? = null

        fun getInstance(context: Context): GeckoManager =
            instance ?: synchronized(this) {
                instance ?: GeckoManager(context.applicationContext).also { instance = it }
            }
    }

    private val mainHandler = Handler(Looper.getMainLooper())
    private val executor = Executors.newFixedThreadPool(3)

    val runtime: GeckoRuntime by lazy {
        initGeckoRuntime()
    }

    var onExtensionPrompt: ((WebExtension, List<String>, (Boolean) -> Unit) -> Unit)? = null
    var onInstallStatus: ((String, Boolean, String?) -> Unit)? = null

    private fun initGeckoRuntime(): GeckoRuntime {
        // High-performance configuration with hardware acceleration, APZ smooth scrolling,
        // and unsigned extension support for Chrome Web Store packages.
        val config = File(appContext.filesDir, "geckoview-config.yaml")
        try {
            config.writeText(
                """
                prefs:
                  xpinstall.signatures.required: false
                  extensions.langpacks.signatures.required: false
                  extensions.experiments.enabled: true
                  apz.overscroll.enabled: true
                  layers.acceleration.force-enabled: true
                  gfx.webrender.all: true
                  network.http.pipelining: true
                  dom.webgpu.enabled: true
                  browser.tabs.remote.autostart: true
                """.trimIndent() + "\n"
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val debuggable = (appContext.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0

        val settings = GeckoRuntimeSettings.Builder()
            .configFilePath(config.absolutePath)
            .javaScriptEnabled(true)
            .aboutConfigEnabled(true)
            .remoteDebuggingEnabled(debuggable)
            .build()

        val runtime = GeckoRuntime.create(appContext, settings)

        runtime.webExtensionController.promptDelegate = object : WebExtensionController.PromptDelegate {
            override fun onInstallPrompt(extension: WebExtension): GeckoResult<AllowOrDeny>? {
                val result = GeckoResult<AllowOrDeny>()
                val meta = extension.metaData
                val perms = meta?.promptPermissions?.toList() ?: emptyList()
                val origs = meta?.origins?.toList() ?: emptyList()

                val permsList = buildList {
                    addAll(perms)
                    if (origs.any { it == "<all_urls>" || it == "*://*/*" }) {
                        add("Доступ ко всем сайтам")
                    } else if (origs.isNotEmpty()) {
                        add("Доступ к ${origs.size} сайтам")
                    }
                }.distinct()

                mainHandler.post {
                    val prompt = onExtensionPrompt
                    if (prompt != null) {
                        prompt(extension, permsList) { allow ->
                            result.complete(if (allow) AllowOrDeny.ALLOW else AllowOrDeny.DENY)
                        }
                    } else {
                        result.complete(AllowOrDeny.ALLOW)
                    }
                }
                return result
            }
        }

        return runtime
    }

    fun createSession(isDesktop: Boolean = false): GeckoSession {
        val settings = GeckoSessionSettings.Builder()
            .usePrivateMode(false)
            .userAgentMode(
                if (isDesktop) GeckoSessionSettings.USER_AGENT_MODE_DESKTOP
                else GeckoSessionSettings.USER_AGENT_MODE_MOBILE
            )
            .viewportMode(GeckoSessionSettings.VIEWPORT_MODE_MOBILE)
            .build()

        val session = GeckoSession(settings)
        session.open(runtime)
        return session
    }

    fun setDesktopMode(session: GeckoSession, isDesktop: Boolean) {
        session.settings.userAgentMode = if (isDesktop) {
            GeckoSessionSettings.USER_AGENT_MODE_DESKTOP
        } else {
            GeckoSessionSettings.USER_AGENT_MODE_MOBILE
        }
        session.reload()
    }

    /**
     * Download and install an extension from Chrome Web Store by 32-char extension ID.
     */
    fun downloadAndInstallExtension(extensionId: String, extensionTitle: String = "Расширение") {
        executor.execute {
            notifyInstallStatus(extensionTitle, false, "Скачивание из Chrome Web Store…")
            val crxFile = File(appContext.cacheDir, "$extensionId.crx")
            val rawZip = File(appContext.cacheDir, "${extensionId}_raw.zip")
            val patchedZip = File(appContext.cacheDir, "${extensionId}_patched.zip")

            try {
                val downloadUrl = URL(
                    "https://clients2.google.com/service/update2/crx" +
                        "?response=redirect&prodversion=126.0.0.0&acceptformat=crx2,crx3" +
                        "&x=id%3D$extensionId%26installsource%3Dondemand%26uc"
                )

                val connection = (downloadUrl.openConnection() as HttpURLConnection).apply {
                    connectTimeout = 15_000
                    readTimeout = 30_000
                    instanceFollowRedirects = true
                    setRequestProperty("User-Agent", CHROME_MOBILE_UA)
                }

                if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                    cleanupFiles(crxFile, rawZip, patchedZip)
                    notifyInstallStatus(extensionTitle, false, "Ошибка скачивания: HTTP ${connection.responseCode}")
                    connection.disconnect()
                    return@execute
                }

                connection.inputStream.use { input ->
                    FileOutputStream(crxFile).use { output -> input.copyTo(output) }
                }
                connection.disconnect()

                if (crxFile.length() == 0L) {
                    cleanupFiles(crxFile, rawZip, patchedZip)
                    notifyInstallStatus(extensionTitle, false, "Ошибка: пустой файл CRX")
                    return@execute
                }

                notifyInstallStatus(extensionTitle, false, "Конвертация CRX и адаптация Manifest…")

                // Step 1: Strip CRX headers to ZIP
                if (!CrxConverter.crxToZip(crxFile, rawZip)) {
                    cleanupFiles(crxFile, rawZip, patchedZip)
                    notifyInstallStatus(extensionTitle, false, "Ошибка конвертации архива CRX")
                    return@execute
                }

                // Step 2: Inject gecko.id and convert service_worker to background scripts
                if (!ManifestPatcher.injectGeckoId(rawZip, patchedZip, extensionId)) {
                    cleanupFiles(crxFile, rawZip, patchedZip)
                    notifyInstallStatus(extensionTitle, false, "Ошибка модификации manifest.json")
                    return@execute
                }

                notifyInstallStatus(extensionTitle, false, "Установка в GeckoView…")

                val fileUri = Uri.fromFile(patchedZip).toString()

                mainHandler.post {
                    runtime.webExtensionController.install(fileUri)
                        .accept(
                            { installed ->
                                val name = installed?.metaData?.name ?: extensionTitle
                                cleanupFiles(crxFile, rawZip, patchedZip)
                                notifyInstallStatus(name, true, "Успешно установлено!")
                            },
                            { error ->
                                cleanupFiles(crxFile, rawZip, patchedZip)
                                val msg = error?.message ?: "Неизвестная ошибка установки"
                                notifyInstallStatus(extensionTitle, false, "Ошибка установки: $msg")
                            }
                        )
                }

            } catch (e: Exception) {
                e.printStackTrace()
                cleanupFiles(crxFile, rawZip, patchedZip)
                notifyInstallStatus(extensionTitle, false, "Исключение: ${e.message}")
            }
        }
    }

    fun listInstalledExtensions(callback: (List<ExtensionItem>) -> Unit) {
        runtime.webExtensionController.list()
            .accept(
                { list ->
                    val items = list?.map { ext ->
                        ExtensionItem(
                            id = ext.id,
                            name = ext.metaData?.name ?: ext.id,
                            version = ext.metaData?.version ?: "1.0",
                            description = ext.metaData?.description ?: "",
                            isEnabled = ext.metaData?.enabled ?: true
                        )
                    } ?: emptyList()
                    mainHandler.post { callback(items) }
                },
                {
                    mainHandler.post { callback(emptyList()) }
                }
            )
    }

    fun uninstallExtension(extensionId: String, callback: (Boolean) -> Unit) {
        runtime.webExtensionController.list().accept({ list ->
            val ext = list?.find { it.id == extensionId || it.id.startsWith(extensionId) }
            if (ext != null) {
                runtime.webExtensionController.uninstall(ext).accept({
                    mainHandler.post { callback(true) }
                }, {
                    mainHandler.post { callback(false) }
                })
            } else {
                mainHandler.post { callback(false) }
            }
        }, {
            mainHandler.post { callback(false) }
        })
    }

    fun toggleExtension(extensionId: String, enable: Boolean, callback: (Boolean) -> Unit) {
        runtime.webExtensionController.list().accept({ list ->
            val ext = list?.find { it.id == extensionId || it.id.startsWith(extensionId) }
            if (ext != null) {
                val action = if (enable) {
                    runtime.webExtensionController.enable(ext, WebExtensionController.EnableSource.USER)
                } else {
                    runtime.webExtensionController.disable(ext, WebExtensionController.EnableSource.USER)
                }
                action.accept({
                    mainHandler.post { callback(true) }
                }, {
                    mainHandler.post { callback(false) }
                })
            } else {
                mainHandler.post { callback(false) }
            }
        }, {
            mainHandler.post { callback(false) }
        })
    }

    private fun cleanupFiles(vararg files: File) {
        for (f in files) {
            try {
                if (f.exists()) f.delete()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun notifyInstallStatus(title: String, success: Boolean, message: String?) {
        mainHandler.post {
            onInstallStatus?.invoke(title, success, message)
        }
    }
}
