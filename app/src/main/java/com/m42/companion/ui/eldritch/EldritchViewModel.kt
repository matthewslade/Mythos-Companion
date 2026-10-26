package com.m42.companion.ui.eldritch

import com.m42.companion.AppContainer
import com.m42.companion.eldritch.AncientOne
import com.m42.companion.eldritch.EldritchExpansion
import com.m42.companion.eldritch.EldritchGame
import com.m42.companion.eldritch.EldritchOption
import com.m42.companion.eldritch.EldritchSession
import com.m42.companion.eldritch.EldritchSetup
import com.m42.companion.engine.CardFace
import com.m42.companion.engine.DISCARD_PILE
import com.m42.companion.engine.Encountered
import com.m42.companion.ui.GameViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import androidx.core.content.edit
import kotlinx.coroutines.flow.updateAndGet

class EldritchViewModel(container: AppContainer) :
    GameViewModel<EldritchGame, EldritchSession>(container::eldritch, container.eldritchStore) {

    private val prefs = container.setupPreferences
    private val _setup = MutableStateFlow(loadSetup())
    val setup: StateFlow<EldritchSetup> = _setup.asStateFlow()

    fun toggle(expansion: EldritchExpansion) = changeSetup { it.toggle(expansion) }

    fun toggle(option: EldritchOption) = changeSetup { it.toggle(option) }

    fun choose(ancientOne: AncientOne?) = changeSetup { it.copy(ancientOne = ancientOne) }

    private fun changeSetup(transform: (EldritchSetup) -> EldritchSetup) {
        val setup = _setup.updateAndGet(transform)
        prefs.edit {
            putStringSet(EXPANSIONS, setup.expansions.map { it.id }.toSet())
            putStringSet(OPTIONS, setup.options.map { it.id }.toSet())
            putString(ANCIENT_ONE, setup.ancientOne?.id)
        }
    }

    private fun loadSetup(): EldritchSetup {
        val expansionIds = prefs.getStringSet(EXPANSIONS, null) ?: return EldritchSetup()
        val expansions = EldritchExpansion.entries.filter { it.id in expansionIds }.toSet()
        val optionIds = prefs.getStringSet(OPTIONS, emptySet()).orEmpty()
        val options = EldritchOption.entries.filter { it.id in optionIds && it.parent in expansions }.toSet()
        val ancientOne = prefs.getString(ANCIENT_ONE, null)
            ?.let { id -> expansions.flatMap { it.ancientOnes }.firstOrNull { it.id == id } }
        return EldritchSetup(expansions, options, ancientOne)
    }

    /** False when no expansion is selected. */
    fun startGame(): Boolean {
        val game = game.value ?: return false
        val config = _setup.value.toConfig(random) ?: return false
        closeGallery()
        store.start(game.newSession(config, random))
        return true
    }

    fun removeExpedition(location: String) = act { g, s -> g.removeExpedition(s, location, random) }

    override fun isValid(game: EldritchGame, session: EldritchSession) = game.isValid(session)

    override fun deckName(deckId: String): String {
        if (deckId == DISCARD_PILE) return "Discard"
        val game = game.value ?: return deckId
        val session = session.value ?: return deckId
        return game.deckName(session, deckId)
    }

    override fun open(game: EldritchGame, session: EldritchSession, deckId: String) = game.open(session, deckId, random)

    override fun resolve(game: EldritchGame, session: EldritchSession, face: CardFace, section: Encountered) =
        game.resolve(session, face, section)

    override fun discard(game: EldritchGame, session: EldritchSession, face: CardFace) = game.discard(session, face)

    override fun shuffle(game: EldritchGame, session: EldritchSession, deckId: String) =
        game.shuffle(session, deckId, random)

    override fun returnToDeck(game: EldritchGame, session: EldritchSession, face: CardFace) =
        game.returnToDeck(session, face, random)

    private companion object {
        const val EXPANSIONS = "eldritch.expansions"
        const val OPTIONS = "eldritch.options"
        const val ANCIENT_ONE = "eldritch.ancientOne"
    }
}
