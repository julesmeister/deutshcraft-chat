package com.julesmeister.deutschcraft.feature.threads.presentation.component

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.isPopup
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.espresso.Espresso.pressBack
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class ThreadTopBarMenuTest {
    @get:Rule val compose = createComposeRule()

    @Test
    fun lightMenuKeepsActionsAndDismissesFromEmbeddedTriggerAndBack() = verifyMenu(false)

    @Test
    fun darkMenuKeepsActionsAndDismissesFromEmbeddedTriggerAndBack() = verifyMenu(true)

    @Test
    fun menuTriggerStaysFixedWhenPopupOpensAndCloses() {
        compose.mainClock.autoAdvance = false
        compose.setContent {
            MaterialTheme {
                ThreadListTopBar(
                    streakDays = 1,
                    onProgressClick = {},
                    onNewConversationClick = {},
                    onSettingsClick = {}
                )
            }
        }
        val trigger = compose.onNodeWithContentDescription("More options")
        val closedX = trigger.fetchSemanticsNode().boundsInRoot.center.x
        trigger.performClick()
        compose.mainClock.advanceTimeBy(1_000)
        compose.onNodeWithText("QUICK ACTIONS").assertIsDisplayed()
        val openX = compose.onNode(
            hasContentDescription("More options") and !hasAnyAncestor(isPopup())
        ).fetchSemanticsNode().boundsInRoot.center.x
        assertEquals(closedX, openX, 1f)
        compose.onNode(
            hasContentDescription("More options") and hasAnyAncestor(isPopup())
        ).performClick()
        compose.mainClock.advanceTimeBy(1_000)
        compose.onNodeWithText("QUICK ACTIONS").assertDoesNotExist()
        assertEquals(
            closedX,
            compose.onNodeWithContentDescription("More options")
                .fetchSemanticsNode().boundsInRoot.center.x,
            1f
        )
    }

    private fun verifyMenu(dark: Boolean) {
        var newChats = 0
        var settings = 0
        compose.setContent {
            MaterialTheme(colorScheme = if (dark) darkColorScheme() else lightColorScheme()) {
                ThreadListTopBar(
                    streakDays = 1,
                    onProgressClick = {},
                    onNewConversationClick = { newChats++ },
                    onSettingsClick = { settings++ }
                )
            }
        }

        compose.onNodeWithContentDescription("More options").performClick()
        compose.onNodeWithText("QUICK ACTIONS").assertIsDisplayed()
        compose.onNode(
            hasContentDescription("More options") and hasAnyAncestor(isPopup())
        ).performClick()
        compose.onNodeWithText("QUICK ACTIONS").assertDoesNotExist()

        compose.onNodeWithContentDescription("More options").performClick()
        compose.onNodeWithText("New conversation").performClick()
        compose.onNodeWithText("QUICK ACTIONS").assertDoesNotExist()
        compose.runOnIdle { assertEquals(1, newChats) }

        compose.onNodeWithContentDescription("More options").performClick()
        compose.onNodeWithText("Settings").performClick()
        compose.onNodeWithText("QUICK ACTIONS").assertDoesNotExist()
        compose.runOnIdle { assertEquals(1, settings) }

        compose.onNodeWithContentDescription("More options").performClick()
        pressBack()
        compose.onNodeWithText("QUICK ACTIONS").assertDoesNotExist()
    }
}
