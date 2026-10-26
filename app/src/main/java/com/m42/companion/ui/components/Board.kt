package com.m42.companion.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.VerticalDivider
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.m42.companion.engine.Gallery

/** Width from which the board shows the deck list and the open deck side by side. */
val TwoPaneWidth = 840.dp

/** One entry of a board: a section heading or a deck button. */
sealed interface BoardItem {
    val key: String

    data class Heading(val title: String) : BoardItem {
        override val key = "heading:$title"
    }

    data class Deck(
        val deckId: String,
        val label: String,
        val detail: String? = null,
        val color: Color,
        /** Gate colours for Arkham Other Worlds. */
        val dots: List<Color> = emptyList(),
        val secondaryAction: Pair<String, () -> Unit>? = null,
        /** Takes a whole row of the grid. */
        val wide: Boolean = false,
    ) : BoardItem {
        override val key = "deck:$deckId"
    }
}

/**
 * The main game screen: deck buttons, with the opened deck shown alongside on wide screens.
 * On narrow screens [onOpenDeck] is expected to navigate to a full-screen gallery.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameBoard(
    title: String,
    items: List<BoardItem>,
    minButtonWidth: Dp,
    gallery: Gallery?,
    galleryActions: GalleryActions,
    snackbar: SnackbarHostState,
    onOpenDeck: (deckId: String, twoPane: Boolean) -> Unit,
    onBack: () -> Unit,
    onEndGame: () -> Unit,
) {
    var menuOpen by remember { mutableStateOf(false) }
    var confirmEnd by rememberSaveable { mutableStateOf(false) }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") }
                },
                actions = {
                    IconButton(onClick = { menuOpen = true }) { Icon(Icons.Default.MoreVert, contentDescription = "More") }
                    DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                        DropdownMenuItem(text = { Text("End game") }, onClick = { menuOpen = false; confirmEnd = true })
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbar) },
    ) { padding ->
        BoxWithConstraints(Modifier.fillMaxSize().padding(padding).consumeWindowInsets(padding)) {
            val twoPane = maxWidth >= TwoPaneWidth
            val listWidth = maxWidth * 0.4f
            // The side pane closes with Back, and when the screen becomes too narrow to show it.
            BackHandler(enabled = twoPane && gallery != null, onBack = galleryActions.onClose)
            LaunchedEffect(twoPane) { if (!twoPane && gallery != null) galleryActions.onClose() }
            if (twoPane) {
                Row(Modifier.fillMaxSize()) {
                    DeckGrid(items, minButtonWidth, Modifier.width(listWidth).fillMaxHeight()) { onOpenDeck(it, true) }
                    VerticalDivider()
                    Box(Modifier.weight(1f).fillMaxHeight()) {
                        if (gallery != null) {
                            // A newly opened deck always starts at its top card.
                            key(gallery) { GalleryPane(gallery, galleryActions, closeIcon = true, textScale = 1.15f) }
                        } else {
                            EmptyPane()
                        }
                    }
                }
            } else {
                DeckGrid(items, minButtonWidth, Modifier.fillMaxSize()) { onOpenDeck(it, false) }
            }
        }
    }
    if (confirmEnd) {
        AlertDialog(
            onDismissRequest = { confirmEnd = false },
            title = { Text("End this game?") },
            text = { Text("The decks and discard pile will be cleared.") },
            confirmButton = { TextButton(onClick = { confirmEnd = false; onEndGame() }) { Text("End game") } },
            dismissButton = { TextButton(onClick = { confirmEnd = false }) { Text("Cancel") } },
        )
    }
}

@Composable
private fun DeckGrid(items: List<BoardItem>, minButtonWidth: Dp, modifier: Modifier, onOpen: (String) -> Unit) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minButtonWidth),
        modifier = modifier.testTag("deck-grid"),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(items, key = { it.key }, span = { if (it is BoardItem.Heading || (it is BoardItem.Deck && it.wide)) GridItemSpan(maxLineSpan) else GridItemSpan(1) }) { item ->
            when (item) {
                is BoardItem.Heading -> Text(
                    item.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 12.dp, bottom = 2.dp),
                )
                is BoardItem.Deck -> DeckButton(item) { onOpen(item.deckId) }
            }
        }
    }
}

@Composable
fun DeckButton(item: BoardItem.Deck, onClick: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Surface(
            onClick = onClick,
            color = item.color,
            contentColor = Color.White,
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier.weight(1f).heightIn(min = 56.dp),
        ) {
            Row(
                Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(Modifier.weight(1f)) {
                    Text(item.label, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    item.detail?.let {
                        Text(it, style = MaterialTheme.typography.bodySmall, modifier = Modifier.alpha(0.88f))
                    }
                }
                item.dots.forEach { dot ->
                    Spacer(Modifier.width(4.dp))
                    Box(Modifier.size(12.dp).background(dot, CircleShape))
                }
            }
        }
        item.secondaryAction?.let { (label, action) ->
            Surface(
                onClick = action,
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.secondaryContainer,
                modifier = Modifier.heightIn(min = 56.dp),
            ) {
                Box(Modifier.padding(horizontal = 16.dp).heightIn(min = 56.dp), contentAlignment = Alignment.Center) {
                    Text(label, style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
}

@Composable
private fun EmptyPane() {
    Column(
        Modifier.fillMaxSize().padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            "Choose a deck to draw an encounter.",
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/** Emblem used on the home and setup screens. */
@Composable
fun Emblem(resource: Int, size: Dp, modifier: Modifier = Modifier) {
    Image(painterResource(resource), contentDescription = null, modifier = modifier.size(size))
}

/** Constrains form-like screens to a readable width on tablets. */
@Composable
fun ReadableColumn(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Box(modifier.fillMaxWidth(), contentAlignment = Alignment.TopCenter) {
        Box(Modifier.widthIn(max = 640.dp).fillMaxWidth()) { content() }
    }
}
