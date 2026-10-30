// app/src/main/java/com/julesmeister/deutschcraft/feature/mission/domain/recommendation/MissionLearningContext.kt

package com.julesmeister.deutschcraft.feature.mission.domain.recommendation

data class MissionLearningContext(
    val reinforcementFocuses:
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
        emptyList()
) {

    val targetCapabilityIds:
            Set<String>
        get() =
            (
                    progressionReadyCapabilities +
                            developingCapabilities
                    )
                .asSequence()
                .mapNotNull(
                    ::extractCapabilityId
                )
                .toSet()

    val progressionReadyCapabilityIds:
            Set<String>
        get() =
            progressionReadyCapabilities
                .asSequence()
                .mapNotNull(
                    ::extractCapabilityId
                )
                .toSet()

    val developingCapabilityIds:
            Set<String>
        get() =
            developingCapabilities
                .asSequence()
                .mapNotNull(
                    ::extractCapabilityId
                )
                .toSet()

    val adaptiveFocusKeys:
            Set<String>
        get() =
            reinforcementFocuses
                .asSequence()
                .mapNotNull(
                    ::extractAdaptiveFocusKey
                )
                .toSet()

    val masteredCapabilityIds:
            Set<String>
        get() =
            masteredCapabilities
                .asSequence()
                .mapNotNull(
                    ::extractCapabilityId
                )
                .toSet()

    val adaptiveSelectionSignals:
            List<String>
        get() =
            (
                    reinforcementFocuses +
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
                .take(
                    MAXIMUM_SELECTION_SIGNAL_COUNT
                )
                .toList()

    val generationTargetSkills:
            Set<String>
        get() =
            (
                    progressionReadyCapabilities +
                            developingCapabilities +
                            reinforcementFocuses
                    )
                .asSequence()
                .map(
                    String::trim
                )
                .filter(
                    String::isNotBlank
                )
                .distinct()
                .take(
                    MAXIMUM_GENERATION_TARGET_COUNT
                )
                .toSet()

    val hasLearningSignals: Boolean
        get() =
            reinforcementFocuses.isNotEmpty() ||
                    progressionReadyCapabilities.isNotEmpty() ||
                    developingCapabilities.isNotEmpty()

    val freshnessFingerprint:
            String
        get() =
            buildString {
                append(
                    "adaptive="
                )

                append(
                    adaptiveFocusKeys
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
                    progressionReadyCapabilityIds
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
                    developingCapabilityIds
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
                    masteredCapabilityIds
                        .sorted()
                        .joinToString(
                            separator =
                                ","
                        )
                )
            }

    fun materiallyMatches(
        other:
        MissionLearningContext
    ): Boolean {

        if (
            targetCapabilityIds.isEmpty() &&
            adaptiveFocusKeys.isEmpty()
        ) {
            return true
        }

        val stillRelevantCapabilities =
            targetCapabilityIds -
                    other.masteredCapabilityIds

        val matchingCapabilityTargets =
            stillRelevantCapabilities.intersect(
                other.targetCapabilityIds
            )

        val matchingAdaptiveTargets =
            adaptiveFocusKeys.intersect(
                other.adaptiveFocusKeys
            )

        val hadCapabilityTargets =
            targetCapabilityIds.isNotEmpty()

        val hadAdaptiveTargets =
            adaptiveFocusKeys.isNotEmpty()

        val capabilityTargetStillRelevant =
            !hadCapabilityTargets ||
                    matchingCapabilityTargets.isNotEmpty()

        val adaptiveTargetStillRelevant =
            !hadAdaptiveTargets ||
                    matchingAdaptiveTargets.isNotEmpty()

        return capabilityTargetStillRelevant &&
                adaptiveTargetStillRelevant
    }

    private fun extractCapabilityId(
        signal: String
    ): String? {

        val normalizedSignal =
            signal.trim()

        if (normalizedSignal.isBlank()) {
            return null
        }

        val prefixSection =
            normalizedSignal
                .substringBefore(
                    delimiter =
                        "|"
                )
                .trim()

        val capabilityId =
            prefixSection
                .substringAfter(
                    delimiter =
                        ":",
                    missingDelimiterValue =
                        ""
                )
                .trim()

        return capabilityId.takeIf(
            String::isNotBlank
        )
    }

    private fun extractAdaptiveFocusKey(
        signal: String
    ): String? {

        val focusKey =
            signal
                .substringBefore(
                    delimiter =
                        "|"
                )
                .trim()

        return focusKey.takeIf { value ->
            value.isNotBlank() &&
                    ":" in
                    value
        }
    }

    companion object {

        val Empty =
            MissionLearningContext()

        private const val MAXIMUM_SELECTION_SIGNAL_COUNT =
            8

        private const val MAXIMUM_GENERATION_TARGET_COUNT =
            5
    }
}