package com.m42.companion.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AllInclusive
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Backpack
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.CenterFocusStrong
import androidx.compose.material.icons.filled.Cyclone
import androidx.compose.material.icons.filled.Dangerous
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.DirectionsBoat
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.HeartBroken
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Report
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SentimentVeryDissatisfied
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SportsMma
import androidx.compose.material.icons.filled.Style
import androidx.compose.material.icons.filled.Train
import androidx.compose.material.icons.filled.TripOrigin
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.ui.graphics.vector.ImageVector
import com.m42.companion.engine.TextStyle

/** Colour families for highlighted terms; each theme maps them to a light or dark shade. */
enum class Tone {
    HEALTH, SANITY, CLUE, FOCUS, MONEY,
    LORE, INFLUENCE, OBSERVATION, STRENGTH, WILL, SPEED, SNEAK, LUCK,
    THREAT, DOOM, ELDRITCH, OMEN, GATE, MYSTERY, RUMOR,
    CONDITION, BLESSED, CURSED, STATUS, LOST, DANGER, POSSESSION, TRAIT,
}

/** A kind of highlighted term: its colour and the icon drawn in front of it (if any). */
enum class TermKind(val label: String, val tone: Tone, val icon: ImageVector?) {
    // Resources
    HEALTH("Health / Stamina", Tone.HEALTH, Icons.Filled.Favorite),
    SANITY("Sanity", Tone.SANITY, Icons.Filled.Psychology),
    CLUE("Clues", Tone.CLUE, Icons.Filled.Search),
    FOCUS("Focus", Tone.FOCUS, Icons.Filled.CenterFocusStrong),
    MONEY("Money", Tone.MONEY, GameIcons.BankNote),

    // Skills: the five Eldritch skills use the investigator sheet colours; Arkham's six reuse the closest ones.
    LORE("Lore", Tone.LORE, Icons.AutoMirrored.Filled.MenuBook),
    INFLUENCE("Influence", Tone.INFLUENCE, Icons.Filled.Handshake),
    OBSERVATION("Observation", Tone.OBSERVATION, Icons.Filled.Visibility),
    STRENGTH("Strength", Tone.STRENGTH, GameIcons.FlexedArm),
    WILL("Will", Tone.WILL, Icons.Filled.Shield),
    SPEED("Speed", Tone.SPEED, Icons.AutoMirrored.Filled.DirectionsRun),
    SNEAK("Sneak", Tone.SNEAK, Icons.Filled.VisibilityOff),
    FIGHT("Fight / Combat", Tone.STRENGTH, Icons.Filled.SportsMma),
    LUCK("Luck", Tone.LUCK, Icons.Filled.Casino),

    // Threats
    DOOM("Doom", Tone.DOOM, Icons.Filled.Dangerous),
    ELDRITCH_TOKEN("Eldritch tokens", Tone.ELDRITCH, GameIcons.Pentagram),
    OMEN("Omen", Tone.OMEN, Icons.Filled.TripOrigin),
    GATE("Gates", Tone.GATE, Icons.Filled.Cyclone),
    MONSTER("Monsters", Tone.THREAT, Icons.Filled.Pets),
    TERROR("Terror level", Tone.THREAT, Icons.Filled.Warning),
    MYSTERY("Mysteries", Tone.MYSTERY, Icons.AutoMirrored.Filled.Help),
    RUMOR("Rumors", Tone.RUMOR, Icons.Filled.Report),

    // Conditions and special cards
    CONDITION("Conditions / special cards", Tone.CONDITION, Icons.Filled.Style),
    BLESSED("Blessed", Tone.BLESSED, Icons.Filled.AutoAwesome),
    CURSED("Cursed", Tone.CURSED, Icons.Filled.NightsStay),

    // Statuses
    DELAYED("Delayed", Tone.STATUS, Icons.Filled.HourglassBottom),
    DETAINED("Detained", Tone.STATUS, Icons.Filled.Lock),
    ARRESTED("Arrested", Tone.STATUS, Icons.Filled.Gavel),
    LOST("Lost in Time and Space", Tone.LOST, Icons.Filled.AllInclusive),
    DEVOURED("Devoured", Tone.DANGER, Icons.Filled.HeartBroken),
    INSANE("Insane", Tone.DANGER, Icons.Filled.SentimentVeryDissatisfied),
    UNCONSCIOUS("Unconscious", Tone.DANGER, Icons.Filled.Bedtime),

    // Things you gain
    UNIQUE("Unique Items / Assets", Tone.POSSESSION, Icons.Filled.WorkspacePremium),
    ITEM("Items / Assets", Tone.POSSESSION, Icons.Filled.Backpack),
    ARTIFACT("Artifacts", Tone.POSSESSION, Icons.Filled.Diamond),
    SPELL("Spells", Tone.POSSESSION, Icons.Filled.AutoFixHigh),
    ALLY("Allies", Tone.POSSESSION, Icons.Filled.Person),
    SKILL_CARD("Skill cards", Tone.POSSESSION, Icons.Filled.School),
    TRAIN_TICKET("Train Tickets", Tone.POSSESSION, Icons.Filled.Train),
    SHIP_TICKET("Ship Tickets", Tone.POSSESSION, Icons.Filled.DirectionsBoat),

    /** Eldritch card traits such as RELIC or INCANTATION, shown as small capitals. */
    TRAIT("Traits", Tone.TRAIT, null),
}

/**
 * One highlighting rule. [kind] picks the colour and icon; a regex group named `k`, when present, marks where
 * the icon goes (e.g. before "Doom" in "advance Doom by 1"), otherwise it goes at the start of the match.
 */
class TermRule(val games: Set<TextStyle>, pattern: String, val kind: (MatchResult) -> TermKind) {
    val regex = Regex(pattern)

    /** Whether the pattern has a `k` group marking where the icon goes. */
    val hasIconGroup = "(?<k>" in pattern

    constructor(games: Set<TextStyle>, pattern: String, kind: TermKind) : this(games, pattern, { kind })
}

private val BOTH = setOf(TextStyle.ELDRITCH, TextStyle.ARKHAM)
private val ELDRITCH = setOf(TextStyle.ELDRITCH)
private val ARKHAM = setOf(TextStyle.ARKHAM)

private val eldritchSkills = mapOf(
    "LORE" to TermKind.LORE, "INFLUENCE" to TermKind.INFLUENCE, "OBSERVATION" to TermKind.OBSERVATION,
    "STRENGTH" to TermKind.STRENGTH, "WILL" to TermKind.WILL,
)
private val arkhamSkills = mapOf(
    "Speed" to TermKind.SPEED, "Sneak" to TermKind.SNEAK, "Fight" to TermKind.FIGHT, "Combat" to TermKind.FIGHT,
    "Will" to TermKind.WILL, "Lore" to TermKind.LORE, "Luck" to TermKind.LUCK,
)

private const val TRAITS = "ITEM|ALLY|RELIC|TOME|WEAPON|MAGICAL|TRINKET|TASK|SERVICE|INCANTATION|RITUAL|GLAMOUR|" +
    "CHARACTER|ELIXIR|MADNESS|INJURY|ILLNESS|BANE|BOON|PURSUIT|TALENT|DEAL|PARANOIA"

// "Deputy" alone is usually Deputy Dingby, the character; only the Deputy of Arkham card is highlighted.
private const val ARKHAM_SPECIAL_CARDS = "Retainer|Exhibit Items?|Madness|Injury|Deputy of Arkham|(?<=you're the )Deputy|" +
    "(?<=you are the )Deputy|Corruption|" +
    "Patrol marker|Bank Loan|(?:Silver Twilight(?: Lodge)?|Sheldon Gang|Cult) Membership|Blight|Tainted|Anointed|" +
    "Harried|Psychic|Wanted|Local Guide|Visions|Necrophobia|Private Investigator|Rare Book Collection|Research Materials"

/** Highlighting rules in priority order: when two matches overlap, the earlier rule wins. */
val TermRules: List<TermRule> = listOf(
    // Skill tests
    TermRule(ELDRITCH, """\[(LORE|INFLUENCE|OBSERVATION|STRENGTH|WILL)(?: [+-] \d)?]""") { eldritchSkills.getValue(it.groupValues[1]) },
    TermRule(ARKHAM, """\b(Speed|Sneak|Fight|Combat|Will|Lore|Luck) \([+-]\d\)(?: \[\d])?(?: check)?""") { arkhamSkills.getValue(it.groupValues[1]) },

    // Conditions, most specific first
    TermRule(BOTH, """\bBlessed Condition\b""", TermKind.BLESSED),
    TermRule(BOTH, """\bCursed Condition\b""", TermKind.CURSED),
    TermRule(BOTH, """\b[Ll]ost in [Tt]ime and [Ss]pace(?: Condition)?""", TermKind.LOST),
    TermRule(BOTH, """\bDetained(?: Condition)?\b""", TermKind.DETAINED),
    TermRule(ELDRITCH, """\b(?:[A-Z][A-Za-z'-]+ )+Conditions?\b""", TermKind.CONDITION),
    TermRule(ELDRITCH, """\b(?:Dark Pact|Paranoia|Amnesia|Hallucinations|Debt|(?:Leg|Back|Internal|Head) Injury|Poisoned|Hypothermia)\b""", TermKind.CONDITION),

    // Statuses
    TermRule(BOTH, """\b[Dd]elayed\b""", TermKind.DELAYED),
    TermRule(BOTH, """\b[Dd]etained\b""", TermKind.DETAINED),
    TermRule(ARKHAM, """\b[Aa]rrested\b""", TermKind.ARRESTED),
    TermRule(BOTH, """\b[Dd]evoured\b""", TermKind.DEVOURED),
    TermRule(ARKHAM, """\b[Ii]nsane\b""", TermKind.INSANE),
    TermRule(ARKHAM, """\b[Uu]nconscious\b""", TermKind.UNCONSCIOUS),
    TermRule(BOTH, """\bBless(?:ed|ing)\b""", TermKind.BLESSED),
    TermRule(BOTH, """\bCurse[ds]?\b""", TermKind.CURSED),

    // Threats
    TermRule(ELDRITCH, """\b(?:[Aa]dvance|[Rr]etreat) (?<k>Doom)(?: by \d+)?|\bDoom\b""", TermKind.DOOM),
    TermRule(ARKHAM, """\b[Dd]oom (?:tokens?|track)\b""", TermKind.DOOM),
    TermRule(ELDRITCH, """\b(?:[Aa]dvance|[Rr]etreat) the (?<k>Omen)(?: by \d+)?|\bOmen\b""", TermKind.OMEN),
    TermRule(ELDRITCH, """\b(?:\d+ )?Eldritch tokens?\b""", TermKind.ELDRITCH_TOKEN),
    TermRule(ELDRITCH, """\b(?:Epic )?[Mm]onsters?\b""", TermKind.MONSTER),
    TermRule(ARKHAM, """\b[Mm]onsters?(?: appears?)?\b""", TermKind.MONSTER),
    TermRule(BOTH, """\b[Gg]ates?(?: opens?)?\b""", TermKind.GATE),
    TermRule(ARKHAM, """\b[Tt]error level\b""", TermKind.TERROR),
    TermRule(ELDRITCH, """\bMyster(?:y|ies)\b""", TermKind.MYSTERY),
    TermRule(ELDRITCH, """\bRumors?\b""", TermKind.RUMOR),

    // Resources
    TermRule(ELDRITCH, """\b(?:\d+ )?Health\b""", TermKind.HEALTH),
    TermRule(ARKHAM, """\b(?:\d+ )?Stamina\b""", TermKind.HEALTH),
    TermRule(BOTH, """\b(?:\d+ )?Sanity\b""", TermKind.SANITY),
    TermRule(ARKHAM, """\b(?:\d+ )?Clue Tokens?\b""", TermKind.CLUE),
    TermRule(ELDRITCH, """\b(?:\d+ )?Clues?\b""", TermKind.CLUE),
    TermRule(ELDRITCH, """\b(?:\d+ )?Focus\b""", TermKind.FOCUS),
    TermRule(ARKHAM, """\$\d+""", TermKind.MONEY),
    TermRule(ELDRITCH, """\b(?:\d+ )?Train Tickets?\b""", TermKind.TRAIN_TICKET),
    TermRule(ELDRITCH, """\b(?:\d+ )?Ship Tickets?\b""", TermKind.SHIP_TICKET),

    // Card traits, before the possessions they describe ("1 RELIC Unique Asset")
    TermRule(ELDRITCH, """\b(?:$TRAITS)\b""", TermKind.TRAIT),

    // Special Arkham cards
    TermRule(ARKHAM, """\b(?:$ARKHAM_SPECIAL_CARDS)\b(?: cards?)?""", TermKind.CONDITION),

    // Things you gain
    TermRule(BOTH, """\b(?:\d+ )?Unique (?:Items?|Assets?)\b""", TermKind.UNIQUE),
    TermRule(ARKHAM, """\b(?:\d+ )?Common Items?\b""", TermKind.ITEM),
    TermRule(ELDRITCH, """\b(?:\d+ )?Assets?\b""", TermKind.ITEM),
    TermRule(ELDRITCH, """\b(?:\d+ )?Artifacts?\b""", TermKind.ARTIFACT),
    TermRule(BOTH, """\b(?:\d+ )?Spells?\b""", TermKind.SPELL),
    TermRule(ARKHAM, """\b(?:\d+ )?(?:Ally(?: cards?)?|Allies)\b""", TermKind.ALLY),
    TermRule(ARKHAM, """\b(?:\d+ )?Skills?\b""", TermKind.SKILL_CARD),
)
