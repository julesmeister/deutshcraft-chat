// app/src/main/java/com/julesmeister/deutschcraft/feature/mission/domain/variation/MissionVariationContext.kt

package com.julesmeister.deutschcraft.feature.mission.domain.variation

import com.julesmeister.deutschcraft.core.cefr.model.CefrLevel

data class MissionVariationContext(
    /*
     * Retained for compatibility with the completed-mission
     * result flow.
     *
     * New active-mission callers should provide
     * sourceMissionId directly.
     */
    val completedMissionId: String? =
        null,

    val mode: MissionVariationMode =
        MissionVariationMode.EXTEND_COMPLETED_MISSION,

    val sourceMissionId: String =
        completedMissionId.orEmpty(),

    val missionTitle: String,

    val cefrLevel: CefrLevel,

    val overallScore: Int =
        0,

    val grammarScore: Int =
        0,

    val vocabularyScore: Int =
        0,

    val strongObjectiveTitles: List<String> =
        emptyList(),

    val improvementObjectiveTitles: List<String> =
        emptyList(),

    val demonstratedVocabulary: List<String> =
        emptyList(),

    val unusedTargetVocabulary: List<String> =
        emptyList()
) {

    val resolvedSourceMissionId: String
        get() =
            sourceMissionId
                .trim()
                .ifBlank {
                    completedMissionId
                        ?.trim()
                        .orEmpty()
                }

    val safeOverallScore: Int
        get() =
            overallScore.coerceIn(
                minimumValue =
                    0,

                maximumValue =
                    100
            )

    val safeGrammarScore: Int
        get() =
            grammarScore.coerceIn(
                minimumValue =
                    0,

                maximumValue =
                    100
            )

    val safeVocabularyScore: Int
        get() =
            vocabularyScore.coerceIn(
                minimumValue =
                    0,

                maximumValue =
                    100
            )
}