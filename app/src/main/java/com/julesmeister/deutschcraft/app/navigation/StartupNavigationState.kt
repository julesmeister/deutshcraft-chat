package com.julesmeister.deutschcraft.app.navigation

sealed interface StartupNavigationState {

    data object Loading :
        StartupNavigationState

    data class OpenChat(
        val conversationId: String
    ) : StartupNavigationState

    data object OpenThreadList :
        StartupNavigationState
}