package com.julesmeister.deutschcraft.app.navigation

import kotlinx.serialization.Serializable

@Serializable
object ThreadListDestination

@Serializable
data class ChatDestination(
    val conversationId: String
)

@Serializable
object ProgressDestination

@Serializable
data class AssessmentDestination(
    val assessmentId: String
)