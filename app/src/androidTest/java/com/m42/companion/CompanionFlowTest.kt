package com.m42.companion

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onLast
import androidx.test.core.app.ActivityScenario
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

/** End-to-end smoke tests on a device: start a game, resolve an encounter, find it in the discard pile. */
@RunWith(AndroidJUnit4::class)
class CompanionFlowTest {
    @get:Rule
    val rule = createEmptyComposeRule()

    private lateinit var scenario: ActivityScenario<MainActivity>

    @Before
    fun startWithoutSavedGames() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        context.container.setupPreferences.edit().clear().commit()
        context.container.eldritchStore.end()
        context.container.arkhamStore.end()
        File(context.filesDir, "eldritch-game.json").delete()
        File(context.filesDir, "arkham-game.json").delete()
        scenario = ActivityScenario.launch(MainActivity::class.java)
    }

    @After
    fun close() = scenario.close()

    private fun grid() = rule.onNodeWithTag("deck-grid")

    @Test
    fun eldritchGateEncounterGoesToTheDiscardPile() {
        rule.onNodeWithText("Eldritch Horror").performClick()
        rule.waitUntil(5_000) { rule.onAllNodesWithText("Start").fetchSemanticsNodes().isNotEmpty() }
        rule.onNodeWithText("Start").performClick()
        rule.onNodeWithText("Gate").performClick()
        rule.onNodeWithText("Card 1 of 24").assertIsDisplayed()
        rule.onNodeWithText("Fail").performScrollTo().performClick()
        grid().performScrollToNode(hasText("Discard"))
        rule.onNodeWithText("1 card").assertIsDisplayed()
        rule.onNodeWithText("Discard").performClick()
        rule.onNodeWithText("Discard – Gate").assertIsDisplayed()
        rule.onNodeWithContentDescription("Return card to its deck").performClick()
        grid().performScrollToNode(hasText("0 cards"))
    }

    @Test
    fun arkhamLocationEncounterAndEndGame() {
        rule.onNodeWithText("Arkham Horror").performClick()
        rule.waitUntil(5_000) { rule.onAllNodesWithText("Start").fetchSemanticsNodes().isNotEmpty() }
        rule.onNodeWithText("Start").performClick()
        grid().performScrollToNode(hasText("Woods"))
        rule.onNodeWithText("Woods").performClick()
        rule.onNodeWithText("Card 1 of 7").assertIsDisplayed()
        rule.onNodeWithContentDescription("Discard card").performClick()
        grid().performScrollToNode(hasText("1 card"))
        rule.onNodeWithText("1 card").assertIsDisplayed()
        rule.onNodeWithContentDescription("More").performClick()
        rule.onNodeWithText("End game").performClick()
        rule.onAllNodesWithText("End game").onLast().performClick()
        rule.onNodeWithText("Mythos Companion").assertIsDisplayed()
    }
}
