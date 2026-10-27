package com.julesmeister.deutschcraft.core.database.model

data class BesserChallengeStatusUpdate(
    val challengeId: String,
    val status: String,
    val acceptedAt: Long? = null,
    val skippedAt: Long? = null,
    val completedAt: Long? = null,
    val updatedAt: Long
)