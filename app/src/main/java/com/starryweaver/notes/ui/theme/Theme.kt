package com.starryweaver.notes.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.starryweaver.notes.data.ThemeColor
import com.starryweaver.notes.data.ThemeMode

val NotesTypography = Typography()

fun colorSchemeFor(accent: AccentColors, dark: Boolean): ColorScheme =
    if (dark) {
        darkColorScheme(
            primary = accent.primary,
            onPrimary = accent.onPrimary,
            primaryContainer = accent.primaryContainer,
            onPrimaryContainer = accent.onPrimaryContainer,
            secondary = accent.secondary,
            onSecondary = accent.onSecondary,
            secondaryContainer = accent.secondaryContainer,
            onSecondaryContainer = accent.onSecondaryContainer,
            tertiary = accent.tertiary,
            onTertiary = accent.onTertiary,
            tertiaryContainer = accent.tertiaryContainer,
            onTertiaryContainer = accent.onTertiaryContainer,
            background = DarkBackground,
            onBackground = DarkOnBackground,
            surface = DarkSurface,
            onSurface = DarkOnSurface,
            surfaceVariant = DarkSurfaceVariant,
            onSurfaceVariant = DarkOnSurfaceVariant,
            outline = DarkOutline,
            outlineVariant = DarkOutlineVariant,
            error = DarkError,
            onError = DarkOnError,
            errorContainer = DarkErrorContainer,
            onErrorContainer = DarkOnErrorContainer,
        )
    } else {
        lightColorScheme(
            primary = accent.primary,
            onPrimary = accent.onPrimary,
            primaryContainer = accent.primaryContainer,
            onPrimaryContainer = accent.onPrimaryContainer,
            secondary = accent.secondary,
            onSecondary = accent.onSecondary,
            secondaryContainer = accent.secondaryContainer,
            onSecondaryContainer = accent.onSecondaryContainer,
            tertiary = accent.tertiary,
            onTertiary = accent.onTertiary,
            tertiaryContainer = accent.tertiaryContainer,
            onTertiaryContainer = accent.onTertiaryContainer,
            background = LightBackground,
            onBackground = LightOnBackground,
            surface = LightSurface,
            onSurface = LightOnSurface,
            surfaceVariant = LightSurfaceVariant,
            onSurfaceVariant = LightOnSurfaceVariant,
            outline = LightOutline,
            outlineVariant = LightOutlineVariant,
            error = LightError,
            onError = LightOnError,
            errorContainer = LightErrorContainer,
            onErrorContainer = LightOnErrorContainer,
        )
    }

@Composable
fun resolveDark(themeMode: ThemeMode): Boolean = when (themeMode) {
    ThemeMode.SYSTEM -> isSystemInDarkTheme()
    ThemeMode.LIGHT -> false
    ThemeMode.DARK -> true
}

@Composable
fun rememberColorScheme(
    dark: Boolean,
    dynamicColor: Boolean,
    themeColor: ThemeColor,
    customColor: Color,
): ColorScheme {
    val context = LocalContext.current
    return remember(dark, dynamicColor, themeColor, customColor) {
        if (dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (dark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        } else {
            colorSchemeFor(accentColors(themeColor, dark, customColor), dark)
        }
    }
}
