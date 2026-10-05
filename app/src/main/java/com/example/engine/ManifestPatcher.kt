package com.example.engine

import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

/**
 * Manifest patcher for Chrome Web Store extensions in GeckoView:
 * 1. Injects gecko.id into browser_specific_settings for persistent extension settings
 * 2. Adapts MV3 service_worker to background.scripts
 * 3. Strips Chrome _metadata folder
 * 4. Zip slip protection
 */
object ManifestPatcher {

    fun injectGeckoId(zipInputFile: File, zipOutputFile: File, extensionId: String): Boolean {
        return try {
            var manifestFound = false

            ZipInputStream(BufferedInputStream(FileInputStream(zipInputFile))).use { zis ->
                ZipOutputStream(BufferedOutputStream(FileOutputStream(zipOutputFile))).use { zos ->
                    var entry: ZipEntry? = zis.nextEntry

                    while (entry != null) {
                        val name = entry.name
                        val unsafe = name.startsWith("/") || name.split('/').any { it == ".." }

                        if (!unsafe && !name.startsWith("_metadata/")) {
                            zos.putNextEntry(ZipEntry(name))
                            when {
                                entry.isDirectory -> Unit
                                name == "manifest.json" -> {
                                    manifestFound = true
                                    val src = zis.readBytes().toString(Charsets.UTF_8)
                                    zos.write(patchJson(src, extensionId).toByteArray(Charsets.UTF_8))
                                }
                                else -> zis.copyTo(zos)
                            }
                            zos.closeEntry()
                        }

                        zis.closeEntry()
                        entry = zis.nextEntry
                    }
                }
            }
            manifestFound
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    private fun patchJson(jsonString: String, extensionId: String): String {
        val json = JSONObject(jsonString.removePrefix("\uFEFF"))

        val bss = json.optJSONObject("browser_specific_settings") ?: JSONObject()
        val gecko = bss.optJSONObject("gecko") ?: JSONObject()
        if (!gecko.has("id")) {
            gecko.put("id", "$extensionId@chrome-web-store")
            bss.put("gecko", gecko)
            json.put("browser_specific_settings", bss)
        }

        val bg = json.optJSONObject("background")
        if (bg != null && bg.has("service_worker") && !bg.has("scripts")) {
            bg.put("scripts", JSONArray().put(bg.getString("service_worker")))
        }

        return json.toString(2)
    }
}
