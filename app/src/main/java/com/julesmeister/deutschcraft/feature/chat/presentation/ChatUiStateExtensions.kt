package com.julesmeister.deutschcraft.feature.chat.presentation

import androidx.compose.ui.text.input.TextFieldValue
import com.julesmeister.deutschcraft.feature.chat.domain.ChatMessage
import com.julesmeister.deutschcraft.feature.chat.domain.ReplySuggestion
import com.julesmeister.deutschcraft.feature.mission.domain.ConversationMission
import com.julesmeister.deutschcraft.feature.mission.domain.MissionObjectiveProgress
import com.julesmeister.deutschcraft.feature.mission.domain.generated.MissionOpportunity

/*
 * ===========================================================
 * INITIALIZATION
 * ===========================================================
 */

internal fun ChatUiState.initializationSucceeded():
        ChatUiState {

    return copy(
        isInitializing =
            false,

        errorMessage =
            null
    )
}

internal fun ChatUiState.initializationFailed(
    message: String
): ChatUiState {

    return copy(
        isInitializing =
            false,

        isSending =
            false,

        isAssistantTyping =
            false,

        isAssessing =
            false,

        isCheckingMissionOpportunity =
            false,

        isStartingMission =
            false,

        isSubmittingBesserRefinement =
            false,

        errorMessage =
            message
    )
}

/*
 * ===========================================================
 * MESSAGES
 * ===========================================================
 */

internal fun ChatUiState.withMessages(
    messages: List<ChatMessage>
): ChatUiState {

    return copy(
        messages =
            messages
    )
}

/*
 * ===========================================================
 * COMPOSER INPUT
 * ===========================================================
 */

internal fun ChatUiState.withInput(
    value: TextFieldValue
): ChatUiState {

    return copy(
        messageInput =
            value
    )
}

/*
 * ===========================================================
 * NORMAL MESSAGE SEND
 * ===========================================================
 */

internal fun ChatUiState.sendingStarted():
        ChatUiState {

    return copy(
        messageInput =
            TextFieldValue(),

        suggestions =
            emptyList(),

        isSending =
            true,

        isAssistantTyping =
            true,

        isAssessing =
            true,

        errorMessage =
            null
    )
}

internal fun ChatUiState.sendingSucceeded(
    suggestions: List<ReplySuggestion>,
    warning: String?
): ChatUiState {

    return copy(
        suggestions =
            suggestions,

        isSending =
            false,

        isAssistantTyping =
            false,

        isAssessing =
            false,

        errorMessage =
            warning
    )
}

internal fun ChatUiState.sendingFailed(
    message: String
): ChatUiState {

    return copy(
        isSending =
            false,

        isAssistantTyping =
            false,

        isAssessing =
            false,

        isCheckingMissionOpportunity =
            false,

        errorMessage =
            message
    )
}

/*
 * ===========================================================
 * BESSER REFINEMENT
 * ===========================================================
 */

/*
 * Enters Besser refinement mode.
 *
 * The learner's original sentence is placed in the composer so
 * the learner can rewrite it directly.
 *
 * Ordinary reply suggestions are removed because they belong
 * to the normal conversation flow, not refinement.
 */
internal fun ChatUiState.besserRefinementStarted(
    refinement: BesserRefinementUiState,
    input: TextFieldValue
): ChatUiState {

    return copy(
        activeBesserRefinement =
            refinement,

        messageInput =
            input,

        suggestions =
            emptyList(),

        isSubmittingBesserRefinement =
            false,

        errorMessage =
            null
    )
}

/*
 * Leaves refinement mode without skipping the persisted
 * challenge.
 *
 * The Besser challenge therefore remains ACCEPTED and the
 * learner can tap Verbessern again later.
 */
internal fun ChatUiState.besserRefinementCancelled():
        ChatUiState {

    return copy(
        activeBesserRefinement =
            null,

        messageInput =
            TextFieldValue(),

        isSubmittingBesserRefinement =
            false,

        errorMessage =
            null
    )
}

/*
 * Starts submission of the rewritten sentence.
 *
 * Refinement submission is deliberately separate from an
 * ordinary chat send.
 *
 * We therefore do NOT:
 *
 * - show assistant typing
 * - run normal assessment UI
 * - clear the rewrite before completion succeeds
 * - start another ordinary conversation turn
 */
internal fun ChatUiState.besserRefinementSubmissionStarted():
        ChatUiState {

    if (
        activeBesserRefinement ==
        null ||
        isSubmittingBesserRefinement
    ) {
        return this
    }

    return copy(
        isSubmittingBesserRefinement =
            true,

        isAssistantTyping =
            false,

        isAssessing =
            false,

        errorMessage =
            null
    )
}

/*
 * The rewritten sentence has been successfully persisted and
 * the Besser challenge reached COMPLETED.
 *
 * Exit refinement mode and clear the composer.
 */
internal fun ChatUiState.besserRefinementSubmissionSucceeded():
        ChatUiState {

    return copy(
        activeBesserRefinement =
            null,

        messageInput =
            TextFieldValue(),

        isSubmittingBesserRefinement =
            false,

        isSending =
            false,

        isAssistantTyping =
            false,

        isAssessing =
            false,

        errorMessage =
            null
    )
}

/*
 * A rejected or failed refinement keeps both:
 *
 * - activeBesserRefinement
 * - messageInput
 *
 * intact.
 *
 * This is important because the learner should be able to edit
 * the sentence again instead of losing the attempted rewrite.
 */
internal fun ChatUiState.besserRefinementSubmissionFailed(
    message: String
): ChatUiState {

    return copy(
        isSubmittingBesserRefinement =
            false,

        isSending =
            false,

        isAssistantTyping =
            false,

        isAssessing =
            false,

        errorMessage =
            message
    )
}

/*
 * ===========================================================
 * MISSION OPPORTUNITY
 * ===========================================================
 */

internal fun ChatUiState.missionCheckStarted():
        ChatUiState {

    if (
        activeMissionId !=
        null ||
        missionOffer !=
        null
    ) {
        return this
    }

    return copy(
        isCheckingMissionOpportunity =
            true
    )
}

internal fun ChatUiState.missionCheckFinished(
    offer: MissionOpportunity?
): ChatUiState {

    return copy(
        missionOffer =
            if (
                activeMissionId ==
                null
            ) {
                offer
            } else {
                null
            },

        isCheckingMissionOpportunity =
            false
    )
}

internal fun ChatUiState.missionOfferDismissed():
        ChatUiState {

    return copy(
        missionOffer =
            null,

        isCheckingMissionOpportunity =
            false
    )
}

/*
 * ===========================================================
 * MISSION START
 * ===========================================================
 */

internal fun ChatUiState.missionStartStarted():
        ChatUiState {

    if (
        missionOffer ==
        null ||
        isStartingMission
    ) {
        return this
    }

    return copy(
        isStartingMission =
            true,

        errorMessage =
            null
    )
}

internal fun ChatUiState.missionStartSucceeded():
        ChatUiState {

    return copy(
        missionOffer =
            null,

        isStartingMission =
            false,

        isCheckingMissionOpportunity =
            false
    )
}

internal fun ChatUiState.missionStartFailed(
    message: String
): ChatUiState {

    return copy(
        isStartingMission =
            false,

        errorMessage =
            message
    )
}

/*
 * ===========================================================
 * STREAK
 * ===========================================================
 */

internal fun ChatUiState.withCurrentStreak(
    streakDays: Int
): ChatUiState {

    return copy(
        currentStreak =
            streakDays
                .coerceAtLeast(
                    0
                )
    )
}

/*
 * ===========================================================
 * ACTIVE MISSION
 * ===========================================================
 */

internal fun ChatUiState.withActiveMission(
    mission: ConversationMission?,
    newlyCompletedObjective:
    MissionObjectiveProgress?
): ChatUiState {

    if (mission == null) {
        return copy(
            activeMissionId =
                null,

            activeMissionTitle =
                null,

            currentMissionObjective =
                null,

            currentMissionGuidance =
                null,

            missionProgressPercentage =
                0
        )
    }

    val currentObjective =
        mission
            .currentObjective
            ?.objective

    val completionEvent =
        newlyCompletedObjective
            ?.let { progress ->
                val completionTimestamp =
                    progress.completedAt
                        ?: System.currentTimeMillis()

                MissionObjectiveCompletionEvent(
                    eventId =
                        "${mission.id}:" +
                                "${progress.objective.id}:" +
                                completionTimestamp,

                    objectiveId =
                        progress.objective.id,

                    objectiveTitle =
                        progress.objective.title
                )
            }

    return copy(
        missionOffer =
            null,

        isCheckingMissionOpportunity =
            false,

        isStartingMission =
            false,

        activeMissionId =
            mission.id,

        activeMissionTitle =
            mission.template.title,

        currentMissionObjective =
            currentObjective
                ?.title
                ?.trim()
                ?.takeIf(
                    String::isNotBlank
                ),

        currentMissionGuidance =
            currentObjective
                ?.description
                ?.trim()
                ?.takeIf(
                    String::isNotBlank
                ),

        missionProgressPercentage =
            mission
                .progress
                .percentage
                .coerceIn(
                    minimumValue =
                        0,

                    maximumValue =
                        100
                ),

        objectiveCompletionEvent =
            completionEvent
                ?: objectiveCompletionEvent
    )
}

/*
 * ===========================================================
 * OBJECTIVE COMPLETION EVENT
 * ===========================================================
 */

internal fun ChatUiState.objectiveCompletionShown(
    eventId: String
): ChatUiState {

    val activeEvent =
        objectiveCompletionEvent

    if (
        activeEvent ==
        null ||
        activeEvent.eventId !=
        eventId
    ) {
        return this
    }

    return copy(
        objectiveCompletionEvent =
            null
    )
}

/*
 * ===========================================================
 * ERROR
 * ===========================================================
 */

internal fun ChatUiState.errorDismissed():
        ChatUiState {

    return copy(
        errorMessage =
            null
    )
}