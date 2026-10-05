package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Bookmark
import com.example.model.BrowserTab
import com.example.model.HistoryItem
import com.example.ui.theme.ChromeBlue
import com.example.ui.theme.ChromeDividerLight
import com.example.ui.theme.ChromeGreen
import com.example.ui.theme.ChromeTextPrimaryLight
import com.example.ui.theme.ChromeTextSecondaryLight

@Composable
fun InstallExtensionDialog(
    extensionTitle: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Установить расширение?",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        },
        text = {
            Text(
                text = "«$extensionTitle» будет скачано из Chrome Web Store и установлено в Gecko с поддержкой API Chrome.",
                fontSize = 14.sp,
                color = ChromeTextSecondaryLight
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = ChromeBlue)
            ) {
                Text("Установить")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена", color = ChromeTextSecondaryLight)
            }
        },
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
fun PermissionPromptDialog(
    extensionName: String,
    permissions: List<String>,
    onAllow: () -> Unit,
    onDeny: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDeny,
        title = {
            Text(
                text = "Добавить «$extensionName»?",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                if (permissions.isEmpty()) {
                    Text("Особые разрешения не требуются.", fontSize = 14.sp, color = ChromeTextSecondaryLight)
                } else {
                    Text("Запрашиваемые разрешения:", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.height(6.dp))
                    permissions.forEach { perm ->
                        Row(modifier = Modifier.padding(vertical = 2.dp)) {
                            Text("• ", color = ChromeBlue, fontWeight = FontWeight.Bold)
                            Text(perm, fontSize = 13.sp, color = ChromeTextSecondaryLight)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onAllow,
                colors = ButtonDefaults.buttonColors(containerColor = ChromeBlue)
            ) {
                Text("Добавить")
            }
        },
        dismissButton = {
            TextButton(onClick = onDeny) {
                Text("Отмена", color = ChromeTextSecondaryLight)
            }
        },
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
fun BookmarksDialog(
    bookmarks: List<Bookmark>,
    onSelectBookmark: (String) -> Unit,
    onDeleteBookmark: (String) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Bookmark, contentDescription = null, tint = ChromeBlue)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Закладки", fontWeight = FontWeight.Bold)
                }
                IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Закрыть")
                }
            }
        },
        text = {
            if (bookmarks.isEmpty()) {
                Text("Нет сохраненных закладок.", color = ChromeTextSecondaryLight)
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 350.dp)
                ) {
                    items(bookmarks, key = { it.url }) { bm ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onSelectBookmark(bm.url)
                                    onDismiss()
                                }
                                .padding(vertical = 8.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Public, contentDescription = null, tint = ChromeBlue, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(bm.title, fontSize = 14.sp, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text(bm.url, fontSize = 11.sp, color = ChromeTextSecondaryLight, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                            IconButton(onClick = { onDeleteBookmark(bm.url) }) {
                                Icon(imageVector = Icons.Default.DeleteOutline, contentDescription = "Удалить", tint = Color.Gray, modifier = Modifier.size(18.dp))
                            }
                        }
                        HorizontalDivider(color = ChromeDividerLight)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Закрыть") }
        },
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
fun HistoryDialog(
    history: List<HistoryItem>,
    onSelectHistory: (String) -> Unit,
    onClearHistory: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.History, contentDescription = null, tint = ChromeBlue)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("История", fontWeight = FontWeight.Bold)
                }
                IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Закрыть")
                }
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                if (history.isEmpty()) {
                    Text("История посещений пуста.", color = ChromeTextSecondaryLight)
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = onClearHistory) {
                            Text("Очистить историю", color = Color(0xFFD93025), fontSize = 13.sp)
                        }
                    }
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 350.dp)
                    ) {
                        items(history, key = { it.id }) { item ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onSelectHistory(item.url)
                                        onDismiss()
                                    }
                                    .padding(vertical = 8.dp)
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(item.title, fontSize = 14.sp, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    Text(item.url, fontSize = 11.sp, color = ChromeTextSecondaryLight, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                }
                            }
                            HorizontalDivider(color = ChromeDividerLight)
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Закрыть") }
        },
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
fun SiteInfoDialog(
    activeTab: BrowserTab?,
    onDismiss: () -> Unit
) {
    val isSecure = activeTab?.url?.startsWith("https://") == true
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (isSecure) Icons.Default.Lock else Icons.Default.Info,
                    contentDescription = null,
                    tint = if (isSecure) ChromeGreen else Color(0xFFEA8600)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isSecure) "Безопасное подключение" else "Сведения о сайте",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = activeTab?.displayHost ?: "Локальная страница",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    color = ChromeTextPrimaryLight
                )
                Text(
                    text = if (isSecure)
                        "Подключение к этому сайту защищено современным шифрованием TLS/HTTPS."
                    else
                        "Подключение не является зашифрованным или является локальным.",
                    fontSize = 13.sp,
                    color = ChromeTextSecondaryLight
                )
                HorizontalDivider(color = ChromeDividerLight)
                Text("Движок: Mozilla GeckoView (Nightly Omni)", fontSize = 12.sp, color = ChromeTextSecondaryLight)
                Text("Поддержка расширений: Включена (Chrome Web Store API)", fontSize = 12.sp, color = ChromeBlue)
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Понятно") }
        },
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
fun SettingsDialog(
    currentSearchEngine: String,
    onSelectSearchEngine: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val engines = listOf("Google", "Yandex", "DuckDuckGo", "Bing")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Настройки", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Поисковая система:", fontWeight = FontWeight.Medium, fontSize = 14.sp)
                engines.forEach { engine ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectSearchEngine(engine) }
                            .padding(vertical = 4.dp)
                    ) {
                        RadioButton(
                            selected = engine == currentSearchEngine,
                            onClick = { onSelectSearchEngine(engine) },
                            colors = RadioButtonDefaults.colors(selectedColor = ChromeBlue)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(engine, fontSize = 14.sp)
                    }
                }
                HorizontalDivider(color = ChromeDividerLight)
                Text("О браузере Chrome Gecko", fontWeight = FontWeight.Medium, fontSize = 14.sp)
                Text("Архитектура: GeckoView Nightly 126\nУскорение: WebRender GPU + APZ Smooth Scrolling\nФормат расширений: CRX3/CRX2 Chrome Web Store", fontSize = 12.sp, color = ChromeTextSecondaryLight)
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Готово") }
        },
        shape = RoundedCornerShape(16.dp)
    )
}
