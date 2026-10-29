package com.julesmeister.deutschcraft.feature.chat.presentation

import com.julesmeister.deutschcraft.feature.adaptivelearning.domain.model.AdaptiveLearningFeedback
import com.julesmeister.deutschcraft.feature.adaptivelearning.domain.model.AdaptiveLearningMessageResult
import com.julesmeister.deutschcraft.feature.chat.domain.ReplySuggestion
import com.julesmeister.deutschcraft.feature.learninganalytics.domain.MissionAnalytics
import com.julesmeister.deutschcraft.feature.mission.domain.usecase.MissionMessageEvaluationSummary

/*
 * Internal result used while post-send learning systems run.
 */
data class ChatLearningProcessingResult(
    val missionEvaluationSummary:
    MissionMessageEvaluationSummary? =
        null,

    val adaptiveLearningResult:
    AdaptiveLearningMessageResult =
        AdaptiveLearningMessageResult(),

    val adaptiveLearningFeedback:
    AdaptiveLearningFeedback? =
        null,

    val completedMissionTitle:
    String? =
        null,

    val completedMissionAnalytics:
    MissionAnalytics? =
        null
)

/*
 * Public result returned to ChatSessionMessageController.
 */
data class ChatMessageSendResult(
    val suggestions:
    List<ReplySuggestion>,

    val warning:
    String?,

    val adaptiveLearningFeedback:
    AdaptiveLearningFeedback? =
        null,

    val completedMissionTitle:
    String? =
        null,

    val completedMissionAnalytics:
    MissionAnalytics? =
        null
) {

    val hasCompletedMissionResult: Boolean
        get() =
            !completedMissionTitle.isNullOrBlank() &&
                    completedMissionAnalytics !=
                    null
}