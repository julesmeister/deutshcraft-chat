// app/src/main/java/com/julesmeister/deutschcraft/feature/chat/presentation/ChatUiStateExtensions.kt

package com.julesmeister.deutschcraft.feature.chat.presentation

import androidx.compose.ui.text.input.TextFieldValue
import com.julesmeister.deutschcraft.feature.chat.domain.ChatMessage
import com.julesmeister.deutschcraft.feature.chat.domain.ReplySuggestion
import com.julesmeister.deutschcraft.feature.chat.domain.ReplySuggestionAction
import com.julesmeister.deutschcraft.feature.mission.domain.ConversationMission
import com.julesmeister.deutschcraft.feature.mission.domain.MissionObjectiveProgress
import com.julesmeister.deutschcraft.feature.mission.domain.generated.MissionOpportunity
import com.julesmeister.deutschcraft.feature.mission.domain.variation.MissionVariationContext
import com.julesmeister.deutschcraft.feature.mission.domain.variation.MissionVariationOption
import com.julesmeister.deutschcraft.feature.mission.domain.variation.MissionVariationType
import com.julesmeister.deutschcraft.feature.mission.domain.variation.MissionVariationUiState

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

        missionVariation =
            missionVariation.copy(
                isStarting =
                    false
            ),

        errorMessage =
            message
    )
}

internal fun ChatUiState.withMessages(
    messages: List<ChatMessage>
): ChatUiState {

    return copy(
        messages =
            messages
    )
}

internal fun ChatUiState.withInput(
    value: TextFieldValue
): ChatUiState {

    return copy(
        messageInput =
            value
    )
}

internal fun ChatUiState.sendingStarted():
        ChatUiState {

    return copy(
        messageInput =
            TextFieldValue(),

        suggestions =
            emptyList(),

        missionVariation =
            MissionVariationUiState(),

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

internal fun ChatUiState.assistantReplyPersisted(
    initialSuggestions: List<ReplySuggestion>
): ChatUiState {

    return copy(
        suggestions =
            initialSuggestions,

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

internal fun ChatUiState.withProgressiveSuggestions(
    suggestions: List<ReplySuggestion>
): ChatUiState {

    if (missionVariation.hasContinuation) {
        return this
    }

    return copy(
        suggestions =
            suggestions
                .distinctBy { suggestion ->
                    suggestion
                        .text
                        .trim()
                        .lowercase()
                }
                .take(
                    MAXIMUM_VISIBLE_SUGGESTIONS
                )
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

        missionVariation =
            MissionVariationUiState(),

        isSubmittingBesserRefinement =
            false,

        errorMessage =
            null
    )
}

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

internal fun ChatUiState.besserRefinementSubmissionStarted():
        ChatUiState {

    if (
        activeBesserRefinement == null ||
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

internal fun ChatUiState.besserRefinementNeedsRevision(
    feedback: String,
    score: Int?
): ChatUiState {

    val refinement =
        activeBesserRefinement
            ?: return copy(
                isSubmittingBesserRefinement =
                    false
            )

    return copy(
        activeBesserRefinement =
            refinement.copy(
                feedback =
                    feedback,

                lastScore =
                    score
            ),

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

internal fun ChatUiState.missionCheckStarted():
        ChatUiState {

    if (
        activeMissionId != null ||
        missionOffer != null ||
        missionVariation.hasContinuation
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
                activeMissionId == null &&
                !missionVariation.hasContinuation
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

internal fun ChatUiState.missionStartStarted():
        ChatUiState {

    if (
        missionOffer == null ||
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
            false,

        errorMessage =
            null
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

internal fun ChatUiState.withCurrentStreak(
    streakDays: Int
): ChatUiState {

    return copy(
        currentStreak =
            streakDays.coerceAtLeast(
                0
            )
    )
}

internal fun ChatUiState.withActiveMission(
    mission: ConversationMission?,
    newlyCompletedObjective: MissionObjectiveProgress?
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

        suggestions =
            if (missionVariation.hasContinuation) {
                emptyList()
            } else {
                suggestions
            },

        missionVariation =
            MissionVariationUiState(),

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
                ?.displayGuidance
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
                ?: objectiveCompletionEvent,

        errorMessage =
            null
    )
}

internal fun ChatUiState.objectiveCompletionShown(
    eventId: String
): ChatUiState {

    val activeEvent =
        objectiveCompletionEvent

    if (
        activeEvent == null ||
        activeEvent.eventId != eventId
    ) {
        return this
    }

    return copy(
        objectiveCompletionEvent =
            null
    )
}

internal fun ChatUiState.errorDismissed():
        ChatUiState {

    return copy(
        errorMessage =
            null
    )
}

internal fun ChatUiState.missionContinuationAvailable(
    context: MissionVariationContext
): ChatUiState {

    val options =
        buildMissionVariationOptions(
            context =
                context
        )

    return copy(
        missionOffer =
            null,

        isCheckingMissionOpportunity =
            false,

        isStartingMission =
            false,

        suggestions =
            listOf(
                ReplySuggestion(
                    id =
                        MISSION_CONTINUATION_SUGGESTION_ID,

                    text =
                        "↻ Noch eine Runde?",

                    action =
                        ReplySuggestionAction
                            .OpenMissionVariations
                ),

                ReplySuggestion(
                    id =
                        MISSION_CONTINUATION_DISMISS_SUGGESTION_ID,

                    text =
                        "✓ Für jetzt fertig",

                    action =
                        ReplySuggestionAction
                            .DismissMissionContinuation
                )
            ),

        missionVariation =
            MissionVariationUiState(
                context =
                    context,

                options =
                    options,

                isPickerVisible =
                    false,

                isStarting =
                    false
            ),

        errorMessage =
            null
    )
}

internal fun ChatUiState.missionVariationPickerOpened():
        ChatUiState {

    if (
        !missionVariation.hasContinuation ||
        missionVariation.isStarting
    ) {
        return this
    }

    return copy(
        missionVariation =
            missionVariation.copy(
                isPickerVisible =
                    true
            )
    )
}

internal fun ChatUiState.missionVariationPickerDismissed():
        ChatUiState {

    if (!missionVariation.isPickerVisible) {
        return this
    }

    return copy(
        missionVariation =
            missionVariation.copy(
                isPickerVisible =
                    false
            )
    )
}

internal fun ChatUiState.missionVariationStartStarted():
        ChatUiState {

    if (
        !missionVariation.hasContinuation ||
        missionVariation.isStarting ||
        isStartingMission
    ) {
        return this
    }

    return copy(
        suggestions =
            emptyList(),

        missionVariation =
            missionVariation.copy(
                isPickerVisible =
                    false,

                isStarting =
                    true
            ),

        isStartingMission =
            true,

        isCheckingMissionOpportunity =
            false,

        errorMessage =
            null
    )
}

internal fun ChatUiState.missionVariationStartSucceeded():
        ChatUiState {

    return copy(
        suggestions =
            emptyList(),

        missionOffer =
            null,

        missionVariation =
            MissionVariationUiState(),

        isStartingMission =
            false,

        isCheckingMissionOpportunity =
            false,

        errorMessage =
            null
    )
}

internal fun ChatUiState.missionVariationStartFailed(
    message: String
): ChatUiState {

    return copy(
        missionVariation =
            missionVariation.copy(
                isPickerVisible =
                    false,

                isStarting =
                    false
            ),

        isStartingMission =
            false,

        errorMessage =
            message
    )
}

internal fun ChatUiState.missionContinuationDismissed():
        ChatUiState {

    return copy(
        suggestions =
            suggestions.filterNot { suggestion ->
                suggestion.id ==
                        MISSION_CONTINUATION_SUGGESTION_ID ||
                        suggestion.id ==
                        MISSION_CONTINUATION_DISMISS_SUGGESTION_ID
            },

        missionVariation =
            MissionVariationUiState(),

        errorMessage =
            null
    )
}

private fun buildMissionVariationOptions(
    context: MissionVariationContext
): List<MissionVariationOption> {

    val complication =
        MissionVariationOption(
            type =
                MissionVariationType.COMPLICATION,

            title =
                "Neue Situation",

            description =
                "Etwas ändert sich unerwartet und du musst neu reagieren."
        )

    val vocabulary =
        MissionVariationOption(
            type =
                MissionVariationType.NEW_VOCABULARY,

            title =
                "Neue Wörter",

            description =
                if (
                    context
                        .unusedTargetVocabulary
                        .isNotEmpty()
                ) {
                    "Nutze Wortschatz, der im ersten Gespräch noch nicht vorkam."
                } else {
                    "Erweitere dasselbe Thema mit präziserem Wortschatz."
                }
        )

    val grammar =
        MissionVariationOption(
            type =
                MissionVariationType.GRAMMAR_STRETCH,

            title =
                "Grammatik-Stufe",

            description =
                "Begründe, vergleiche und formuliere Bedingungen."
        )

    return when {
        context.unusedTargetVocabulary.isNotEmpty() &&
                context.safeVocabularyScore <=
                context.safeGrammarScore -> {
            listOf(
                vocabulary,
                grammar,
                complication
            )
        }

        context.safeGrammarScore <
                context.safeVocabularyScore -> {
            listOf(
                grammar,
                vocabulary,
                complication
            )
        }

        context.safeOverallScore >=
                STRONG_VARIATION_SCORE_THRESHOLD -> {
            listOf(
                complication,
                grammar,
                vocabulary
            )
        }

        else -> {
            listOf(
                complication,
                vocabulary,
                grammar
            )
        }
    }
}

internal const val MISSION_CONTINUATION_SUGGESTION_ID =
    "mission-continuation-open"

internal const val MISSION_CONTINUATION_DISMISS_SUGGESTION_ID =
    "mission-continuation-dismiss"

private const val MAXIMUM_VISIBLE_SUGGESTIONS =
    3

private const val STRONG_VARIATION_SCORE_THRESHOLD =
    75