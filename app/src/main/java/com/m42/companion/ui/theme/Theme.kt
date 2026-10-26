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
import com.m42.companion.ui.components.Tone

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

/** Colours that are not part of the Material scheme: the card itself and the highlighted game terms. */
@Immutable
data class CardColors(
    val paper: Color,
    val ink: Color,
    val shaded: Color,
    private val tones: Map<Tone, Color>,
) {
    fun tone(tone: Tone): Color = tones.getValue(tone)
}

/**
 * Term colours. Skills follow the Eldritch Horror investigator sheet (Lore purple, Influence orange,
 * Observation green, Strength red, Will blue); tokens follow their printed colours where there is one.
 */
private val LightTones = mapOf(
    Tone.HEALTH to Color(0xFFC62828),
    Tone.SANITY to Color(0xFF0D6FB8),
    Tone.CLUE to Color(0xFF2E7D32),
    Tone.FOCUS to Color(0xFFB26A00),
    Tone.MONEY to Color(0xFF8A6D00),
    Tone.LORE to Color(0xFF7B3FA0),
    Tone.INFLUENCE to Color(0xFFC25400),
    Tone.OBSERVATION to Color(0xFF1B7A45),
    Tone.STRENGTH to Color(0xFFB3261E),
    Tone.WILL to Color(0xFF1F5FBF),
    Tone.SPEED to Color(0xFF00796B),
    Tone.SNEAK to Color(0xFF4E5B6E),
    Tone.LUCK to Color(0xFF9A7000),
    Tone.THREAT to Color(0xFFA0153E),
    Tone.DOOM to Color(0xFF8E1B1B),
    Tone.ELDRITCH to Color(0xFF5E2A84),
    Tone.OMEN to Color(0xFF424242),
    Tone.GATE to Color(0xFF00727F),
    Tone.MYSTERY to Color(0xFF33691E),
    Tone.RUMOR to Color(0xFF006C80),
    Tone.CONDITION to Color(0xFF8E2463),
    Tone.BLESSED to Color(0xFF9A7400),
    Tone.CURSED to Color(0xFF4A148C),
    Tone.STATUS to Color(0xFFA0522D),
    Tone.LOST to Color(0xFF283593),
    Tone.DANGER to Color(0xFF7F1D1D),
    Tone.POSSESSION to Color(0xFF8D5A2B),
    Tone.TRAIT to Color(0xFF6D5D4B),
)

private val DarkTones = mapOf(
    Tone.HEALTH to Color(0xFFFF6E6C),
    Tone.SANITY to Color(0xFF4FC3FF),
    Tone.CLUE to Color(0xFF6EE07A),
    Tone.FOCUS to Color(0xFFFFC046),
    Tone.MONEY to Color(0xFFF2CC4B),
    Tone.LORE to Color(0xFFCE9CF2),
    Tone.INFLUENCE to Color(0xFFFFA45C),
    Tone.OBSERVATION to Color(0xFF5FD68F),
    Tone.STRENGTH to Color(0xFFFF7A6E),
    Tone.WILL to Color(0xFF82B4FF),
    Tone.SPEED to Color(0xFF4DD0C4),
    Tone.SNEAK to Color(0xFFB0BDD0),
    Tone.LUCK to Color(0xFFE8C04A),
    Tone.THREAT to Color(0xFFFF6B8E),
    Tone.DOOM to Color(0xFFFF8A80),
    Tone.ELDRITCH to Color(0xFFC39BE8),
    Tone.OMEN to Color(0xFFD0D0D0),
    Tone.GATE to Color(0xFF5FD3DE),
    Tone.MYSTERY to Color(0xFFA5D66C),
    Tone.RUMOR to Color(0xFF4FC8E0),
    Tone.CONDITION to Color(0xFFF48FC4),
    Tone.BLESSED to Color(0xFFFFD54F),
    Tone.CURSED to Color(0xFFB39DDB),
    Tone.STATUS to Color(0xFFF0A070),
    Tone.LOST to Color(0xFF9FA8DA),
    Tone.DANGER to Color(0xFFFF8A80),
    Tone.POSSESSION to Color(0xFFE0A96D),
    Tone.TRAIT to Color(0xFFC7B59C),
)

private val LightCard = CardColors(
    paper = Color(0xFFFBF3E0),
    ink = Color(0xFF1F1A12),
    shaded = Color(0x33000000),
    tones = LightTones,
)

private val DarkCard = CardColors(
    paper = Color(0xFF26231D),
    ink = Color(0xFFEDE6D6),
    shaded = Color(0x66000000),
    tones = DarkTones,
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
