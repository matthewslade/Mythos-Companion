package com.m42.companion.eldritch

import com.m42.companion.data.EldritchCard
import com.m42.companion.data.EldritchData
import com.m42.companion.engine.CardFace
import com.m42.companion.engine.DISCARD_PILE
import com.m42.companion.engine.DeckState
import com.m42.companion.engine.Encountered
import com.m42.companion.engine.Gallery
import com.m42.companion.engine.SectionPositions
import com.m42.companion.engine.TextStyle
import kotlinx.serialization.Serializable
import kotlin.random.Random

/** Everything needed to resume a game; this is what gets saved. */
@Serializable
data class EldritchSession(val config: EldritchConfig, val state: DeckState)

/** Game logic over a session. Every mutation returns a new session. */
class EldritchGame(private val data: EldritchData) {
    private val cards: Map<String, EldritchCard> = data.cards.associateBy { it.id }

    fun card(id: String) = cards.getValue(id)

    /** True when every card and the Ancient One the session refers to still exist in the bundled data. */
    fun isValid(session: EldritchSession): Boolean =
        EldritchExpansion.allAncientOnes.any { it.id == session.config.ancientOne } &&
            session.state.decks.values.all { deck -> deck.all(cards::containsKey) } &&
            session.state.discard.all { it.cardId in cards }

    fun newSession(config: EldritchConfig, random: Random) =
        EldritchSession(config, EldritchDecks.build(data, config, random))

    fun title(session: EldritchSession) = EldritchExpansion.ancientOne(session.config.ancientOne).name

    fun hasDeck(session: EldritchSession, deckId: String) = session.state.hasDeck(deckId)

    /** Name of a special deck, taken from its cards, or null when the Ancient One has no such deck. */
    fun specialDeckName(session: EldritchSession, deckId: String): String? {
        if (!session.state.hasDeck(deckId)) return null
        return data.cards.firstOrNull { it.deck == deckId && it.ancientOne == session.config.ancientOne }?.title
    }

    /** The three places a location deck covers, e.g. "Arkham · San Francisco · Buenos Aires". */
    fun deckHeaders(deckId: String): String? =
        data.cards.firstOrNull { it.deck == deckId }?.headerLine()

    fun deckName(session: EldritchSession, deckId: String): String =
        specialDeckName(session, deckId) ?: EldritchDecks.name(deckId)

    /** Header of the top card, as shown on the Expedition / Mystic Ruins / Dream-Quest buttons. */
    fun currentLocation(session: EldritchSession, deckId: String): String {
        val deck = session.state.deck(deckId)
        if (deck.isEmpty()) return "EMPTY"
        val first = card(deck[0]).title.orEmpty()
        // Litany of Secrets keeps two expeditions active.
        if (deckId == EldritchDecks.EXPEDITION && session.config.has(EldritchOption.LITANY_OF_SECRETS) && deck.size > 1) {
            return "$first, ${card(deck[1]).title.orEmpty()}"
        }
        return first
    }

    fun expeditionLocations(session: EldritchSession): List<String> =
        session.state.deck(EldritchDecks.EXPEDITION).map { card(it).title.orEmpty() }.toSortedSet().toList()

    fun removeExpedition(session: EldritchSession, location: String, random: Random) = session.copy(
        state = session.state.remove(EldritchDecks.EXPEDITION, random) { card(it).title == location },
    )

    /** Opening a deck refills it from the discard pile first if it has run out. */
    fun open(session: EldritchSession, deckId: String, random: Random): Pair<EldritchSession, Gallery?> {
        if (deckId == DISCARD_PILE) {
            val pile = session.state.discard
            if (pile.isEmpty()) return session to null
            val faces = pile.map { face(session, card(it.cardId), it.encountered, discardView = true) }
            return session to Gallery(deckId, faces, isDiscardPile = true)
        }
        if (!session.state.hasDeck(deckId)) return session to null
        val prepared = session.copy(state = session.state.prepare(deckId, random))
        val deck = prepared.state.deck(deckId)
        if (deck.isEmpty()) return prepared to null
        val faces = deck.map { face(prepared, card(it), Encountered.NONE, discardView = false) }
        return prepared to Gallery(deckId, faces, isDiscardPile = false)
    }

    fun resolve(session: EldritchSession, face: CardFace, section: Encountered) =
        if (section in face.selectable) session.copy(state = session.state.discard(face.deck, face.cardId, section))
        else session

    fun discard(session: EldritchSession, face: CardFace) =
        session.copy(state = session.state.discard(face.deck, face.cardId, Encountered.DISCARDED))

    fun shuffle(session: EldritchSession, deckId: String, random: Random) =
        session.copy(state = session.state.shuffleFull(deckId, random))

    fun returnToDeck(session: EldritchSession, face: CardFace, random: Random) =
        session.copy(state = session.state.returnFromDiscard(face.cardId, random))

    private fun face(session: EldritchSession, card: EldritchCard, encountered: Encountered, discardView: Boolean): CardFace {
        val name = deckName(session, card.deck)
        // Named cards open with a lead-in paragraph; only their Pass/Fail parts resolve the card.
        val selectable = when {
            discardView -> emptySet()
            card.title != null -> setOf(Encountered.MIDDLE, Encountered.BOTTOM)
            else -> setOf(Encountered.TOP, Encountered.MIDDLE, Encountered.BOTTOM)
        }.filter { card.sections[SectionPositions.indexOf(it)].text.isNotBlank() }.toSet()
        return CardFace(
            cardId = card.id,
            deck = card.deck,
            pageTitle = if (discardView) "Discard – $name" else name,
            sections = card.sections,
            selectable = selectable,
            encountered = encountered,
            textStyle = TextStyle.ELDRITCH,
        )
    }
}
