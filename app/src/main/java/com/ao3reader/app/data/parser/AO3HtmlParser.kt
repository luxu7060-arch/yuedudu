package com.ao3reader.app.data.parser

import com.ao3reader.app.data.model.Chapter
import com.ao3reader.app.data.model.Work
import com.ao3reader.app.data.model.WorkRating
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import java.util.regex.Pattern

object AO3HtmlParser {

    private val WORK_ID_PATTERN = Pattern.compile("works/([0-9]+)")
    private val DIGITS_ONLY_PATTERN = Pattern.compile("^[0-9]+$")

    /**
     * Extracts Work ID from a URL or raw ID string.
     * Examples:
     * - "https://archiveofourown.org/works/12345678" -> "12345678"
     * - "https://archiveofourown.org/works/12345678/chapters/99999" -> "12345678"
     * - "12345678" -> "12345678"
     */
    fun extractWorkId(input: String): String? {
        val trimmed = input.trim()
        if (DIGITS_ONLY_PATTERN.matcher(trimmed).matches()) {
            return trimmed
        }
        val matcher = WORK_ID_PATTERN.matcher(trimmed)
        return if (matcher.find()) {
            matcher.group(1)
        } else {
            null
        }
    }

    /**
     * Parses AO3 Work HTML into a clean Work data object.
     */
    fun parseWork(html: String, fallbackId: String = ""): Work {
        val doc: Document = Jsoup.parse(html)

        // 1. Title
        val titleEl = doc.selectFirst("h2.title.heading, h2.title, div.preface h2.title")
        val title = titleEl?.text()?.trim() ?: "无题作品"

        // 2. Author
        val authorEl = doc.selectFirst("h3.byline.heading a[rel=author], h3.byline a")
        val author = authorEl?.text()?.trim() ?: "匿名"
        val authorUrl = authorEl?.attr("href") ?: ""

        // 3. Work ID
        val canonicalLink = doc.selectFirst("link[rel=canonical]")?.attr("href") ?: ""
        val workId = extractWorkId(canonicalLink) ?: fallbackId.ifBlank { "unknown" }

        // 4. Meta & Tags
        val ratingStr = doc.select("dd.rating.tags ul li a").text()
        val rating = WorkRating.fromString(ratingStr)

        val warnings = doc.select("dd.warning.tags ul li a").map { it.text().trim() }
        val fandoms = doc.select("dd.fandom.tags ul li a").map { it.text().trim() }
        val relationships = doc.select("dd.relationship.tags ul li a").map { it.text().trim() }
        val characters = doc.select("dd.character.tags ul li a").map { it.text().trim() }
        val freeforms = doc.select("dd.freeform.tags ul li a").map { it.text().trim() }
        val language = doc.select("dd.language").text().trim().ifBlank { "中文" }

        // 5. Stats
        val wordsStr = doc.select("dd.words").text().replace(",", "").trim()
        val wordCount = wordsStr.toLongOrNull() ?: 0L

        val chaptersStatsStr = doc.select("dd.chapters").text().trim()
        var chaptersTotal = 1
        var chaptersAvailable = 1
        if (chaptersStatsStr.contains("/")) {
            val parts = chaptersStatsStr.split("/")
            chaptersAvailable = parts[0].trim().toIntOrNull() ?: 1
            chaptersTotal = if (parts[1].trim() == "?") chaptersAvailable else parts[1].trim().toIntOrNull() ?: chaptersAvailable
        }

        val publishDate = doc.select("dd.published").text().trim()
        val updateDate = doc.select("dd.status").text().trim().ifBlank { publishDate }

        // 6. Summary
        val summaryEl = doc.selectFirst("div.summary.module blockquote.userstuff, div.preface div.summary blockquote")
        val summary = cleanHtmlText(summaryEl?.html() ?: "")

        // 7. Chapters & Content
        val chapterElements = doc.select("div#chapters > div.chapter")
        val chapters = mutableListOf<Chapter>()

        if (chapterElements.isNotEmpty()) {
            // Multi-chapter full work view
            chapterElements.forEachIndexed { index, chapterEl ->
                val chTitleEl = chapterEl.selectFirst("h3.title, div.chapter.preface h3.title, h3.heading")
                val chTitle = chTitleEl?.text()?.trim() ?: "第 ${index + 1} 章"

                val chSummaryEl = chapterEl.selectFirst("div.summary blockquote.userstuff")
                val chSummary = cleanHtmlText(chSummaryEl?.html() ?: "")

                val chPreNotesEl = chapterEl.selectFirst("div.notes.module blockquote.userstuff")
                val chPreNotes = cleanHtmlText(chPreNotesEl?.html() ?: "")

                val contentEl = chapterEl.selectFirst("div.userstuff[role=article], div.userstuff")
                val paragraphs = extractParagraphs(contentEl)

                chapters.add(
                    Chapter(
                        id = "${workId}_ch_${index + 1}",
                        workId = workId,
                        chapterIndex = index + 1,
                        title = chTitle,
                        summary = chSummary,
                        preNotes = chPreNotes,
                        paragraphs = paragraphs,
                        rawHtml = contentEl?.html() ?: ""
                    )
                )
            }
        } else {
            // Single chapter or single-page chapter view
            val contentEl = doc.selectFirst("div#chapters div.userstuff, div#workskin div.userstuff, div.userstuff")
            val paragraphs = extractParagraphs(contentEl)

            val chTitle = doc.selectFirst("div#chapters h3.title, div.chapter h3.title")?.text()?.trim()
                ?: title

            chapters.add(
                Chapter(
                    id = "${workId}_ch_1",
                    workId = workId,
                    chapterIndex = 1,
                    title = chTitle,
                    summary = summary,
                    paragraphs = paragraphs,
                    rawHtml = contentEl?.html() ?: ""
                )
            )
        }

        return Work(
            id = workId,
            title = title,
            author = author,
            authorUrl = authorUrl,
            summary = summary,
            rating = rating,
            fandoms = fandoms,
            relationships = relationships,
            characters = characters,
            freeformTags = freeforms,
            warnings = warnings,
            language = language,
            wordCount = wordCount,
            chaptersTotal = if (chapters.size > chaptersTotal) chapters.size else chaptersTotal,
            chaptersAvailable = chapters.size,
            publishDate = publishDate,
            updateDate = updateDate,
            chapters = chapters
        )
    }

    private fun extractParagraphs(element: Element?): List<String> {
        if (element == null) return emptyList()

        val paragraphs = mutableListOf<String>()
        val pElements = element.select("p")

        if (pElements.isNotEmpty()) {
            for (p in pElements) {
                val clean = cleanHtmlText(p.html())
                if (clean.isNotBlank()) {
                    paragraphs.add(clean)
                }
            }
        } else {
            // Fallback for works that use <br> instead of <p> tags
            val textWithBreaks = element.html()
                .replace("(?i)<br\\s*/?>".toRegex(), "\n")
                .replace("(?i)</p>".toRegex(), "\n\n")
            val docClean = Jsoup.parse(textWithBreaks).text()
            val split = docClean.split("\n")
            for (line in split) {
                val trimmed = line.trim()
                if (trimmed.isNotBlank()) {
                    paragraphs.add(trimmed)
                }
            }
        }
        return paragraphs
    }

    private fun cleanHtmlText(rawHtml: String): String {
        if (rawHtml.isBlank()) return ""
        val doc = Jsoup.parse(rawHtml)
        // Convert breaklines to linebreaks before extracting text
        doc.select("br").append("\\n")
        doc.select("p").prepend("\\n\\n")
        val clean = doc.text().replace("\\n", "\n").trim()
        return clean
    }
}
