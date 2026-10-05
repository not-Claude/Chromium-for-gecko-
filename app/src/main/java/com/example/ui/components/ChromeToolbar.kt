package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.BrowserTab
import com.example.ui.theme.ChromeBlue
import com.example.ui.theme.ChromeDarkBg
import com.example.ui.theme.ChromeGreen
import com.example.ui.theme.ChromeOmniboxBgDark
import com.example.ui.theme.ChromeOmniboxBgLight
import com.example.ui.theme.ChromeTextPrimaryDark
import com.example.ui.theme.ChromeTextPrimaryLight
import com.example.ui.theme.ChromeTextSecondaryDark
import com.example.ui.theme.ChromeTextSecondaryLight
import com.example.ui.theme.ChromeToolbarBgDark
import com.example.ui.theme.ChromeToolbarBgLight

@Composable
fun ChromeToolbar(
    activeTab: BrowserTab?,
    openTabsCount: Int,
    onHomeClick: () -> Unit,
    onOmniboxClick: () -> Unit,
    onTabSwitcherClick: () -> Unit,
    onMenuClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isIncognito = activeTab?.isIncognito == true
    val isNewTab = activeTab?.isNewTab == true

    val bgColor = if (isIncognito) ChromeToolbarBgDark else ChromeToolbarBgLight
    val omniboxBg = if (isIncognito) ChromeOmniboxBgDark else ChromeOmniboxBgLight
    val textPrimary = if (isIncognito) ChromeTextPrimaryDark else ChromeTextPrimaryLight
    val textSecondary = if (isIncognito) ChromeTextSecondaryDark else ChromeTextSecondaryLight
    val iconColor = if (isIncognito) ChromeTextPrimaryDark else ChromeTextSecondaryLight

    val animatedProgress by animateFloatAsState(
        targetValue = if (activeTab?.isLoading == true) (activeTab.progress / 100f).coerceIn(0.1f, 1f) else 0f,
        label = "progress"
    )

    Surface(
        color = bgColor,
        shadowElevation = if (isNewTab) 0.dp else 3.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(horizontal = 8.dp)
            ) {
                // 1. Home Button
                IconButton(
                    onClick = onHomeClick,
                    modifier = Modifier.size(44.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Home,
                        contentDescription = "Главная страница",
                        tint = iconColor,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                // 2. Omnibox Pill
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(42.dp)
                        .clip(RoundedCornerShape(21.dp))
                        .background(omniboxBg)
                        .clickable { onOmniboxClick() }
                        .padding(horizontal = 14.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (isNewTab) {
                            // On NTP, prompt to enter address or search query
                            Text(
                                text = "Введите адрес или поисковый з...",
                                color = textSecondary,
                                fontSize = 14.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = "Голосовой поиск",
                                tint = iconColor,
                                modifier = Modifier.size(20.dp)
                            )
                        } else {
                            // On web page, show lock icon and domain name
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Безопасное подключение",
                                tint = if (activeTab?.url?.startsWith("https") == true) ChromeGreen else iconColor,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = activeTab?.displayHost ?: "",
                                color = textPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Normal,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(6.dp))

                // 3. Tab Switcher Button (Rounded Square with Tab Count)
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onTabSwitcherClick() }
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(22.dp)
                            .border(
                                width = 1.8.dp,
                                color = iconColor,
                                shape = RoundedCornerShape(5.dp)
                            )
                    ) {
                        Text(
                            text = if (openTabsCount > 99) ":D" else openTabsCount.coerceAtLeast(1).toString(),
                            color = iconColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // 4. 3-Dots Menu Button
                IconButton(
                    onClick = onMenuClick,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Меню Chrome",
                        tint = iconColor,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            // Animated Loading Progress Bar
            AnimatedVisibility(
                visible = activeTab?.isLoading == true,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                LinearProgressIndicator(
                    progress = { animatedProgress },
                    color = ChromeBlue,
                    trackColor = Color.Transparent,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.5.dp)
                )
            }
        }
    }
}
