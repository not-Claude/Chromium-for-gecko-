package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.model.BrowserTab
import com.example.ui.ActiveDialog
import com.example.ui.BrowserViewModel
import com.example.ui.components.BookmarksDialog
import com.example.ui.components.ChromeMenu
import com.example.ui.components.ChromeToolbar
import com.example.ui.components.ExtensionsSheet
import com.example.ui.components.HistoryDialog
import com.example.ui.components.InstallExtensionDialog
import com.example.ui.components.NewTabPage
import com.example.ui.components.OmniboxEditor
import com.example.ui.components.PermissionPromptDialog
import com.example.ui.components.SettingsDialog
import com.example.ui.components.SiteInfoDialog
import com.example.ui.components.TabsSwitcher
import com.example.ui.theme.ChromeDarkBg
import com.example.ui.theme.ChromeLightBg
import com.example.ui.theme.MyApplicationTheme
import org.mozilla.geckoview.GeckoView

class MainActivity : ComponentActivity() {

    private val viewModel: BrowserViewModel by viewModels()
    private var sharedGeckoView: GeckoView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                val uiState by viewModel.uiState.collectAsState()
                val snackbarHostState = remember { SnackbarHostState() }

                // Show status toasts/snackbars (e.g. extension download progress)
                LaunchedEffect(uiState.statusMessage) {
                    val msg = uiState.statusMessage
                    if (!msg.isNullOrBlank()) {
                        Toast.makeText(this@MainActivity, msg, Toast.LENGTH_SHORT).show()
                        viewModel.clearStatusMessage()
                    }
                }

                // Handle Android Back Key
                BackHandler {
                    when {
                        uiState.isOmniboxEditing -> viewModel.setOmniboxEditing(false)
                        uiState.isTabSwitcherOpen -> viewModel.toggleTabSwitcher()
                        uiState.activeDialog != ActiveDialog.NONE -> viewModel.closeDialog()
                        uiState.isMenuExpanded -> viewModel.closeMenu()
                        uiState.activeTab?.canGoBack == true -> viewModel.goBack()
                        uiState.activeTab?.isNewTab == false -> viewModel.goHome()
                        uiState.tabs.size > 1 -> viewModel.closeTab(uiState.activeTabId)
                        else -> finish()
                    }
                }

                val activeTab = uiState.activeTab
                val isIncognito = activeTab?.isIncognito == true
                val appBg = if (isIncognito) ChromeDarkBg else ChromeLightBg

                Scaffold(
                    snackbarHost = { SnackbarHost(snackbarHostState) },
                    modifier = Modifier
                        .fillMaxSize()
                        .background(appBg)
                        .windowInsetsPadding(WindowInsets.statusBars)
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        Column(modifier = Modifier.fillMaxSize()) {
                            // ── 1. CHROME TOP TOOLBAR ──
                            ChromeToolbar(
                                activeTab = activeTab,
                                openTabsCount = uiState.openTabsCount,
                                onHomeClick = { viewModel.goHome() },
                                onOmniboxClick = { viewModel.setOmniboxEditing(true) },
                                onTabSwitcherClick = { viewModel.toggleTabSwitcher() },
                                onMenuClick = { viewModel.toggleMenu() }
                            )

                            // ── 2. MAIN BROWSER CONTENT (New Tab Page vs GeckoView) ──
                            Box(modifier = Modifier.weight(1f)) {
                                if (activeTab?.isNewTab == true) {
                                    NewTabPage(
                                        isIncognito = isIncognito,
                                        isArticlesExpanded = uiState.isArticlesExpanded,
                                        onSearchClick = { viewModel.setOmniboxEditing(true) },
                                        onShortcutClick = { url -> viewModel.loadUrl(url) },
                                        onToggleArticles = { viewModel.toggleArticles() }
                                    )
                                } else {
                                    // Hardware accelerated GeckoView
                                    AndroidView(
                                        factory = { ctx ->
                                            (sharedGeckoView ?: GeckoView(ctx).also { gv ->
                                                sharedGeckoView = gv
                                            }).apply {
                                                activeTab?.geckoSession?.let { setSession(it) }
                                            }
                                        },
                                        update = { gv ->
                                            val currentSession = activeTab?.geckoSession
                                            if (currentSession != null && gv.session != currentSession) {
                                                gv.setSession(currentSession)
                                            }
                                        },
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                            }
                        }

                        // ── 3. CHROME POPUP DROPDOWN MENU (Top Right) ──
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(top = 48.dp, end = 8.dp)
                        ) {
                            ChromeMenu(
                                expanded = uiState.isMenuExpanded,
                                activeTab = activeTab,
                                isBookmarked = uiState.isCurrentBookmarked,
                                onDismissRequest = { viewModel.closeMenu() },
                                onForwardClick = { viewModel.goForward(); viewModel.closeMenu() },
                                onBookmarkClick = { viewModel.toggleBookmark(); viewModel.closeMenu() },
                                onDownloadClick = {
                                    Toast.makeText(this@MainActivity, "Скачивание страницы...", Toast.LENGTH_SHORT).show()
                                    viewModel.closeMenu()
                                },
                                onInfoClick = { viewModel.openDialog(ActiveDialog.SITE_INFO) },
                                onReloadClick = { viewModel.reload(); viewModel.closeMenu() },
                                onNewTabClick = { viewModel.addNewTab(); viewModel.closeMenu() },
                                onNewIncognitoTabClick = { viewModel.addNewTab(isIncognito = true); viewModel.closeMenu() },
                                onBookmarksClick = { viewModel.openDialog(ActiveDialog.BOOKMARKS) },
                                onRecentTabsClick = { viewModel.openDialog(ActiveDialog.HISTORY) },
                                onHistoryClick = { viewModel.openDialog(ActiveDialog.HISTORY) },
                                onDownloadsClick = {
                                    Toast.makeText(this@MainActivity, "Папка Загрузки пуста", Toast.LENGTH_SHORT).show()
                                    viewModel.closeMenu()
                                },
                                onDesktopModeToggle = { viewModel.toggleDesktopMode() },
                                onExtensionsClick = { viewModel.openDialog(ActiveDialog.EXTENSIONS) },
                                onSettingsClick = { viewModel.openDialog(ActiveDialog.SETTINGS) },
                                onHelpClick = { viewModel.openDialog(ActiveDialog.HELP) }
                            )
                        }

                        // ── 4. FULL-SCREEN OMNIBOX EDITOR OVERLAY ──
                        AnimatedVisibility(
                            visible = uiState.isOmniboxEditing,
                            enter = fadeIn(),
                            exit = fadeOut()
                        ) {
                            OmniboxEditor(
                                initialText = activeTab?.url ?: "",
                                isIncognito = isIncognito,
                                recentHistory = uiState.history,
                                onLoadUrl = { url -> viewModel.loadUrl(url) },
                                onDismiss = { viewModel.setOmniboxEditing(false) }
                            )
                        }

                        // ── 5. TAB SWITCHER GRID OVERLAY ──
                        AnimatedVisibility(
                            visible = uiState.isTabSwitcherOpen,
                            enter = fadeIn(),
                            exit = fadeOut()
                        ) {
                            TabsSwitcher(
                                tabs = uiState.tabs,
                                activeTabId = uiState.activeTabId,
                                onSelectTab = { tabId -> viewModel.selectTab(tabId) },
                                onCloseTab = { tabId -> viewModel.closeTab(tabId) },
                                onNewTab = { viewModel.addNewTab() },
                                onCloseAllTabs = { viewModel.closeAllTabs() },
                                onDismiss = { viewModel.toggleTabSwitcher() }
                            )
                        }

                        // ── 6. EXTENSIONS MANAGEMENT SHEET ──
                        if (uiState.activeDialog == ActiveDialog.EXTENSIONS) {
                            ExtensionsSheet(
                                extensions = uiState.extensions,
                                onOpenWebStore = { viewModel.loadUrl("https://chromewebstore.google.com") },
                                onInstallById = { id, title -> viewModel.installExtensionFromStore(id, title) },
                                onToggleExtension = { id, enabled -> viewModel.toggleExtension(id, enabled) },
                                onUninstallExtension = { id -> viewModel.uninstallExtension(id) },
                                onDismiss = { viewModel.closeDialog() }
                            )
                        }

                        // ── 7. DIALOGS (Bookmarks, History, Site Info, Settings, Help) ──
                        when (uiState.activeDialog) {
                            ActiveDialog.BOOKMARKS -> {
                                BookmarksDialog(
                                    bookmarks = uiState.bookmarks,
                                    onSelectBookmark = { url -> viewModel.loadUrl(url) },
                                    onDeleteBookmark = { url -> viewModel.deleteBookmark(url) },
                                    onDismiss = { viewModel.closeDialog() }
                                )
                            }
                            ActiveDialog.HISTORY -> {
                                HistoryDialog(
                                    history = uiState.history,
                                    onSelectHistory = { url -> viewModel.loadUrl(url) },
                                    onClearHistory = { viewModel.clearHistory() },
                                    onDismiss = { viewModel.closeDialog() }
                                )
                            }
                            ActiveDialog.SITE_INFO -> {
                                SiteInfoDialog(
                                    activeTab = activeTab,
                                    onDismiss = { viewModel.closeDialog() }
                                )
                            }
                            ActiveDialog.SETTINGS, ActiveDialog.HELP -> {
                                SettingsDialog(
                                    currentSearchEngine = uiState.searchEngine,
                                    onSelectSearchEngine = { engine -> viewModel.setSearchEngine(engine) },
                                    onDismiss = { viewModel.closeDialog() }
                                )
                            }
                            else -> Unit
                        }

                        // ── 8. CHROME WEB STORE INSTALL PROMPT DIALOG ──
                        uiState.extensionInstallPrompt?.let { prompt ->
                            InstallExtensionDialog(
                                extensionTitle = prompt.title,
                                onConfirm = { viewModel.installExtensionFromStore(prompt.extensionId, prompt.title) },
                                onDismiss = { viewModel.dismissExtensionPrompt() }
                            )
                        }

                        // ── 9. EXTENSION PERMISSIONS CONFIRMATION DIALOG ──
                        uiState.permissionPrompt?.let { prompt ->
                            PermissionPromptDialog(
                                extensionName = prompt.extensionName,
                                permissions = prompt.permissions,
                                onAllow = { prompt.onResponse(true) },
                                onDeny = { prompt.onResponse(false) }
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        sharedGeckoView?.releaseSession()
        sharedGeckoView = null
        super.onDestroy()
    }
}
