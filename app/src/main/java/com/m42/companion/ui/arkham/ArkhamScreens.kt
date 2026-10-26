package com.m42.companion.ui.arkham

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.m42.companion.R
import com.m42.companion.arkham.ArkhamDecks
import com.m42.companion.arkham.ArkhamGame
import com.m42.companion.arkham.ArkhamSession
import com.m42.companion.data.GateColour
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
fun ArkhamSetupScreen(vm: ArkhamViewModel, onBack: () -> Unit, onPlay: () -> Unit) {
    val game by vm.game.collectAsStateWithLifecycle()
    val selected by vm.expansions.collectAsStateWithLifecycle()
    val ready by vm.ready.collectAsStateWithLifecycle()
    val session by vm.session.collectAsStateWithLifecycle()
    var confirmNew by rememberSaveable { mutableStateOf(false) }
    val hasSave = ready && (session != null || vm.hasSavedGame())

    fun start() {
        vm.startGame()
        onPlay()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Arkham Horror") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") } },
            )
        },
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
                Emblem(R.drawable.emblem_cthulhu, 96.dp, Modifier.align(Alignment.CenterHorizontally))
                Text("Choose your Expansions", style = MaterialTheme.typography.headlineSmall)
                Text(
                    "The base game is always included.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                game?.expansions?.forEach { expansion ->
                    CheckRow(
                        if (expansion.board) "${expansion.name} (board)" else expansion.name,
                        expansion.code in selected,
                        onToggle = { vm.toggle(expansion.code) },
                    )
                }
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

@Composable
fun ArkhamBoardScreen(vm: ArkhamViewModel, onBack: () -> Unit, onOpenGallery: () -> Unit, onNoGame: () -> Unit) {
    val game by vm.game.collectAsStateWithLifecycle()
    val session by vm.session.collectAsStateWithLifecycle()
    val ready by vm.ready.collectAsStateWithLifecycle()
    val gallery by vm.gallery.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val g = game
    val s = session
    if (g == null || s == null) {
        if (ready && s == null) LaunchedEffect(Unit) { onNoGame() }
        return
    }
    GameBoard(
        title = "Arkham Horror",
        items = arkhamBoardItems(g, s),
        minButtonWidth = 148.dp,
        gallery = gallery,
        galleryActions = galleryActions(vm),
        snackbar = snackbar,
        onOpenDeck = { deckId, twoPane ->
            when {
                vm.openGallery(deckId) -> if (!twoPane) onOpenGallery()
                else -> scope.launch {
                    snackbar.showSnackbar(
                        if (deckId == DISCARD_PILE) "The discard pile is empty."
                        else "There are no encounters for ${vm.deckName(deckId)} with these expansions.",
                    )
                }
            }
        },
        onBack = onBack,
        onEndGame = vm::endGame,
    )
}

private object ArkhamColors {
    /** Card back colours of the Arkham neighbourhood location decks. */
    val neighbourhoods = mapOf(
        "Northside" to Color(0xFFC96D2D),
        "Downtown" to Color(0xFFA8A8A3),
        "Easttown" to Color(0xFF555751),
        "Merchant District" to Color(0xFF2F8768),
        "Rivertown" to Color(0xFF76528E),
        "Miskatonic U." to Color(0xFFD9AA32),
        "French Hill" to Color(0xFF187FC1),
        "Uptown" to Color(0xFFAA352C),
        "Southside" to Color(0xFF79553C),
    )
    val boards = mapOf(
        "AH" to Color(0xFF2E5E3E),
        "DH" to Color(0xFF6D4C2F),
        "KH" to Color(0xFF3A5A78),
        "IH" to Color(0xFF1E6B6B),
    )
    val otherWorld = Color(0xFF4A2C6B)
    val discard = Color(0xFF3D4876)
    val gates = mapOf(
        GateColour.red to Color(0xFFE53935),
        GateColour.green to Color(0xFF43A047),
        GateColour.blue to Color(0xFF1E88E5),
        GateColour.yellow to Color(0xFFFDD835),
    )
}

private fun arkhamBoardItems(game: ArkhamGame, session: ArkhamSession): List<BoardItem> = buildList {
    for (group in game.locationGroups(session.config)) {
        add(BoardItem.Heading(group.title))
        for (location in group.locations) {
            val color = location.neighbourhood?.let(ArkhamColors.neighbourhoods::get) ?: ArkhamColors.boards.getValue(location.board)
            add(BoardItem.Deck(ArkhamDecks.location(location.name), location.name, color = color))
        }
    }
    add(BoardItem.Heading("Other Worlds"))
    for (world in game.worlds(session.config)) {
        add(
            BoardItem.Deck(
                ArkhamDecks.world(world.name),
                world.name,
                color = ArkhamColors.otherWorld,
                dots = world.colours.map { ArkhamColors.gates.getValue(it) },
            ),
        )
    }
    add(BoardItem.Heading(""))
    val discards = session.state.discard.size
    add(BoardItem.Deck(DISCARD_PILE, "Discard", if (discards == 1) "1 card" else "$discards cards", ArkhamColors.discard))
}
