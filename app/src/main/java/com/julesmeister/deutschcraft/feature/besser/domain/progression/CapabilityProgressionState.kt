// app/src/main/java/com/julesmeister/deutschcraft/feature/besser/domain/progression/CapabilityProgressionState.kt

package com.julesmeister.deutschcraft.feature.besser.domain.progression

data class CapabilityProgressionState(
    val capabilityId: String,

    val prerequisiteCapabilityIds: Set<String>,

    val satisfiedPrerequisiteIds: Set<String>,

    val unsatisfiedPrerequisiteIds: Set<String>,

    val averagePrerequisiteMastery: Float,

    val isProgressionReady: Boolean,

    val isDirectSuccessorOfMasteredCapability: Boolean
) {

    val safeAveragePrerequisiteMastery: Float
        get() =
            averagePrerequisiteMastery.coerceIn(
                minimumValue =
                    0f,
                maximumValue =
                    1f
            )

    val hasPrerequisites: Boolean
        get() =
            prerequisiteCapabilityIds.isNotEmpty()

    val satisfiedPrerequisiteCount: Int
        get() =
            satisfiedPrerequisiteIds.size

    val totalPrerequisiteCount: Int
        get() =
            prerequisiteCapabilityIds.size

    companion object {

        fun open(
            capabilityId: String
        ): CapabilityProgressionState {
            return CapabilityProgressionState(
                capabilityId =
                    capabilityId,

                prerequisiteCapabilityIds =
                    emptySet(),

                satisfiedPrerequisiteIds =
                    emptySet(),

                unsatisfiedPrerequisiteIds =
                    emptySet(),

                averagePrerequisiteMastery =
                    1f,

                isProgressionReady =
                    true,

                isDirectSuccessorOfMasteredCapability =
                    false
            )
        }
    }
}