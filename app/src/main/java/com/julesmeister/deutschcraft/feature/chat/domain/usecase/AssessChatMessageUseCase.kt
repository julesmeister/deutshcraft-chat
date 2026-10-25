package com.julesmeister.deutschcraft.feature.chat.domain.usecase

import com.julesmeister.deutschcraft.core.activity.ActivityTracker
import com.julesmeister.deutschcraft.core.cefr.engine.LocalMessageEvaluator
import com.julesmeister.deutschcraft.core.cefr.model.CefrLevel
import com.julesmeister.deutschcraft.core.cefr.model.DefaultLearnerProfile
import com.julesmeister.deutschcraft.feature.assessment.domain.ProcessAssessmentUseCase
import com.julesmeister.deutschcraft.feature.chat.domain.ChatMessage
import com.julesmeister.deutschcraft.feature.progress.domain.ProgressRepository
import javax.inject.Inject

class AssessChatMessageUseCase @Inject constructor(
    private val progressRepository:
    ProgressRepository,
    private val localMessageEvaluator:
    LocalMessageEvaluator,
    private val processAssessment:
    ProcessAssessmentUseCase,
    private val activityTracker:
    ActivityTracker
) {

    suspend operator fun invoke(
        userMessage: ChatMessage,
        activeDayCount: Int
    ) {
        val currentProgress =
            progressRepository.getProgress()

        val currentLevel =
            currentProgress?.currentLevel
                ?: CefrLevel.A1

        /*
         * Produce conservative local CEFR evidence from
         * the learner's saved message.
         */
        val localEvidence =
            localMessageEvaluator.evaluate(
                messageId =
                    userMessage.id,
                conversationId =
                    userMessage.conversationId,
                content =
                    userMessage.content,
                currentLevel =
                    currentLevel
            )

        /*
         * Update assessment evidence, skill scores, and
         * the learner profile.
         */
        processAssessment(
            evidence = localEvidence,
            activeDayCount = activeDayCount
        )

        /*
         * Increment today's assessment count.
         *
         * This updates the existing ActivityDayEntity and
         * does not create another active day.
         */
        activityTracker.recordAssessmentActivity(
            profileId =
                DefaultLearnerProfile
                    .LOCAL_PROFILE_ID
        )
    }
}