package com.julesmeister.deutschcraft.app.navigation

import kotlinx.serialization.Serializable

@Serializable
data object ChatRoute

@Serializable
data object ProgressRoute

@Serializable
data class ConversationRoute(
    val conversationId: String
)

@Serializable
data class AssessmentRoute(
    val assessmentId: String
)