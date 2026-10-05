package com.example.ui

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.engine.GeckoManager
import com.example.model.Bookmark
import com.example.model.BrowserTab
import com.example.model.ExtensionItem
import com.example.model.HistoryItem
import com.example.model.SecurityLevel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.mozilla.geckoview.GeckoSession

enum class ActiveDialog {
    NONE,
    EXTENSIONS,
    BOOKMARKS,
    HISTORY,
    DOWNLOADS,
    SETTINGS,
    SITE_INFO,
    HELP
}

data class ExtensionPrompt(
    val extensionId: String,
    val title: String
)

data class PermissionPrompt(
    val extensionName: String,
    val permissions: List<String>,
    val onResponse: (Boolean) -> Unit
)

data class BrowserUiState(
    val tabs: List<BrowserTab> = emptyList(),
    val activeTabId: String = "",
    val isTabSwitcherOpen: Boolean = false,
    val isMenuExpanded: Boolean = false,
    val isOmniboxEditing: Boolean = false,
    val activeDialog: ActiveDialog = ActiveDialog.NONE,
    val bookmarks: List<Bookmark> = emptyList(),
    val history: List<HistoryItem> = emptyList(),
    val extensions: List<ExtensionItem> = emptyList(),
    val extensionInstallPrompt: ExtensionPrompt? = null,
    val permissionPrompt: PermissionPrompt? = null,
    val statusMessage: String? = null,
    val isArticlesExpanded: Boolean = false,
    val searchEngine: String = "Google" // Google, Yandex, DuckDuckGo, Bing
) {
    val activeTab: BrowserTab?
        get() = tabs.find { it.id == activeTabId } ?: tabs.firstOrNull()

    val openTabsCount: Int
        get() = tabs.size

    val isCurrentBookmarked: Boolean
        get() {
            val url = activeTab?.url ?: return false
            return bookmarks.any { it.url == url }
        }
}

class BrowserViewModel(application: Application) : AndroidViewModel(application) {

    private val geckoManager = GeckoManager.getInstance(application)

    private val _uiState = MutableStateFlow(BrowserUiState())
    val uiState: StateFlow<BrowserUiState> = _uiState.asStateFlow()

    private var offeredExtensionIds = mutableSetOf<String>()

    init {
        // Setup initial home tab
        addNewTab(isIncognito = false, initialUrl = "")

        // Setup default bookmarks
        val defaultBookmarks = listOf(
            Bookmark(title = "Chrome Web Store", url = GeckoManager.CHROME_WEB_STORE_URL),
            Bookmark(title = "The Chromium Projects", url = "https://www.chromium.org"),
            Bookmark(title = "Google", url = "https://www.google.com"),
            Bookmark(title = "GitHub", url = "https://github.com")
        )
        _uiState.update { it.copy(bookmarks = defaultBookmarks) }

        // Setup GeckoManager listeners
        geckoManager.onExtensionPrompt = { ext, perms, callback ->
            val name = ext.metaData?.name ?: "Расширение"
            _uiState.update {
                it.copy(
                    permissionPrompt = PermissionPrompt(
                        extensionName = name,
                        permissions = perms,
                        onResponse = { allow ->
                            _uiState.update { s -> s.copy(permissionPrompt = null) }
                            callback(allow)
                        }
                    )
                )
            }
        }

        geckoManager.onInstallStatus = { title, success, msg ->
            _uiState.update {
                it.copy(
                    statusMessage = if (success) "«$title» установлено!" else msg
                )
            }
            if (success) {
                refreshExtensions()
            }
        }

        refreshExtensions()
    }

    fun clearStatusMessage() {
        _uiState.update { it.copy(statusMessage = null) }
    }

    fun addNewTab(isIncognito: Boolean = false, initialUrl: String = ""): BrowserTab {
        val session = geckoManager.createSession()
        val newTab = BrowserTab(
            title = if (initialUrl.isEmpty()) "Новая вкладка" else initialUrl,
            url = initialUrl,
            isIncognito = isIncognito,
            geckoSession = session
        )

        attachSessionListeners(session, newTab.id)

        if (initialUrl.isNotEmpty()) {
            session.loadUri(initialUrl)
        }

        _uiState.update { state ->
            val updated = state.tabs + newTab
            state.copy(
                tabs = updated,
                activeTabId = newTab.id,
                isTabSwitcherOpen = false,
                isMenuExpanded = false
            )
        }

        return newTab
    }

    fun selectTab(tabId: String) {
        _uiState.update {
            it.copy(
                activeTabId = tabId,
                isTabSwitcherOpen = false,
                isMenuExpanded = false
            )
        }
    }

    fun closeTab(tabId: String) {
        val currentTabs = _uiState.value.tabs
        val tabToClose = currentTabs.find { it.id == tabId }
        tabToClose?.geckoSession?.close()

        val remaining = currentTabs.filterNot { it.id == tabId }
        if (remaining.isEmpty()) {
            // Always keep at least one tab
            _uiState.update { it.copy(tabs = emptyList()) }
            addNewTab()
        } else {
            val newActiveId = if (_uiState.value.activeTabId == tabId) {
                remaining.last().id
            } else {
                _uiState.value.activeTabId
            }
            _uiState.update {
                it.copy(
                    tabs = remaining,
                    activeTabId = newActiveId
                )
            }
        }
    }

    fun closeAllTabs() {
        _uiState.value.tabs.forEach { it.geckoSession?.close() }
        _uiState.update { it.copy(tabs = emptyList()) }
        addNewTab()
    }

    fun toggleTabSwitcher() {
        _uiState.update {
            it.copy(
                isTabSwitcherOpen = !it.isTabSwitcherOpen,
                isMenuExpanded = false,
                isOmniboxEditing = false
            )
        }
    }

    fun toggleMenu() {
        _uiState.update { it.copy(isMenuExpanded = !it.isMenuExpanded) }
    }

    fun closeMenu() {
        _uiState.update { it.copy(isMenuExpanded = false) }
    }

    fun setOmniboxEditing(editing: Boolean) {
        _uiState.update { it.copy(isOmniboxEditing = editing, isMenuExpanded = false) }
    }

    fun openDialog(dialog: ActiveDialog) {
        _uiState.update { it.copy(activeDialog = dialog, isMenuExpanded = false) }
        if (dialog == ActiveDialog.EXTENSIONS) {
            refreshExtensions()
        }
    }

    fun closeDialog() {
        _uiState.update { it.copy(activeDialog = ActiveDialog.NONE) }
    }

    fun toggleArticles() {
        _uiState.update { it.copy(isArticlesExpanded = !it.isArticlesExpanded) }
    }

    fun loadUrl(url: String) {
        val tab = _uiState.value.activeTab ?: return
        var formatted = url.trim()
        if (!formatted.startsWith("http://") && !formatted.startsWith("https://") && !formatted.startsWith("about:")) {
            formatted = if (formatted.contains(".") && !formatted.contains(" ")) {
                "https://$formatted"
            } else {
                // Search query
                val query = Uri.encode(formatted)
                when (_uiState.value.searchEngine) {
                    "Yandex" -> "https://yandex.ru/search/?text=$query"
                    "DuckDuckGo" -> "https://duckduckgo.com/?q=$query"
                    "Bing" -> "https://www.bing.com/search?q=$query"
                    else -> "https://www.google.com/search?q=$query"
                }
            }
        }

        tab.url = formatted
        tab.isLoading = true
        tab.progress = 10
        tab.geckoSession?.loadUri(formatted)

        _uiState.update { state ->
            val updatedTabs = state.tabs.map { if (it.id == tab.id) tab.copy(url = formatted, isLoading = true) else it }
            state.copy(
                tabs = updatedTabs,
                isOmniboxEditing = false
            )
        }
    }

    fun goHome() {
        val tab = _uiState.value.activeTab ?: return
        tab.url = ""
        tab.title = "Новая вкладка"
        tab.isLoading = false
        tab.progress = 0
        tab.geckoSession?.loadUri("about:blank")
        _uiState.update { state ->
            val updatedTabs = state.tabs.map { if (it.id == tab.id) tab.copy(url = "", title = "Новая вкладка", isLoading = false) else it }
            state.copy(tabs = updatedTabs, isOmniboxEditing = false)
        }
    }

    fun goBack() {
        _uiState.value.activeTab?.geckoSession?.goBack()
    }

    fun goForward() {
        _uiState.value.activeTab?.geckoSession?.goForward()
    }

    fun reload() {
        _uiState.value.activeTab?.geckoSession?.reload()
    }

    fun toggleDesktopMode() {
        val tab = _uiState.value.activeTab ?: return
        val newDesktop = !tab.isDesktopMode
        tab.isDesktopMode = newDesktop
        tab.geckoSession?.let { session ->
            geckoManager.setDesktopMode(session, newDesktop)
        }
        _uiState.update { state ->
            val updatedTabs = state.tabs.map { if (it.id == tab.id) tab.copy(isDesktopMode = newDesktop) else it }
            state.copy(tabs = updatedTabs, isMenuExpanded = false)
        }
    }

    fun toggleBookmark() {
        val tab = _uiState.value.activeTab ?: return
        val currentUrl = tab.url
        if (currentUrl.isEmpty() || currentUrl == "about:blank") return

        val existing = _uiState.value.bookmarks.find { it.url == currentUrl }
        if (existing != null) {
            _uiState.update { it.copy(bookmarks = it.bookmarks.filterNot { b -> b.url == currentUrl }) }
        } else {
            val bookmark = Bookmark(title = tab.title.ifEmpty { tab.displayHost }, url = currentUrl)
            _uiState.update { it.copy(bookmarks = it.bookmarks + bookmark) }
        }
    }

    fun deleteBookmark(url: String) {
        _uiState.update { it.copy(bookmarks = it.bookmarks.filterNot { b -> b.url == url }) }
    }

    fun clearHistory() {
        _uiState.update { it.copy(history = emptyList()) }
    }

    fun setSearchEngine(engine: String) {
        _uiState.update { it.copy(searchEngine = engine) }
    }

    // Extension Management
    fun installExtensionFromStore(extensionId: String, title: String) {
        _uiState.update { it.copy(extensionInstallPrompt = null) }
        geckoManager.downloadAndInstallExtension(extensionId, title)
    }

    fun dismissExtensionPrompt() {
        _uiState.update { it.copy(extensionInstallPrompt = null) }
    }

    fun uninstallExtension(extensionId: String) {
        geckoManager.uninstallExtension(extensionId) { success ->
            if (success) {
                refreshExtensions()
                _uiState.update { it.copy(statusMessage = "Расширение удалено") }
            }
        }
    }

    fun toggleExtension(extensionId: String, enable: Boolean) {
        geckoManager.toggleExtension(extensionId, enable) { success ->
            if (success) {
                refreshExtensions()
            }
        }
    }

    fun refreshExtensions() {
        geckoManager.listInstalledExtensions { list ->
            _uiState.update { it.copy(extensions = list) }
        }
    }

    private fun attachSessionListeners(session: GeckoSession, tabId: String) {
        session.navigationDelegate = object : GeckoSession.NavigationDelegate {
            override fun onLocationChange(
                s: GeckoSession,
                url: String?,
                perms: MutableList<GeckoSession.PermissionDelegate.ContentPermission>,
                hasUserGesture: Boolean
            ) {
                if (url == null || url == "about:blank") return

                viewModelScope.launch {
                    _uiState.update { state ->
                        val updated = state.tabs.map { tab ->
                            if (tab.id == tabId) {
                                tab.copy(
                                    url = url,
                                    securityLevel = if (url.startsWith("https://")) SecurityLevel.SECURE else SecurityLevel.INSECURE
                                )
                            } else tab
                        }
                        state.copy(tabs = updated)
                    }

                    // Check for Chrome Web Store extension page
                    val extId = GeckoManager.STORE_REGEX.find(url)?.groupValues?.get(1)
                    if (extId != null && !offeredExtensionIds.contains(extId)) {
                        offeredExtensionIds.add(extId)
                        val title = _uiState.value.tabs.find { it.id == tabId }?.title
                            ?.removeSuffix(" - Chrome Web Store")
                            ?.takeIf { it.isNotBlank() } ?: "Расширение Chrome"

                        _uiState.update {
                            it.copy(
                                extensionInstallPrompt = ExtensionPrompt(
                                    extensionId = extId,
                                    title = title
                                )
                            )
                        }
                    }
                }
            }

            override fun onCanGoBack(s: GeckoSession, canGoBack: Boolean) {
                _uiState.update { state ->
                    val updated = state.tabs.map { if (it.id == tabId) it.copy(canGoBack = canGoBack) else it }
                    state.copy(tabs = updated)
                }
            }

            override fun onCanGoForward(s: GeckoSession, canGoForward: Boolean) {
                _uiState.update { state ->
                    val updated = state.tabs.map { if (it.id == tabId) it.copy(canGoForward = canGoForward) else it }
                    state.copy(tabs = updated)
                }
            }
        }

        session.contentDelegate = object : GeckoSession.ContentDelegate {
            override fun onTitleChange(s: GeckoSession, title: String?) {
                if (title.isNullOrBlank()) return
                viewModelScope.launch {
                    _uiState.update { state ->
                        val updated = state.tabs.map { tab ->
                            if (tab.id == tabId) {
                                tab.copy(title = title)
                            } else tab
                        }
                        // Also record in history if not blank and not about:blank
                        val active = state.tabs.find { it.id == tabId }
                        val hist = if (active != null && active.url.isNotBlank() && !active.url.startsWith("about:") && !active.isIncognito) {
                            val newHist = HistoryItem(title = title, url = active.url)
                            (listOf(newHist) + state.history).distinctBy { it.url }.take(100)
                        } else {
                            state.history
                        }

                        // Update pending extension prompt title if it matches current page
                        val currentPrompt = state.extensionInstallPrompt
                        val updatedPrompt = if (currentPrompt != null && currentPrompt.title == "Расширение Chrome") {
                            val cleanTitle = title.removeSuffix(" - Chrome Web Store").trim()
                            currentPrompt.copy(title = cleanTitle)
                        } else currentPrompt

                        state.copy(
                            tabs = updated,
                            history = hist,
                            extensionInstallPrompt = updatedPrompt
                        )
                    }
                }
            }
        }

        session.progressDelegate = object : GeckoSession.ProgressDelegate {
            override fun onPageStart(s: GeckoSession, url: String) {
                _uiState.update { state ->
                    val updated = state.tabs.map { if (it.id == tabId) it.copy(isLoading = true, progress = 15) else it }
                    state.copy(tabs = updated)
                }
            }

            override fun onPageStop(s: GeckoSession, success: Boolean) {
                _uiState.update { state ->
                    val updated = state.tabs.map { if (it.id == tabId) it.copy(isLoading = false, progress = 100) else it }
                    state.copy(tabs = updated)
                }
            }

            override fun onProgressChange(s: GeckoSession, progress: Int) {
                _uiState.update { state ->
                    val updated = state.tabs.map { if (it.id == tabId) it.copy(progress = progress, isLoading = progress < 100) else it }
                    state.copy(tabs = updated)
                }
            }
        }
    }
}
