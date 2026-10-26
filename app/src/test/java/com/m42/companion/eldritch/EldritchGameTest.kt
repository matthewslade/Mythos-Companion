package com.m42.companion.eldritch

import com.m42.companion.TestData
import com.m42.companion.engine.DISCARD_PILE
import com.m42.companion.engine.Encountered
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class EldritchGameTest {
    private val game = EldritchGame(TestData.eldritch)
    private val random = Random(7)

    private fun config(vararg expansions: EldritchExpansion, ao: String, options: Set<EldritchOption> = emptySet()) =
        EldritchConfig(expansions.map { it.id }.toSet(), options.map { it.id }.toSet(), ao)

    @Test
    fun `setup only offers options whose expansion is selected`() {
        var setup = EldritchSetup()
        assertFalse(setup.isOptionEnabled(EldritchOption.ANTARCTICA))
        assertEquals(setup, setup.toggle(EldritchOption.ANTARCTICA))
        setup = setup.toggle(EldritchExpansion.MOUNTAINS_OF_MADNESS).toggle(EldritchOption.ANTARCTICA)
        assertTrue(EldritchOption.ANTARCTICA in setup.options)
        setup = setup.toggle(EldritchExpansion.MOUNTAINS_OF_MADNESS)
        assertTrue(setup.options.isEmpty())
    }

    @Test
    fun `only one prelude card can be chosen`() {
        val setup = EldritchSetup()
            .toggle(EldritchExpansion.STRANGE_REMNANTS).toggle(EldritchExpansion.UNDER_THE_PYRAMIDS)
            .toggle(EldritchOption.COSMIC_ALIGNMENT).toggle(EldritchOption.LITANY_OF_SECRETS)
        assertEquals(setOf(EldritchOption.LITANY_OF_SECRETS), setup.options)
    }

    @Test
    fun `ancient one list follows expansions and keeps a still-valid choice`() {
        val yig = EldritchExpansion.ancientOne("yig")
        var setup = EldritchSetup().toggle(EldritchExpansion.FORSAKEN_LORE).copy(ancientOne = yig)
        assertEquals(listOf("azathoth", "yog-sothoth", "shub-niggurath", "cthulhu", "yig"), setup.availableAncientOnes.map { it.id })
        setup = setup.toggle(EldritchExpansion.SIGNS_OF_CARCOSA)
        assertEquals(yig, setup.ancientOne)
        setup = setup.toggle(EldritchExpansion.FORSAKEN_LORE)
        assertNull(setup.ancientOne)
    }

    @Test
    fun `random ancient one comes from the selected expansions and no expansion means no game`() {
        assertNull(EldritchSetup(expansions = emptySet()).toConfig(random))
        val config = EldritchSetup(expansions = setOf(EldritchExpansion.SIGNS_OF_CARCOSA)).toConfig(random)!!
        assertEquals("hastur", config.ancientOne)
    }

    @Test
    fun `research and special decks only hold the chosen ancient one's cards`() {
        val session = game.newSession(config(EldritchExpansion.BASE, EldritchExpansion.FORSAKEN_LORE, ao = "yog-sothoth"), random)
        val research = session.state.deck(EldritchDecks.RESEARCH).map(game::card)
        assertEquals(24, research.size)
        assertTrue(research.all { it.ancientOne == "yog-sothoth" })
        assertEquals(8, session.state.deck(EldritchDecks.SPECIAL_1).size) // 6 base + 2 Forsaken Lore
        assertEquals("The Key and the Gate", game.specialDeckName(session, EldritchDecks.SPECIAL_1))
        assertEquals("Void Between Worlds", game.specialDeckName(session, EldritchDecks.SPECIAL_2))
        assertNull(game.specialDeckName(session, EldritchDecks.SPECIAL_3))
    }

    @Test
    fun `expansion decks merge into the base decks`() {
        val session = game.newSession(config(*EldritchExpansion.entries.toTypedArray(), ao = "azathoth"), random)
        assertEquals(48, session.state.deck(EldritchDecks.AMERICAS).size)
        assertEquals(72, session.state.deck(EldritchDecks.GATE).size)
        assertEquals(40, session.state.deck(EldritchDecks.MYSTIC_RUINS).size)
        assertTrue(session.state.hasDeck(EldritchDecks.DISASTER))
    }

    @Test
    fun `board shows side boards for their options or ancient ones`() {
        assertTrue(EldritchBoardRules.showAntarctica(config(EldritchExpansion.MOUNTAINS_OF_MADNESS, ao = "rise-of-the-elder-things")))
        assertFalse(EldritchBoardRules.showAntarctica(config(EldritchExpansion.MOUNTAINS_OF_MADNESS, ao = "ithaqua")))
        assertTrue(EldritchBoardRules.showEgypt(config(EldritchExpansion.UNDER_THE_PYRAMIDS, ao = "abhoth", options = setOf(EldritchOption.EGYPT))))
        assertTrue(EldritchBoardRules.showDreamlands(config(EldritchExpansion.THE_DREAMLANDS, ao = "hypnos")))
        assertTrue(EldritchBoardRules.showMysticRuins(config(EldritchExpansion.MASKS_OF_NYARLATHOTEP, ao = "antediluvium")))
        assertFalse(EldritchBoardRules.showMysticRuins(config(EldritchExpansion.MASKS_OF_NYARLATHOTEP, ao = "nyarlathotep")))
    }

    @Test
    fun `litany of secrets shows two active expeditions`() {
        val base = config(EldritchExpansion.BASE, EldritchExpansion.UNDER_THE_PYRAMIDS, ao = "cthulhu")
        val litany = base.copy(options = setOf(EldritchOption.LITANY_OF_SECRETS.id))
        val session = game.newSession(litany, random)
        val deck = session.state.deck(EldritchDecks.EXPEDITION).map { game.card(it).title }
        assertEquals("${deck[0]}, ${deck[1]}", game.currentLocation(session, EldritchDecks.EXPEDITION))
        assertEquals(deck[0], game.currentLocation(session.copy(config = base), EldritchDecks.EXPEDITION))
    }

    @Test
    fun `removing an expedition location discards all of its cards`() {
        val session = game.newSession(config(EldritchExpansion.BASE, ao = "cthulhu"), random)
        val location = game.expeditionLocations(session).first()
        val after = game.removeExpedition(session, location, random)
        assertFalse(location in game.expeditionLocations(after))
        assertTrue(after.state.discard.all { it.encountered == Encountered.REMOVED && game.card(it.cardId).title == location })
        assertEquals(3, after.state.discard.size)
    }

    @Test
    fun `resolving a section discards the card with that section marked`() {
        val session = game.newSession(config(EldritchExpansion.BASE, ao = "cthulhu"), random)
        val (opened, gallery) = game.open(session, EldritchDecks.GATE, random)
        val top = gallery!!.faces.first()
        // Gates open with a lead-in; only Pass and Fail resolve them.
        assertEquals(setOf(Encountered.MIDDLE, Encountered.BOTTOM), top.selectable)
        assertEquals(opened, game.resolve(opened, top, Encountered.TOP))
        val after = game.resolve(opened, top, Encountered.BOTTOM)
        assertEquals(Encountered.BOTTOM, after.state.discard.single().encountered)
        val (_, pile) = game.open(after, DISCARD_PILE, random)
        assertEquals("Discard – Gate", pile!!.faces.single().pageTitle)
        assertTrue(pile.faces.single().selectable.isEmpty())
    }

    @Test
    fun `an exhausted deck refills from the discard pile when opened`() {
        var session = game.newSession(config(EldritchExpansion.BASE, ao = "cthulhu"), random)
        repeat(8) {
            val (s, g) = game.open(session, EldritchDecks.AMERICAS, random)
            session = game.resolve(s, g!!.faces.first(), Encountered.TOP)
        }
        assertTrue(session.state.deck(EldritchDecks.AMERICAS).isEmpty())
        val (refilled, gallery) = game.open(session, EldritchDecks.AMERICAS, random)
        assertEquals(8, gallery!!.faces.size)
        assertTrue(refilled.state.discard.isEmpty())
    }
}
