package com.ao3reader.app.ui.screens.home

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.ao3reader.app.AO3Application
import com.ao3reader.app.data.model.Work
import com.ao3reader.app.ui.components.RatingBadge
import com.ao3reader.app.ui.components.TagChip
import com.ao3reader.app.ui.components.TagType
import com.ao3reader.app.ui.navigation.Screen
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val workRepo = remember { AO3Application.instance.workRepository }
    val worksMap by workRepo.works.collectAsState()

    var urlInput by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val clipboardManager = LocalClipboardManager.current

    val allWorks = remember(worksMap) { worksMap.values.toList().sortedByDescending { it.lastReadTimestamp } }
    val favorites = remember(allWorks) { allWorks.filter { it.isFavorite } }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.MenuBook,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(26.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "AO3 纯净阅读器",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { navController.navigate(Screen.Browser.createRoute()) }) {
                        Icon(Icons.Default.Explore, contentDescription = "AO3 浏览器", tint = MaterialTheme.colorScheme.primary)
                    }
                    IconButton(onClick = { navController.navigate(Screen.Settings.route) }) {
                        Icon(Icons.Default.Palette, contentDescription = "界面主题配色设置")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = true,
                    onClick = { },
                    icon = { Icon(Icons.Default.Book, contentDescription = null) },
                    label = { Text("书架") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { navController.navigate(Screen.Browser.createRoute()) },
                    icon = { Icon(Icons.Default.Language, contentDescription = null) },
                    label = { Text("AO3 探索") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { navController.navigate(Screen.Settings.route) },
                    icon = { Icon(Icons.Default.Settings, contentDescription = null) },
                    label = { Text("设置") }
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // URL / ID quick input card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "快速导入 AO3 文章",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    OutlinedTextField(
                        value = urlInput,
                        onValueChange = { urlInput = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("粘贴作品链接 (works/...) 或纯数字 ID", fontSize = 13.sp) },
                        singleLine = true,
                        trailingIcon = {
                            Row {
                                if (urlInput.isNotBlank()) {
                                    IconButton(onClick = { urlInput = "" }) {
                                        Icon(Icons.Default.Clear, contentDescription = "清空")
                                    }
                                }
                                IconButton(onClick = {
                                    clipboardManager.getText()?.text?.let { urlInput = it }
                                }) {
                                    Icon(Icons.Default.ContentPaste, contentDescription = "粘贴")
                                }
                            }
                        }
                    )
                    Button(
                        onClick = {
                            if (urlInput.isBlank()) {
                                Toast.makeText(context, "请先输入作品链接或 ID", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            isLoading = true
                            scope.launch {
                                val result = workRepo.fetchWork(urlInput)
                                isLoading = false
                                result.onSuccess { work ->
                                    navController.navigate(Screen.WorkDetail.createRoute(work.id))
                                }.onFailure { e ->
                                    Toast.makeText(context, "加载失败: ${e.message}", Toast.LENGTH_LONG).show()
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                            Spacer(Modifier.width(8.dp))
                            Text("正在解析纯净排版...")
                        } else {
                            Icon(Icons.Default.AutoStories, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("立即解析并阅读")
                        }
                    }
                }
            }

            // Tabs: Favorites & History
            TabRow(selectedTabIndex = selectedTabIndex) {
                Tab(
                    selected = selectedTabIndex == 0,
                    onClick = { selectedTabIndex = 0 },
                    text = { Text("我的书架 (${favorites.size})") }
                )
                Tab(
                    selected = selectedTabIndex == 1,
                    onClick = { selectedTabIndex = 1 },
                    text = { Text("阅读历史 (${allWorks.size})") }
                )
            }

            val displayList = if (selectedTabIndex == 0) favorites else allWorks

            if (displayList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Inbox,
                            contentDescription = null,
                            modifier = Modifier.size(56.dp),
                            tint = MaterialTheme.colorScheme.outline
                        )
                        Spacer(Modifier.height(12.dp))
                        Text(
                            text = if (selectedTabIndex == 0) "书架暂无作品，快去收藏或导入吧" else "暂无阅读记录",
                            color = MaterialTheme.colorScheme.outline,
                            fontSize = 14.sp
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(14.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(displayList, key = { it.id }) { work ->
                        WorkListItem(
                            work = work,
                            onClick = {
                                navController.navigate(
                                    Screen.Reader.createRoute(
                                        work.id,
                                        if (work.lastReadChapterIndex > 0) work.lastReadChapterIndex else 1
                                    )
                                )
                            },
                            onDetailClick = {
                                navController.navigate(Screen.WorkDetail.createRoute(work.id))
                            },
                            onToggleFavorite = {
                                workRepo.toggleFavorite(work.id)
                            },
                            onDelete = {
                                workRepo.deleteWork(work.id)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun WorkListItem(
    work: Work,
    onClick: () -> Unit,
    onDetailClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Header: Title & Favorite
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = work.title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = "作者: ${work.author}  •  ${work.wordCount} 字  •  ${work.chaptersAvailable}/${work.chaptersTotal} 章",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = onToggleFavorite) {
                    Icon(
                        imageVector = if (work.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "收藏",
                        tint = if (work.isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                    )
                }
            }

            // Tags row (Rating + Relationship + Fandom)
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RatingBadge(rating = work.rating)
                if (work.relationships.isNotEmpty()) {
                    TagChip(text = work.relationships.first(), type = TagType.RELATIONSHIP)
                }
                if (work.fandoms.isNotEmpty()) {
                    TagChip(text = work.fandoms.first(), type = TagType.FANDOM)
                }
            }

            // Summary snippet
            if (work.summary.isNotBlank()) {
                Text(
                    text = work.summary,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Reading progress & Action buttons
            Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val progressText = if (work.lastReadChapterIndex > 0) {
                    "读至第 ${work.lastReadChapterIndex} 章"
                } else {
                    "尚未阅读"
                }
                Text(
                    text = progressText,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
                Row {
                    TextButton(onClick = onDetailClick) {
                        Text("详情目录", fontSize = 12.sp)
                    }
                    TextButton(onClick = onClick) {
                        Text("继续阅读", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
