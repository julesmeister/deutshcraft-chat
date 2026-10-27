package com.julesmeister.deutschcraft.feature.learninganalytics.domain

/*
 * Real Besser / assessment evidence collected from the learner
 * messages sent while one mission was active.
 */
data class MissionBesserEvidence(
    /*
     * Learner messages sent between mission start and
     * mission completion.
     */
    val learnerMessageCount: Int,

    /*
     * Learner messages that received a meaningful Besser
     * correction or had persisted detected mistakes.
     */
    val correctedMessageCount: Int,

    /*
     * Corrected messages whose weakness did not reappear
     * later in the mission, or whose mistakes were already
     * marked resolved by the assessment system.
     */
    val resolvedCorrectionCount: Int,

    /*
     * Weakness keys that were still being repeated by the
     * end of the mission, most frequent first.
     *
     * A key is the grammar feature name when one exists,
     * otherwise the mistake category name.
     *
     * Examples:
     * DATIVE_CASE
     * WORD_ORDER
     */
    val persistentWeaknessKeys: List<String>,

    /*
     * Number of corrected learner messages attributed to
     * each objective.
     *
     * Key: objectiveId
     */
    val correctionsByObjectiveId: Map<String, Int>
) {

    val unresolvedCorrectionCount: Int
        get() =
            (
                    correctedMessageCount -
                            resolvedCorrectionCount
                    )
                .coerceAtLeast(
                    minimumValue =
                        0
                )

    companion object {

        val EMPTY =
            MissionBesserEvidence(
                learnerMessageCount =
                    0,
                correctedMessageCount =
                    0,
                resolvedCorrectionCount =
                    0,
                persistentWeaknessKeys =
                    emptyList(),
                correctionsByObjectiveId =
                    emptyMap()
            )
    }
}