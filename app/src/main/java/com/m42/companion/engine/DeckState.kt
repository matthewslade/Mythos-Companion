package com.m42.companion.engine

import kotlinx.serialization.Serializable
import kotlin.random.Random

/** Which part of a card was resolved when it went to the discard pile. */
@Serializable
enum class Encountered {
    /** Still in its deck. */
    NONE,
    TOP,
    MIDDLE,
    BOTTOM,

    /** Discarded from the deck menu without choosing an encounter. */
    DISCARDED,

    /** Taken out of the game (expedition locations). Never shuffled back by a refill. */
    REMOVED,
}

@Serializable
data class DiscardedCard(val cardId: String, val deck: String, val encountered: Encountered)

/**
 * Every deck in play plus the shared discard pile. Index 0 of a deck is its top card and index 0 of the
 * discard pile is the most recent discard. Operations return a new state and never mutate.
 */
@Serializable
data class DeckState(
    val decks: Map<String, List<String>>,
    val discard: List<DiscardedCard> = emptyList(),
) {
    fun deck(deckId: String): List<String> = decks[deckId].orEmpty()

    fun hasDeck(deckId: String): Boolean = deckId in decks

    /** The deck ready to draw from: an empty deck is first refilled from its discards and shuffled. */
    fun prepare(deckId: String, random: Random): DeckState =
        if (decks[deckId]?.isEmpty() == true) refill(deckId).shuffled(deckId, random) else this

    /** "Shuffle deck": every discard belonging to the deck goes back in before shuffling. */
    fun shuffleFull(deckId: String, random: Random): DeckState =
        if (deckId in decks) refill(deckId).shuffled(deckId, random) else this

    fun discard(deckId: String, cardId: String, encountered: Encountered): DeckState {
        val deck = deck(deckId)
        if (cardId !in deck) return this
        return copy(
            decks = decks + (deckId to deck - cardId),
            discard = listOf(DiscardedCard(cardId, deckId, encountered)) + discard,
        )
    }

    /** "Return to deck": the card leaves the discard pile and its deck is reshuffled. */
    fun returnFromDiscard(cardId: String, random: Random): DeckState {
        val card = discard.firstOrNull { it.cardId == cardId } ?: return this
        return copy(
            decks = decks + (card.deck to deck(card.deck) + cardId),
            discard = discard - card,
        ).shuffled(card.deck, random)
    }

    /** Takes every matching card out of [deckId] and shuffles what is left. */
    fun remove(deckId: String, random: Random, predicate: (String) -> Boolean): DeckState {
        val (removed, kept) = deck(deckId).partition(predicate)
        return copy(
            decks = decks + (deckId to kept),
            discard = discard + removed.map { DiscardedCard(it, deckId, Encountered.REMOVED) },
        ).shuffled(deckId, random)
    }

    fun shuffled(deckId: String, random: Random): DeckState =
        if (deckId in decks) copy(decks = decks + (deckId to deck(deckId).shuffled(random))) else this

    private fun refill(deckId: String): DeckState {
        val (back, stay) = discard.partition { it.deck == deckId && it.encountered != Encountered.REMOVED }
        if (back.isEmpty()) return this
        return copy(decks = decks + (deckId to deck(deckId) + back.map { it.cardId }), discard = stay)
    }

    companion object {
        fun of(decks: Map<String, List<String>>, random: Random): DeckState =
            DeckState(decks.mapValues { (_, cards) -> cards.shuffled(random) })
    }
}
