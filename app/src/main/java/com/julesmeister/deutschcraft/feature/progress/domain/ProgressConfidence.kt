package com.julesmeister.deutschcraft.feature.progress.domain.model

enum class ProgressConfidence(
    val displayName: String,
    val description: String
) {
    NOT_ENOUGH_DATA(
        displayName = "Noch nicht genug Daten",
        description =
            "Schreibe weitere Nachrichten, damit " +
                    "DeutschCraft deinen Fortschritt besser " +
                    "einschätzen kann."
    ),

    EARLY(
        displayName = "Erste Einschätzung",
        description =
            "Deine Einschätzung basiert bisher auf " +
                    "wenigen analysierten Nachrichten."
    ),

    DEVELOPING(
        displayName = "Sich entwickelnd",
        description =
            "DeutschCraft verfügt über genügend Daten, " +
                    "um erkennbare Lernmuster zu zeigen."
    ),

    RELIABLE(
        displayName = "Zuverlässig",
        description =
            "Deine Einschätzung basiert auf einer " +
                    "größeren Anzahl analysierter Nachrichten."
    )
}