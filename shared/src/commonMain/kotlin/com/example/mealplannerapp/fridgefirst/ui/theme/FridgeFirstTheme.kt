package com.example.mealplannerapp.fridgefirst.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

// "Fresh produce" palette: leafy green primary, carrot-orange tertiary, warm neutral surfaces.
// Defined separately from the starter's ui/theme (left untouched).

private val LightColors = lightColorScheme(
    primary = Color(0xFF386A20),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFB8F397),
    onPrimaryContainer = Color(0xFF042100),
    secondary = Color(0xFF55624C),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFD9E7CB),
    onSecondaryContainer = Color(0xFF131F0D),
    tertiary = Color(0xFF8B5000),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFFFDCBE),
    onTertiaryContainer = Color(0xFF2C1600),
    error = Color(0xFFBA1A1A),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
    background = Color(0xFFF9FAF1),
    onBackground = Color(0xFF1A1C18),
    surface = Color(0xFFF9FAF1),
    onSurface = Color(0xFF1A1C18),
    surfaceVariant = Color(0xFFE0E4D6),
    onSurfaceVariant = Color(0xFF43483E),
    outline = Color(0xFF74796D),
    outlineVariant = Color(0xFFC4C8BB),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFF3F5EB),
    surfaceContainer = Color(0xFFEEEFE6),
    surfaceContainerHigh = Color(0xFFE8E9E0),
    surfaceContainerHighest = Color(0xFFE2E3DA),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF9DD67D),
    onPrimary = Color(0xFF0C3900),
    primaryContainer = Color(0xFF20510A),
    onPrimaryContainer = Color(0xFFB8F397),
    secondary = Color(0xFFBDCBB0),
    onSecondary = Color(0xFF283421),
    secondaryContainer = Color(0xFF3E4A36),
    onSecondaryContainer = Color(0xFFD9E7CB),
    tertiary = Color(0xFFFFB870),
    onTertiary = Color(0xFF4A2800),
    tertiaryContainer = Color(0xFF693C00),
    onTertiaryContainer = Color(0xFFFFDCBE),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    background = Color(0xFF121410),
    onBackground = Color(0xFFE3E3DC),
    surface = Color(0xFF121410),
    onSurface = Color(0xFFE3E3DC),
    surfaceVariant = Color(0xFF43483E),
    onSurfaceVariant = Color(0xFFC4C8BB),
    outline = Color(0xFF8E9286),
    outlineVariant = Color(0xFF43483E),
    surfaceContainerLowest = Color(0xFF0D0F0B),
    surfaceContainerLow = Color(0xFF1A1C18),
    surfaceContainer = Color(0xFF1E201C),
    surfaceContainerHigh = Color(0xFF282B26),
    surfaceContainerHighest = Color(0xFF333531),
)

private val FridgeFirstTypography = Typography().run {
    copy(
        titleLarge = titleLarge.copy(fontWeight = FontWeight.SemiBold),
        titleMedium = titleMedium.copy(fontWeight = FontWeight.SemiBold),
        titleSmall = titleSmall.copy(fontWeight = FontWeight.SemiBold),
    )
}

private val FridgeFirstShapes = Shapes(
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
)

@Composable
fun FridgeFirstTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = FridgeFirstTypography,
        shapes = FridgeFirstShapes,
        content = content
    )
}

/** Semantic colors for "have it" / "missing" so every screen uses the same ones. */
object StockColors {
    val haveIt: Color @Composable get() = MaterialTheme.colorScheme.primary
    val missing: Color @Composable get() = MaterialTheme.colorScheme.error
    val partial: Color @Composable get() = MaterialTheme.colorScheme.tertiary
}
