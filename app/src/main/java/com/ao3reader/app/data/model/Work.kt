package com.ao3reader.app.data.model

import com.google.gson.annotations.SerializedName

enum class WorkRating(val displayName: String, val colorHex: Long) {
    GENERAL("General Audiences (全年龄)", 0xFF4CAF50),
    TEEN("Teen And Up Audiences (青少年)", 0xFFFFC107),
    MATURE("Mature (成熟)", 0xFFFF9800),
    EXPLICIT("Explicit (限制级/R18)", 0xFFE53935),
    NOT_RATED("Not Rated (未评级)", 0xFF757575);

    companion object {
        fun fromString(str: String): WorkRating {
            return when {
                str.contains("General", ignoreCase = true) -> GENERAL
                str.contains("Teen", ignoreCase = true) -> TEEN
                str.contains("Mature", ignoreCase = true) -> MATURE
                str.contains("Explicit", ignoreCase = true) -> EXPLICIT
                else -> NOT_RATED
            }
        }
    }
}

data class Chapter(
    @SerializedName("id") val id: String = "",
    @SerializedName("workId") val workId: String = "",
    @SerializedName("chapterIndex") val chapterIndex: Int = 1,
    @SerializedName("title") val title: String = "",
    @SerializedName("summary") val summary: String = "",
    @SerializedName("preNotes") val preNotes: String = "",
    @SerializedName("postNotes") val postNotes: String = "",
    @SerializedName("paragraphs") val paragraphs: List<String> = emptyList(),
    @SerializedName("rawHtml") val rawHtml: String = ""
)

data class Work(
    @SerializedName("id") val id: String,
    @SerializedName("title") val title: String,
    @SerializedName("author") val author: String = "匿名",
    @SerializedName("authorUrl") val authorUrl: String = "",
    @SerializedName("summary") val summary: String = "",
    @SerializedName("rating") val rating: WorkRating = WorkRating.NOT_RATED,
    @SerializedName("fandoms") val fandoms: List<String> = emptyList(),
    @SerializedName("relationships") val relationships: List<String> = emptyList(),
    @SerializedName("characters") val characters: List<String> = emptyList(),
    @SerializedName("freeformTags") val freeformTags: List<String> = emptyList(),
    @SerializedName("warnings") val warnings: List<String> = emptyList(),
    @SerializedName("language") val language: String = "中文",
    @SerializedName("wordCount") val wordCount: Long = 0,
    @SerializedName("chaptersTotal") val chaptersTotal: Int = 1,
    @SerializedName("chaptersAvailable") val chaptersAvailable: Int = 1,
    @SerializedName("publishDate") val publishDate: String = "",
    @SerializedName("updateDate") val updateDate: String = "",
    @SerializedName("chapters") val chapters: List<Chapter> = emptyList(),
    @SerializedName("lastReadChapterIndex") val lastReadChapterIndex: Int = 0,
    @SerializedName("lastReadPageIndex") val lastReadPageIndex: Int = 0,
    @SerializedName("lastReadScrollOffset") val lastReadScrollOffset: Int = 0,
    @SerializedName("lastReadTimestamp") val lastReadTimestamp: Long = 0L,
    @SerializedName("isFavorite") val isFavorite: Boolean = false,
    @SerializedName("isDownloaded") val isDownloaded: Boolean = false
) {
    val displayChaptersCount: String
        get() = "$chaptersAvailable/$chaptersTotal"
}
