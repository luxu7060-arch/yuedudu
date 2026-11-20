package com.ao3reader.app.ui.screens.settings

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.ao3reader.app.AO3Application
import com.ao3reader.app.data.model.ThemeMode
import com.ao3reader.app.ui.components.ColorPickerDialog
import com.ao3reader.app.ui.theme.PresetColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemeSettingsScreen(navController: NavController) {
    val context = LocalContext.current
    val prefRepo = remember { AO3Application.instance.preferenceRepository }
    val themeSettings by prefRepo.themeSettings.collectAsState()

    var showCustomColorPicker by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("界面颜色与全局设置", fontSize = 18.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 18.dp, vertical = 12.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // 1. Dark/Light Mode selection
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(text = "系统显示模式", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ThemeMode.values().forEach { mode ->
                            val selected = themeSettings.themeMode == mode
                            FilterChip(
                                selected = selected,
                                onClick = { prefRepo.setThemeMode(mode) },
                                label = { Text(mode.displayName, fontSize = 12.sp) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // 2. Entire Interface Color Customization
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = "调节整个界面的主题色", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                            Text(
                                text = "更改顶部栏、高亮按钮、标签及交互组件基调",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Preset color grid
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(5),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(PresetColors) { (colorHex, _) ->
                            val isSelected = themeSettings.primaryColorHex == colorHex
                            val color = Color(colorHex)
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(color)
                                    .clickable {
                                        prefRepo.setAppThemeColor(colorHex)
                                    }
                                    .border(
                                        width = if (isSelected) 3.dp else 1.dp,
                                        color = if (isSelected) MaterialTheme.colorScheme.onSurface else Color.LightGray,
                                        shape = CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isSelected) {
                                    Icon(
                                        Icons.Default.Check,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Button for custom color
                    OutlinedButton(
                        onClick = { showCustomColorPicker = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Colorize, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("自定义任意界面主色 (RGB / 色盘)", fontWeight = FontWeight.SemiBold)
                    }

                    // Live theme preview
                    Text(text = "当前界面主题预览效果：", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(onClick = {}) { Text("主要按钮") }
                        FilledTonalButton(onClick = {}) { Text("次要按钮") }
                        SuggestionChip(onClick = {}, label = { Text("标签高亮") })
                    }
                }
            }

            // 3. Storage & About
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(text = "关于与说明", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    Text(
                        text = "本应用为开源 AO3 纯净阅读器。支持自动跳过成人内容警告、离线缓存、滑动与无动画瞬切翻页，以及全套自定义配色。",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 20.sp
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "版本：v1.0.0 (GitHub CI 自动打包构建)",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }

    if (showCustomColorPicker) {
        ColorPickerDialog(
            title = "自定义整套界面主题主色",
            initialColorHex = themeSettings.primaryColorHex,
            onColorSelected = { colorHex ->
                prefRepo.setAppThemeColor(colorHex)
                Toast.makeText(context, "已更新全应用主题颜色", Toast.LENGTH_SHORT).show()
            },
            onDismissRequest = { showCustomColorPicker = false }
        )
    }
}
