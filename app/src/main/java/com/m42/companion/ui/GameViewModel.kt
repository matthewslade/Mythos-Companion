package com.m42.companion.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.m42.companion.engine.CardFace
import com.m42.companion.engine.Encountered
import com.m42.companion.engine.Gallery
import com.m42.companion.store.GameStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.random.Random

/**
 * Shared state for one game: its card data [G], the saved session [S], and the gallery currently open.
 * Every card action closes the gallery, as the original app returned to the main screen after each one.
 */
abstract class GameViewModel<G : Any, S : Any>(
    loadGame: suspend () -> G,
    protected val store: GameStore<S>,
) : ViewModel() {
    protected val random: Random = Random.Default

    private val _game = MutableStateFlow<G?>(null)
    val game: StateFlow<G?> = _game.asStateFlow()

    /** True once the card data is parsed and any saved game has been read back. */
    private val _ready = MutableStateFlow(false)
    val ready: StateFlow<Boolean> = _ready.asStateFlow()

    val session: StateFlow<S?> = store.session

    private val _gallery = MutableStateFlow<Gallery?>(null)
    val gallery: StateFlow<Gallery?> = _gallery.asStateFlow()

    init {
        viewModelScope.launch {
            val game = loadGame()
            _game.value = game
            // A save that no longer matches the bundled card data cannot be continued.
            store.restore()?.let { if (!isValid(game, it)) store.end() }
            _ready.value = true
        }
    }

    fun hasSavedGame() = store.hasSavedGame()

    fun endGame() {
        _gallery.value = null
        store.end()
    }

    /** Opens [deckId] in the gallery; false when there is nothing to show. */
    fun openGallery(deckId: String): Boolean {
        val game = _game.value ?: return false
        var opened: Gallery? = null
        store.update { session ->
            val (next, gallery) = open(game, session, deckId)
            opened = gallery
            next
        }
        _gallery.value = opened
        return opened != null
    }

    fun closeGallery() {
        _gallery.value = null
    }

    fun resolve(face: CardFace, section: Encountered) = act { g, s -> resolve(g, s, face, section) }

    fun discard(face: CardFace) = act { g, s -> discard(g, s, face) }

    fun shuffleOpenDeck() {
        val deckId = _gallery.value?.deckId ?: return
        act { g, s -> shuffle(g, s, deckId) }
    }

    fun returnToDeck(face: CardFace) = act { g, s -> returnToDeck(g, s, face) }

    abstract fun deckName(deckId: String): String

    protected fun update(transform: (G, S) -> S) {
        val game = _game.value ?: return
        store.update { transform(game, it) }
    }

    /** Applies a change and closes the gallery, whose cards may no longer match the decks. */
    protected fun act(transform: (G, S) -> S) {
        update(transform)
        closeGallery()
    }

    protected abstract fun isValid(game: G, session: S): Boolean
    protected abstract fun open(game: G, session: S, deckId: String): Pair<S, Gallery?>
    protected abstract fun resolve(game: G, session: S, face: CardFace, section: Encountered): S
    protected abstract fun discard(game: G, session: S, face: CardFace): S
    protected abstract fun shuffle(game: G, session: S, deckId: String): S
    protected abstract fun returnToDeck(game: G, session: S, face: CardFace): S
}
