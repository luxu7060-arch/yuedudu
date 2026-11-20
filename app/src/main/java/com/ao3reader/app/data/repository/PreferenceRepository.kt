package com.ao3reader.app.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.ao3reader.app.data.model.AppThemeSettings
import com.ao3reader.app.data.model.BackgroundPreset
import com.ao3reader.app.data.model.FontFamilyType
import com.ao3reader.app.data.model.ReaderSettings
import com.ao3reader.app.data.model.ReadingMode
import com.ao3reader.app.data.model.ThemeMode
import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class PreferenceRepository(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("ao3_reader_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    private val _readerSettings = MutableStateFlow(loadReaderSettings())
    val readerSettings: StateFlow<ReaderSettings> = _readerSettings.asStateFlow()

    private val _themeSettings = MutableStateFlow(loadThemeSettings())
    val themeSettings: StateFlow<AppThemeSettings> = _themeSettings.asStateFlow()

    private fun loadReaderSettings(): ReaderSettings {
        val json = prefs.getString("reader_settings_json", null)
        return if (!json.isNullOrBlank()) {
            try {
                gson.fromJson(json, ReaderSettings::class.java)
            } catch (e: Exception) {
                ReaderSettings()
            }
        } else {
            ReaderSettings()
        }
    }

    private fun loadThemeSettings(): AppThemeSettings {
        val json = prefs.getString("app_theme_json", null)
        return if (!json.isNullOrBlank()) {
            try {
                gson.fromJson(json, AppThemeSettings::class.java)
            } catch (e: Exception) {
                AppThemeSettings()
            }
        } else {
            AppThemeSettings()
        }
    }

    fun updateReaderSettings(transform: (ReaderSettings) -> ReaderSettings) {
        val updated = transform(_readerSettings.value)
        _readerSettings.value = updated
        prefs.edit().putString("reader_settings_json", gson.toJson(updated)).apply()
    }

    fun updateThemeSettings(transform: (AppThemeSettings) -> AppThemeSettings) {
        val updated = transform(_themeSettings.value)
        _themeSettings.value = updated
        prefs.edit().putString("app_theme_json", gson.toJson(updated)).apply()
    }

    fun setReadingMode(mode: ReadingMode) {
        updateReaderSettings { it.copy(readingMode = mode) }
    }

    fun setFontSize(size: Float) {
        updateReaderSettings { it.copy(fontSize = size.coerceIn(12f, 38f)) }
    }

    fun setLineHeightMultiplier(mult: Float) {
        updateReaderSettings { it.copy(lineHeightMultiplier = mult.coerceIn(1.1f, 2.5f)) }
    }

    fun setBackgroundPreset(preset: BackgroundPreset) {
        updateReaderSettings {
            if (preset == BackgroundPreset.CUSTOM) {
                it.copy(backgroundPreset = preset)
            } else {
                it.copy(
                    backgroundPreset = preset,
                    backgroundColorHex = preset.backgroundColorHex,
                    textColorHex = preset.textColorHex
                )
            }
        }
    }

    fun setCustomColors(backgroundColor: Long, textColor: Long) {
        updateReaderSettings {
            it.copy(
                backgroundPreset = BackgroundPreset.CUSTOM,
                backgroundColorHex = backgroundColor,
                textColorHex = textColor
            )
        }
    }

    fun setFontFamily(type: FontFamilyType, path: String? = null, name: String? = null) {
        updateReaderSettings {
            it.copy(
                fontFamilyType = type,
                customFontPath = path,
                customFontName = name
            )
        }
    }

    fun setCustomBackgroundImage(uri: String?, opacity: Float = 0.35f) {
        updateReaderSettings {
            it.copy(
                customBackgroundImageUri = uri,
                backgroundOverlayOpacity = opacity
            )
        }
    }

    fun setAppThemeColor(primaryColorHex: Long) {
        updateThemeSettings { it.copy(primaryColorHex = primaryColorHex) }
    }

    fun setThemeMode(mode: ThemeMode) {
        updateThemeSettings { it.copy(themeMode = mode) }
    }
}
