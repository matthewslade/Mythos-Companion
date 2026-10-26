package com.m42.companion.engine

import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class DeckStateTest {
    private val random = Random(42)
    private val start = DeckState(mapOf("a" to listOf("a1", "a2", "a3"), "b" to listOf("b1")))

    @Test
    fun `discarding moves the card to the top of the discard pile`() {
        val s = start.discard("a", "a2", Encountered.MIDDLE).discard("b", "b1", Encountered.DISCARDED)
        assertEquals(listOf("a1", "a3"), s.deck("a"))
        assertEquals(
            listOf(DiscardedCard("b1", "b", Encountered.DISCARDED), DiscardedCard("a2", "a", Encountered.MIDDLE)),
            s.discard,
        )
    }

    @Test
    fun `an empty deck refills from its own discards when prepared`() {
        val s = start.discard("b", "b1", Encountered.TOP).discard("a", "a1", Encountered.TOP)
        val prepared = s.prepare("b", random)
        assertEquals(listOf("b1"), prepared.deck("b"))
        assertEquals(listOf(DiscardedCard("a1", "a", Encountered.TOP)), prepared.discard)
    }

    @Test
    fun `a non-empty deck is left alone when prepared`() {
        val s = start.discard("a", "a1", Encountered.TOP)
        assertSame(s, s.prepare("a", random))
    }

    @Test
    fun `shuffle returns every discard of that deck`() {
        val s = start.discard("a", "a1", Encountered.TOP).discard("a", "a2", Encountered.BOTTOM)
            .discard("b", "b1", Encountered.TOP)
        val shuffled = s.shuffleFull("a", random)
        assertEquals(setOf("a1", "a2", "a3"), shuffled.deck("a").toSet())
        assertEquals(listOf("b1"), shuffled.discard.map { it.cardId })
    }

    @Test
    fun `returning a card puts it back in its deck`() {
        val s = start.discard("a", "a1", Encountered.TOP).returnFromDiscard("a1", random)
        assertEquals(setOf("a1", "a2", "a3"), s.deck("a").toSet())
        assertTrue(s.discard.isEmpty())
    }

    @Test
    fun `removed cards stay out of the deck when it refills`() {
        val s = start.remove("a", random) { it == "a1" }
            .discard("a", "a2", Encountered.TOP).discard("a", "a3", Encountered.TOP)
        val refilled = s.prepare("a", random)
        assertEquals(setOf("a2", "a3"), refilled.deck("a").toSet())
        assertEquals(listOf(DiscardedCard("a1", "a", Encountered.REMOVED)), refilled.discard)
        // A removed card can still be put back by hand.
        assertEquals(setOf("a1", "a2", "a3"), refilled.returnFromDiscard("a1", random).deck("a").toSet())
    }
}
