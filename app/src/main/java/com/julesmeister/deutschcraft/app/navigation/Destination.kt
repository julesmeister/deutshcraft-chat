package com.julesmeister.deutschcraft.navigation

import kotlinx.serialization.Serializable

@Serializable
data object ChatDestination

@Serializable
data object ProgressDestination

@Serializable
data class ConversationDestination(
    val conversationId: String
)

@Serializable
data class AssessmentDestination(
    val assessmentId: String
)