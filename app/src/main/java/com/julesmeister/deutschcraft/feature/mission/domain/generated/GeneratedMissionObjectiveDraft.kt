package com.julesmeister.deutschcraft.feature.mission.domain.generated

data class GeneratedMissionObjectiveDraft(
    val title: String,
    val description: String,

    /*
     * Internal semantic rule used by the objective evaluator.
     *
     * This must never be displayed directly to the learner.
     */
    val completionHint: String,

    /*
     * AI-generated learner-facing hints.
     *
     * They should progress from a gentle reminder to a more
     * specific instruction.
     */
    val coachingHints: List<String> =
        emptyList(),

    /*
     * German vocabulary that may help the learner complete
     * this objective.
     */
    val targetVocabulary: List<String> =
        emptyList(),

    /*
     * German example sentences appropriate for the mission
     * and the learner's CEFR level.
     */
    val examplePhrases: List<String> =
        emptyList(),

    val isRequired: Boolean
) {

    val sanitizedCoachingHints: List<String>
        get() =
            coachingHints.sanitizedValues(
                maximumSize =
                    MAXIMUM_COACHING_HINT_COUNT
            )

    val sanitizedTargetVocabulary: List<String>
        get() =
            targetVocabulary.sanitizedValues(
                maximumSize =
                    MAXIMUM_TARGET_VOCABULARY_COUNT
            )

    val sanitizedExamplePhrases: List<String>
        get() =
            examplePhrases.sanitizedValues(
                maximumSize =
                    MAXIMUM_EXAMPLE_PHRASE_COUNT
            )

    private fun List<String>.sanitizedValues(
        maximumSize: Int
    ): List<String> {
        return asSequence()
            .map(String::trim)
            .filter(String::isNotBlank)
            .distinct()
            .take(
                maximumSize
            )
            .toList()
    }

    private companion object {
        const val MAXIMUM_COACHING_HINT_COUNT =
            3

        const val MAXIMUM_TARGET_VOCABULARY_COUNT =
            4

        const val MAXIMUM_EXAMPLE_PHRASE_COUNT =
            2
    }
}