package com.julesmeister.deutschcraft.feature.chat.presentation

import android.util.Log
import com.julesmeister.deutschcraft.core.cefr.model.CefrLevel
import com.julesmeister.deutschcraft.feature.chat.domain.ChatMessage
import com.julesmeister.deutschcraft.feature.chat.presentation.theme.ChatPresentationTokens
import com.julesmeister.deutschcraft.feature.learninganalytics.domain.MissionAnalytics
import com.julesmeister.deutschcraft.feature.mission.domain.ConversationMission
import com.julesmeister.deutschcraft.feature.mission.domain.usecase.MissionMessageEvaluationSummary
import javax.inject.Inject
import kotlinx.coroutines.CancellationException

/*
 * Handles post-send mission evaluation and extracts finalized
 * mission analytics when the learner completes the mission.
 */
class ChatMissionLearningProcessor
@Inject constructor(
    private val missionEvaluationCoordinator:
    ChatMissionEvaluationCoordinator
) {

    suspend fun process(
        activeMission: ConversationMission?,
        learnerMessage: ChatMessage,
        recentMessages: List<ChatMessage>,
        cefrLevel: CefrLevel
    ): ChatMissionLearningResult {
        if (activeMission == null) {
            Log.d(
                ChatPresentationTokens.MissionLearningProcessor.ProcessorTag,
                "Mission evaluation skipped because this " +
                        "conversation has no active mission."
            )

            return ChatMissionLearningResult()
        }

        val evaluationSummary =
            try {
                missionEvaluationCoordinator.evaluate(
                    mission =
                        activeMission,
                    learnerMessage =
                        learnerMessage,
                    recentMessages =
                        recentMessages,
                    cefrLevel =
                        cefrLevel
                )
            } catch (
                cancellationException:
                CancellationException
            ) {
                throw cancellationException
            } catch (throwable: Throwable) {
                Log.e(
                    ChatPresentationTokens.MissionLearningProcessor.ProcessorTag,
                    "Mission evaluation could not be completed.",
                    throwable
                )

                null
            }

        val completedMissionAnalytics =
            evaluationSummary
                ?.analytics

        val completedMissionTitle =
            if (
                completedMissionAnalytics !=
                null
            ) {
                activeMission
                    .template
                    .title
                    .trim()
                    .takeIf(
                        String::isNotBlank
                    )
            } else {
                null
            }

        if (
            completedMissionAnalytics !=
            null
        ) {
            Log.i(
                ChatPresentationTokens.MissionLearningProcessor.ProcessorTag,
                buildString {
                    append(
                        "Final mission analytics returned. "
                    )
                    append(
                        "missionId="
                    )
                    append(
                        completedMissionAnalytics.missionId
                    )
                    append(
                        ", overall="
                    )
                    append(
                        completedMissionAnalytics.overallScore
                    )
                    append(
                        ", title="
                    )
                    append(
                        completedMissionTitle
                    )
                }
            )
        }

        return ChatMissionLearningResult(
            evaluationSummary =
                evaluationSummary,

            completedMissionTitle =
                completedMissionTitle,

            completedMissionAnalytics =
                completedMissionAnalytics
        )
    }

}

data class ChatMissionLearningResult(
    val evaluationSummary:
    MissionMessageEvaluationSummary? =
        null,

    val completedMissionTitle:
    String? =
        null,

    val completedMissionAnalytics:
    MissionAnalytics? =
        null
)
