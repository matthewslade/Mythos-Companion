package com.m42.companion.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.zIndex
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.m42.companion.engine.CardFace
import com.m42.companion.engine.Encountered
import com.m42.companion.engine.Gallery
import kotlin.math.absoluteValue

/** Callbacks for the actions available on a gallery; each one closes the gallery afterwards. */
class GalleryActions(
    val onResolve: (CardFace, Encountered) -> Unit,
    val onDiscard: (CardFace) -> Unit,
    val onShuffle: () -> Unit,
    val onReturnToDeck: (CardFace) -> Unit,
    val onClose: () -> Unit,
)

/**
 * Swipe through a deck (top card first) or the discard pile (most recent first), mirroring the original
 * DeckGallery / DiscardGallery screens including their depth page transition.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GalleryPane(
    gallery: Gallery,
    actions: GalleryActions,
    modifier: Modifier = Modifier,
    /** Two-pane layouts close the pane rather than navigate back. */
    closeIcon: Boolean = false,
    textScale: Float = 1f,
) {
    val pager = rememberPagerState { gallery.faces.size }
    val current = gallery.faces[pager.currentPage.coerceIn(gallery.faces.indices)]
    Column(modifier) {
        TopAppBar(
            title = {
                Column {
                    Text(current.pageTitle, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(
                        "Card ${pager.currentPage + 1} of ${gallery.faces.size}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            },
            navigationIcon = {
                IconButton(onClick = actions.onClose) {
                    if (closeIcon) Icon(Icons.Default.Close, contentDescription = "Close")
                    else Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            },
            actions = {
                if (gallery.isDiscardPile) {
                    IconButton(onClick = { actions.onReturnToDeck(current) }) {
                        Icon(Icons.AutoMirrored.Filled.Undo, contentDescription = "Return card to its deck")
                    }
                } else {
                    IconButton(onClick = actions.onShuffle) {
                        Icon(Icons.Default.Shuffle, contentDescription = "Shuffle deck")
                    }
                    IconButton(onClick = { actions.onDiscard(current) }) {
                        Icon(Icons.Default.DeleteSweep, contentDescription = "Discard card")
                    }
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface),
        )
        gallery.banner?.let {
            Surface(
                color = MaterialTheme.colorScheme.secondaryContainer,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                shape = MaterialTheme.shapes.medium,
            ) {
                Text(it, modifier = Modifier.padding(12.dp), style = MaterialTheme.typography.bodyMedium)
            }
        }
        Text(
            when {
                gallery.isDiscardPile -> "Swipe to browse the discard pile."
                else -> "Tap the encounter you resolved to discard the card. Swipe to look further into the deck."
            },
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 4.dp),
        )
        HorizontalPager(
            state = pager,
            modifier = Modifier.fillMaxSize(),
            beyondViewportPageCount = 1,
        ) { page ->
            val face = gallery.faces[page]
            Box(
                // Earlier pages draw on top (like ViewPager's reverse drawing order), so the card waiting behind
                // the current one never receives its taps.
                Modifier.fillMaxSize().zIndex(-page.toFloat()).depthTransition(pager, page).padding(16.dp)
                    // Only the card on screen is exposed to accessibility services.
                    .then(if (page == pager.currentPage) Modifier else Modifier.clearAndSetSemantics {}),
                contentAlignment = Alignment.TopCenter,
            ) {
                Column(
                    Modifier.widthIn(max = 720.dp).fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    face.tag?.let {
                        Text(it, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.secondary)
                    }
                    EncounterCard(
                        face = face,
                        onSection = if (gallery.isDiscardPile) null else { section -> actions.onResolve(face, section) },
                        modifier = Modifier.fillMaxWidth(),
                        textScale = textScale,
                    )
                }
            }
        }
    }
}

/** The DepthPageTransformer from the original app: the next card rises from behind the current one. */
private fun Modifier.depthTransition(pager: PagerState, page: Int) = graphicsLayer {
    val position = (page - pager.currentPage) - pager.currentPageOffsetFraction
    if (position > 0f && position <= 1f) {
        alpha = 1f - position
        translationX = size.width * -position
        val scale = 0.75f + 0.25f * (1f - position.absoluteValue)
        scaleX = scale
        scaleY = scale
    }
}
