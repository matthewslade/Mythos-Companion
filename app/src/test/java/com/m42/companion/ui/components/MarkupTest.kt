package com.m42.companion.ui.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import com.m42.companion.engine.TextStyle
import com.m42.companion.ui.theme.CardColors
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MarkupTest {
    private val colors = CardColors(Color.White, Color.Black, Color.Gray, Color.Red, Color.Blue, Color.Green)

    private fun styledRanges(text: String, style: TextStyle = TextStyle.ARKHAM) =
        cardMarkup(text, style, colors).let { s -> s.text to s.spanStyles.map { s.text.substring(it.start, it.end) to it.item } }

    @Test
    fun `bold and italic markers are removed and styled`() {
        val (text, spans) = styledRanges("Pass a **Will (-1) check** or you are *delayed*.")
        assertEquals("Pass a Will (-1) check or you are delayed.", text)
        assertTrue(spans.any { it.first == "Will (-1) check" && it.second.fontWeight == FontWeight.Bold })
        assertTrue(spans.any { it.first == "delayed" && it.second.fontStyle == FontStyle.Italic })
    }

    @Test
    fun `bold italic headings render both styles`() {
        val (text, spans) = styledRanges("***'Ware the walker!***\n\nYou must fight.")
        assertEquals("'Ware the walker!\n\nYou must fight.", text)
        assertTrue(spans.any { it.first == "'Ware the walker!" && it.second.fontWeight == FontWeight.Bold })
        assertTrue(spans.any { it.first == "'Ware the walker!" && it.second.fontStyle == FontStyle.Italic })
    }

    @Test
    fun `a lone star is kept as text`() {
        assertEquals("5 * 2 stars", styledRanges("5 * 2 stars").first)
    }

    @Test
    fun `arkham resources are coloured and eldritch tests are bold`() {
        val (_, spans) = styledRanges("Lose 2 Stamina and 1 Sanity, gain 3 Clue Tokens.")
        assertEquals(Color.Red, spans.first { it.first == "2 Stamina" }.second.color)
        assertEquals(Color.Blue, spans.first { it.first == "1 Sanity" }.second.color)
        assertEquals(Color.Green, spans.first { it.first == "3 Clue Tokens" }.second.color)

        val (_, eldritch) = styledRanges("Test [LORE - 1]. Lose 1 Sanity.", TextStyle.ELDRITCH)
        assertTrue(eldritch.any { it.first == "[LORE - 1]" && it.second.fontWeight == FontWeight.Bold })
        assertTrue(eldritch.none { it.first.contains("Sanity") })
    }
}
