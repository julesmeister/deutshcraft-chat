package com.julesmeister.deutschcraft.feature.learninganalytics.domain

import com.julesmeister.deutschcraft.core.cefr.model.CefrLevel

/*
 * Complete analytics evidence collected for one mission
 * attempt.
 *
 * This class does not calculate scores. It only describes
 * what actually happened during the mission.
 */
data class MissionCompletionEvidence(
    val missionId: String,
    val conversationId: String,
    val cefrLevel: CefrLevel,
    val objectiveEvidence:
    List<MissionObjectiveEvidence>,

    /*
     * Number of learner messages sent during the mission.
     */
    val learnerMessageCount: Int,

    /*
     * Number of Besser corrections generated during the
     * mission.
     */
    val besserCorrectionCount: Int,

    /*
     * Number of Besser corrections that the learner later
     * demonstrated correctly without another correction.
     */
    val resolvedBesserCorrectionCount: Int,

    /*
     * Mission target-vocabulary items used correctly by the
     * learner.
     */
    val correctlyUsedTargetVocabulary:
    Set<String>,

    /*
     * Complete set of vocabulary targeted by the mission.
     */
    val availableTargetVocabulary:
    Set<String>,

    /*
     * Number of appropriate follow-up questions independently
     * asked by the learner.
     */
    val independentFollowUpQuestionCount: Int,

    /*
     * Number of messages where the learner successfully
     * clarified or repaired a misunderstanding.
     */
    val successfulClarificationCount: Int,

    /*
     * Number of assistant responses that received a
     * contextually relevant learner reply.
     */
    val contextuallyRelevantReplyCount: Int,

    /*
     * Total assistant responses that reasonably expected a
     * learner reply.
     */
    val expectedReplyCount: Int,

    val startedAt: Long,
    val completedAt: Long?
) {

    val missionWasCompleted: Boolean
        get() =
            completedAt != null

    val durationMillis: Long?
        get() =
            completedAt
                ?.let { completionTime ->
                    (
                            completionTime -
                                    startedAt
                            )
                        .coerceAtLeast(
                            0L
                        )
                }
}

/*
 * Performance evidence for one mission objective.
 */
data class MissionObjectiveEvidence(
    val objectiveId: String,
    val title: String,
    val isRequired: Boolean,
    val wasCompleted: Boolean,

    /*
     * Number of learner-facing hints revealed before the
     * objective was completed.
     *
     * Expected range:
     *
     * 0 = independent
     * 1 = gentle support
     * 2 = substantial support
     * 3 = direct wording support
     */
    val hintsUsed: Int,

    /*
     * Number of Besser corrections connected to learner
     * messages that contributed to this objective.
     */
    val besserCorrectionCount: Int,

    /*
     * Confidence returned by the semantic objective
     * evaluator.
     */
    val evaluationConfidence: Float,

    /*
     * Vocabulary specifically targeted by this objective.
     */
    val targetVocabulary:
    Set<String>,

    /*
     * Target vocabulary used correctly in learner messages
     * while completing this objective.
     */
    val correctlyUsedVocabulary:
    Set<String>
) {

    val normalizedHintsUsed: Int
        get() =
            hintsUsed.coerceIn(
                minimumValue =
                    0,
                maximumValue =
                    MAXIMUM_HINT_COUNT
            )

    val normalizedEvaluationConfidence: Float
        get() =
            evaluationConfidence.coerceIn(
                minimumValue =
                    0f,
                maximumValue =
                    1f
            )

    companion object {
        const val MAXIMUM_HINT_COUNT =
            3
    }
}