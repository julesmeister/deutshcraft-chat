package com.julesmeister.deutschcraft.feature.learninganalytics.domain

/*
 * Vocabulary evidence collected from learner messages sent
 * while a mission was active.
 *
 * This does not represent saved vocabulary or vocabulary the
 * learner merely viewed.
 *
 * It represents target vocabulary that actually appeared in
 * learner messages during the mission.
 */
data class MissionVocabularyEvidence(

    /*
     * Every target word or phrase available across the
     * mission's objectives.
     */
    val availableVocabulary:
    Set<String>,

    /*
     * Target vocabulary found in learner messages during the
     * mission.
     */
    val correctlyUsedVocabulary:
    Set<String>,

    /*
     * Vocabulary demonstrated while each objective was active.
     *
     * Key:
     * objectiveId
     *
     * Value:
     * target words/phrases demonstrated during that objective
     */
    val usedByObjectiveId:
    Map<String, Set<String>>
) {

    val unusedVocabulary:
            Set<String>
        get() =
            availableVocabulary -
                    correctlyUsedVocabulary

    val demonstratedCount: Int
        get() =
            correctlyUsedVocabulary.size

    val availableCount: Int
        get() =
            availableVocabulary.size

    companion object {

        val EMPTY =
            MissionVocabularyEvidence(
                availableVocabulary =
                    emptySet(),
                correctlyUsedVocabulary =
                    emptySet(),
                usedByObjectiveId =
                    emptyMap()
            )
    }
}