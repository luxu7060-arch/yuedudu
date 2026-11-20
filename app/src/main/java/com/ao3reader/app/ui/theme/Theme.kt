package com.ao3reader.app.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.ao3reader.app.data.model.AppThemeSettings
import com.ao3reader.app.data.model.ThemeMode

@Composable
fun AO3ReaderTheme(
    themeSettings: AppThemeSettings,
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeSettings.themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }

    val context = LocalContext.current
    val primaryColor = Color(themeSettings.primaryColorHex)

    val colorScheme: ColorScheme = when {
        themeSettings.useDynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> {
            darkColorScheme(
                primary = primaryColor,
                onPrimary = Color.White,
                primaryContainer = primaryColor.copy(alpha = 0.35f),
                onPrimaryContainer = Color.White,
                secondary = primaryColor.copy(alpha = 0.85f),
                onSecondary = Color.White,
                background = Color(0xFF121212),
                onBackground = Color(0xFFEEEEEE),
                surface = Color(0xFF1E1E1E),
                onSurface = Color(0xFFEEEEEE),
                surfaceVariant = Color(0xFF2C2C2C),
                onSurfaceVariant = Color(0xFFCCCCCC)
            )
        }
        else -> {
            lightColorScheme(
                primary = primaryColor,
                onPrimary = Color.White,
                primaryContainer = primaryColor.copy(alpha = 0.12f),
                onPrimaryContainer = primaryColor,
                secondary = primaryColor.copy(alpha = 0.85f),
                onSecondary = Color.White,
                background = Color(0xFFF9FAFB),
                onBackground = Color(0xFF1F2937),
                surface = Color.White,
                onSurface = Color(0xFF1F2937),
                surfaceVariant = Color(0xFFF3F4F6),
                onSurfaceVariant = Color(0xFF4B5563)
            )
        }
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
