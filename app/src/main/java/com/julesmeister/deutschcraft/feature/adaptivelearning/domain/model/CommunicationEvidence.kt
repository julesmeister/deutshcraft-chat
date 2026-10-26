package com.julesmeister.deutschcraft.feature.adaptivelearning.domain.model

import com.julesmeister.deutschcraft.core.cefr.model.CefrLevel

/*
 * Represents evidence that one learner message did or did
 * not demonstrate a CEFR-appropriate communication skill.
 *
 * This is different from DetectedMistakeEntity.
 *
 * A message may contain no grammar mistake while still
 * failing to demonstrate communication expected at the
 * learner's current level.
 *
 * Example:
 *
 * Learner CEFR:
 * B1
 *
 * Learner message:
 * "Ich mag Berlin."
 *
 * Grammar:
 * Correct
 *
 * Communication evidence:
 * DEVELOPING_A_RESPONSE = PARTIAL_SUCCESS
 * GIVING_REASONS = NOT_OBSERVED
 *
 * A later AI assessment can decide which communication
 * evidence is meaningful enough to record.
 */
data class CommunicationEvidence(
    val skill: CommunicationSkill,

    /*
     * CEFR level against which the message was evaluated.
     */
    val targetCefrLevel: CefrLevel,

    /*
     * What the learner demonstrated in this message.
     */
    val result: LearningEvidenceResult,

    /*
     * Evaluator confidence from 0 to 1.
     */
    val confidence: Float,

    /*
     * Short internal explanation.
     *
     * Example:
     *
     * "The learner expressed a preference but did not explain
     * the reason."
     */
    val reason: String,

    /*
     * Original learner message.
     */
    val learnerText: String,

    /*
     * Internal instruction for Emil.
     *
     * Example:
     *
     * "Ask a natural follow-up question that encourages the
     * learner to explain why."
     */
    val reinforcementInstruction: String =
        defaultReinforcementInstruction(
            skill
        ),

    /*
     * Short learner-facing guidance.
     *
     * Example:
     *
     * "Try explaining why."
     */
    val learnerGuidance: String =
        skill.learnerGuidance,

    /*
     * Optional natural version of the same thought at the
     * learner's current CEFR level.
     *
     * This must preserve the learner's meaning and must not
     * invent personal facts.
     */
    val levelAppropriateText: String? =
        null
) {

    val focusKey: String
        get() =
            "communication:" +
                    skill.name.lowercase()

    val title: String
        get() =
            skill.displayName

    val description: String
        get() =
            communicationDescription(
                skill =
                    skill
            )

    val normalizedConfidence: Float
        get() =
            confidence.coerceIn(
                minimumValue =
                    0f,
                maximumValue =
                    1f
            )

    val sanitizedReason: String
        get() =
            reason.trim()

    val sanitizedLearnerText: String
        get() =
            learnerText.trim()

    val sanitizedReinforcementInstruction: String
        get() =
            reinforcementInstruction.trim()

    val sanitizedLearnerGuidance: String
        get() =
            learnerGuidance.trim()

    val sanitizedLevelAppropriateText: String?
        get() =
            levelAppropriateText
                ?.trim()
                ?.takeIf(
                    String::isNotBlank
                )

    val hasUsableEvidence: Boolean
        get() =
            result !=
                    LearningEvidenceResult.NOT_OBSERVED &&
                    sanitizedLearnerText.isNotBlank() &&
                    normalizedConfidence >=
                    MINIMUM_USABLE_CONFIDENCE

    fun toAdaptiveLearningObservation():
            AdaptiveLearningObservation {
        return AdaptiveLearningObservation(
            focusKey =
                focusKey,
            type =
                LearningFocusType.COMMUNICATION,
            title =
                title,
            description =
                description,
            targetCefrLevel =
                targetCefrLevel,
            communicationSkill =
                skill,
            result =
                result,
            confidence =
                normalizedConfidence,
            reason =
                sanitizedReason,
            reinforcementInstruction =
                sanitizedReinforcementInstruction,
            learnerGuidance =
                sanitizedLearnerGuidance,
            learnerText =
                sanitizedLearnerText,
            correctedText =
                null,
            levelAppropriateText =
                sanitizedLevelAppropriateText
        )
    }

    companion object {
        const val MINIMUM_USABLE_CONFIDENCE =
            0.55f

        fun defaultReinforcementInstruction(
            skill: CommunicationSkill
        ): String {
            return when (skill) {
                CommunicationSkill
                    .GREETING_AND_SOCIAL_OPENING -> {

                    "Create a natural social situation where " +
                            "the learner can open an interaction."
                }

                CommunicationSkill.ASKING_QUESTIONS -> {
                    "Leave a natural information gap that " +
                            "encourages the learner to ask a " +
                            "relevant follow-up question."
                }

                CommunicationSkill.GIVING_REASONS -> {
                    "Ask a natural follow-up question that " +
                            "encourages the learner to explain why."
                }

                CommunicationSkill.EXPRESSING_OPINIONS -> {
                    "Introduce a familiar topic and invite the " +
                            "learner to express and support an opinion."
                }

                CommunicationSkill.CONNECTING_IDEAS -> {
                    "Encourage the learner to combine related " +
                            "ideas using appropriate linking language."
                }

                CommunicationSkill.DESCRIBING_EXPERIENCES -> {
                    "Ask about a personal experience and invite " +
                            "the learner to include relevant details."
                }

                CommunicationSkill.STORYTELLING -> {
                    "Ask the learner to describe events in a " +
                            "clear chronological sequence."
                }

                CommunicationSkill.COMPARING_OPTIONS -> {
                    "Present two relevant options and ask the " +
                            "learner to compare them."
                }

                CommunicationSkill.CLARIFYING_MEANING -> {
                    "Ask a natural clarification question that " +
                            "encourages rephrasing or an example."
                }

                CommunicationSkill.DEFENDING_AN_ARGUMENT -> {
                    "Present a respectful alternative viewpoint " +
                            "and invite the learner to support their " +
                            "position."
                }

                CommunicationSkill.DEVELOPING_A_RESPONSE -> {
                    "Ask a follow-up question that encourages " +
                            "the learner to add one useful detail."
                }

                CommunicationSkill
                    .USING_CONVERSATION_CONNECTORS -> {

                    "Encourage a longer response that benefits " +
                            "from linking expressions."
                }

                CommunicationSkill.POLITE_INTERACTION -> {
                    "Create a social situation requiring " +
                            "appropriate polite language."
                }

                CommunicationSkill
                    .NATURAL_CONVERSATION_CLOSING -> {

                    "Allow the interaction to reach a natural " +
                            "ending where the learner can close it."
                }
            }
        }

        fun communicationDescription(
            skill: CommunicationSkill
        ): String {
            return when (skill) {
                CommunicationSkill
                    .GREETING_AND_SOCIAL_OPENING -> {

                    "Opening social interactions naturally and " +
                            "acknowledging the other person."
                }

                CommunicationSkill.ASKING_QUESTIONS -> {
                    "Asking relevant questions that move the " +
                            "conversation forward."
                }

                CommunicationSkill.GIVING_REASONS -> {
                    "Explaining why the learner thinks, prefers, " +
                            "or chooses something."
                }

                CommunicationSkill.EXPRESSING_OPINIONS -> {
                    "Expressing a personal viewpoint and " +
                            "supporting it with relevant information."
                }

                CommunicationSkill.CONNECTING_IDEAS -> {
                    "Connecting related thoughts into coherent " +
                            "responses instead of isolated statements."
                }

                CommunicationSkill.DESCRIBING_EXPERIENCES -> {
                    "Describing events and experiences with " +
                            "relevant context and details."
                }

                CommunicationSkill.STORYTELLING -> {
                    "Presenting events in a clear and " +
                            "understandable sequence."
                }

                CommunicationSkill.COMPARING_OPTIONS -> {
                    "Explaining similarities, differences, " +
                            "advantages, or disadvantages."
                }

                CommunicationSkill.CLARIFYING_MEANING -> {
                    "Rephrasing ideas or adding examples when " +
                            "additional clarity is needed."
                }

                CommunicationSkill.DEFENDING_AN_ARGUMENT -> {
                    "Supporting a position with reasons and " +
                            "responding to another viewpoint."
                }

                CommunicationSkill.DEVELOPING_A_RESPONSE -> {
                    "Developing an answer beyond a minimal " +
                            "single statement."
                }

                CommunicationSkill
                    .USING_CONVERSATION_CONNECTORS -> {

                    "Using linking expressions to organize and " +
                            "relate ideas."
                }

                CommunicationSkill.POLITE_INTERACTION -> {
                    "Using language appropriate to the social " +
                            "situation."
                }

                CommunicationSkill
                    .NATURAL_CONVERSATION_CLOSING -> {

                    "Concluding an interaction naturally and " +
                            "appropriately."
                }
            }
        }
    }
}