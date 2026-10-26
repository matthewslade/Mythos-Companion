package com.m42.companion.engine

import com.m42.companion.data.CardSection

enum class TextStyle {
    /** Eldritch Horror: skill tests in [BRACKETS] are emphasised. */
    ELDRITCH,

    /** Arkham Horror: Stamina, Sanity and Clue Tokens are also coloured, as on the printed cards. */
    ARKHAM,
}

/** A card as the gallery shows it, independent of which game it came from. */
data class CardFace(
    val cardId: String,
    /** The deck the card belongs to, which is where it is discarded from. */
    val deck: String,
    val pageTitle: String,
    val sections: List<CardSection>,
    /** Sections the player can tap to resolve the card. */
    val selectable: Set<Encountered>,
    val encountered: Encountered,
    val textStyle: TextStyle,
    /** Small label shown above the card, e.g. "Other" for a generic gate encounter. */
    val tag: String? = null,
)

/** Position of each section in [CardFace.sections]. */
val SectionPositions = listOf(Encountered.TOP, Encountered.MIDDLE, Encountered.BOTTOM)

data class Gallery(
    val deckId: String,
    val faces: List<CardFace>,
    val isDiscardPile: Boolean,
    /** Shown above the cards when opening the deck did something notable. */
    val banner: String? = null,
)

/** Deck id used to open the discard pile in a gallery. */
const val DISCARD_PILE = "discard"
