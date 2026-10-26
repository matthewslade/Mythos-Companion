package com.m42.companion.eldritch

import com.m42.companion.data.EldritchCard
import com.m42.companion.data.EldritchData
import com.m42.companion.engine.DeckState
import kotlinx.serialization.Serializable
import kotlin.random.Random

data class AncientOne(val id: String, val name: String)

enum class EldritchExpansion(val id: String, val displayName: String, val ancientOnes: List<AncientOne>) {
    BASE(
        "base", "Base",
        listOf(
            AncientOne("azathoth", "Azathoth"),
            AncientOne("yog-sothoth", "Yog-Sothoth"),
            AncientOne("shub-niggurath", "Shub-Niggurath"),
            AncientOne("cthulhu", "Cthulhu"),
        ),
    ),
    FORSAKEN_LORE("forsaken-lore", "Forsaken Lore", listOf(AncientOne("yig", "Yig"))),
    MOUNTAINS_OF_MADNESS(
        "mountains-of-madness", "Mountains of Madness",
        listOf(AncientOne("rise-of-the-elder-things", "Rise of the Elder Things"), AncientOne("ithaqua", "Ithaqua")),
    ),
    STRANGE_REMNANTS("strange-remnants", "Strange Remnants", listOf(AncientOne("syzygy", "Syzygy"))),
    UNDER_THE_PYRAMIDS(
        "under-the-pyramids", "Under the Pyramids",
        listOf(AncientOne("abhoth", "Abhoth"), AncientOne("nephren-ka", "Nephren-Ka")),
    ),
    SIGNS_OF_CARCOSA("signs-of-carcosa", "Signs of Carcosa", listOf(AncientOne("hastur", "Hastur"))),
    THE_DREAMLANDS(
        "the-dreamlands", "The Dreamlands",
        listOf(AncientOne("hypnos", "Hypnos"), AncientOne("atlach-nacha", "Atlach-Nacha")),
    ),
    CITIES_IN_RUIN("cities-in-ruin", "Cities in Ruin", listOf(AncientOne("shudde-mell", "Shudde M'ell"))),
    MASKS_OF_NYARLATHOTEP(
        "masks-of-nyarlathotep", "Masks of Nyarlathotep",
        listOf(AncientOne("nyarlathotep", "Nyarlathotep"), AncientOne("antediluvium", "Antediluvium")),
    );

    companion object {
        fun byId(id: String) = entries.first { it.id == id }
        val allAncientOnes = entries.flatMap { it.ancientOnes }
        fun ancientOne(id: String) = allAncientOnes.first { it.id == id }
    }
}

/** Side boards and prelude cards that only make sense with their expansion. */
enum class EldritchOption(val id: String, val displayName: String, val parent: EldritchExpansion) {
    ANTARCTICA("antarctica", "Antarctica", EldritchExpansion.MOUNTAINS_OF_MADNESS),
    COSMIC_ALIGNMENT("cosmic-alignment", "Cosmic Alignment – Prelude Card", EldritchExpansion.STRANGE_REMNANTS),
    EGYPT("egypt", "Egypt", EldritchExpansion.UNDER_THE_PYRAMIDS),
    LITANY_OF_SECRETS("litany-of-secrets", "Litany of Secrets – Prelude Card", EldritchExpansion.UNDER_THE_PYRAMIDS),
    DREAMLANDS_BOARD("dreamlands-board", "Dreamlands Board", EldritchExpansion.THE_DREAMLANDS);

    companion object {
        fun forParent(expansion: EldritchExpansion) = entries.filter { it.parent == expansion }
    }
}

@Serializable
data class EldritchConfig(
    val expansions: Set<String>,
    val options: Set<String>,
    val ancientOne: String,
) {
    fun has(expansion: EldritchExpansion) = expansion.id in expansions
    fun has(option: EldritchOption) = option.id in options
}

/** Setup screen state with the original app's checkbox rules. */
data class EldritchSetup(
    val expansions: Set<EldritchExpansion> = setOf(EldritchExpansion.BASE),
    val options: Set<EldritchOption> = emptySet(),
    /** Null means "Random". */
    val ancientOne: AncientOne? = null,
) {
    val availableAncientOnes: List<AncientOne>
        get() = EldritchExpansion.entries.filter { it in expansions }.flatMap { it.ancientOnes }

    fun isOptionEnabled(option: EldritchOption) = option.parent in expansions

    fun toggle(expansion: EldritchExpansion): EldritchSetup {
        val newExpansions = if (expansion in expansions) expansions - expansion else expansions + expansion
        val newOptions = options.filter { it.parent in newExpansions }.toSet()
        val keepAncientOne = ancientOne?.takeIf { aoIn(newExpansions, it) }
        return copy(expansions = newExpansions, options = newOptions, ancientOne = keepAncientOne)
    }

    fun toggle(option: EldritchOption): EldritchSetup {
        if (!isOptionEnabled(option)) return this
        if (option in options) return copy(options = options - option)
        // Only one prelude card can be in play.
        val exclusive = when (option) {
            EldritchOption.COSMIC_ALIGNMENT -> EldritchOption.LITANY_OF_SECRETS
            EldritchOption.LITANY_OF_SECRETS -> EldritchOption.COSMIC_ALIGNMENT
            else -> null
        }
        return copy(options = options - setOfNotNull(exclusive) + option)
    }

    /** Null when no expansion is selected. */
    fun toConfig(random: Random): EldritchConfig? {
        if (expansions.isEmpty()) return null
        val chosen = ancientOne ?: availableAncientOnes.random(random)
        return EldritchConfig(expansions.map { it.id }.toSet(), options.map { it.id }.toSet(), chosen.id)
    }

    private fun aoIn(set: Set<EldritchExpansion>, ao: AncientOne) = set.any { ao in it.ancientOnes }
}

object EldritchDecks {
    const val AMERICAS = "americas"
    const val EUROPE = "europe"
    const val ASIA = "asia"
    const val GENERAL = "general"
    const val ANTARCTICA_WEST = "antarctica-west"
    const val ANTARCTICA_EAST = "antarctica-east"
    const val ANTARCTICA_RESEARCH = "antarctica-research"
    const val AFRICA = "africa"
    const val EGYPT = "egypt"
    const val DREAMLANDS = "dreamlands"
    const val GATE = "gate"
    const val EXPEDITION = "expedition"
    const val MYSTIC_RUINS = "mystic-ruins"
    const val DREAM_QUEST = "dream-quest"
    const val RESEARCH = "research"
    const val SPECIAL_1 = "special-1"
    const val SPECIAL_2 = "special-2"
    const val SPECIAL_3 = "special-3"
    const val DISASTER = "disaster"
    const val DEVASTATION = "devastation"

    private val names = mapOf(
        AMERICAS to "Americas", EUROPE to "Europe", ASIA to "Asia", GENERAL to "General",
        ANTARCTICA_WEST to "Antarctica West", ANTARCTICA_EAST to "Antarctica East",
        ANTARCTICA_RESEARCH to "Antarctica Research", AFRICA to "Africa", EGYPT to "Egypt",
        DREAMLANDS to "Dreamlands", GATE to "Gate", EXPEDITION to "Expedition", MYSTIC_RUINS to "Mystic Ruins",
        DREAM_QUEST to "Dream-Quest", RESEARCH to "Research", DISASTER to "Disaster", DEVASTATION to "Devastation",
    )

    fun name(deckId: String) = names[deckId] ?: deckId

    /** Decks the original app only loaded for the selected Ancient One. */
    fun isAncientOneDeck(deckId: String) = deckId == RESEARCH || deckId.startsWith("special-")

    /** Builds and shuffles every deck for a new game, mirroring the original loader's merge order. */
    fun build(data: EldritchData, config: EldritchConfig, random: Random): DeckState {
        val decks = linkedMapOf<String, MutableList<String>>(RESEARCH to mutableListOf())
        for (expansion in EldritchExpansion.entries) {
            if (!config.has(expansion)) continue
            for (card in data.cards) {
                if (card.expansion != expansion.id) continue
                if (isAncientOneDeck(card.deck) && card.ancientOne != config.ancientOne) continue
                decks.getOrPut(card.deck) { mutableListOf() } += card.id
            }
        }
        return DeckState.of(decks, random)
    }
}

/** Which buttons the main screen shows, from EldritchCompanion.onCreate in the original app. */
object EldritchBoardRules {
    fun showAntarctica(c: EldritchConfig) =
        c.has(EldritchOption.ANTARCTICA) || c.ancientOne == "rise-of-the-elder-things"

    fun showEgypt(c: EldritchConfig) = c.has(EldritchOption.EGYPT) || c.ancientOne == "nephren-ka"

    fun showDreamlands(c: EldritchConfig) = c.has(EldritchOption.DREAMLANDS_BOARD) || c.ancientOne == "hypnos"

    fun showMysticRuins(c: EldritchConfig) =
        c.has(EldritchOption.COSMIC_ALIGNMENT) || c.ancientOne == "syzygy" || c.ancientOne == "antediluvium"

    fun showCitiesInRuin(c: EldritchConfig) = c.has(EldritchExpansion.CITIES_IN_RUIN)
}

fun EldritchCard.headerLine(): String = sections.mapNotNull { it.header }.joinToString(" · ")
