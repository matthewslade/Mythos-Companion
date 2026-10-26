package com.m42.companion.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.sp
import com.m42.companion.R

/** The typeface the Eldritch Companion used for card headings. */
val Caslon = FontFamily(Font(R.font.caslon_antique))

private val LightColors = lightColorScheme(
    primary = Color(0xFF2E5E3E),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFC4E5CC),
    onPrimaryContainer = Color(0xFF0B2414),
    secondary = Color(0xFF5B4A2E),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE9DDC2),
    onSecondaryContainer = Color(0xFF2A1E08),
    background = Color(0xFFF4EFE3),
    onBackground = Color(0xFF1D1B16),
    surface = Color(0xFFF4EFE3),
    onSurface = Color(0xFF1D1B16),
    surfaceVariant = Color(0xFFE6E0D2),
    onSurfaceVariant = Color(0xFF4A463C),
    surfaceContainer = Color(0xFFEDE7DA),
    surfaceContainerHigh = Color(0xFFE7E1D3),
    surfaceContainerLow = Color(0xFFF1EBDF),
    outline = Color(0xFF7B7568),
    outlineVariant = Color(0xFFCBC5B7),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF9DD3AA),
    onPrimary = Color(0xFF0B2E18),
    primaryContainer = Color(0xFF1F4A2C),
    onPrimaryContainer = Color(0xFFC4E5CC),
    secondary = Color(0xFFD7C49F),
    onSecondary = Color(0xFF3A2D12),
    secondaryContainer = Color(0xFF524428),
    onSecondaryContainer = Color(0xFFF2E1BE),
    background = Color(0xFF121410),
    onBackground = Color(0xFFE4E2DA),
    surface = Color(0xFF121410),
    onSurface = Color(0xFFE4E2DA),
    surfaceVariant = Color(0xFF3A3D35),
    onSurfaceVariant = Color(0xFFC3C6BA),
    surfaceContainer = Color(0xFF1E211C),
    surfaceContainerHigh = Color(0xFF282B25),
    surfaceContainerLow = Color(0xFF1A1C18),
    outline = Color(0xFF8D9185),
    outlineVariant = Color(0xFF43473F),
)

/** Colours that are not part of the Material scheme: the card itself and the Arkham resource highlights. */
@Immutable
data class CardColors(
    val paper: Color,
    val ink: Color,
    val shaded: Color,
    val stamina: Color,
    val sanity: Color,
    val clue: Color,
)

private val LightCard = CardColors(
    paper = Color(0xFFFBF3E0),
    ink = Color(0xFF1F1A12),
    shaded = Color(0x33000000),
    stamina = Color(0xFFC62828),
    sanity = Color(0xFF0D6FB8),
    clue = Color(0xFF2E7D32),
)

private val DarkCard = CardColors(
    paper = Color(0xFF26231D),
    ink = Color(0xFFEDE6D6),
    shaded = Color(0x66000000),
    stamina = Color(0xFFFF6E6C),
    sanity = Color(0xFF4FC3FF),
    clue = Color(0xFF6EE07A),
)

val LocalCardColors = staticCompositionLocalOf { LightCard }

private val base = Typography()
private val AppTypography = base.copy(
    displaySmall = base.displaySmall.copy(fontFamily = Caslon),
    headlineLarge = base.headlineLarge.copy(fontFamily = Caslon),
    headlineMedium = base.headlineMedium.copy(fontFamily = Caslon),
    headlineSmall = base.headlineSmall.copy(fontFamily = Caslon),
    titleLarge = base.titleLarge.copy(fontFamily = Caslon, fontSize = 24.sp),
)

val CardBodyStyle = TextStyle(fontFamily = FontFamily.Serif, fontSize = 17.sp, lineHeight = 25.sp)

@Composable
fun CompanionTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalCardColors provides if (darkTheme) DarkCard else LightCard) {
        MaterialTheme(
            colorScheme = if (darkTheme) DarkColors else LightColors,
            typography = AppTypography,
            content = content,
        )
    }
}
