package com.julesmeister.deutschcraft.feature.besser.domain

enum class CapabilityCategory {
    GRAMMAR,
    VOCABULARY,
    FLUENCY,
    INTERACTION,
    COHERENCE,
    PRAGMATICS,
    COMPREHENSION,
    MISSION;

    companion object {

        fun fromStorage(
            value: String
        ): CapabilityCategory {
            return entries.firstOrNull { category ->
                category.name.equals(
                    other = value,
                    ignoreCase = true
                )
            } ?: FLUENCY
        }
    }
}