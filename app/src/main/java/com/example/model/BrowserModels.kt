package com.example.model

import org.mozilla.geckoview.GeckoSession
import java.util.UUID

enum class SecurityLevel {
    SECURE,
    INSECURE,
    LOCAL
}

data class BrowserTab(
    val id: String = UUID.randomUUID().toString(),
    var title: String = "Новая вкладка",
    var url: String = "",
    var isIncognito: Boolean = false,
    var isDesktopMode: Boolean = false,
    var isLoading: Boolean = false,
    var progress: Int = 0,
    var canGoBack: Boolean = false,
    var canGoForward: Boolean = false,
    var securityLevel: SecurityLevel = SecurityLevel.SECURE,
    var geckoSession: GeckoSession? = null
) {
    val isNewTab: Boolean get() = url.isEmpty() || url == "about:blank" || url == "chrome://newtab"
    val displayHost: String
        get() {
            if (isNewTab) return ""
            return try {
                val clean = if (url.contains("://")) {
                    url.substringAfter("://").substringBefore("/").substringBefore("?").substringBefore(":")
                } else {
                    url.substringBefore("/").substringBefore("?")
                }
                clean.removePrefix("www.")
            } catch (e: Exception) {
                url
            }
        }
}

data class Bookmark(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val url: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class HistoryItem(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val url: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class ExtensionItem(
    val id: String,
    val name: String,
    val version: String,
    val description: String = "",
    val isEnabled: Boolean = true,
    val canBeDisabled: Boolean = true
)
