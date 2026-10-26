package com.m42.companion.data

import com.m42.companion.TestData
import com.m42.companion.eldritch.EldritchDecks
import com.m42.companion.eldritch.EldritchExpansion
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CardDataValidationTest {
    private val eldritch = TestData.eldritch
    private val arkham = TestData.arkham

    private val eldritchDecks = setOf(
        EldritchDecks.AMERICAS, EldritchDecks.EUROPE, EldritchDecks.ASIA, EldritchDecks.GENERAL,
        EldritchDecks.ANTARCTICA_WEST, EldritchDecks.ANTARCTICA_EAST, EldritchDecks.ANTARCTICA_RESEARCH,
        EldritchDecks.AFRICA, EldritchDecks.EGYPT, EldritchDecks.DREAMLANDS, EldritchDecks.GATE,
        EldritchDecks.EXPEDITION, EldritchDecks.MYSTIC_RUINS, EldritchDecks.DREAM_QUEST, EldritchDecks.RESEARCH,
        EldritchDecks.SPECIAL_1, EldritchDecks.SPECIAL_2, EldritchDecks.SPECIAL_3, EldritchDecks.DISASTER,
        EldritchDecks.DEVASTATION,
    )

    @Test
    fun `eldritch cards are complete and consistent`() {
        assertEquals(1086, eldritch.cards.size)
        assertEquals("card ids must be unique", eldritch.cards.size, eldritch.cards.map { it.id }.toSet().size)
        val expansionIds = EldritchExpansion.entries.map { it.id }.toSet()
        val ancientOneIds = EldritchExpansion.allAncientOnes.map { it.id }.toSet()
        for (card in eldritch.cards) {
            assertTrue(card.id, card.deck in eldritchDecks)
            assertTrue(card.id, card.expansion in expansionIds)
            assertEquals(card.id, 3, card.sections.size)
            assertTrue(card.id, card.sections.take(2).all { it.text.isNotBlank() })
            assertBalancedMarkup(card.id, card.sections.joinToString(" ") { it.text })
            if (EldritchDecks.isAncientOneDeck(card.deck)) {
                assertTrue(card.id, card.ancientOne in ancientOneIds)
            } else {
                assertEquals(card.id, null, card.ancientOne)
            }
        }
    }

    @Test
    fun `every ancient one has research cards`() {
        for (ao in EldritchExpansion.allAncientOnes) {
            assertTrue(ao.id, eldritch.cards.any { it.deck == EldritchDecks.RESEARCH && it.ancientOne == ao.id })
        }
    }

    @Test
    fun `eldritch skill tests use one notation`() {
        val tests = eldritch.cards.flatMap { c -> c.sections.flatMap { Regex("""\[[^\]]*]""").findAll(it.text).map { m -> m.value } } }
        val valid = Regex("""\[(LORE|INFLUENCE|OBSERVATION|STRENGTH|WILL)( [+-] \d)?]|\[RECKONING]""")
        assertEquals(emptyList<String>(), tests.filterNot { valid.matches(it) }.distinct())
    }

    @Test
    fun `arkham encounters reference known locations worlds and expansions`() {
        val codes = arkham.expansions.map { it.code }.toSet() + "AH"
        val locations = arkham.locations.map { it.name }.toSet()
        val worlds = arkham.otherWorlds.map { it.name }.toSet()
        val ids = arkham.locationEncounters.map { it.id } + arkham.otherWorldEncounters.map { it.id }
        assertEquals("card ids must be unique", ids.size, ids.toSet().size)
        for (e in arkham.locationEncounters) {
            assertTrue(e.id, e.location in locations)
            assertTrue(e.id, e.expansions.isNotEmpty() && codes.containsAll(e.expansions))
            assertTrue(e.id, e.text.isNotBlank())
            assertBalancedMarkup(e.id, e.text)
            // Miskatonic Horror cards always name the expansion they extend.
            if ("MH" in e.expansions) assertEquals(e.id, 2, e.expansions.size)
        }
        for (e in arkham.otherWorldEncounters) {
            assertTrue(e.id, e.world == null || e.world in worlds)
            assertTrue(e.id, (e.world == null) == (e.colour != null))
            assertTrue(e.id, codes.containsAll(e.expansions))
            assertBalancedMarkup(e.id, e.text)
        }
        assertTrue(arkham.locations.all { loc -> arkham.locationEncounters.any { it.location == loc.name } })
    }

    @Test
    fun `arkham text has no leftover html or broken markup`() {
        val texts = arkham.locationEncounters.map { it.id to it.text } + arkham.otherWorldEncounters.map { it.id to it.text }
        for ((id, text) in texts) {
            assertTrue("$id contains html: $text", '<' !in text && '>' !in text)
            assertTrue("$id contains '[[': $text", "[[" !in text)
            assertTrue("$id has a field separator: $text", '%' !in text)
        }
    }

    private fun assertBalancedMarkup(id: String, text: String) {
        assertEquals("$id has unbalanced bold markup", 0, Regex("""\*\*""").findAll(text).count() % 2)
    }
}
