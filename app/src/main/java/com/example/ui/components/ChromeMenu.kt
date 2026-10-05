package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.BrowserTab
import com.example.ui.theme.ChromeBlue
import com.example.ui.theme.ChromeDarkBg
import com.example.ui.theme.ChromeDividerDark
import com.example.ui.theme.ChromeDividerLight
import com.example.ui.theme.ChromeLightBg
import com.example.ui.theme.ChromeTextPrimaryDark
import com.example.ui.theme.ChromeTextPrimaryLight
import com.example.ui.theme.ChromeTextSecondaryDark
import com.example.ui.theme.ChromeTextSecondaryLight

@Composable
fun ChromeMenu(
    expanded: Boolean,
    activeTab: BrowserTab?,
    isBookmarked: Boolean,
    onDismissRequest: () -> Unit,
    onForwardClick: () -> Unit,
    onBookmarkClick: () -> Unit,
    onDownloadClick: () -> Unit,
    onInfoClick: () -> Unit,
    onReloadClick: () -> Unit,
    onNewTabClick: () -> Unit,
    onNewIncognitoTabClick: () -> Unit,
    onBookmarksClick: () -> Unit,
    onRecentTabsClick: () -> Unit,
    onHistoryClick: () -> Unit,
    onDownloadsClick: () -> Unit,
    onDesktopModeToggle: () -> Unit,
    onExtensionsClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onHelpClick: () -> Unit
) {
    val isIncognito = activeTab?.isIncognito == true
    val menuBg = if (isIncognito) ChromeDarkBg else ChromeLightBg
    val textPrimary = if (isIncognito) ChromeTextPrimaryDark else ChromeTextPrimaryLight
    val textSecondary = if (isIncognito) ChromeTextSecondaryDark else ChromeTextSecondaryLight
    val dividerColor = if (isIncognito) ChromeDividerDark else ChromeDividerLight

    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
        offset = DpOffset(x = (-8).dp, y = 0.dp),
        modifier = Modifier
            .widthIn(min = 230.dp, max = 260.dp)
            .background(menuBg, shape = RoundedCornerShape(12.dp))
            .shadow(12.dp, shape = RoundedCornerShape(12.dp))
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // ── TOP ACTION ROW: Forward, Star, Download, Info, Reload ──
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 1. Forward Arrow
                IconButton(
                    onClick = onForwardClick,
                    enabled = activeTab?.canGoForward == true,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Вперед",
                        tint = if (activeTab?.canGoForward == true) textPrimary else textSecondary.copy(alpha = 0.4f),
                        modifier = Modifier.size(20.dp)
                    )
                }

                // 2. Star / Bookmark
                IconButton(
                    onClick = onBookmarkClick,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                        contentDescription = "Закладка",
                        tint = if (isBookmarked) ChromeBlue else textPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // 3. Download
                IconButton(
                    onClick = onDownloadClick,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = "Скачать",
                        tint = textPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // 4. Info
                IconButton(
                    onClick = onInfoClick,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Сведения о сайте",
                        tint = textPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // 5. Reload
                IconButton(
                    onClick = onReloadClick,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Обновить",
                        tint = textPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            HorizontalDivider(color = dividerColor, thickness = 0.8.dp)

            // ── MENU ITEMS ──
            ChromeMenuItem(text = "Новая вкладка", textColor = textPrimary, onClick = onNewTabClick)
            ChromeMenuItem(text = "Новая вкладка инкогнито", textColor = textPrimary, onClick = onNewIncognitoTabClick)

            HorizontalDivider(color = dividerColor, thickness = 0.8.dp, modifier = Modifier.padding(vertical = 4.dp))

            ChromeMenuItem(text = "Закладки", textColor = textPrimary, onClick = onBookmarksClick)
            ChromeMenuItem(text = "Недавние вкладки", textColor = textPrimary, onClick = onRecentTabsClick)
            ChromeMenuItem(text = "История", textColor = textPrimary, onClick = onHistoryClick)
            ChromeMenuItem(text = "Скачанные файлы", textColor = textPrimary, onClick = onDownloadsClick)

            HorizontalDivider(color = dividerColor, thickness = 0.8.dp, modifier = Modifier.padding(vertical = 4.dp))

            // Desktop Version Checkbox Item
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onDesktopModeToggle() }
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Версия для ПК",
                    color = textPrimary,
                    fontSize = 15.sp
                )
                Checkbox(
                    checked = activeTab?.isDesktopMode == true,
                    onCheckedChange = { onDesktopModeToggle() },
                    colors = CheckboxDefaults.colors(
                        checkedColor = ChromeBlue,
                        uncheckedColor = textSecondary
                    ),
                    modifier = Modifier.size(20.dp)
                )
            }

            // Extensions Menu Item (Chrome Web Store / Extensions Manager)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onExtensionsClick() }
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Extension,
                    contentDescription = null,
                    tint = ChromeBlue,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Расширения Chrome",
                    color = textPrimary,
                    fontSize = 15.sp
                )
            }

            HorizontalDivider(color = dividerColor, thickness = 0.8.dp, modifier = Modifier.padding(vertical = 4.dp))

            ChromeMenuItem(text = "Настройки", textColor = textPrimary, onClick = onSettingsClick)
            ChromeMenuItem(text = "Справка/отзыв", textColor = textPrimary, onClick = onHelpClick)
        }
    }
}

@Composable
private fun ChromeMenuItem(
    text: String,
    textColor: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(
            text = text,
            color = textColor,
            fontSize = 15.sp
        )
    }
}
