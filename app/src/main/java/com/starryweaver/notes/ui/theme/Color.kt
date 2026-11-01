package com.starryweaver.notes.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.ColorUtils
import com.starryweaver.notes.data.ThemeColor

// 中性色（各主题共用）
val LightBackground = Color(0xFFFBF8FF)
val LightOnBackground = Color(0xFF1B1B21)
val LightSurface = Color(0xFFFBF8FF)
val LightOnSurface = Color(0xFF1B1B21)
val LightSurfaceVariant = Color(0xFFE2E1EC)
val LightOnSurfaceVariant = Color(0xFF45464F)
val LightOutline = Color(0xFF757680)
val LightOutlineVariant = Color(0xFFC5C6D0)

val DarkBackground = Color(0xFF121318)
val DarkOnBackground = Color(0xFFE3E1E9)
val DarkSurface = Color(0xFF121318)
val DarkOnSurface = Color(0xFFE3E1E9)
val DarkSurfaceVariant = Color(0xFF45464F)
val DarkOnSurfaceVariant = Color(0xFFC6C5D0)
val DarkOutline = Color(0xFF90909A)
val DarkOutlineVariant = Color(0xFF45464F)

val LightError = Color(0xFFBA1A1A)
val LightOnError = Color(0xFFFFFFFF)
val LightErrorContainer = Color(0xFFFFDAD6)
val LightOnErrorContainer = Color(0xFF410002)

val DarkError = Color(0xFFFFB4AB)
val DarkOnError = Color(0xFF690005)
val DarkErrorContainer = Color(0xFF93000A)
val DarkOnErrorContainer = Color(0xFFFFDAD6)

/** 每套主题的强调色系；中性色与错误色共用。 */
data class AccentColors(
    val primary: Color,
    val onPrimary: Color,
    val primaryContainer: Color,
    val onPrimaryContainer: Color,
    val secondary: Color,
    val onSecondary: Color,
    val secondaryContainer: Color,
    val onSecondaryContainer: Color,
    val tertiary: Color,
    val onTertiary: Color,
    val tertiaryContainer: Color,
    val onTertiaryContainer: Color,
)

private val IndigoLight = AccentColors(
    Color(0xFF4355B9), Color(0xFFFFFFFF), Color(0xFFDEE0FF), Color(0xFF00105C),
    Color(0xFF5A5D72), Color(0xFFFFFFFF), Color(0xFFDFE1F9), Color(0xFF171B2C),
    Color(0xFF76546E), Color(0xFFFFFFFF), Color(0xFFFFD7F3), Color(0xFF2C1229),
)
private val IndigoDark = AccentColors(
    Color(0xFFBAC3FF), Color(0xFF0B1D63), Color(0xFF2A3B8F), Color(0xFFDEE0FF),
    Color(0xFFC3C5DD), Color(0xFF2C2F42), Color(0xFF424659), Color(0xFFDFE1F9),
    Color(0xFFE5BAD8), Color(0xFF44273F), Color(0xFF5D3D56), Color(0xFFFFD7F3),
)

private val BlueLight = AccentColors(
    Color(0xFF1E6FD9), Color(0xFFFFFFFF), Color(0xFFD6E3FF), Color(0xFF001B3D),
    Color(0xFF555F71), Color(0xFFFFFFFF), Color(0xFFD9E2F5), Color(0xFF121C2B),
    Color(0xFF3A6478), Color(0xFFFFFFFF), Color(0xFFBEE9FF), Color(0xFF001E2B),
)
private val BlueDark = AccentColors(
    Color(0xFFA9C7FF), Color(0xFF003062), Color(0xFF00468C), Color(0xFFD6E3FF),
    Color(0xFFBDC7DC), Color(0xFF273141), Color(0xFF3D4758), Color(0xFFD9E2F5),
    Color(0xFFA2CDE3), Color(0xFF063547), Color(0xFF224C5F), Color(0xFFBEE9FF),
)

private val TealLight = AccentColors(
    Color(0xFF00696F), Color(0xFFFFFFFF), Color(0xFF9CF0F8), Color(0xFF002022),
    Color(0xFF4A6365), Color(0xFFFFFFFF), Color(0xFFCCE8EA), Color(0xFF051F21),
    Color(0xFF4B607C), Color(0xFFFFFFFF), Color(0xFFD3E4FF), Color(0xFF041C35),
)
private val TealDark = AccentColors(
    Color(0xFF80D4DC), Color(0xFF00373B), Color(0xFF004F54), Color(0xFF9CF0F8),
    Color(0xFFB0CCCE), Color(0xFF1B3436), Color(0xFF324B4D), Color(0xFFCCE8EA),
    Color(0xFFB3C8E8), Color(0xFF1C314B), Color(0xFF334863), Color(0xFFD3E4FF),
)

private val GreenLight = AccentColors(
    Color(0xFF006D3B), Color(0xFFFFFFFF), Color(0xFF8AF8B6), Color(0xFF002110),
    Color(0xFF4E6353), Color(0xFFFFFFFF), Color(0xFFD1E8D3), Color(0xFF0B1F12),
    Color(0xFF3B6470), Color(0xFFFFFFFF), Color(0xFFBFE9F8), Color(0xFF001F28),
)
private val GreenDark = AccentColors(
    Color(0xFF6EDBA0), Color(0xFF00391C), Color(0xFF00522B), Color(0xFF8AF8B6),
    Color(0xFFB5CCB8), Color(0xFF203525), Color(0xFF364B3A), Color(0xFFD1E8D3),
    Color(0xFFA3CDDA), Color(0xFF053541), Color(0xFF234C58), Color(0xFFBFE9F8),
)

private val AmberLight = AccentColors(
    Color(0xFF7A5900), Color(0xFFFFFFFF), Color(0xFFFFDF9B), Color(0xFF261A00),
    Color(0xFF6B5D3F), Color(0xFFFFFFFF), Color(0xFFF3E0BC), Color(0xFF241A04),
    Color(0xFF4C6640), Color(0xFFFFFFFF), Color(0xFFCDEDBD), Color(0xFF092100),
)
private val AmberDark = AccentColors(
    Color(0xFFF0C048), Color(0xFF402D00), Color(0xFF5D4300), Color(0xFFFFDF9B),
    Color(0xFFD6C6A2), Color(0xFF3A2F15), Color(0xFF52452A), Color(0xFFF3E0BC),
    Color(0xFFB2D19F), Color(0xFF1F3617), Color(0xFF354E2B), Color(0xFFCDEDBD),
)

private val VioletLight = AccentColors(
    Color(0xFF6B4E9B), Color(0xFFFFFFFF), Color(0xFFEBDBFF), Color(0xFF26094F),
    Color(0xFF625B70), Color(0xFFFFFFFF), Color(0xFFE8DEF8), Color(0xFF1E192B),
    Color(0xFF7E5261), Color(0xFFFFFFFF), Color(0xFFFFD9E3), Color(0xFF31101C),
)
private val VioletDark = AccentColors(
    Color(0xFFD3BBFF), Color(0xFF3A1D6B), Color(0xFF523584), Color(0xFFEBDBFF),
    Color(0xFFCCC2DB), Color(0xFF332D41), Color(0xFF4A4458), Color(0xFFE8DEF8),
    Color(0xFFEFB7C9), Color(0xFF482534), Color(0xFF613B4A), Color(0xFFFFD9E3),
)

fun accentColors(color: ThemeColor, dark: Boolean, customSeed: Color): AccentColors =
    when (color) {
        ThemeColor.INDIGO -> if (dark) IndigoDark else IndigoLight
        ThemeColor.BLUE -> if (dark) BlueDark else BlueLight
        ThemeColor.TEAL -> if (dark) TealDark else TealLight
        ThemeColor.GREEN -> if (dark) GreenDark else GreenLight
        ThemeColor.AMBER -> if (dark) AmberDark else AmberLight
        ThemeColor.VIOLET -> if (dark) VioletDark else VioletLight
        ThemeColor.CUSTOM -> accentFromSeed(customSeed, dark)
    }

/** 由任意种子色推导一套浅/深强调色，供自定义主题色使用。 */
private fun accentFromSeed(seed: Color, dark: Boolean): AccentColors {
    val hsl = FloatArray(3)
    ColorUtils.colorToHSL(seed.toArgb(), hsl)
    val hue = hsl[0]
    val sat = hsl[1].coerceIn(0.2f, 0.9f)
    val alt = (hue + 60f) % 360f

    return if (dark) {
        AccentColors(
            primary = hslColor(hue, sat * 0.85f, 0.78f),
            onPrimary = hslColor(hue, sat, 0.18f),
            primaryContainer = hslColor(hue, sat, 0.34f),
            onPrimaryContainer = hslColor(hue, sat * 0.85f, 0.90f),
            secondary = hslColor(hue, sat * 0.35f, 0.78f),
            onSecondary = hslColor(hue, sat * 0.4f, 0.20f),
            secondaryContainer = hslColor(hue, sat * 0.35f, 0.32f),
            onSecondaryContainer = hslColor(hue, sat * 0.4f, 0.90f),
            tertiary = hslColor(alt, sat * 0.5f, 0.80f),
            onTertiary = hslColor(alt, sat * 0.5f, 0.20f),
            tertiaryContainer = hslColor(alt, sat * 0.5f, 0.34f),
            onTertiaryContainer = hslColor(alt, sat * 0.5f, 0.90f),
        )
    } else {
        AccentColors(
            primary = hslColor(hue, sat, 0.40f),
            onPrimary = Color(0xFFFFFFFF),
            primaryContainer = hslColor(hue, sat * 0.9f, 0.90f),
            onPrimaryContainer = hslColor(hue, sat, 0.16f),
            secondary = hslColor(hue, sat * 0.35f, 0.42f),
            onSecondary = Color(0xFFFFFFFF),
            secondaryContainer = hslColor(hue, sat * 0.4f, 0.90f),
            onSecondaryContainer = hslColor(hue, sat * 0.5f, 0.16f),
            tertiary = hslColor(alt, sat * 0.5f, 0.45f),
            onTertiary = Color(0xFFFFFFFF),
            tertiaryContainer = hslColor(alt, sat * 0.5f, 0.90f),
            onTertiaryContainer = hslColor(alt, sat * 0.6f, 0.16f),
        )
    }
}

private fun hslColor(hue: Float, saturation: Float, lightness: Float): Color =
    Color(ColorUtils.HSLToColor(floatArrayOf(hue, saturation.coerceIn(0f, 1f), lightness.coerceIn(0f, 1f))))

fun ThemeColor.displayName(): String = when (this) {
    ThemeColor.INDIGO -> "靛蓝"
    ThemeColor.BLUE -> "蔚蓝"
    ThemeColor.TEAL -> "青碧"
    ThemeColor.GREEN -> "松绿"
    ThemeColor.AMBER -> "琥珀"
    ThemeColor.VIOLET -> "紫罗兰"
    ThemeColor.CUSTOM -> "自定义"
}
