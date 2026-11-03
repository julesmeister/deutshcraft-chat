package com.julesmeister.deutschcraft.feature.chat.presentation.component

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeDown
import androidx.compose.ui.test.swipeLeft
import com.julesmeister.deutschcraft.feature.mission.data.ai.MissionGrammar
import com.julesmeister.deutschcraft.feature.mission.data.ai.MissionLanguageSupport
import com.julesmeister.deutschcraft.feature.mission.data.ai.MissionVocabulary
import com.julesmeister.deutschcraft.feature.mission.presentation.guidance.MissionGuidanceUiState
import com.julesmeister.deutschcraft.feature.settings.domain.UserSettings
import com.julesmeister.deutschcraft.feature.settings.presentation.LocalUserSettings
import org.junit.Rule
import org.junit.Test

class MissionTopBarExpansionTest {
    @get:Rule val compose = createComposeRule()

    @Test
    fun handleBelowTopBarTogglesAndDragsDrawer() {
        var expanded by mutableStateOf(false)
        compose.setContent {
            CompositionLocalProvider(LocalUserSettings provides UserSettings(reduceAnimations = true)) {
                MaterialTheme {
                    val drawerState = rememberMissionDrawerState()
                    Column {
                        ChatTopBar(
                            isAssistantTyping = false,
                            isAssessing = false,
                            onBackClick = {},
                            activeMissionTitle = "A restaurant mission",
                            currentMissionObjective = "Order a meal",
                            isMissionExpanded = expanded
                        )
                        MissionTopBarDrawer(
                            state = drawerState,
                            guidance = MissionGuidanceUiState(
                                missionId = "mission",
                                objectiveId = "goal",
                                objectiveTitle = "Order a meal"
                            ),
                            expanded = expanded,
                            onExpand = { expanded = true },
                            onCollapse = { expanded = false },
                            onRevealNextHint = {},
                            onRetryLanguageSupport = {}
                        )
                    }
                }
            }
        }
        compose.onNodeWithContentDescription("Expand mission details").performClick()
        compose.onNodeWithText("Mission details").assertIsDisplayed()
        compose.onNodeWithContentDescription("Back").assertIsDisplayed()
        compose.onNodeWithContentDescription("Collapse mission details").performClick()
        compose.onNodeWithText("Mission details").assertDoesNotExist()
        compose.onNodeWithContentDescription("Expand mission details").performTouchInput { swipeDown(startY = centerY, endY = centerY + 600f) }
        compose.onNodeWithText("Mission details").assertIsDisplayed()
    }

    @Test
    fun expandedPagerIgnoresSwipesAndResumesAfterCollapse() {
        var expanded by mutableStateOf(true)
        compose.setContent {
            CompositionLocalProvider(LocalUserSettings provides UserSettings(reduceAnimations = true)) {
                MaterialTheme {
                    MissionTopBarPager(
                        missionTitle = "A restaurant mission",
                        currentObjective = "Order a meal",
                        currentGuidance = "Ask politely",
                        missionProgressPercentage = 20,
                        onMissionGuidanceClick = {},
                        isExpanded = expanded,
                        modifier = Modifier.testTag("mission-pager")
                    )
                }
            }
        }
        compose.onNodeWithTag("mission-pager").performTouchInput { swipeLeft() }
        compose.onNodeWithText("A restaurant mission").assertIsDisplayed()
        compose.runOnIdle { expanded = false }
        compose.onNodeWithTag("mission-pager").performTouchInput { swipeLeft() }
        compose.onNodeWithText("Order a meal").assertIsDisplayed()
    }

    @Test
    fun disabledSupportSettingsHideGeneratedVocabularyAndGrammar() {
        compose.setContent {
            CompositionLocalProvider(
                LocalUserSettings provides UserSettings(showTargetVocabulary = false, showExamplePhrases = false)
            ) {
                MaterialTheme {
                    MissionTopBarDetails(
                        state = MissionGuidanceUiState(
                            objectiveTitle = "Order a meal",
                            languageSupport = MissionLanguageSupport(
                                listOf(MissionVocabulary("die Speisekarte", "menu", "Die Speisekarte, bitte.")),
                                listOf(MissionGrammar("Polite requests", "Use bitte.", "Ein Wasser, bitte."))
                            )
                        ),
                        onRevealNextHint = {},
                        onRetryLanguageSupport = {}
                    )
                }
            }
        }
        compose.onNodeWithText("Useful vocabulary").assertDoesNotExist()
        compose.onNodeWithText("Grammar for this mission").assertDoesNotExist()
        compose.onNodeWithText("Order a meal").assertIsDisplayed()
    }
}
