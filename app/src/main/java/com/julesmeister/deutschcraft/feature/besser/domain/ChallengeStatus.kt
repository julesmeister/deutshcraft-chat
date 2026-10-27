package com.julesmeister.deutschcraft.feature.besser.domain

enum class ChallengeStatus {
    AVAILABLE,
    ACCEPTED,
    SKIPPED,
    COMPLETED;

    val isActive: Boolean
        get() =
            this == AVAILABLE ||
                    this == ACCEPTED

    val isTerminal: Boolean
        get() =
            this == SKIPPED ||
                    this == COMPLETED

    companion object {

        fun fromStorage(
            value: String
        ): ChallengeStatus {
            return entries.firstOrNull { status ->
                status.name.equals(
                    other = value,
                    ignoreCase = true
                )
            } ?: AVAILABLE
        }
    }
}