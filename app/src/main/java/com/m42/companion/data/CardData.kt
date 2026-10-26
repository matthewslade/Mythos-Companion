package com.m42.companion.data

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.InputStream

/** One headed block of encounter text on a card. A null header renders the text without a heading. */
@Serializable
data class CardSection(val header: String? = null, val text: String)

// ---------------------------------------------------------------- Eldritch Horror (assets/eldritch.json)

@Serializable
data class EldritchData(val game: String, val cards: List<EldritchCard>)

@Serializable
data class EldritchCard(
    val id: String,
    val deck: String,
    val expansion: String,
    /** Set on Research and Special cards, which only enter the game for this Ancient One. */
    val ancientOne: String? = null,
    /** Set on named cards (gates, expeditions, special decks, ...). Their first section is a lead-in. */
    val title: String? = null,
    val sections: List<CardSection>,
)

// ---------------------------------------------------------------- Arkham Horror 2nd edition (assets/arkham.json)

@Serializable
data class ArkhamData(
    val game: String,
    val expansions: List<ArkhamExpansion>,
    val locations: List<ArkhamLocation>,
    val otherWorlds: List<OtherWorld>,
    val locationEncounters: List<LocationEncounter>,
    val otherWorldEncounters: List<OtherWorldEncounter>,
)

@Serializable
data class ArkhamExpansion(val code: String, val name: String, val board: Boolean)

@Serializable
data class ArkhamLocation(val name: String, val board: String, val neighbourhood: String? = null)

@Serializable
data class OtherWorld(val name: String, val expansion: String, val colours: List<GateColour>)

@Serializable
enum class GateColour { red, green, blue, yellow }

@Serializable
data class LocationEncounter(
    val id: String,
    val location: String,
    /** Every listed expansion must be in play for the encounter to be used. */
    val expansions: List<String>,
    val text: String,
    val note: String? = null,
)

@Serializable
data class OtherWorldEncounter(
    val id: String,
    /** Null for the generic "Other" entries that any world sharing [colour] can draw. */
    val world: String? = null,
    val colour: GateColour? = null,
    val expansions: List<String>,
    val text: String,
    /** Where a card came from or what is uncertain about it; not shown in the app. */
    val note: String? = null,
)

object CardDataParser {
    private val json = Json { ignoreUnknownKeys = false }

    fun eldritch(input: InputStream): EldritchData =
        input.use { json.decodeFromString(EldritchData.serializer(), it.readBytes().decodeToString()) }

    fun arkham(input: InputStream): ArkhamData =
        input.use { json.decodeFromString(ArkhamData.serializer(), it.readBytes().decodeToString()) }
}
