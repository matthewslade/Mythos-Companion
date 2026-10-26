package com.m42.companion.arkham

import com.m42.companion.data.ArkhamData
import com.m42.companion.data.ArkhamLocation
import com.m42.companion.data.OtherWorld
import com.m42.companion.engine.CardFace
import com.m42.companion.engine.DISCARD_PILE
import com.m42.companion.engine.DeckState
import com.m42.companion.engine.Encountered
import com.m42.companion.engine.Gallery
import com.m42.companion.data.CardSection
import com.m42.companion.engine.TextStyle
import kotlinx.serialization.Serializable
import kotlin.random.Random

const val BASE_GAME = "AH"
const val DREAMLANDS = "The Dreamlands"
const val ANOTHER_DIMENSION = "Another Dimension"

@Serializable
data class ArkhamConfig(val expansions: Set<String>) {
    /** Expansion codes in play, always including the base game. */
    val inPlay: Set<String> get() = expansions + BASE_GAME
}

@Serializable
data class ArkhamSession(
    val config: ArkhamConfig,
    val state: DeckState,
    /**
     * Seeds the Other World draw so reopening a world shows the same cards. It changes whenever a card is
     * resolved, discarded, returned or shuffled, which is when the legacy app would have drawn again.
     */
    val drawSeed: Long = 0L,
)

object ArkhamDecks {
    const val OTHER = "ow-other"
    fun location(name: String) = "loc:$name"
    fun world(name: String) = "ow:$name"
    fun isWorld(deckId: String) = deckId.startsWith("ow:")
}

/** Arkham locations grouped the way the board screen lists them. */
data class LocationGroup(
    /** The board's name, e.g. "Dunwich". */
    val board: String,
    /** The neighbourhood's name, or the board's when its locations have no neighbourhoods. */
    val title: String,
    val locations: List<ArkhamLocation>,
)

class ArkhamGame(private val data: ArkhamData) {
    private val locationCards = data.locationEncounters.associateBy { it.id }
    private val worldCards = data.otherWorldEncounters.associateBy { it.id }
    private val worldsByName = data.otherWorlds.associateBy { it.name }
    private val boardNames = mapOf("AH" to "Arkham") + data.expansions.filter { it.board }
        .associate { it.code to it.name.removeSuffix(" Horror") }

    val expansions get() = data.expansions

    fun newSession(config: ArkhamConfig, random: Random): ArkhamSession {
        val inPlay = config.inPlay
        val decks = linkedMapOf<String, List<String>>()
        for (location in locations(config)) {
            decks[ArkhamDecks.location(location.name)] = data.locationEncounters
                .filter { it.location == location.name && inPlay.containsAll(it.expansions) }.map { it.id }
        }
        for (world in worlds(config)) {
            decks[ArkhamDecks.world(world.name)] = data.otherWorldEncounters
                .filter { it.world == world.name && inPlay.containsAll(it.expansions) }.map { it.id }
        }
        decks[ArkhamDecks.OTHER] = data.otherWorldEncounters
            .filter { it.world == null && inPlay.containsAll(it.expansions) }.map { it.id }
        return ArkhamSession(config, DeckState.of(decks, random), random.nextLong())
    }

    fun locations(config: ArkhamConfig) = data.locations.filter { it.board in config.inPlay }

    fun worlds(config: ArkhamConfig) = data.otherWorlds.filter { it.expansion in config.inPlay }

    fun locationGroups(config: ArkhamConfig): List<LocationGroup> = locations(config)
        .groupBy { loc -> loc.board to (loc.neighbourhood ?: boardNames.getValue(loc.board)) }
        .map { (key, locs) -> LocationGroup(boardNames.getValue(key.first), key.second, locs) }

    fun deckName(deckId: String): String = when {
        deckId == ArkhamDecks.OTHER -> "Other"
        deckId.startsWith("loc:") -> deckId.removePrefix("loc:")
        deckId.startsWith("ow:") -> deckId.removePrefix("ow:")
        else -> deckId
    }

    fun open(session: ArkhamSession, deckId: String, random: Random): Pair<ArkhamSession, Gallery?> = when {
        deckId == DISCARD_PILE -> session to discardGallery(session)
        ArkhamDecks.isWorld(deckId) -> openWorld(session, worldsByName.getValue(deckId.removePrefix("ow:")))
        else -> openLocation(session, deckId, random)
    }

    private fun openLocation(session: ArkhamSession, deckId: String, random: Random): Pair<ArkhamSession, Gallery?> {
        if (!session.state.hasDeck(deckId)) return session to null
        val prepared = session.copy(state = session.state.prepare(deckId, random))
        val deck = prepared.state.deck(deckId)
        if (deck.isEmpty()) return prepared to null
        return prepared to Gallery(deckId, deck.map { locationFace(it, deckId, Encountered.NONE, false) }, false)
    }

    /**
     * Other World draws keep the legacy app's approximation of the gate deck: each draw comes from the world's
     * own encounters or from the "Other" encounters sharing one of its gate colours, weighted by deck sizes.
     * Every so often the whole gate deck is reshuffled ("The Stars Are Right").
     */
    private fun openWorld(session: ArkhamSession, world: OtherWorld): Pair<ArkhamSession, Gallery?> {
        val namedDeck = ArkhamDecks.world(world.name)
        val random = Random(session.drawSeed * 31 + world.name.hashCode())
        var state = session.state
        var seed = session.drawSeed
        val otherPool = otherPoolSize(session.config)
        var banner: String? = null
        if (otherPool > 0 && random.nextInt(otherPool) == 0) {
            state = reshuffleGateDeck(state, random)
            seed = random.nextLong() // the reshuffle has happened; reopening must not trigger it again
            banner = "The Stars Are Right — the gate deck has been reshuffled."
        }
        state = state.prepare(namedDeck, random)
        val matchesGate = { id: String -> worldCards.getValue(id).colour in world.colours }
        // The shared deck may still hold cards while none of this world's colours are left in it.
        if (state.deck(ArkhamDecks.OTHER).none(matchesGate)) state = state.shuffleFull(ArkhamDecks.OTHER, random)
        val named = state.deck(namedDeck)
        val others = state.deck(ArkhamDecks.OTHER).filter(matchesGate)
        val ratio = namedRatio(session.config, world)

        val faces = mutableListOf<CardFace>()
        var n = 0
        var o = 0
        while (n < named.size || o < others.size) {
            val wantOther = random.nextFloat() > ratio
            val takeOther = if (wantOther) o < others.size else n >= named.size
            faces += if (takeOther) worldFace(others[o++], ArkhamDecks.OTHER, world.name, Encountered.NONE, false)
            else worldFace(named[n++], namedDeck, world.name, Encountered.NONE, false)
        }
        val updated = session.copy(state = state, drawSeed = seed)
        return updated to faces.takeIf { it.isNotEmpty() }?.let { Gallery(namedDeck, it, false, banner) }
    }

    /** Chance of drawing the world's own encounter rather than an "Other" one, as in the legacy app. */
    fun namedRatio(config: ArkhamConfig, world: OtherWorld): Float {
        if (world.name == ANOTHER_DIMENSION) return 0f
        val named = data.otherWorldEncounters.count { it.world == world.name && config.inPlay.containsAll(it.expansions) }
        var other = otherPoolSize(config)
        // The Dreamlands appears on twice as many gate cards as the other worlds.
        if (world.name != DREAMLANDS) other /= 2
        return if (other == 0) 1f else named.toFloat() / other
    }

    private fun otherPoolSize(config: ArkhamConfig) =
        data.otherWorldEncounters.count { it.world == null && config.inPlay.containsAll(it.expansions) }

    private fun reshuffleGateDeck(state: DeckState, random: Random): DeckState =
        state.decks.keys.filter { ArkhamDecks.isWorld(it) || it == ArkhamDecks.OTHER }
            .fold(state) { s, deck -> s.shuffleFull(deck, random) }

    private fun discardGallery(session: ArkhamSession): Gallery? {
        val pile = session.state.discard
        if (pile.isEmpty()) return null
        val faces = pile.map {
            if (it.deck.startsWith("loc:")) locationFace(it.cardId, it.deck, it.encountered, true)
            else worldFace(it.cardId, it.deck, worldCards.getValue(it.cardId).world, it.encountered, true)
        }
        return Gallery(DISCARD_PILE, faces, true)
    }

    fun resolve(session: ArkhamSession, face: CardFace, section: Encountered, random: Random) =
        if (section in face.selectable) session.next(session.state.discard(face.deck, face.cardId, section), random)
        else session

    fun discard(session: ArkhamSession, face: CardFace, random: Random) =
        session.next(session.state.discard(face.deck, face.cardId, Encountered.DISCARDED), random)

    fun shuffle(session: ArkhamSession, deckId: String, random: Random): ArkhamSession {
        var state = session.state.shuffleFull(deckId, random)
        if (ArkhamDecks.isWorld(deckId)) state = state.shuffleFull(ArkhamDecks.OTHER, random)
        return session.next(state, random)
    }

    fun returnToDeck(session: ArkhamSession, face: CardFace, random: Random) =
        session.next(session.state.returnFromDiscard(face.cardId, random), random)

    /** True when every card the session refers to still exists in the bundled data. */
    fun isValid(session: ArkhamSession): Boolean =
        session.state.decks.values.all { deck -> deck.all { it in locationCards || it in worldCards } } &&
            session.state.discard.all { it.cardId in locationCards || it.cardId in worldCards } &&
            session.state.decks.keys.filter(ArkhamDecks::isWorld).all { it.removePrefix("ow:") in worldsByName }

    private fun ArkhamSession.next(state: DeckState, random: Random) = copy(state = state, drawSeed = random.nextLong())

    private fun locationFace(id: String, deckId: String, encountered: Encountered, discardView: Boolean): CardFace {
        val card = locationCards.getValue(id)
        return CardFace(
            cardId = id,
            deck = deckId,
            pageTitle = if (discardView) "Discard – ${card.location}" else card.location,
            sections = listOf(CardSection(card.location, card.text)),
            selectable = if (discardView) emptySet() else setOf(Encountered.TOP),
            encountered = encountered,
            textStyle = TextStyle.ARKHAM,
        )
    }

    private fun worldFace(id: String, deckId: String, worldName: String?, encountered: Encountered, discardView: Boolean): CardFace {
        val card = worldCards.getValue(id)
        val shownWorld = worldName ?: "Other World"
        return CardFace(
            cardId = id,
            deck = deckId,
            pageTitle = if (discardView) "Discard – $shownWorld" else shownWorld,
            sections = listOf(CardSection(card.world ?: worldName ?: "Other", card.text)),
            selectable = if (discardView) emptySet() else setOf(Encountered.TOP),
            encountered = encountered,
            textStyle = TextStyle.ARKHAM,
            tag = card.colour?.let { "Other · ${it.name.replaceFirstChar(Char::uppercase)} gate" },
        )
    }
}
