package com.m42.companion

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.m42.companion.ui.HomeScreen
import com.m42.companion.ui.arkham.ArkhamBoardScreen
import com.m42.companion.ui.arkham.ArkhamSetupScreen
import com.m42.companion.ui.arkham.ArkhamViewModel
import com.m42.companion.ui.components.GalleryScreen
import com.m42.companion.ui.eldritch.EldritchBoardScreen
import com.m42.companion.ui.eldritch.EldritchSetupScreen
import com.m42.companion.ui.eldritch.EldritchViewModel
import com.m42.companion.ui.theme.CompanionTheme
import kotlinx.serialization.Serializable

@Serializable object Home
@Serializable object EldritchSetup
@Serializable object EldritchBoard
@Serializable object EldritchGallery
@Serializable object ArkhamSetup
@Serializable object ArkhamBoard
@Serializable object ArkhamGallery

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            CompanionTheme {
                CompanionNavHost()
            }
        }
    }
}

@Composable
private fun CompanionNavHost(nav: NavHostController = rememberNavController()) {
    val container = LocalContext.current.container
    // One view model per game for the whole activity, so setup, board and gallery share the open game.
    val eldritch: EldritchViewModel = viewModel { EldritchViewModel(container) }
    val arkham: ArkhamViewModel = viewModel { ArkhamViewModel(container) }
    val eldritchSession by eldritch.session.collectAsStateWithLifecycle()
    val arkhamSession by arkham.session.collectAsStateWithLifecycle()
    val eldritchReady by eldritch.ready.collectAsStateWithLifecycle()
    val arkhamReady by arkham.ready.collectAsStateWithLifecycle()

    fun backToHome() = nav.popBackStack(Home, inclusive = false)

    NavHost(nav, startDestination = Home) {
        composable<Home> {
            HomeScreen(
                eldritchInProgress = eldritchReady && eldritchSession != null,
                arkhamInProgress = arkhamReady && arkhamSession != null,
                onEldritch = { nav.navigate(EldritchSetup) },
                onArkham = { nav.navigate(ArkhamSetup) },
            )
        }
        composable<EldritchSetup> {
            EldritchSetupScreen(eldritch, onBack = { nav.popBackStack() }, onPlay = {
                nav.navigate(EldritchBoard) { launchSingleTop = true }
            })
        }
        composable<EldritchBoard> {
            EldritchBoardScreen(
                eldritch,
                onBack = { nav.popBackStack() },
                onOpenGallery = { nav.navigate(EldritchGallery) { launchSingleTop = true } },
                onNoGame = { backToHome() },
            )
        }
        composable<EldritchGallery> {
            GalleryScreen(eldritch, onClosed = { nav.popBackStack(EldritchBoard, inclusive = false) })
        }
        composable<ArkhamSetup> {
            ArkhamSetupScreen(arkham, onBack = { nav.popBackStack() }, onPlay = {
                nav.navigate(ArkhamBoard) { launchSingleTop = true }
            })
        }
        composable<ArkhamBoard> {
            ArkhamBoardScreen(
                arkham,
                onBack = { nav.popBackStack() },
                onOpenGallery = { nav.navigate(ArkhamGallery) { launchSingleTop = true } },
                onNoGame = { backToHome() },
            )
        }
        composable<ArkhamGallery> {
            GalleryScreen(arkham, onClosed = { nav.popBackStack(ArkhamBoard, inclusive = false) })
        }
    }
}
