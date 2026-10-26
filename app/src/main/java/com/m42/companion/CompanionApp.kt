package com.m42.companion

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import com.m42.companion.arkham.ArkhamGame
import com.m42.companion.arkham.ArkhamSession
import com.m42.companion.data.CardDataParser
import com.m42.companion.eldritch.EldritchGame
import com.m42.companion.eldritch.EldritchSession
import com.m42.companion.store.GameStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async

class CompanionApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this).also { it.preload() }
    }
}

/** App-wide singletons: the card data for each game and the saved sessions. */
class AppContainer(context: Context) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val assets = context.assets

    private val eldritchGame = scope.async(Dispatchers.IO, CoroutineStart.LAZY) {
        EldritchGame(CardDataParser.eldritch(assets.open("eldritch.json")))
    }
    private val arkhamGame = scope.async(Dispatchers.IO, CoroutineStart.LAZY) {
        ArkhamGame(CardDataParser.arkham(assets.open("arkham.json")))
    }

    /** Remembers the expansions chosen on each setup screen between games. */
    val setupPreferences: SharedPreferences = context.getSharedPreferences("setup", Context.MODE_PRIVATE)

    val eldritchStore = GameStore(scope, context.filesDir, "eldritch-game.json", EldritchSession.serializer())
    val arkhamStore = GameStore(scope, context.filesDir, "arkham-game.json", ArkhamSession.serializer())

    suspend fun eldritch(): EldritchGame = eldritchGame.await()

    suspend fun arkham(): ArkhamGame = arkhamGame.await()

    /** Parses the card data in the background while the first screen is drawn. */
    fun preload() {
        eldritchGame.start()
        arkhamGame.start()
    }
}

val Context.container: AppContainer get() = (applicationContext as CompanionApp).container
