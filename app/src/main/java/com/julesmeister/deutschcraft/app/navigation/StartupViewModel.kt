package com.julesmeister.deutschcraft.app.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.julesmeister.deutschcraft.feature.chat.domain.ChatRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class StartupViewModel @Inject constructor(
    private val chatRepository: ChatRepository
) : ViewModel() {

    private val _navigationState =
        MutableStateFlow<StartupNavigationState>(
            StartupNavigationState.Loading
        )

    val navigationState:
            StateFlow<StartupNavigationState> =
        _navigationState.asStateFlow()

    init {
        determineStartDestination()
    }

    private fun determineStartDestination() {
        viewModelScope.launch {
            try {
                val todayConversation =
                    chatRepository
                        .getOpenConversationForToday()

                _navigationState.value =
                    if (todayConversation != null) {
                        StartupNavigationState.OpenChat(
                            conversationId =
                                todayConversation.id
                        )
                    } else {
                        StartupNavigationState
                            .OpenThreadList
                    }
            } catch (
                cancellationException:
                CancellationException
            ) {
                throw cancellationException
            } catch (throwable: Throwable) {
                /*
                 * If the database lookup fails, the
                 * thread list is the safer fallback.
                 */
                _navigationState.value =
                    StartupNavigationState
                        .OpenThreadList
            }
        }
    }
}