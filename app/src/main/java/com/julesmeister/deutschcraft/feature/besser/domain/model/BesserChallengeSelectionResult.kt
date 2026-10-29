package com.julesmeister.deutschcraft.feature.besser.domain.model

/*
 * Final contextual decision after evaluating the learner's
 * message against the local capability shortlist.
 */
sealed interface BesserChallengeSelectionResult {

    /*
     * There genuinely is a useful growth opportunity.
     */
    data class Selected(
        val capability: BesserCapabilityCandidate,

        /*
         * Short learner-facing explanation of why this
         * capability fits the current message.
         *
         * Example:
         *
         * "Du hast deine Meinung bereits begründet. Jetzt
         * kannst du sie mit einem konkreten Beispiel stärken."
         */
        val rationale: String,

        /*
         * Challenge instruction shown in BesserChallengeBubble.
         *
         * This should tell the learner WHAT communication goal
         * to achieve without simply supplying the answer.
         */
        val instruction: String,

        /*
         * Optional progressively revealable hints.
         *
         * These may later map naturally to the existing Hint
         * button in BesserChallengeBubble.
         */
        val hints: List<String>,

        /*
         * 0f..1f confidence reported by the selector.
         */
        val confidence: Float
    ) : BesserChallengeSelectionResult

    /*
     * A valid outcome.
     *
     * Besser must not interrupt every learner message.
     */
    data class NoChallenge(
          val reason: String? = null
    ) : BesserChallengeSelectionResult
}