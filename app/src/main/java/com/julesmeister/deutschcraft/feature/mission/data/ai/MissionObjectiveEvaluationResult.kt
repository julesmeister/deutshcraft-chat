package com.julesmeister.deutschcraft.feature.mission.data.ai

/*
 * Structurally validated result returned by the mission
 * evaluation AI gateway.
 *
 * One Qwen request now evaluates two things:
 *
 * 1. Whether the current mission objective was completed.
 * 2. How the learner interacted with Emil on the latest turn.
 *
 * EvaluateMissionMessageUseCase still performs the final
 * domain validation before objective progress is changed in
 * Room.
 */
data class MissionObjectiveEvaluationResult(
    val completedObjectives:
    List<EvaluatedMissionObjective>,

    /*
     * Interaction and comprehension evidence for the latest
     * learner message.
     *
     * Keeping this in the existing mission-evaluation result
     * avoids making another Qwen request.
     */
    val interaction:
    MissionInteractionEvaluation =
        MissionInteractionEvaluation.EMPTY
)

/*
 * objectiveId contains the exact ID of an unfinished
 * MissionObjective supplied to Qwen.
 *
 * It must never contain the objective title or description.
 */
data class EvaluatedMissionObjective(
    val objectiveId: String,

    /*
     * Confidence from 0 to 1.
     */
    val confidence: Float,

    val reason: String
)

/*
 * Semantic conversation evidence for the latest learner
 * message.
 *
 * This later feeds:
 *
 * - Interaction score
 * - Comprehension score
 * - CEFR evidence
 *
 * It intentionally describes only the latest learner turn.
 * Mission-level totals are accumulated separately.
 */
data class MissionInteractionEvaluation(

    /*
     * Did the learner appropriately respond to the preceding
     * conversational context?
     *
     * Examples:
     *
     * Emil:
     * Wann können Sie anfangen?
     *
     * Learner:
     * Ich könnte nächsten Montag anfangen.
     *
     * relevantReply = true
     */
    val relevantReply: Boolean,

    /*
     * Did the learner independently ask a useful question
     * that meaningfully advances the conversation?
     *
     * Examples:
     *
     * Welche Technologien verwendet Ihr Team?
     *
     * followUpQuestion = true
     *
     * A question should not count merely because it contains
     * a question mark. It should be conversationally useful.
     */
    val followUpQuestion: Boolean,

    /*
     * Did the learner successfully clarify, correct, repair,
     * or disambiguate something?
     *
     * Example:
     *
     * Emil:
     * Meinen Sie Ihre aktuelle Stelle?
     *
     * Learner:
     * Nein, ich meine meine vorherige Stelle.
     *
     * clarification = true
     */
    val clarification: Boolean,

    /*
     * Whether there was enough preceding conversational
     * context to reasonably expect a meaningful learner
     * response.
     *
     * This becomes the denominator for comprehension:
     *
     * relevant replies / expected replies
     */
    val replyExpected: Boolean,

    /*
     * Confidence in the interaction classification.
     *
     * 0 = no confidence
     * 1 = very high confidence
     */
    val confidence: Float
) {

    val normalizedConfidence: Float
        get() =
            confidence.coerceIn(
                minimumValue =
                    0f,
                maximumValue =
                    1f
            )

    companion object {

        /*
         * Safe neutral result used when Qwen does not return
         * usable interaction evidence.
         *
         * replyExpected is false so failed/missing interaction
         * evaluation does not artificially hurt the learner's
         * comprehension denominator.
         */
        val EMPTY =
            MissionInteractionEvaluation(
                relevantReply =
                    false,
                followUpQuestion =
                    false,
                clarification =
                    false,
                replyExpected =
                    false,
                confidence =
                    0f
            )
    }
}