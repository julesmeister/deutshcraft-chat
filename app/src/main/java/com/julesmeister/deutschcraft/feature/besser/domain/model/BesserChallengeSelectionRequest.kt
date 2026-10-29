package com.julesmeister.deutschcraft.feature.besser.domain.model

/*
 * Context sent to the final AI capability selector.
 *
 * The selector is intentionally NOT given the complete
 * capability catalogue.
 *
 * It may choose only from candidates supplied here.
 */
data class BesserChallengeSelectionRequest(
    /*
     * Exact learner message that may contain a contextual
     * growth opportunity.
     */
    val userMessage: String,

    /*
     * Current assessed CEFR.
     *
     * Example:
     *
     * B1
     */
    val currentCefrLevel: String,

    /*
     * Development target.
     *
     * Example:
     *
     * B2
     */
    val targetCefrLevel: String,

    /*
     * Optional mission context.
     *
     * Qwen should prefer opportunities that help the learner
     * communicate better in the current conversation rather
     * than forcing an unrelated grammar exercise.
     */
    val missionTitle: String? = null,

    val missionObjective: String? = null,

    /*
     * Locally shortlisted capabilities.
     *
     * Qwen may return only one capabilityId from this list or
     * NO_CHALLENGE.
     */
    val candidates: List<BesserCapabilityCandidate>
)