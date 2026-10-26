package com.m42.companion.arkham

import com.m42.companion.TestData
import com.m42.companion.engine.DISCARD_PILE
import com.m42.companion.engine.Encountered
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class ArkhamGameTest {
    private val data = TestData.arkham
    private val game = ArkhamGame(data)
    private val base = ArkhamConfig(emptySet())
    private val everything = ArkhamConfig(data.expansions.map { it.code }.toSet())

    @Test
    fun `base game only has arkham locations and base cards`() {
        val session = game.newSession(base, Random(1))
        assertEquals(27, game.locations(base).size)
        assertEquals(8, game.worlds(base).size)
        val deck = session.state.deck(ArkhamDecks.location("Library"))
        assertEquals(7, deck.size)
        val ids = data.locationEncounters.associateBy { it.id }
        assertTrue(deck.all { ids.getValue(it).expansions == listOf("AH") })
    }

    @Test
    fun `board expansions add their locations and worlds`() {
        val config = ArkhamConfig(setOf("DH", "KH"))
        val boards = game.locations(config).map { it.board }.toSet()
        assertEquals(setOf("AH", "DH", "KH"), boards)
        assertTrue(game.worlds(config).any { it.name == "Lost Carcosa" })
        assertTrue(game.worlds(config).any { it.name == "Unknown Kadath" })
        assertEquals(
            listOf("Arkham" to "Northside", "Arkham" to "Downtown"),
            game.locationGroups(config).take(2).map { it.board to it.title },
        )
        assertEquals(
            listOf("Village Commons", "Blasted Heath", "Backwoods Country"),
            game.locationGroups(config).drop(9).take(3).map { it.title },
        )
        assertTrue(game.locationGroups(config).all { it.locations.size == 3 || it.title == "French Hill" })
    }

    @Test
    fun `miskatonic horror cards need the expansion they extend`() {
        val mhOnly = game.newSession(ArkhamConfig(setOf("MH")), Random(1))
        val ids = data.locationEncounters.associateBy { it.id }
        assertFalse(mhOnly.state.decks.values.flatten().any { "MH" in ids[it]?.expansions.orEmpty() })
        val withKingsport = game.newSession(ArkhamConfig(setOf("MH", "KH")), Random(1))
        assertEquals(22, withKingsport.state.deck(ArkhamDecks.location("Wireless Station")).size)
    }

    @Test
    fun `other world galleries mix named and matching gate colour encounters`() {
        val session = game.newSession(everything, Random(3))
        val world = data.otherWorlds.first { it.name == "Abyss" }
        val (_, gallery) = game.open(session, ArkhamDecks.world("Abyss"), Random(3))
        val faces = gallery!!.faces
        val byId = data.otherWorldEncounters.associateBy { it.id }
        assertTrue(faces.all { byId.getValue(it.cardId).world == "Abyss" || byId.getValue(it.cardId).colour in world.colours })
        assertTrue(faces.any { it.deck == ArkhamDecks.OTHER } && faces.any { it.deck == ArkhamDecks.world("Abyss") })
        assertTrue(faces.all { it.sections.single().header == "Abyss" })
    }

    @Test
    fun `reopening a world shows the same cards until something is drawn`() {
        val session = game.newSession(everything, Random(4))
        val (s1, first) = game.open(session, ArkhamDecks.world("R'lyeh"), Random(1))
        val (s2, again) = game.open(s1, ArkhamDecks.world("R'lyeh"), Random(2))
        assertEquals(first!!.faces.map { it.cardId }, again!!.faces.map { it.cardId })
        val drawn = game.resolve(s2, again.faces.first(), Encountered.TOP, Random(3))
        val (_, after) = game.open(drawn, ArkhamDecks.world("R'lyeh"), Random(4))
        assertFalse(again.faces.first().cardId in after!!.faces.map { it.cardId })
    }

    @Test
    fun `another dimension only draws other encounters`() {
        val session = game.newSession(base, Random(5))
        val (_, gallery) = game.open(session, ArkhamDecks.world(ANOTHER_DIMENSION), Random(5))
        assertEquals(48, gallery!!.faces.size)
        assertTrue(gallery.faces.all { it.deck == ArkhamDecks.OTHER })
    }

    @Test
    fun `legacy weighting is preserved`() {
        val abyss = data.otherWorlds.first { it.name == "Abyss" }
        val dreamlands = data.otherWorlds.first { it.name == DREAMLANDS }
        // Base game: 12 Abyss cards against 48 "Other" cards, halved for every world but the Dreamlands.
        assertEquals(12f / 24f, game.namedRatio(base, abyss))
        assertEquals(24f / 48f, game.namedRatio(base, dreamlands))
    }

    @Test
    fun `the stars are right reshuffles the whole gate deck`() {
        var session = game.newSession(base, Random(9))
        val (s, g) = game.open(session, ArkhamDecks.world("Yuggoth"), Random(9))
        session = game.resolve(s, g!!.faces.first(), Encountered.TOP, Random(9))
        assertEquals(1, session.state.discard.size)
        // Find a draw seed where the 1-in-48 reshuffle triggers.
        val seed = (0L..5000L).first { Random(it * 31 + "Yuggoth".hashCode()).nextInt(48) == 0 }
        val (reshuffled, gallery) = game.open(session.copy(drawSeed = seed), ArkhamDecks.world("Yuggoth"), Random(0))
        assertTrue(gallery!!.banner!!.startsWith("The Stars Are Right"))
        assertTrue(reshuffled.state.discard.isEmpty())
        // Reopening does not reshuffle again.
        assertEquals(null, game.open(reshuffled, ArkhamDecks.world("Yuggoth"), Random(0)).second!!.banner)
    }

    @Test
    fun `discard pile shows where each card came from`() {
        var session = game.newSession(base, Random(2))
        val (s, g) = game.open(session, ArkhamDecks.location("Woods"), Random(2))
        session = game.resolve(s, g!!.faces.first(), Encountered.TOP, Random(2))
        val (_, pile) = game.open(session, DISCARD_PILE, Random(2))
        assertEquals("Discard – Woods", pile!!.faces.single().pageTitle)
        val back = game.returnToDeck(session, pile.faces.single(), Random(2))
        assertEquals(7, back.state.deck(ArkhamDecks.location("Woods")).size)
    }
}
