package com.example

import com.example.engine.GeckoManager
import com.example.model.BrowserTab
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun testChromeWebStoreRegexExtraction() {
        val urlNew = "https://chromewebstore.google.com/detail/ublock-origin/cjpalhdlnbpafiamejdnhcphjbkeiagm"
        val matchNew = GeckoManager.STORE_REGEX.find(urlNew)
        assertNotNull(matchNew)
        assertEquals("cjpalhdlnbpafiamejdnhcphjbkeiagm", matchNew!!.groupValues[1])

        val urlOld = "https://chrome.google.com/webstore/detail/dark-reader/eimadpbcbfnmbkopoojfekhnkhdbieeh?hl=en"
        val matchOld = GeckoManager.STORE_REGEX.find(urlOld)
        assertNotNull(matchOld)
        assertEquals("eimadpbcbfnmbkopoojfekhnkhdbieeh", matchOld!!.groupValues[1])
    }

    @Test
    fun testBrowserTabDisplayHost() {
        val tab = BrowserTab(url = "https://www.chromium.org/developers/how-tos/")
        assertEquals("chromium.org", tab.displayHost)
        assertFalse(tab.isNewTab)

        val newTab = BrowserTab(url = "")
        assertTrue(newTab.isNewTab)
        assertEquals("", newTab.displayHost)
    }
}

