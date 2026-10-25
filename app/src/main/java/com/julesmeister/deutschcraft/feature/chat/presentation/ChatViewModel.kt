package com.julesmeister.deutschcraft.feature.chat.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.julesmeister.deutschcraft.app.navigation.ChatDestination
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.StateFlow

@HiltViewModel
class ChatViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val coordinator:
    ChatSessionCoordinator
) : ViewModel() {

    val uiState: StateFlow<ChatUiState> =
        coordinator.uiState

    init {
        val destination =
            savedStateHandle
                .toRoute<ChatDestination>()

        coordinator.start(
            conversationId =
                destination.conversationId,
            scope =
                viewModelScope
        )
    }

    fun onAction(
        action: ChatUiAction
    ) {
        coordinator.onAction(
            action = action,
            scope = viewModelScope
        )
    }

    override fun onCleared() {
        coordinator.stop()
        super.onCleared()
    }
}