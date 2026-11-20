package com.ao3reader.app.ui.screens.reader

import android.app.Activity
import android.view.WindowManager
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.ao3reader.app.AO3Application
import com.ao3reader.app.data.model.Chapter
import com.ao3reader.app.data.model.FontFamilyType
import com.ao3reader.app.data.model.ReaderSettings
import com.ao3reader.app.data.model.ReadingMode
import com.ao3reader.app.data.model.Work
import com.ao3reader.app.ui.components.ReaderSettingsSheet
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReaderScreen(
    navController: NavController,
    workId: String,
    initialChapterIndex: Int = 1
) {
    val context = LocalContext.current
    val workRepo = remember { AO3Application.instance.workRepository }
    val prefRepo = remember { AO3Application.instance.preferenceRepository }

    val worksMap by workRepo.works.collectAsState()
    val settings by prefRepo.readerSettings.collectAsState()

    val work = worksMap[workId]
    if (work == null || work.chapters.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("作品加载中或暂无章节...")
        }
        return
    }

    var currentChapterIndex by remember {
        mutableIntStateOf(initialChapterIndex.coerceIn(1, work.chapters.size))
    }
    val currentChapter = work.chapters.getOrNull(currentChapterIndex - 1) ?: work.chapters.first()

    var showControls by remember { mutableStateOf(false) }
    var showSettingsSheet by remember { mutableStateOf(false) }
    var showChapterDrawer by remember { mutableStateOf(false) }

    // Page state for no-animation page flip mode
    var currentPageIndex by remember { mutableIntStateOf(work.lastReadPageIndex) }

    // Keep screen on if requested
    DisposableEffect(settings.keepScreenOn) {
        val window = (context as? Activity)?.window
        if (settings.keepScreenOn) {
            window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
        onDispose {
            window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    // Save reading progress on exit
    BackHandler {
        workRepo.updateReadingProgress(work.id, currentChapterIndex, currentPageIndex)
        navController.popBackStack()
    }

    val readerBgColor = Color(settings.backgroundColorHex)
    val readerTextColor = Color(settings.textColorHex)

    val customFontFamily = when (settings.fontFamilyType) {
        FontFamilyType.SERIF -> FontFamily.Serif
        FontFamilyType.SANS_SERIF -> FontFamily.SansSerif
        FontFamilyType.MONOSPACE -> FontFamily.Monospace
        else -> FontFamily.Default
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(readerBgColor)
    ) {
        if (settings.readingMode == ReadingMode.SCROLL) {
            // ================= 模式 1: 平滑滑动模式 =================
            ScrollReaderView(
                chapter = currentChapter,
                totalChapters = work.chapters.size,
                settings = settings,
                fontFamily = customFontFamily,
                textColor = readerTextColor,
                onCenterTap = { showControls = !showControls },
                onPrevChapter = {
                    if (currentChapterIndex > 1) {
                        currentChapterIndex--
                        currentPageIndex = 0
                        workRepo.updateReadingProgress(work.id, currentChapterIndex, 0)
                    }
                },
                onNextChapter = {
                    if (currentChapterIndex < work.chapters.size) {
                        currentChapterIndex++
                        currentPageIndex = 0
                        workRepo.updateReadingProgress(work.id, currentChapterIndex, 0)
                    }
                }
            )
        } else {
            // ================= 模式 2: 无动画翻页模式 =================
            InstantPageFlipReaderView(
                workTitle = work.title,
                chapter = currentChapter,
                totalChapters = work.chapters.size,
                settings = settings,
                fontFamily = customFontFamily,
                textColor = readerTextColor,
                pageIndex = currentPageIndex,
                onPageIndexChange = { newPage ->
                    currentPageIndex = newPage
                    workRepo.updateReadingProgress(work.id, currentChapterIndex, newPage)
                },
                onCenterTap = { showControls = !showControls },
                onPrevChapter = {
                    if (currentChapterIndex > 1) {
                        currentChapterIndex--
                        currentPageIndex = 0
                        workRepo.updateReadingProgress(work.id, currentChapterIndex, 0)
                    }
                },
                onNextChapter = {
                    if (currentChapterIndex < work.chapters.size) {
                        currentChapterIndex++
                        currentPageIndex = 0
                        workRepo.updateReadingProgress(work.id, currentChapterIndex, 0)
                    }
                }
            )
        }

        // ================= 沉浸式阅读悬浮控制菜单 =================
        AnimatedVisibility(
            visible = showControls,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.TopCenter)
        ) {
            ReaderTopBar(
                title = currentChapter.title.ifBlank { "第 $currentChapterIndex 章" },
                onBack = {
                    workRepo.updateReadingProgress(work.id, currentChapterIndex, currentPageIndex)
                    navController.popBackStack()
                },
                onChapterList = { showChapterDrawer = true }
            )
        }

        AnimatedVisibility(
            visible = showControls,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            ReaderBottomBar(
                settings = settings,
                currentChapter = currentChapterIndex,
                totalChapters = work.chapters.size,
                onPrevChapter = {
                    if (currentChapterIndex > 1) {
                        currentChapterIndex--
                        currentPageIndex = 0
                    }
                },
                onNextChapter = {
                    if (currentChapterIndex < work.chapters.size) {
                        currentChapterIndex++
                        currentPageIndex = 0
                    }
                },
                onToggleReadingMode = {
                    val nextMode = if (settings.readingMode == ReadingMode.SCROLL)
                        ReadingMode.PAGE_FLIP_NO_ANIM
                    else
                        ReadingMode.SCROLL
                    prefRepo.setReadingMode(nextMode)
                },
                onOpenSettings = { showSettingsSheet = true },
                onOpenChapters = { showChapterDrawer = true }
            )
        }
    }

    // Settings sheet
    if (showSettingsSheet) {
        ReaderSettingsSheet(
            settings = settings,
            onSettingsChange = { transform -> prefRepo.updateReaderSettings(transform) },
            onDismissRequest = { showSettingsSheet = false }
        )
    }

    // Chapter list modal sheet
    if (showChapterDrawer) {
        ModalBottomSheet(onDismissRequest = { showChapterDrawer = false }) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "目录 (${work.chapters.size} 章)",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                LazyColumn(modifier = Modifier.fillMaxHeight(0.6f)) {
                    items(work.chapters) { ch ->
                        val isCurrent = ch.chapterIndex == currentChapterIndex
                        ListItem(
                            headlineContent = {
                                Text(
                                    text = ch.title.ifBlank { "第 ${ch.chapterIndex} 章" },
                                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isCurrent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                )
                            },
                            trailingContent = {
                                if (isCurrent) {
                                    Text("阅读中", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                                }
                            },
                            modifier = Modifier.clickable {
                                currentChapterIndex = ch.chapterIndex
                                currentPageIndex = 0
                                workRepo.updateReadingProgress(work.id, currentChapterIndex, 0)
                                showChapterDrawer = false
                            }
                        )
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 1. 滑动阅读组件 (Scroll Mode)
// -------------------------------------------------------------
@Composable
fun ScrollReaderView(
    chapter: Chapter,
    totalChapters: Int,
    settings: ReaderSettings,
    fontFamily: FontFamily,
    textColor: Color,
    onCenterTap: () -> Unit,
    onPrevChapter: () -> Unit,
    onNextChapter: () -> Unit
) {
    val listState = rememberLazyListState()

    LazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { onCenterTap() }
                )
            }
            .padding(horizontal = settings.horizontalPadding.dp),
        contentPadding = PaddingValues(top = 48.dp, bottom = 64.dp),
        verticalArrangement = Arrangement.spacedBy(settings.paragraphSpacing.dp)
    ) {
        // Chapter Title Header
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = chapter.title.ifBlank { "第 ${chapter.chapterIndex} 章" },
                    fontSize = (settings.fontSize + 4f).sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = fontFamily,
                    color = textColor,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(8.dp))
                Divider(
                    color = textColor.copy(alpha = 0.25f),
                    modifier = Modifier.width(60.dp),
                    thickness = 2.dp
                )
            }
        }

        // Pre-notes if any
        if (chapter.preNotes.isNotBlank()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = textColor.copy(alpha = 0.05f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "作者留言 (Chapter Notes):",
                            fontSize = (settings.fontSize - 3f).sp,
                            fontWeight = FontWeight.Bold,
                            color = textColor.copy(alpha = 0.75f)
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = chapter.preNotes,
                            fontSize = (settings.fontSize - 2f).sp,
                            lineHeight = ((settings.fontSize - 2f) * settings.lineHeightMultiplier).sp,
                            color = textColor.copy(alpha = 0.85f)
                        )
                    }
                }
            }
        }

        // Paragraphs of the body
        items(chapter.paragraphs) { paragraph ->
            Text(
                text = "　　$paragraph",
                fontSize = settings.fontSize.sp,
                lineHeight = (settings.fontSize * settings.lineHeightMultiplier).sp,
                fontFamily = fontFamily,
                color = textColor,
                textAlign = TextAlign.Justify
            )
        }

        // Bottom chapter nav buttons
        item {
            Spacer(Modifier.height(32.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(
                    onClick = onPrevChapter,
                    enabled = chapter.chapterIndex > 1
                ) {
                    Text("上一章")
                }
                Button(
                    onClick = onNextChapter,
                    enabled = chapter.chapterIndex < totalChapters
                ) {
                    Text("下一章")
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 2. 无动画瞬切翻页组件 (Instant Page Flip Mode)
// -------------------------------------------------------------
@Composable
fun InstantPageFlipReaderView(
    workTitle: String,
    chapter: Chapter,
    totalChapters: Int,
    settings: ReaderSettings,
    fontFamily: FontFamily,
    textColor: Color,
    pageIndex: Int,
    onPageIndexChange: (Int) -> Unit,
    onCenterTap: () -> Unit,
    onPrevChapter: () -> Unit,
    onNextChapter: () -> Unit
) {
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current

    // Dynamically paginate text into instant cut pages
    val pages = remember(chapter.paragraphs, settings.fontSize, settings.lineHeightMultiplier, configuration) {
        paginateText(
            paragraphs = chapter.paragraphs,
            chapterTitle = chapter.title.ifBlank { "第 ${chapter.chapterIndex} 章" },
            preNotes = chapter.preNotes,
            fontSize = settings.fontSize,
            lineHeightMult = settings.lineHeightMultiplier,
            screenWidthDp = configuration.screenWidthDp - (settings.horizontalPadding * 2).toInt(),
            screenHeightDp = configuration.screenHeightDp - 90
        )
    }

    val totalPages = if (pages.isEmpty()) 1 else pages.size
    val safePageIndex = pageIndex.coerceIn(0, totalPages - 1)
    val currentPageContent = pages.getOrNull(safePageIndex) ?: listOf("本章暂无文字")

    val currentTime = remember { SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date()) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(safePageIndex, totalPages) {
                detectTapGestures(
                    onTap = { offset ->
                        val screenWidth = size.width
                        val x = offset.x
                        when {
                            // Left 30%: Instant Prev Page
                            x < screenWidth * 0.3f -> {
                                if (safePageIndex > 0) {
                                    onPageIndexChange(safePageIndex - 1)
                                } else if (chapter.chapterIndex > 1) {
                                    onPrevChapter()
                                }
                            }
                            // Right 30%: Instant Next Page
                            x > screenWidth * 0.7f -> {
                                if (safePageIndex < totalPages - 1) {
                                    onPageIndexChange(safePageIndex + 1)
                                } else if (chapter.chapterIndex < totalChapters) {
                                    onNextChapter()
                                }
                            }
                            // Center 40%: Toggle Controls
                            else -> onCenterTap()
                        }
                    }
                )
            }
            .padding(horizontal = settings.horizontalPadding.dp, vertical = 12.dp)
    ) {
        // Subtle Header: Work Title & Chapter Title
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = workTitle,
                fontSize = 11.sp,
                color = textColor.copy(alpha = 0.5f),
                maxLines = 1,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = chapter.title.ifBlank { "第 ${chapter.chapterIndex} 章" },
                fontSize = 11.sp,
                color = textColor.copy(alpha = 0.5f),
                maxLines = 1
            )
        }

        // Body Content for the current page (Zero animation, instant render)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 26.dp, bottom = 26.dp),
            verticalArrangement = Arrangement.spacedBy(settings.paragraphSpacing.dp)
        ) {
            currentPageContent.forEach { p ->
                Text(
                    text = p,
                    fontSize = settings.fontSize.sp,
                    lineHeight = (settings.fontSize * settings.lineHeightMultiplier).sp,
                    fontFamily = fontFamily,
                    color = textColor,
                    textAlign = TextAlign.Justify
                )
            }
        }

        // Subtle Footer: Time & Instant Page Indicator (e.g. 5 / 24)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = currentTime,
                fontSize = 11.sp,
                color = textColor.copy(alpha = 0.5f)
            )
            Text(
                text = "${safePageIndex + 1} / $totalPages",
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = textColor.copy(alpha = 0.6f)
            )
        }
    }
}

/**
 * Robust text pagination algorithm that groups paragraphs into screen-fitted pages.
 */
private fun paginateText(
    paragraphs: List<String>,
    chapterTitle: String,
    preNotes: String,
    fontSize: Float,
    lineHeightMult: Float,
    screenWidthDp: Int,
    screenHeightDp: Int
): List<List<String>> {
    val pages = mutableListOf<MutableList<String>>()

    // Estimated characters per line & lines per page
    val charWidthDp = (fontSize * 0.72f).coerceAtLeast(10f)
    val charsPerLine = (screenWidthDp / charWidthDp).toInt().coerceAtLeast(12)
    val lineHeightDp = (fontSize * lineHeightMult * 1.15f)
    val maxLinesPerPage = (screenHeightDp / lineHeightDp).toInt().coerceAtLeast(10)

    var currentPage = mutableListOf<String>()
    var currentLinesCount = 0

    // Page 1 header
    currentPage.add("【$chapterTitle】\n")
    currentLinesCount += 2

    if (preNotes.isNotBlank()) {
        currentPage.add("作者留言：$preNotes\n")
        currentLinesCount += (preNotes.length / charsPerLine) + 2
    }

    for (rawP in paragraphs) {
        val indentedP = "　　$rawP"
        val linesNeeded = (indentedP.length / charsPerLine).coerceAtLeast(1) + 1

        if (currentLinesCount + linesNeeded > maxLinesPerPage && currentPage.isNotEmpty()) {
            pages.add(currentPage)
            currentPage = mutableListOf()
            currentLinesCount = 0
        }

        currentPage.add(indentedP)
        currentLinesCount += linesNeeded
    }

    if (currentPage.isNotEmpty()) {
        pages.add(currentPage)
    }

    return pages
}

// -------------------------------------------------------------
// 3. 悬浮菜单栏组件
// -------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReaderTopBar(
    title: String,
    onBack: () -> Unit,
    onChapterList: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f),
        tonalElevation = 6.dp,
        shadowElevation = 6.dp
    ) {
        TopAppBar(
            title = {
                Text(
                    text = title,
                    maxLines = 1,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                }
            },
            actions = {
                IconButton(onClick = onChapterList) {
                    Icon(Icons.Default.FormatListBulleted, contentDescription = "目录")
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
        )
    }
}

@Composable
fun ReaderBottomBar(
    settings: ReaderSettings,
    currentChapter: Int,
    totalChapters: Int,
    onPrevChapter: () -> Unit,
    onNextChapter: () -> Unit,
    onToggleReadingMode: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenChapters: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f),
        tonalElevation = 6.dp,
        shadowElevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Chapter fast switcher
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = onPrevChapter,
                    enabled = currentChapter > 1
                ) {
                    Icon(Icons.Default.NavigateBefore, contentDescription = null)
                    Text("上一章", fontSize = 12.sp)
                }

                Text(
                    text = "第 $currentChapter / $totalChapters 章",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )

                TextButton(
                    onClick = onNextChapter,
                    enabled = currentChapter < totalChapters
                ) {
                    Text("下一章", fontSize = 12.sp)
                    Icon(Icons.Default.NavigateNext, contentDescription = null)
                }
            }

            Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            // Action row: Mode toggle, Settings, Directory
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onOpenChapters) {
                    Icon(Icons.Default.MenuBook, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("目录", fontSize = 13.sp)
                }

                FilledTonalButton(onClick = onToggleReadingMode) {
                    Icon(
                        if (settings.readingMode == ReadingMode.PAGE_FLIP_NO_ANIM) Icons.Default.SwapHoriz else Icons.Default.SwapVert,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        if (settings.readingMode == ReadingMode.PAGE_FLIP_NO_ANIM) "切为滑动" else "切为无动画翻页",
                        fontSize = 12.sp
                    )
                }

                TextButton(onClick = onOpenSettings) {
                    Icon(Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("排版与颜色", fontSize = 13.sp)
                }
            }
        }
    }
}
