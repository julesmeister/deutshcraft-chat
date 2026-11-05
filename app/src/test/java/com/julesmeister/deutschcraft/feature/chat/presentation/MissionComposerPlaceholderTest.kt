package com.julesmeister.deutschcraft.feature.chat.presentation

import androidx.compose.ui.text.input.TextFieldValue
import com.julesmeister.deutschcraft.feature.mission.domain.ConversationMission
import com.julesmeister.deutschcraft.feature.mission.domain.MissionDifficulty
import com.julesmeister.deutschcraft.feature.mission.domain.MissionObjective
import com.julesmeister.deutschcraft.feature.mission.domain.MissionObjectiveProgress
import com.julesmeister.deutschcraft.feature.mission.domain.MissionStatus
import com.julesmeister.deutschcraft.feature.mission.domain.MissionTemplate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MissionComposerPlaceholderTest {
    private val first = MissionObjective(
        id = "first", missionTemplateId = "template", title = "Order",
        description = "Ask the waiter for a meal and a drink.",
        sequenceNumber = 1, isRequired = true, completionHint = "Ordered",
        coachingHints = listOf("Use bitte.")
    )
    private val second = first.copy(
        id = "second", title = "Pay", description = "Ask for the bill politely.", sequenceNumber = 2
    )
    private val mission = ConversationMission(
        id = "mission", conversationId = "conversation",
        template = MissionTemplate(
            id = "template", title = "Restaurant", description = "Practice ordering.",
            scenario = "A restaurant", learnerRole = "Guest", assistantRole = "Waiter",
            difficulty = MissionDifficulty.A1, icon = "", objectives = listOf(first, second),
            isEnabled = true
        ),
        status = MissionStatus.ACTIVE,
        objectiveProgress = listOf(first, second).map {
            MissionObjectiveProgress(it, false, null, null, 0f)
        },
        startedAt = 0, completedAt = null, dismissedAt = null, createdAt = 0, updatedAt = 0
    )

    @Test
    fun usesFullDescriptionNotTitleOrHintAndDoesNotModifyDraft() {
        val state = ChatUiState(messageInput = TextFieldValue("My own answer"))
            .withActiveMission(mission, null)

        assertEquals(first.description, state.currentMissionObjectiveDescription)
        assertEquals(first.title, state.currentMissionObjective)
        assertEquals("Use bitte.", state.currentMissionGuidance)
        assertEquals("My own answer", state.messageInput.text)
    }

    @Test
    fun completionAdvancesPlaceholderAndRemovingMissionClearsIt() {
        val progressed = mission.copy(
            objectiveProgress = mission.objectiveProgress.map {
                if (it.objective.id == first.id) it.copy(isCompleted = true) else it
            }
        )
        val state = ChatUiState().withActiveMission(progressed, null)
        assertEquals(second.description, state.currentMissionObjectiveDescription)
        assertNull(state.withActiveMission(null, null).currentMissionObjectiveDescription)
        val completed = progressed.copy(
            objectiveProgress = progressed.objectiveProgress.map { it.copy(isCompleted = true) }
        )
        assertNull(state.withActiveMission(completed, null).currentMissionObjectiveDescription)
    }
}
