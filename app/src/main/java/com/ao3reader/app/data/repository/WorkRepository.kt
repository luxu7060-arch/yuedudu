package com.ao3reader.app.data.repository

import android.content.Context
import com.ao3reader.app.data.model.Chapter
import com.ao3reader.app.data.model.Work
import com.ao3reader.app.data.model.WorkRating
import com.ao3reader.app.data.network.AO3HttpClient
import com.ao3reader.app.data.parser.AO3HtmlParser
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.io.File

class WorkRepository(private val context: Context) {

    private val httpClient = AO3HttpClient(context)
    private val gson = Gson()
    private val storeFile = File(context.filesDir, "ao3_saved_works.json")

    private val _works = MutableStateFlow<Map<String, Work>>(emptyMap())
    val works: StateFlow<Map<String, Work>> = _works.asStateFlow()

    init {
        loadSavedWorks()
    }

    private fun loadSavedWorks() {
        if (storeFile.exists()) {
            try {
                val json = storeFile.readText()
                val type = object : TypeToken<Map<String, Work>>() {}.type
                val map: Map<String, Work> = gson.fromJson(json, type) ?: emptyMap()
                _works.value = map
                return
            } catch (ignored: Exception) {}
        }
        // Initialize with a demo fanfiction work if empty
        val sampleWork = createSampleWork()
        val initialMap = mapOf(sampleWork.id to sampleWork)
        _works.value = initialMap
        saveWorksToDisk(initialMap)
    }

    private fun saveWorksToDisk(map: Map<String, Work>) {
        try {
            val json = gson.toJson(map)
            storeFile.writeText(json)
        } catch (ignored: Exception) {}
    }

    fun getWorkById(workId: String): Work? {
        return _works.value[workId]
    }

    suspend fun fetchWork(workIdOrUrl: String): Result<Work> = withContext(Dispatchers.IO) {
        val workId = AO3HtmlParser.extractWorkId(workIdOrUrl)
            ?: return@withContext Result.failure(IllegalArgumentException("无法识别的 AO3 作品链接或 ID: $workIdOrUrl"))

        // Fetch from network
        val htmlResult = httpClient.fetchWorkHtml(workId)
        if (htmlResult.isFailure) {
            // If offline, check if we already have it in local cache
            val local = _works.value[workId]
            if (local != null) {
                return@withContext Result.success(local)
            }
            return@withContext Result.failure(htmlResult.exceptionOrNull() ?: Exception("加载失败"))
        }

        val html = htmlResult.getOrNull() ?: ""
        val parsedWork = AO3HtmlParser.parseWork(html, fallbackId = workId)

        // Merge with existing state (preserve favorite, reading progress)
        val existing = _works.value[workId]
        val finalWork = if (existing != null) {
            parsedWork.copy(
                isFavorite = existing.isFavorite,
                lastReadChapterIndex = existing.lastReadChapterIndex,
                lastReadPageIndex = existing.lastReadPageIndex,
                lastReadScrollOffset = existing.lastReadScrollOffset,
                lastReadTimestamp = System.currentTimeMillis()
            )
        } else {
            parsedWork.copy(lastReadTimestamp = System.currentTimeMillis())
        }

        val newMap = _works.value.toMutableMap()
        newMap[workId] = finalWork
        _works.value = newMap
        saveWorksToDisk(newMap)

        Result.success(finalWork)
    }

    fun toggleFavorite(workId: String) {
        val current = _works.value[workId] ?: return
        val updated = current.copy(isFavorite = !current.isFavorite)
        val newMap = _works.value.toMutableMap()
        newMap[workId] = updated
        _works.value = newMap
        saveWorksToDisk(newMap)
    }

    fun updateReadingProgress(
        workId: String,
        chapterIndex: Int,
        pageIndex: Int = 0,
        scrollOffset: Int = 0
    ) {
        val current = _works.value[workId] ?: return
        val updated = current.copy(
            lastReadChapterIndex = chapterIndex,
            lastReadPageIndex = pageIndex,
            lastReadScrollOffset = scrollOffset,
            lastReadTimestamp = System.currentTimeMillis()
        )
        val newMap = _works.value.toMutableMap()
        newMap[workId] = updated
        _works.value = newMap
        saveWorksToDisk(newMap)
    }

    fun deleteWork(workId: String) {
        val newMap = _works.value.toMutableMap()
        newMap.remove(workId)
        _works.value = newMap
        saveWorksToDisk(newMap)
    }

    private fun createSampleWork(): Work {
        val paragraphs1 = listOf(
            "夜幕降临，微凉的晚风穿过空旷的长廊，吹拂起窗前层叠的薄纱帘幔。",
            "这是一篇为了演示 AO3 纯净阅读器各项排版与阅读功能的范例文档。您可以直接在手机上体验丝滑的阅读过程。",
            "本阅读器专为 AO3 读者打造，彻底解决了原生网页在移动端字体细小、排版拥挤、误触等问题。您可以随时点击屏幕中央调出阅读菜单。",
            "在阅读菜单中，您可以体验两种核心阅读模式：",
            "第一种是【平滑滑动模式】，适合习惯连续滚动阅读的读者，配合流畅的手势滑行；",
            "第二种是【无动画瞬切翻页模式】，点击屏幕左侧回翻、点击右侧前进，零延迟、零多余晃眼动画，带来如同墨水屏电纸书般纯粹舒适的文字沉浸体验。",
            "不仅如此，您还可以在【背景与字体设置】中自由选择纯白、暖阳羊皮纸、豆沙护眼绿、石墨暗黑等多种专业配色方案，或者随意自定义任何心仪的背景颜色与文字大小。"
        )

        val paragraphs2 = listOf(
            "第二章的晨光静悄悄地洒落在窗台上。",
            "如果您想阅读 AO3 上的任意同人作品，只需要在首页输入作品的网址（例如 https://archiveofourown.org/works/12345678）或直接输入纯数字作品 ID，阅读器就会自动获取全文内容并为您转换为精美的纯净版面。",
            "另外，在下方的【AO3 浏览】标签页中，您也可以像使用浏览器一样自由探索 AO3。当浏览到任何作品详情页时，底部会自动浮现【一键进入纯净阅读】按钮，轻触即可告别杂乱页面，享受极致的阅读快感。",
            "祝您在文字的世界里畅游愉快！"
        )

        val ch1 = Chapter(
            id = "demo_1",
            workId = "demo_work",
            chapterIndex = 1,
            title = "第一章：星芒与薄暮",
            summary = "夜幕降临时的初遇，以及纯净阅读器的基础功能导览。",
            paragraphs = paragraphs1
        )

        val ch2 = Chapter(
            id = "demo_2",
            workId = "demo_work",
            chapterIndex = 2,
            title = "第二章：晨光与远行",
            summary = "关于如何在 AO3 自由导入、收藏与探索作品的指南说明。",
            paragraphs = paragraphs2
        )

        return Work(
            id = "demo_work",
            title = "繁星轨迹下的漫步 (功能演示指南)",
            author = "AO3 Reader Dev",
            summary = "欢迎使用 AO3 纯净阅读器！本文档用于演示排版、无动画翻页、自定义背景、字号调节及界面主题等所有核心功能。",
            rating = WorkRating.GENERAL,
            fandoms = listOf("原创", "阅读器演示指南"),
            relationships = listOf("读者 / 纯净排版", "夜读 / 护眼模式"),
            characters = listOf("爱看文的读者", "贴心的阅读器"),
            freeformTags = listOf("舒适排版", "无动画瞬切翻页", "自定义字体字号", "多主题变色", "离线缓存"),
            warnings = listOf("No Archive Warnings Apply (无警告)"),
            language = "中文",
            wordCount = 1860,
            chaptersTotal = 2,
            chaptersAvailable = 2,
            publishDate = "2026-11-20",
            updateDate = "2026-11-20",
            chapters = listOf(ch1, ch2),
            isFavorite = true,
            lastReadTimestamp = System.currentTimeMillis()
        )
    }
}
