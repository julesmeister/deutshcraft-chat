package com.julesmeister.deutschcraft.feature.besser.domain

enum class AssistanceLevel(
    val evidenceWeight: Float
) {
    INDEPENDENT(
        evidenceWeight = 1.0f
    ),

    PROMPTED(
        evidenceWeight = 0.8f
    ),

    SCAFFOLDED(
        evidenceWeight = 0.55f
    ),

    MODELED(
        evidenceWeight = 0.3f
    );

    companion object {

        fun fromStorage(
            value: String?
        ): AssistanceLevel? {
            if (value.isNullOrBlank()) {
                return null
            }

            return entries.firstOrNull { level ->
                level.name.equals(
                    other = value,
                    ignoreCase = true
                )
            }
        }
    }
}