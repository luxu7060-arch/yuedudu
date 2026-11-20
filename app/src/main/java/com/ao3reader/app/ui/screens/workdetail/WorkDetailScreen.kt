package com.ao3reader.app.ui.screens.workdetail

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.ao3reader.app.AO3Application
import com.ao3reader.app.ui.components.RatingBadge
import com.ao3reader.app.ui.components.TagChip
import com.ao3reader.app.ui.components.TagType
import com.ao3reader.app.ui.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun WorkDetailScreen(
    navController: NavController,
    workId: String
) {
    val context = LocalContext.current
    val workRepo = remember { AO3Application.instance.workRepository }
    val worksMap by workRepo.works.collectAsState()
    val work = worksMap[workId]

    if (work == null) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("作品详情") },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                        }
                    }
                )
            }
        ) { padding ->
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("未找到该作品或正在加载中...")
            }
        }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("作品详情", fontSize = 18.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    IconButton(onClick = { workRepo.toggleFavorite(work.id) }) {
                        Icon(
                            imageVector = if (work.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "收藏",
                            tint = if (work.isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                        )
                    }
                    IconButton(onClick = {
                        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://archiveofourown.org/works/${work.id}"))
                        context.startActivity(browserIntent)
                    }) {
                        Icon(Icons.Default.OpenInBrowser, contentDescription = "在浏览器中打开")
                    }
                }
            )
        },
        bottomBar = {
            Surface(
                tonalElevation = 4.dp,
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = { workRepo.toggleFavorite(work.id) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            if (work.isFavorite) Icons.Default.BookmarkAdded else Icons.Default.BookmarkAdd,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(if (work.isFavorite) "已在书架" else "加入书架")
                    }

                    Button(
                        onClick = {
                            val targetChapter = if (work.lastReadChapterIndex > 0) work.lastReadChapterIndex else 1
                            navController.navigate(Screen.Reader.createRoute(work.id, targetChapter))
                        },
                        modifier = Modifier.weight(1.4f)
                    ) {
                        Icon(Icons.Default.AutoStories, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = if (work.lastReadChapterIndex > 0) "继续阅读 (第${work.lastReadChapterIndex}章)" else "开始纯净阅读",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Title & Author
            item {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = work.title,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "作者: ${work.author}",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Stats row
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "总字数", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(text = "${work.wordCount}", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "章节", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(text = "${work.chaptersAvailable}/${work.chaptersTotal}", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "语言", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(text = work.language, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "更新时间", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(text = work.updateDate.ifBlank { "近期" }, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Beautified Tag Cloud
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = "作品标签与分类", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        RatingBadge(rating = work.rating)
                        work.relationships.forEach { rel ->
                            TagChip(text = rel, type = TagType.RELATIONSHIP)
                        }
                        work.fandoms.forEach { fandom ->
                            TagChip(text = fandom, type = TagType.FANDOM)
                        }
                        work.characters.forEach { char ->
                            TagChip(text = char, type = TagType.CHARACTER)
                        }
                        work.freeformTags.forEach { tag ->
                            TagChip(text = tag, type = TagType.FREEFORM)
                        }
                    }
                }
            }

            // Summary
            if (work.summary.isNotBlank()) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(text = "作品简介 (Summary)", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Text(
                                text = work.summary,
                                modifier = Modifier.padding(14.dp),
                                fontSize = 14.sp,
                                lineHeight = 22.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Chapter list
            item {
                Text(
                    text = "章节目录 (${work.chapters.size} 章)",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            itemsIndexed(work.chapters) { index, chapter ->
                val chIndex = index + 1
                val isCurrent = work.lastReadChapterIndex == chIndex

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .clickable {
                            navController.navigate(Screen.Reader.createRoute(work.id, chIndex))
                        },
                    colors = CardDefaults.cardColors(
                        containerColor = if (isCurrent)
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                        else
                            MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = chapter.title.ifBlank { "第 $chIndex 章" },
                                fontSize = 15.sp,
                                fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium,
                                color = if (isCurrent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                            if (chapter.summary.isNotBlank()) {
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = chapter.summary,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1
                                )
                            }
                        }
                        if (isCurrent) {
                            Text(
                                text = "正在阅读",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        } else {
                            Icon(
                                Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                }
            }
        }
    }
}
