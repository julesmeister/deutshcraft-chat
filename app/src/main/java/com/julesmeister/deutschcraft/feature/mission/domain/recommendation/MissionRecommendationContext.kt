// app/src/main/java/com/julesmeister/deutschcraft/feature/mission/domain/recommendation/MissionRecommendationContext.kt

package com.julesmeister.deutschcraft.feature.mission.domain.recommendation

import com.julesmeister.deutschcraft.core.cefr.model.CefrLevel

data class MissionRecommendationContext(
    val cefrLevel:
    CefrLevel,

    val recentConversationText:
    String,

    val recentlyOfferedScenarioTypes:
    List<MissionScenarioType> =
        emptyList(),

    val recentlyCompletedScenarioTypes:
    List<MissionScenarioType> =
        emptyList(),

    val recentlyDismissedScenarioTypes:
    List<MissionScenarioType> =
        emptyList(),

    val adaptiveLearningFocuses:
    List<String> =
        emptyList(),

    val progressionReadyCapabilities:
    List<String> =
        emptyList(),

    val developingCapabilities:
    List<String> =
        emptyList(),

    val masteredCapabilities:
    List<String> =
        emptyList(),

    val activeScenarioType:
    MissionScenarioType? =
        null
) {

    val positiveLearningSignals:
            List<String>
        get() =
            (
                    adaptiveLearningFocuses +
                            progressionReadyCapabilities +
                            developingCapabilities
                    )
                .asSequence()
                .map(
                    String::trim
                )
                .filter(
                    String::isNotBlank
                )
                .distinct()
                .toList()

    val normalizedMasteredCapabilities:
            Set<String>
        get() =
            masteredCapabilities
                .asSequence()
                .map(
                    String::trim
                )
                .filter(
                    String::isNotBlank
                )
                .map(
                    String::lowercase
                )
                .toSet()

    val recentlyUsedContextCategories:
            List<MissionScenarioContextCategory>
        get() =
            (
                    recentlyOfferedScenarioTypes +
                            recentlyCompletedScenarioTypes
                    )
                .map(
                    MissionScenarioType::contextCategory
                )

    val recentlyDismissedContextCategories:
            List<MissionScenarioContextCategory>
        get() =
            recentlyDismissedScenarioTypes
                .map(
                    MissionScenarioType::contextCategory
                )

    val freshnessFingerprint:
            String
        get() =
            buildString {
                append(
                    cefrLevel.name
                )

                append(
                    "|adaptive="
                )

                append(
                    adaptiveLearningFocuses
                        .mapNotNull(
                            ::extractSignalIdentity
                        )
                        .sorted()
                        .joinToString(
                            separator =
                                ","
                        )
                )

                append(
                    "|progression="
                )

                append(
                    progressionReadyCapabilities
                        .mapNotNull(
                            ::extractSignalIdentity
                        )
                        .sorted()
                        .joinToString(
                            separator =
                                ","
                        )
                )

                append(
                    "|developing="
                )

                append(
                    developingCapabilities
                        .mapNotNull(
                            ::extractSignalIdentity
                        )
                        .sorted()
                        .joinToString(
                            separator =
                                ","
                        )
                )

                append(
                    "|mastered="
                )

                append(
                    masteredCapabilities
                        .mapNotNull(
                            ::extractSignalIdentity
                        )
                        .sorted()
                        .joinToString(
                            separator =
                                ","
                        )
                )
            }

    private fun extractSignalIdentity(
        signal: String
    ): String? {

        val normalized =
            signal
                .substringBefore(
                    delimiter =
                        "|"
                )
                .trim()
                .lowercase()

        return normalized.takeIf(
            String::isNotBlank
        )
    }
}