package com.m42.companion.ui.arkham

import com.m42.companion.AppContainer
import com.m42.companion.arkham.ArkhamConfig
import com.m42.companion.arkham.ArkhamGame
import com.m42.companion.arkham.ArkhamSession
import com.m42.companion.engine.CardFace
import com.m42.companion.engine.DISCARD_PILE
import com.m42.companion.engine.Encountered
import com.m42.companion.ui.GameViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import androidx.core.content.edit
import kotlinx.coroutines.flow.updateAndGet

class ArkhamViewModel(container: AppContainer) :
    GameViewModel<ArkhamGame, ArkhamSession>(container::arkham, container.arkhamStore) {

    private val prefs = container.setupPreferences
    private val _expansions = MutableStateFlow(prefs.getStringSet(EXPANSIONS, emptySet()).orEmpty().toSet())
    val expansions: StateFlow<Set<String>> = _expansions.asStateFlow()

    fun toggle(code: String) {
        val selected = _expansions.updateAndGet { if (code in it) it - code else it + code }
        prefs.edit { putStringSet(EXPANSIONS, selected) }
    }

    fun startGame() {
        val game = game.value ?: return
        closeGallery()
        store.start(game.newSession(ArkhamConfig(_expansions.value), random))
    }

    override fun deckName(deckId: String): String =
        if (deckId == DISCARD_PILE) "Discard" else game.value?.deckName(deckId) ?: deckId

    override fun open(game: ArkhamGame, session: ArkhamSession, deckId: String) = game.open(session, deckId, random)

    override fun resolve(game: ArkhamGame, session: ArkhamSession, face: CardFace, section: Encountered) =
        game.resolve(session, face, section, random)

    override fun discard(game: ArkhamGame, session: ArkhamSession, face: CardFace) = game.discard(session, face, random)

    override fun isValid(game: ArkhamGame, session: ArkhamSession) = game.isValid(session)

    override fun shuffle(game: ArkhamGame, session: ArkhamSession, deckId: String) = game.shuffle(session, deckId, random)

    override fun returnToDeck(game: ArkhamGame, session: ArkhamSession, face: CardFace) =
        game.returnToDeck(session, face, random)

    private companion object {
        const val EXPANSIONS = "arkham.expansions"
    }
}
