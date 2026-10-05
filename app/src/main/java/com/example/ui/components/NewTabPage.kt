package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ChromeBlue
import com.example.ui.theme.ChromeDarkBg
import com.example.ui.theme.ChromeDividerLight
import com.example.ui.theme.ChromeGreen
import com.example.ui.theme.ChromeLightBg
import com.example.ui.theme.ChromeOmniboxBgDark
import com.example.ui.theme.ChromeOmniboxBgLight
import com.example.ui.theme.ChromeRed
import com.example.ui.theme.ChromeTextPrimaryDark
import com.example.ui.theme.ChromeTextPrimaryLight
import com.example.ui.theme.ChromeTextSecondaryDark
import com.example.ui.theme.ChromeTextSecondaryLight
import com.example.ui.theme.ChromeYellow

data class ShortcutItem(
    val title: String,
    val url: String,
    val iconType: ShortcutIconType,
    val letter: String = ""
)

enum class ShortcutIconType {
    CHROME,
    CHROMIUM,
    STORE,
    GOOGLE_G,
    FREENODE,
    GITHUB,
    FLAGS_C,
    GRID_DOTS
}

@Composable
fun NewTabPage(
    isIncognito: Boolean,
    isArticlesExpanded: Boolean,
    onSearchClick: () -> Unit,
    onShortcutClick: (String) -> Unit,
    onToggleArticles: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bgColor = if (isIncognito) ChromeDarkBg else ChromeLightBg
    val textPrimary = if (isIncognito) ChromeTextPrimaryDark else ChromeTextPrimaryLight
    val textSecondary = if (isIncognito) ChromeTextSecondaryDark else ChromeTextSecondaryLight
    val searchBarBg = if (isIncognito) ChromeOmniboxBgDark else ChromeOmniboxBgLight

    val shortcuts = listOf(
        ShortcutItem("The Chromi...", "https://www.chromium.org", ShortcutIconType.CHROMIUM),
        ShortcutItem("Chromium ...", "https://blog.chromium.org", ShortcutIconType.CHROME),
        ShortcutItem("Chrome Store", "https://chromewebstore.google.com", ShortcutIconType.STORE),
        ShortcutItem("Google Ope...", "https://opensource.google", ShortcutIconType.GOOGLE_G, "G"),
        ShortcutItem("freenode", "https://freenode.net", ShortcutIconType.FREENODE, "fn"),
        ShortcutItem("Github Pag...", "https://github.com", ShortcutIconType.GITHUB, "G"),
        ShortcutItem("Chrome Fla...", "about:config", ShortcutIconType.FLAGS_C, "C"),
        ShortcutItem("Топ сайтов", "https://chromewebstore.google.com", ShortcutIconType.GRID_DOTS)
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(bgColor),
        horizontalAlignment = Alignment.CenterHorizontally,
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 24.dp)
    ) {
        // ── 1. GOOGLE COLORFUL LOGO ──
        item {
            Spacer(modifier = Modifier.height(32.dp))
            GoogleLogo()
            Spacer(modifier = Modifier.height(28.dp))
        }

        // ── 2. OMNIBOX PILL SEARCH BAR ──
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(searchBarBg)
                    .clickable { onSearchClick() }
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Введите адрес или поисковый з...",
                        color = textSecondary,
                        fontSize = 15.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = "Голосовой поиск",
                        tint = textSecondary,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(28.dp))
        }

        // ── 3. SHORTCUTS GRID (2 rows of 4) ──
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Row 1
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    shortcuts.take(4).forEach { item ->
                        ShortcutTile(
                            shortcut = item,
                            textColor = textPrimary,
                            onClick = { onShortcutClick(item.url) }
                        )
                    }
                }

                // Row 2
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    shortcuts.drop(4).take(4).forEach { item ->
                        ShortcutTile(
                            shortcut = item,
                            textColor = textPrimary,
                            onClick = { onShortcutClick(item.url) }
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
        }

        // ── 4. "СТАТЬИ ДЛЯ ВАС" COLLAPSIBLE CARD ──
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (isIncognito) ChromeOmniboxBgDark else Color.White
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 1.dp,
                        color = if (isIncognito) Color(0xFF3C4043) else ChromeDividerLight,
                        shape = RoundedCornerShape(14.dp)
                    )
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onToggleArticles() }
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Статьи для вас",
                            color = textPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = if (isArticlesExpanded) "Скрыть" else "Показать",
                            color = ChromeBlue,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    AnimatedVisibility(
                        visible = isArticlesExpanded,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            ArticleItem(
                                title = "Поддержка Chrome Web Store на движке GeckoView",
                                source = "Chrome Gecko · 2 ч. назад",
                                onClick = { onShortcutClick("https://chromewebstore.google.com") }
                            )
                            ArticleItem(
                                title = "Как устанавливать расширения .crx и .zip в мобильный браузер",
                                source = "Mozilla & Chromium · 5 ч. назад",
                                onClick = { onShortcutClick("https://chromewebstore.google.com") }
                            )
                            ArticleItem(
                                title = "Аппаратное ускорение WebRender и плавная прокрутка APZ",
                                source = "Gecko Dev · Вчера",
                                onClick = { onShortcutClick("https://www.chromium.org") }
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun GoogleLogo() {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text("G", color = Color(0xFF4285F4), fontSize = 48.sp, fontWeight = FontWeight.SemiBold, fontFamily = FontFamily.SansSerif)
        Text("o", color = ChromeRed, fontSize = 48.sp, fontWeight = FontWeight.SemiBold, fontFamily = FontFamily.SansSerif)
        Text("o", color = ChromeYellow, fontSize = 48.sp, fontWeight = FontWeight.SemiBold, fontFamily = FontFamily.SansSerif)
        Text("g", color = Color(0xFF4285F4), fontSize = 48.sp, fontWeight = FontWeight.SemiBold, fontFamily = FontFamily.SansSerif)
        Text("l", color = ChromeGreen, fontSize = 48.sp, fontWeight = FontWeight.SemiBold, fontFamily = FontFamily.SansSerif)
        Text("e", color = ChromeRed, fontSize = 48.sp, fontWeight = FontWeight.SemiBold, fontFamily = FontFamily.SansSerif)
    }
}

@Composable
private fun ShortcutTile(
    shortcut: ShortcutItem,
    textColor: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(68.dp)
            .clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(
                    when (shortcut.iconType) {
                        ShortcutIconType.CHROME, ShortcutIconType.CHROMIUM -> Color(0xFFE8F0FE)
                        ShortcutIconType.STORE -> Color(0xFFFEEFC3)
                        ShortcutIconType.FREENODE -> Color(0xFFE6F4EA)
                        ShortcutIconType.FLAGS_C -> Color(0xFFE8F0FE)
                        else -> Color(0xFFF1F3F4)
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            when (shortcut.iconType) {
                ShortcutIconType.CHROME -> {
                    // Chrome colorful icon representation
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(ChromeRed),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF4285F4))
                        )
                    }
                }
                ShortcutIconType.CHROMIUM -> {
                    // Chromium blue icon
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF1976D2)),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(Color.White)
                        )
                    }
                }
                ShortcutIconType.STORE -> {
                    Icon(
                        imageVector = Icons.Default.Extension,
                        contentDescription = "Chrome Web Store",
                        tint = Color(0xFFE37400),
                        modifier = Modifier.size(22.dp)
                    )
                }
                ShortcutIconType.GRID_DOTS -> {
                    // 9 dots (3x3 grid)
                    Column(
                        verticalArrangement = Arrangement.spacedBy(2.5.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        repeat(3) {
                            Row(horizontalArrangement = Arrangement.spacedBy(2.5.dp)) {
                                repeat(3) {
                                    Box(
                                        modifier = Modifier
                                            .size(4.dp)
                                            .clip(CircleShape)
                                            .background(ChromeBlue)
                                    )
                                }
                            }
                        }
                    }
                }
                ShortcutIconType.FREENODE -> {
                    Text(
                        text = "fn",
                        color = Color(0xFF137333),
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                ShortcutIconType.FLAGS_C -> {
                    Text(
                        text = "C",
                        color = ChromeBlue,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                else -> {
                    // Google G or Github G
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF5F6368)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = shortcut.letter.ifEmpty { "G" },
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = shortcut.title,
            color = textColor,
            fontSize = 11.5.sp,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun ArticleItem(
    title: String,
    source: String,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 4.dp)
    ) {
        Text(
            text = title,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = ChromeTextPrimaryLight,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = source,
            fontSize = 12.sp,
            color = ChromeTextSecondaryLight
        )
    }
}
