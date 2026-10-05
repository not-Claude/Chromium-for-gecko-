package com.example.ui.components

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ExtensionItem
import com.example.ui.theme.ChromeBlue
import com.example.ui.theme.ChromeDividerLight
import com.example.ui.theme.ChromeLightBg
import com.example.ui.theme.ChromeTextPrimaryLight
import com.example.ui.theme.ChromeTextSecondaryLight

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExtensionsSheet(
    extensions: List<ExtensionItem>,
    onOpenWebStore: () -> Unit,
    onInstallById: (String, String) -> Unit,
    onToggleExtension: (String, Boolean) -> Unit,
    onUninstallExtension: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var inputIdOrUrl by remember { mutableStateOf("") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = ChromeLightBg
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE8F0FE)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Extension,
                            contentDescription = null,
                            tint = ChromeBlue,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Расширения Chrome",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = ChromeTextPrimaryLight
                        )
                        Text(
                            text = "Поддержка Chrome Web Store в Gecko",
                            fontSize = 12.sp,
                            color = ChromeTextSecondaryLight
                        )
                    }
                }
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Закрыть",
                        tint = ChromeTextSecondaryLight
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Button: Open Chrome Web Store
            Button(
                onClick = {
                    onOpenWebStore()
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = ChromeBlue),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Перейти в Chrome Web Store")
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Quick Install Input
            OutlinedTextField(
                value = inputIdOrUrl,
                onValueChange = { inputIdOrUrl = it },
                label = { Text("Установить по ID или ссылке CWS") },
                placeholder = { Text("ID (32 буквы) или chromewebstore URL") },
                singleLine = true,
                trailingIcon = {
                    if (inputIdOrUrl.isNotBlank()) {
                        IconButton(onClick = {
                            val id = extractId(inputIdOrUrl)
                            if (id != null) {
                                onInstallById(id, "Расширение")
                                inputIdOrUrl = ""
                            }
                        }) {
                            Icon(
                                imageVector = Icons.Default.Download,
                                contentDescription = "Установить",
                                tint = ChromeBlue
                            )
                        }
                    }
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = {
                    val id = extractId(inputIdOrUrl)
                    if (id != null) {
                        onInstallById(id, "Расширение")
                        inputIdOrUrl = ""
                    }
                }),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            )

            // Popular Extension presets
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                FilterChip(
                    selected = false,
                    onClick = { onInstallById("cjpalhdlnbpafiamejdnhcphjbkeiagm", "uBlock Origin") },
                    label = { Text("uBlock Origin", fontSize = 11.sp) }
                )
                FilterChip(
                    selected = false,
                    onClick = { onInstallById("eimadpbcbfnmbkopoojfekhnkhdbieeh", "Dark Reader") },
                    label = { Text("Dark Reader", fontSize = 11.sp) }
                )
                FilterChip(
                    selected = false,
                    onClick = { onInstallById("aapbdbdomjkkjkaonfhkkikfgjllcleb", "Translate") },
                    label = { Text("Translate", fontSize = 11.sp) }
                )
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = ChromeDividerLight)
            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Установленные (${extensions.size})",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = ChromeTextPrimaryLight
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (extensions.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Нет установленных расширений.\nОткройте Chrome Web Store, чтобы установить.",
                        color = ChromeTextSecondaryLight,
                        fontSize = 13.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(extensions, key = { it.id }) { ext ->
                        ExtensionRow(
                            extension = ext,
                            onToggle = { enabled -> onToggleExtension(ext.id, enabled) },
                            onDelete = { onUninstallExtension(ext.id) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun ExtensionRow(
    extension: ExtensionItem,
    onToggle: (Boolean) -> Unit,
    onDelete: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Extension,
                contentDescription = null,
                tint = ChromeBlue,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = extension.name,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = ChromeTextPrimaryLight,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "v${extension.version} · ${extension.id.take(12)}...",
                    fontSize = 11.sp,
                    color = ChromeTextSecondaryLight
                )
            }
            Switch(
                checked = extension.isEnabled,
                onCheckedChange = onToggle,
                colors = SwitchDefaults.colors(checkedThumbColor = ChromeBlue)
            )
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = "Удалить",
                    tint = Color(0xFFD93025),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

private fun extractId(input: String): String? {
    val trimmed = input.trim()
    val match = Regex("""(?:chromewebstore\.google\.com|chrome\.google\.com/webstore)/detail/(?:[^/?#]+/)?([a-p]{32})""").find(trimmed)
    if (match != null) return match.groupValues[1]
    if (Regex("""^[a-p]{32}$""").matches(trimmed)) return trimmed
    return null
}
