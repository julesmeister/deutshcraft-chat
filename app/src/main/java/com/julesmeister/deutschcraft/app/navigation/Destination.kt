package com.julesmeister.deutschcraft.app.navigation

import kotlinx.serialization.Serializable

@Serializable
data object StartupDestination

@Serializable
data object ThreadListDestination

@Serializable
data class ChatDestination(
    val conversationId: String
)

@Serializable
data object ProgressDestination

@Serializable
data class AssessmentDestination(
    val assessmentId: String
)