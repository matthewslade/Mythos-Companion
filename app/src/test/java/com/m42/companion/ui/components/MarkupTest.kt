package com.m42.companion.ui.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import com.m42.companion.TestData
import com.m42.companion.engine.TextStyle
import com.m42.companion.ui.theme.CardColors
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MarkupTest {
    /** Every tone gets a distinct fake colour so tests can tell which tone a span used. */
    private val colors = CardColors(Color.White, Color.Black, Color.Gray, Tone.entries.associateWith { Color(it.ordinal + 1, 0, 0) })

    /** An inline icon placeholder followed by the word joiner that keeps it on the term's line. */
    private val icon = " ${Char(0x2060)}"
    private val nbsp = Char(0xA0)

    private fun terms(text: String, style: TextStyle) = findTerms(text, style).map { text.substring(it.range) to it.kind }

    private fun plain(text: String, style: TextStyle = TextStyle.ARKHAM) =
        cardMarkup(text, style, colors).text.text.replace(icon, "").replace(nbsp, ' ')

    @Test
    fun `bold and italic markers are removed and styled`() {
        val marked = cardMarkup("Pass a **Will (-1) check** or you are *delayed*.", TextStyle.ARKHAM, colors).text
        val check = "${icon}Will${nbsp}(-1)${nbsp}check"
        assertEquals("Pass a $check or you are ${icon}delayed.", marked.text)
        val styles = marked.spanStyles.map { marked.text.substring(it.start, it.end) to it.item }
        assertTrue(styles.any { it.first == check && it.second.fontWeight == FontWeight.Bold })
        assertTrue(styles.any { it.first == "${icon}delayed" && it.second.fontStyle == FontStyle.Italic })
    }

    @Test
    fun `bold italic headings and lone stars`() {
        assertEquals("'Ware the walker!\n\nYou must fight.", plain("***'Ware the walker!***\n\nYou must fight."))
        assertEquals("5 * 2 stars", plain("5 * 2 stars"))
    }

    @Test
    fun `eldritch skill tests use the investigator sheet colours`() {
        assertEquals(
            listOf("[LORE - 1]" to TermKind.LORE, "[OBSERVATION]" to TermKind.OBSERVATION, "[WILL + 1]" to TermKind.WILL),
            terms("Test [LORE - 1], then improve [OBSERVATION] or test [WILL + 1].", TextStyle.ELDRITCH),
        )
    }

    @Test
    fun `arkham skill checks are matched only as checks`() {
        assertEquals(
            listOf("Luck (-1) [2] check" to TermKind.LUCK, "Fight (+0) check" to TermKind.FIGHT),
            terms("Make a Luck (-1) [2] check. You will need to pass a Fight (+0) check.", TextStyle.ARKHAM),
        )
        // Combat checks are Fight checks with weapon bonuses.
        assertEquals(listOf("Combat (-1) check" to TermKind.FIGHT), terms("Make a Combat (-1) check.", TextStyle.ARKHAM))
    }

    @Test
    fun `resources and money`() {
        assertEquals(
            listOf("2 Stamina" to TermKind.HEALTH, "1 Sanity" to TermKind.SANITY, "3 Clue Tokens" to TermKind.CLUE, "\$5" to TermKind.MONEY),
            terms("Lose 2 Stamina and 1 Sanity, gain 3 Clue Tokens and \$5.", TextStyle.ARKHAM),
        )
        assertEquals(
            listOf("2 Health" to TermKind.HEALTH, "1 Clue" to TermKind.CLUE, "1 Focus" to TermKind.FOCUS, "1 Ship Ticket" to TermKind.SHIP_TICKET),
            terms("Lose 2 Health, gain 1 Clue, gain 1 Focus and 1 Ship Ticket.", TextStyle.ELDRITCH),
        )
    }

    @Test
    fun `threats put the icon on the keyword`() {
        val text = "Retreat Doom? No: advance Doom by 1, place 1 Eldritch token and advance the Omen by 1; a Monster appears."
        val found = findTerms(text, TextStyle.ELDRITCH)
        val doom = found.first { text.substring(it.range) == "advance Doom by 1" }
        assertEquals(text.indexOf("Doom by 1"), doom.iconAt)
        assertEquals(
            listOf(TermKind.DOOM, TermKind.DOOM, TermKind.ELDRITCH_TOKEN, TermKind.OMEN, TermKind.MONSTER),
            found.map { it.kind },
        )
        assertEquals(
            listOf("gate" to TermKind.GATE, "monster appear" to TermKind.MONSTER, "doom track" to TermKind.DOOM, "terror level" to TermKind.TERROR),
            terms("A gate and a monster appear! Add 1 to the doom track and raise the terror level.", TextStyle.ARKHAM),
        )
        assertEquals(
            listOf("monster" to TermKind.MONSTER, "gate" to TermKind.GATE),
            terms("Return the monster to the cup and close the gate.", TextStyle.ARKHAM),
        )
        assertEquals(listOf("Advance Doom by 1" to TermKind.DOOM), terms("Advance Doom by 1.", TextStyle.ELDRITCH))
    }

    @Test
    fun `conditions statuses and blessings`() {
        assertEquals(
            listOf(
                "Dark Pact Condition" to TermKind.CONDITION, "MADNESS Condition" to TermKind.CONDITION,
                "Blessed Condition" to TermKind.BLESSED, "Delayed" to TermKind.DELAYED,
                "Lost in Time and Space Condition" to TermKind.LOST, "devoured" to TermKind.DEVOURED,
            ),
            terms(
                "Gain a Dark Pact Condition and a MADNESS Condition, or a Blessed Condition; become Delayed, " +
                    "gain a Lost in Time and Space Condition, or you are devoured.",
                TextStyle.ELDRITCH,
            ),
        )
        assertEquals(
            listOf("Dark Pact" to TermKind.CONDITION, "INJURY Conditions" to TermKind.CONDITION),
            terms("You may gain a Dark Pact and gain 2 INJURY Conditions.", TextStyle.ELDRITCH),
        )
        assertEquals(
            listOf("Blessed" to TermKind.BLESSED, "Cursed" to TermKind.CURSED, "arrested" to TermKind.ARRESTED, "Retainer card" to TermKind.CONDITION),
            terms("You are Blessed, or Cursed and arrested. Take a Retainer card.", TextStyle.ARKHAM),
        )
    }

    @Test
    fun `only the Deputy card is highlighted, not Deputy Dingby`() {
        assertEquals(emptyList<Pair<String, TermKind>>(), terms("Deputy Dingby strides past you.", TextStyle.ARKHAM).filter { "Deputy" in it.first })
        assertEquals(
            listOf("Deputy of Arkham card" to TermKind.CONDITION, "Deputy" to TermKind.CONDITION),
            terms("If you have the Deputy of Arkham card... you're arrested unless you're the Deputy.", TextStyle.ARKHAM)
                .filter { "Deputy" in it.first },
        )
    }

    @Test
    fun `traits are small capitals and possessions are highlighted separately`() {
        assertEquals(
            listOf("RELIC" to TermKind.TRAIT, "Unique Asset" to TermKind.UNIQUE, "INCANTATION" to TermKind.TRAIT, "Spell" to TermKind.SPELL),
            terms("Gain 1 RELIC Unique Asset and 1 INCANTATION Spell.", TextStyle.ELDRITCH),
        )
        assertEquals(
            listOf(
                "1 Unique Item" to TermKind.UNIQUE, "2 Spells" to TermKind.SPELL, "Ally card" to TermKind.ALLY,
                "1 Skill" to TermKind.SKILL_CARD, "1 Ally" to TermKind.ALLY,
            ),
            terms("Draw 1 Unique Item and 2 Spells, take his Ally card or draw 1 Skill. Gain 1 Ally.", TextStyle.ARKHAM),
        )
    }

    @Test
    fun `icons are inserted before terms and short terms do not wrap`() {
        val marked = cardMarkup("Pass a **Luck (-1) check** to gain 2 Clue Tokens.", TextStyle.ARKHAM, colors)
        assertEquals(setOf(TermKind.LUCK, TermKind.CLUE), marked.kinds)
        val text = marked.text
        val bold = text.spanStyles.first { it.item.fontWeight == FontWeight.Bold && it.item.color == Color.Unspecified }
        assertEquals("${icon}Luck${nbsp}(-1)${nbsp}check", text.text.substring(bold.start, bold.end))
        val clue = text.spanStyles.first { it.item.color == colors.tone(Tone.CLUE) }
        assertEquals("${icon}2${nbsp}Clue${nbsp}Tokens", text.text.substring(clue.start, clue.end))
    }

    @Test
    fun `long terms can still wrap`() {
        val text = cardMarkup("Gain a Lost in Time and Space Condition.", TextStyle.ELDRITCH, colors).text.text
        assertTrue("Lost in Time and Space Condition" in text)
    }

    @Test
    fun `every card in both games renders`() {
        TestData.eldritch.cards.flatMap { it.sections }.forEach { cardMarkup(it.text, TextStyle.ELDRITCH, colors) }
        (TestData.arkham.locationEncounters.map { it.text } + TestData.arkham.otherWorldEncounters.map { it.text })
            .forEach { cardMarkup(it, TextStyle.ARKHAM, colors) }
    }
}
