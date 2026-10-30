// app/src/main/java/com/julesmeister/deutschcraft/feature/besser/domain/model/BesserCandidateSelectionRequest.kt

package com.julesmeister.deutschcraft.feature.besser.domain.model

data class BesserCandidateSelectionRequest(
    val userMessage: String,

    val currentCefrLevel: String,

    val targetCefrLevel: String,

    val detectedCategories: Set<String> =
        emptySet(),

    /*
     * The latest distinct Besser capabilities shown to the
     * learner.
     *
     * These remain eligible but receive a strong local ranking
     * penalty so repetition is discouraged rather than
     * absolutely forbidden.
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