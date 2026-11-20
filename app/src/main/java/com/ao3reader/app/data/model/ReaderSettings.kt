package com.ao3reader.app.data.model

import com.google.gson.annotations.SerializedName

enum class ReadingMode(val displayName: String) {
    SCROLL("平滑滑动模式"),
    PAGE_FLIP_NO_ANIM("无动画翻页模式")
}

enum class BackgroundPreset(
    val displayName: String,
    val backgroundColorHex: Long,
    val textColorHex: Long
) {
    WHITE("纯白明亮", 0xFFFFFFFF, 0xFF1C1B1F),
    SEPIA("暖阳羊皮纸", 0xFFF7EED8, 0xFF3E2D20),
    MINT("豆沙护眼绿", 0xFFE8F5E9, 0xFF1B3B1B),
    GRAY("极简水墨灰", 0xFFF0F2F5, 0xFF2A2E33),
    CHARCOAL("石墨暗灰", 0xFF232529, 0xFFE3E5E8),
    OLED_BLACK("极黑省电 (OLED)", 0xFF000000, 0xFFE0E0E0),
    MIDNIGHT("静谧深蓝", 0xFF0D1B2A, 0xFFE0E1DD),
    CUSTOM("自定义背景", 0xFFFFFFFF, 0xFF1C1B1F)
}

enum class FontFamilyType(val displayName: String) {
    SYSTEM("系统默认"),
    SERIF("经典衬线体 (宋体风格)"),
    SANS_SERIF("现代无衬线 (黑体风格)"),
    MONOSPACE("等宽字体"),
    CUSTOM("自定义字体文件")
}

data class ReaderSettings(
    @SerializedName("readingMode") val readingMode: ReadingMode = ReadingMode.PAGE_FLIP_NO_ANIM,
    @SerializedName("fontSize") val fontSize: Float = 19f, // in sp
    @SerializedName("lineHeightMultiplier") val lineHeightMultiplier: Float = 1.65f,
    @SerializedName("paragraphSpacing") val paragraphSpacing: Float = 14f, // in dp
    @SerializedName("horizontalPadding") val horizontalPadding: Float = 20f, // in dp
    @SerializedName("fontFamilyType") val fontFamilyType: FontFamilyType = FontFamilyType.SYSTEM,
    @SerializedName("customFontPath") val customFontPath: String? = null,
    @SerializedName("customFontName") val customFontName: String? = null,
    @SerializedName("backgroundColorHex") val backgroundColorHex: Long = 0xFFF7EED8, // default sepia
    @SerializedName("textColorHex") val textColorHex: Long = 0xFF3E2D20,
    @SerializedName("backgroundPreset") val backgroundPreset: BackgroundPreset = BackgroundPreset.SEPIA,
    @SerializedName("customBackgroundImageUri") val customBackgroundImageUri: String? = null,
    @SerializedName("backgroundOverlayOpacity") val backgroundOverlayOpacity: Float = 0.35f,
    @SerializedName("keepScreenOn") val keepScreenOn: Boolean = true,
    @SerializedName("volumeKeysTurnPage") val volumeKeysTurnPage: Boolean = true,
    @SerializedName("showProgressIndicator") val showProgressIndicator: Boolean = true
)

enum class ThemeMode(val displayName: String) {
    SYSTEM("跟随系统"),
    LIGHT("明亮模式"),
    DARK("暗黑模式")
}

data class AppThemeSettings(
    @SerializedName("themeMode") val themeMode: ThemeMode = ThemeMode.SYSTEM,
    @SerializedName("primaryColorHex") val primaryColorHex: Long = 0xFF990000, // AO3 classic red
    @SerializedName("useDynamicColor") val useDynamicColor: Boolean = false
)
