// app/src/main/java/com/julesmeister/deutschcraft/feature/besser/domain/progression/CapabilityProgressionRule.kt

package com.julesmeister.deutschcraft.feature.besser.domain.progression

data class CapabilityProgressionRule(
    val capabilityId: String,

    /*
     * Capabilities that provide useful preparation for this
     * capability.
     *
     * These are soft prerequisites. Besser may still surface
     * the capability when conversation context strongly
     * supports it.
     */
    val prerequisiteCapabilityIds: Set<String> =
        emptySet(),

    /*
     * Capabilities that naturally follow this capability once
     * the learner has accumulated strong evidence.
     */
    val successorCapabilityIds: Set<String> =
        emptySet(),

    /*
     * Minimum mastery expected across available prerequisites
     * before this capability receives the progression-ready
     * ranking bonus.
     */
    val prerequisiteMasteryThreshold: Float =
        DEFAULT_PREREQUISITE_MASTERY_THRESHOLD
) {

    val safePrerequisiteMasteryThreshold: Float
        get() =
            prerequisiteMasteryThreshold.coerceIn(
                minimumValue =
                    0f,
                maximumValue =
                    1f
            )

    companion object {

        const val DEFAULT_PREREQUISITE_MASTERY_THRESHOLD =
            0.55f
    }
}