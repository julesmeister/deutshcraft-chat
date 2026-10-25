package com.julesmeister.deutschcraft.feature.threads.presentation

sealed interface ThreadListNavigationEvent {

    data class OpenConversation(
        val conversationId: String
    ) : ThreadListNavigationEvent
}