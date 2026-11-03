package com.julesmeister.deutschcraft.feature.mission.presentation.guidance

import com.julesmeister.deutschcraft.feature.settings.domain.UserSettings
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MissionSupportVisibilityTest {
    private val state = MissionGuidanceUiState(
        missionId = "mission",
        objectiveId = "objective",
        isVisible = true,
        coachingHints = listOf("First hint", "Second hint"),
        revealedHints = listOf("First hint"),
        targetVocabulary = listOf("Wasser"),
        examplePhrases = listOf("Ein Wasser, bitte."),
        isVocabularyVisible = true,
        isExamplesVisible = true
    )

    @Test
    fun everyPreferenceCombinationControlsOnlyItsSupportType() {
        for (hints in listOf(false, true)) {
            for (words in listOf(false, true)) {
                for (examples in listOf(false, true)) {
                    val settings = UserSettings(
                        showMissionHints = hints,
                        showTargetVocabulary = words,
                        showExamplePhrases = examples
                    )
                    val visibility = state.supportVisibility(settings)
                    assertEquals(hints, visibility.hints)
                    assertEquals(words, visibility.vocabulary)
                    assertEquals(examples, visibility.examples)
                    assertEquals(hints || words || examples, visibility.hasSupport)
                }
            }
        }
        assertEquals(listOf("First hint"), state.revealedHints)
        assertEquals(1, state.revealedHintCount)
        assertEquals(MissionAssistanceLevel.GENTLE, state.assistanceLevel)
        assertEquals(1, state.nextHintIndex)
        assertTrue(state.hasActiveMission)
        assertTrue(state.isVisible)
    }

    @Test
    fun restoringPreferencesRestoresContentWithoutNewAssistance() {
        val hidden = UserSettings(
            showMissionHints = false,
            showTargetVocabulary = false,
            showExamplePhrases = false
        )
        assertFalse(state.supportVisibility(hidden).hasSupport)
        assertTrue(state.supportVisibility(UserSettings()).hasSupport)
        assertEquals(1, state.revealedHintCount)
        assertTrue(state.isVocabularyVisible)
        assertTrue(state.isExamplesVisible)
    }

    @Test
    fun emptySupportDoesNotShowEmptyControls() {
        val visibility = MissionGuidanceUiState().supportVisibility(UserSettings())
        assertFalse(visibility.hints)
        assertFalse(visibility.vocabulary)
        assertFalse(visibility.examples)
        assertFalse(visibility.hasSupport)
    }
}
