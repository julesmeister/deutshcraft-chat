package com.julesmeister.deutschcraft.feature.besser.domain.model

/*
 * Input for the LOCAL capability shortlist.
 *
 * This does not call Qwen.
 */
data class BesserCandidateSelectionRequest(
    /*
     * Exact message sent by the learner.
     */
    val userMessage: String,

    /*
     * Current assessed CEFR level.
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
     * Optional hints from an upstream classifier.
     *
     * Examples:
     *
     * grammar
     * discourse
     * vocabulary
     * pragmatics
     * communicative_function
     *
     * Empty is perfectly valid.
     */
    val detectedCategories: Set<String> =
        emptySet(),

    /*
     * Prevent the same capability from repeatedly appearing
     * across nearby learner turns.
     *
     * We apply a penalty rather than removing it completely
     * because sometimes the same capability genuinely is the
     * most appropriate continuation.
     */
    val recentlyChallengedCapabilityIds: Set<String> =
        emptySet(),

    val limit: Int =
        DEFAULT_SHORTLIST_LIMIT
) {

    companion object {

        const val DEFAULT_SHORTLIST_LIMIT =
            6
    }
}