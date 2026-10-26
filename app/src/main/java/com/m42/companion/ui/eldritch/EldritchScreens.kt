package com.m42.companion.ui.eldritch

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.m42.companion.R
import com.m42.companion.eldritch.EldritchBoardRules
import com.m42.companion.eldritch.EldritchDecks
import com.m42.companion.eldritch.EldritchExpansion
import com.m42.companion.eldritch.EldritchGame
import com.m42.companion.eldritch.EldritchOption
import com.m42.companion.eldritch.EldritchSession
import com.m42.companion.engine.DISCARD_PILE
import com.m42.companion.ui.components.BoardItem
import com.m42.companion.ui.components.CheckRow
import com.m42.companion.ui.components.Emblem
import com.m42.companion.ui.components.GameBoard
import com.m42.companion.ui.components.ReadableColumn
import com.m42.companion.ui.components.SetupActions
import com.m42.companion.ui.components.galleryActions
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EldritchSetupScreen(vm: EldritchViewModel, onBack: () -> Unit, onPlay: () -> Unit) {
    val setup by vm.setup.collectAsStateWithLifecycle()
    val ready by vm.ready.collectAsStateWithLifecycle()
    val session by vm.session.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var confirmNew by rememberSaveable { mutableStateOf(false) }
    val hasSave = ready && (session != null || vm.hasSavedGame())

    fun start() {
        if (vm.startGame()) onPlay()
        else scope.launch { snackbar.showSnackbar("Choose at least one expansion.") }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Eldritch Horror") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") } },
            )
        },
        snackbarHost = { SnackbarHost(snackbar) },
        bottomBar = {
            SetupActions(
                ready = ready,
                hasSave = hasSave,
                onStart = { if (hasSave) confirmNew = true else start() },
                onContinue = onPlay,
            )
        },
    ) { padding ->
        ReadableColumn(Modifier.padding(padding)) {
            Column(
                Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Emblem(R.drawable.emblem_elder_sign, 96.dp, Modifier.align(Alignment.CenterHorizontally))
                Text("Select Expansions", style = MaterialTheme.typography.headlineSmall)
                for (expansion in EldritchExpansion.entries) {
                    CheckRow(expansion.displayName, expansion in setup.expansions, onToggle = { vm.toggle(expansion) })
                    for (option in EldritchOption.forParent(expansion)) {
                        CheckRow(
                            option.displayName,
                            option in setup.options,
                            enabled = setup.isOptionEnabled(option),
                            indent = true,
                            onToggle = { vm.toggle(option) },
                        )
                    }
                }
                Text("Select Ancient One", style = MaterialTheme.typography.headlineSmall, modifier = Modifier.padding(top = 16.dp))
                AncientOnePicker(vm)
            }
        }
    }
    if (confirmNew) {
        AlertDialog(
            onDismissRequest = { confirmNew = false },
            title = { Text("Start a new game?") },
            text = { Text("The game in progress will be lost.") },
            confirmButton = { TextButton(onClick = { confirmNew = false; start() }) { Text("Start new game") } },
            dismissButton = { TextButton(onClick = { confirmNew = false }) { Text("Cancel") } },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AncientOnePicker(vm: EldritchViewModel) {
    val setup by vm.setup.collectAsStateWithLifecycle()
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = setup.ancientOne?.name ?: "Random",
            onValueChange = {},
            readOnly = true,
            label = { Text("Ancient One") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier = Modifier.fillMaxWidth().menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(text = { Text("Random") }, onClick = { vm.choose(null); expanded = false })
            for (ao in setup.availableAncientOnes) {
                DropdownMenuItem(text = { Text(ao.name) }, onClick = { vm.choose(ao); expanded = false })
            }
        }
    }
}

@Composable
fun EldritchBoardScreen(vm: EldritchViewModel, onBack: () -> Unit, onOpenGallery: () -> Unit, onNoGame: () -> Unit) {
    val game by vm.game.collectAsStateWithLifecycle()
    val session by vm.session.collectAsStateWithLifecycle()
    val ready by vm.ready.collectAsStateWithLifecycle()
    val gallery by vm.gallery.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var removing by rememberSaveable { mutableStateOf(false) }

    val g = game
    val s = session
    if (g == null || s == null) {
        if (ready && s == null) LaunchedEffect(Unit) { onNoGame() }
        return
    }
    fun message(text: String) = scope.launch { snackbar.showSnackbar(text) }

    GameBoard(
        title = g.title(s),
        items = eldritchBoardItems(g, s) {
            if (s.state.deck(EldritchDecks.EXPEDITION).isEmpty()) message("The Expedition deck is empty.")
            else removing = true
        },
        minButtonWidth = 148.dp,
        gallery = gallery,
        galleryActions = galleryActions(vm),
        snackbar = snackbar,
        onOpenDeck = { deckId, twoPane ->
            when {
                vm.openGallery(deckId) -> if (!twoPane) onOpenGallery()
                deckId == DISCARD_PILE -> message("The discard pile is empty.")
                else -> message("The ${vm.deckName(deckId)} deck is empty.")
            }
        },
        onBack = onBack,
        onEndGame = vm::endGame,
    )
    if (removing) {
        RemoveExpeditionDialog(
            current = g.currentLocation(s, EldritchDecks.EXPEDITION),
            locations = g.expeditionLocations(s),
            onRemove = { vm.removeExpedition(it); removing = false },
            onDismiss = { removing = false },
        )
    }
}

private object DeckColors {
    val americas = Color(0xFF00741E)
    val europe = Color(0xFFB05C00)
    val asia = Color(0xFF5A0E8A)
    val antarcticaWest = Color(0xFF7E5A82)
    val antarcticaEast = Color(0xFF746100)
    val africa = Color(0xFF7B5000)
    val egypt = Color(0xFFB60101)
    val dreamlands = Color(0xFF5F7A1F)
    val general = Color(0xFF4A4A4A)
    val gate = Color(0xFF00804A)
    val expedition = Color(0xFF1A0077)
    val mysticRuins = Color(0xFF3A20C8)
    val dreamQuest = Color(0xFF5E30AD)
    val research = Color(0xFF2B2B2B)
    val antarcticaResearch = Color(0xFF8B4512)
    val special = listOf(Color(0xFF4F7F79), Color(0xFF237A52), Color(0xFF075E35))
    val disaster = Color(0xFF0077A3)
    val devastation = Color(0xFFC62828)
    val discard = Color(0xFF3D4876)
}

/**
 * The buttons of the original main screen, in the same order and with the same visibility rules. Three-part
 * decks are shown as one button per part, each drawing from its own deck.
 */
private fun eldritchBoardItems(game: EldritchGame, session: EldritchSession, onRemoveExpedition: () -> Unit): List<BoardItem> {
    val c = session.config
    fun deck(id: String, color: Color, detail: String? = null) =
        BoardItem.Deck(id, game.deckName(session, id), detail, color, wide = true)

    return buildList {
        fun split(deckId: String, color: Color) {
            add(BoardItem.Heading(EldritchDecks.name(deckId)))
            for (part in EldritchDecks.parts(deckId)) add(BoardItem.Deck(part, game.partName(part), color = color))
        }

        split(EldritchDecks.AMERICAS, DeckColors.americas)
        split(EldritchDecks.EUROPE, DeckColors.europe)
        split(EldritchDecks.ASIA, DeckColors.asia)
        if (EldritchBoardRules.showAntarctica(c)) {
            split(EldritchDecks.ANTARCTICA_WEST, DeckColors.antarcticaWest)
            split(EldritchDecks.ANTARCTICA_EAST, DeckColors.antarcticaEast)
        }
        if (EldritchBoardRules.showEgypt(c)) {
            split(EldritchDecks.AFRICA, DeckColors.africa)
            split(EldritchDecks.EGYPT, DeckColors.egypt)
        }
        if (EldritchBoardRules.showDreamlands(c)) split(EldritchDecks.DREAMLANDS, DeckColors.dreamlands)
        split(EldritchDecks.GENERAL, DeckColors.general)

        add(BoardItem.Heading("Gates & Expeditions"))
        add(deck(EldritchDecks.GATE, DeckColors.gate))
        add(
            deck(EldritchDecks.EXPEDITION, DeckColors.expedition, detail = game.currentLocation(session, EldritchDecks.EXPEDITION))
                .copy(secondaryAction = "Remove" to onRemoveExpedition),
        )
        if (EldritchBoardRules.showMysticRuins(c)) {
            add(deck(EldritchDecks.MYSTIC_RUINS, DeckColors.mysticRuins, detail = game.currentLocation(session, EldritchDecks.MYSTIC_RUINS)))
        }
        if (EldritchBoardRules.showDreamlands(c)) {
            add(deck(EldritchDecks.DREAM_QUEST, DeckColors.dreamQuest, detail = game.currentLocation(session, EldritchDecks.DREAM_QUEST)))
        }

        split(EldritchDecks.RESEARCH, DeckColors.research)
        if (EldritchBoardRules.showAntarctica(c)) split(EldritchDecks.ANTARCTICA_RESEARCH, DeckColors.antarcticaResearch)

        val specials = listOf(EldritchDecks.SPECIAL_1, EldritchDecks.SPECIAL_2, EldritchDecks.SPECIAL_3)
            .mapIndexedNotNull { i, id -> game.specialDeckName(session, id)?.let { deck(id, DeckColors.special[i]) } }
        if (specials.isNotEmpty()) {
            add(BoardItem.Heading("Special"))
            addAll(specials)
        }

        if (EldritchBoardRules.showCitiesInRuin(c)) {
            add(BoardItem.Heading("Cities in Ruin"))
            add(deck(EldritchDecks.DISASTER, DeckColors.disaster))
            add(deck(EldritchDecks.DEVASTATION, DeckColors.devastation))
        }

        add(BoardItem.Heading(""))
        val discards = session.state.discard.size
        add(deck(DISCARD_PILE, DeckColors.discard, detail = if (discards == 1) "1 card" else "$discards cards").copy(label = "Discard"))
    }
}

@Composable
private fun RemoveExpeditionDialog(current: String, locations: List<String>, onRemove: (String) -> Unit, onDismiss: () -> Unit) {
    val firstCurrent = current.substringBefore(", ")
    var selected by rememberSaveable { mutableStateOf(locations.firstOrNull { it == firstCurrent } ?: locations.firstOrNull()) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Remove expedition") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()).selectableGroup()) {
                Text("Current: $current", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(bottom = 8.dp))
                for (location in locations) {
                    Row(
                        Modifier.fillMaxWidth().heightIn(min = 48.dp)
                            .selectable(location == selected, role = Role.RadioButton) { selected = location },
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(selected = location == selected, onClick = null)
                        Text(location, modifier = Modifier.padding(start = 12.dp))
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = { selected?.let(onRemove) }, enabled = selected != null) { Text("Remove") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}
