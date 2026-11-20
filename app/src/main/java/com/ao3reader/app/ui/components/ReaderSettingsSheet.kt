package com.ao3reader.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ao3reader.app.data.model.BackgroundPreset
import com.ao3reader.app.data.model.FontFamilyType
import com.ao3reader.app.data.model.ReaderSettings
import com.ao3reader.app.data.model.ReadingMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReaderSettingsSheet(
    settings: ReaderSettings,
    onSettingsChange: ((ReaderSettings) -> ReaderSettings) -> Unit,
    onDismissRequest: () -> Unit
) {
    var showBgColorPicker by remember { mutableStateOf(false) }
    var showTextColorPicker by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Text(
                text = "阅读偏好与排版设置",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            // 1. Reading Mode Selection
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(text = "翻页与排版模式", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ReadingMode.values().forEach { mode ->
                        val selected = settings.readingMode == mode
                        FilterChip(
                            selected = selected,
                            onClick = { onSettingsChange { it.copy(readingMode = mode) } },
                            label = { Text(mode.displayName) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                if (settings.readingMode == ReadingMode.PAGE_FLIP_NO_ANIM) {
                    Text(
                        text = "💡 无动画瞬切：点击屏幕左右侧无缝切页，零动画残影，电纸书般沉浸感",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Divider()

            // 2. Background Presets & Custom Colors
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "背景与配色方案", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextButton(onClick = { showBgColorPicker = true }) {
                            Icon(Icons.Default.ColorLens, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("自定义背景色", fontSize = 12.sp)
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    BackgroundPreset.values().take(6).forEach { preset ->
                        val isSelected = settings.backgroundPreset == preset
                        val bg = Color(preset.backgroundColorHex)
                        val text = Color(preset.textColorHex)

                        Column(
                            horizontalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    onSettingsChange {
                                        it.copy(
                                            backgroundPreset = preset,
                                            backgroundColorHex = preset.backgroundColorHex,
                                            textColorHex = preset.textColorHex
                                        )
                                    }
                                }
                                .padding(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(bg)
                                    .border(
                                        width = if (isSelected) 2.5.dp else 1.dp,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else Color.LightGray,
                                        shape = CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "文",
                                    color = text,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = preset.displayName.take(2),
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Divider()

            // 3. Font Size Adjustment
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "字体大小: ${settings.fontSize.toInt()} sp", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        FilledTonalIconButton(
                            onClick = {
                                onSettingsChange { it.copy(fontSize = (it.fontSize - 1f).coerceAtLeast(12f)) }
                            },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(Icons.Default.Remove, contentDescription = "减小字号", modifier = Modifier.size(18.dp))
                        }
                        FilledTonalIconButton(
                            onClick = {
                                onSettingsChange { it.copy(fontSize = (it.fontSize + 1f).coerceAtMost(36f)) }
                            },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "增大字号", modifier = Modifier.size(18.dp))
                        }
                    }
                }

                Slider(
                    value = settings.fontSize,
                    onValueChange = { size -> onSettingsChange { it.copy(fontSize = size) } },
                    valueRange = 12f..36f,
                    steps = 23
                )
            }

            // 4. Line Spacing Multiplier
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "行间距: ${String.format("%.2f", settings.lineHeightMultiplier)} 倍",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Slider(
                    value = settings.lineHeightMultiplier,
                    onValueChange = { mult -> onSettingsChange { it.copy(lineHeightMultiplier = mult) } },
                    valueRange = 1.2f..2.4f,
                    steps = 11
                )
            }

            Divider()

            // 5. Font Family Selection
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(text = "字体风格", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(
                        FontFamilyType.SYSTEM to "系统默认",
                        FontFamilyType.SERIF to "经典宋体",
                        FontFamilyType.SANS_SERIF to "无衬线黑体",
                        FontFamilyType.MONOSPACE to "等宽字体"
                    ).forEach { (fontType, label) ->
                        val selected = settings.fontFamilyType == fontType
                        FilterChip(
                            selected = selected,
                            onClick = { onSettingsChange { it.copy(fontFamilyType = fontType) } },
                            label = { Text(label, fontSize = 11.sp) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // 6. Switches: Screen awake & Volume keys
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "阅读时屏幕常亮", fontSize = 14.sp)
                Switch(
                    checked = settings.keepScreenOn,
                    onCheckedChange = { checked -> onSettingsChange { it.copy(keepScreenOn = checked) } }
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "音量物理键翻页", fontSize = 14.sp)
                Switch(
                    checked = settings.volumeKeysTurnPage,
                    onCheckedChange = { checked -> onSettingsChange { it.copy(volumeKeysTurnPage = checked) } }
                )
            }

            Spacer(Modifier.height(16.dp))
        }
    }

    if (showBgColorPicker) {
        ColorPickerDialog(
            title = "自定义阅读背景颜色",
            initialColorHex = settings.backgroundColorHex,
            onColorSelected = { colorHex ->
                onSettingsChange {
                    it.copy(
                        backgroundPreset = BackgroundPreset.CUSTOM,
                        backgroundColorHex = colorHex
                    )
                }
            },
            onDismissRequest = { showBgColorPicker = false }
        )
    }

    if (showTextColorPicker) {
        ColorPickerDialog(
            title = "自定义阅读文字颜色",
            initialColorHex = settings.textColorHex,
            onColorSelected = { colorHex ->
                onSettingsChange {
                    it.copy(
                        backgroundPreset = BackgroundPreset.CUSTOM,
                        textColorHex = colorHex
                    )
                }
            },
            onDismissRequest = { showTextColorPicker = false }
        )
    }
}
