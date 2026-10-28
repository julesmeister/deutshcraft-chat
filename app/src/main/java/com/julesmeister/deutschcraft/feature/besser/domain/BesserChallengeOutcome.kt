package com.julesmeister.deutschcraft.feature.besser.domain

enum class BesserChallengeOutcome {
    NO_INTERVENTION,
    CORRECTION_NEEDED,
    GROWTH_OPPORTUNITY,
    MISSION_GAP,
    NEEDS_CLARIFICATION;

    companion object {

        fun fromStorage(
            value: String
        ): BesserChallengeOutcome {
            return entries.firstOrNull { outcome ->
                outcome.name.equals(
                    other = value,
                    ignoreCase = true
                )
            } ?: GROWTH_OPPORTUNITY
        }
    }
}