package com.ao3reader.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ao3reader.app.ui.theme.PresetColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ColorPickerDialog(
    title: String,
    initialColorHex: Long,
    onColorSelected: (Long) -> Unit,
    onDismissRequest: () -> Unit
) {
    val initialColor = Color(initialColorHex)
    var red by remember { mutableFloatStateOf(initialColor.red) }
    var green by remember { mutableFloatStateOf(initialColor.green) }
    var blue by remember { mutableFloatStateOf(initialColor.blue) }

    val currentColor = Color(red, green, blue, 1f)
    val currentHex = String.format("#%02X%02X%02X", (red * 255).toInt(), (green * 255).toInt(), (blue * 255).toInt())

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = {
            Text(text = title, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Color Preview Box
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(currentColor)
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
                    )
                    Column {
                        Text(
                            text = "当前颜色代码: $currentHex",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "RGB: (${(red * 255).toInt()}, ${(green * 255).toInt()}, ${(blue * 255).toInt()})",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Preset palette
                Text(text = "推荐预设色盘", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                LazyVerticalGrid(
                    columns = GridCells.Fixed(5),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(88.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(PresetColors) { (colorHex, _) ->
                        val c = Color(colorHex)
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(c)
                                .clickable {
                                    red = c.red
                                    green = c.green
                                    blue = c.blue
                                }
                                .border(
                                    width = if (currentColor == c) 2.5.dp else 1.dp,
                                    color = if (currentColor == c) MaterialTheme.colorScheme.primary else Color.LightGray,
                                    shape = CircleShape
                                )
                        )
                    }
                }

                Divider()

                // RGB sliders for exact fine-tuning
                Text(text = "自由滑块调色", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(text = "红 (R): ${(red * 255).toInt()}", fontSize = 12.sp)
                    Slider(
                        value = red,
                        onValueChange = { red = it },
                        colors = SliderDefaults.colors(thumbColor = Color.Red, activeTrackColor = Color.Red)
                    )

                    Text(text = "绿 (G): ${(green * 255).toInt()}", fontSize = 12.sp)
                    Slider(
                        value = green,
                        onValueChange = { green = it },
                        colors = SliderDefaults.colors(thumbColor = Color.Green, activeTrackColor = Color.Green)
                    )

                    Text(text = "蓝 (B): ${(blue * 255).toInt()}", fontSize = 12.sp)
                    Slider(
                        value = blue,
                        onValueChange = { blue = it },
                        colors = SliderDefaults.colors(thumbColor = Color.Blue, activeTrackColor = Color.Blue)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val r = (red * 255).toLong() and 0xFF
                    val g = (green * 255).toLong() and 0xFF
                    val b = (blue * 255).toLong() and 0xFF
                    val hex = (0xFFL shl 24) or (r shl 16) or (g shl 8) or b
                    onColorSelected(hex)
                    onDismissRequest()
                }
            ) {
                Text("确定应用")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text("取消")
            }
        }
    )
}
