package com.julesmeister.deutschcraft.feature.learninganalytics.domain

/*
 * Interaction evidence for one evaluated learner message.
 */
data class MissionMessageInteractionEvidence(
    val conversationMissionId: String,
    val messageId: String,
    val relevantReply: Boolean,
    val followUpQuestion: Boolean,
    val clarification: Boolean,
    val replyExpected: Boolean,
    val confidence: Float,
    val evaluatedAt: Long
) {

    val normalizedConfidence: Float
        get() =
            confidence.coerceIn(
                minimumValue =
                    0f,
                maximumValue =
                    1f
            )
}

/*
 * Mission-level totals consumed by
 * MissionCompletionEvidence.
 */
data class MissionInteractionEvidence(
    val expectedReplyCount: Int,
    val contextuallyRelevantReplyCount: Int,
    val independentFollowUpQuestionCount: Int,
    val successfulClarificationCount: Int,
    val evaluatedMessageCount: Int
) {

    companion object {

        val EMPTY =
            MissionInteractionEvidence(
                expectedReplyCount =
                    0,
                contextuallyRelevantReplyCount =
                    0,
                independentFollowUpQuestionCount =
                    0,
                successfulClarificationCount =
                    0,
                evaluatedMessageCount =
                    0
            )
    }
}