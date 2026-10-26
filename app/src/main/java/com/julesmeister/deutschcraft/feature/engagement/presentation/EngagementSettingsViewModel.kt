package com.julesmeister.deutschcraft.feature.engagement.presentation

import androidx.lifecycle.ViewModel
import com.julesmeister.deutschcraft.feature.engagement.data.EngagementPreferences
import com.julesmeister.deutschcraft.feature.engagement.domain.ScheduleIdlePromptUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

@HiltViewModel
class EngagementSettingsViewModel @Inject constructor(
    private val preferences:
    EngagementPreferences,
    private val scheduleIdlePrompt:
    ScheduleIdlePromptUseCase
) : ViewModel() {

    private val _uiState =
        MutableStateFlow(
            EngagementSettingsState(
                remindersEnabled =
                    preferences.remindersEnabled
            )
        )

    val uiState:
            StateFlow<EngagementSettingsState> =
        _uiState.asStateFlow()

    fun onReminderToggleChanged(
        enabled: Boolean
    ) {
        if (enabled) {
            _uiState.update { state ->
                state.copy(
                    shouldRequestPermission =
                        true
                )
            }
        } else {
            scheduleIdlePrompt.setEnabled(
                enabled = false
            )

            _uiState.update { state ->
                state.copy(
                    remindersEnabled =
                        false,
                    shouldRequestPermission =
                        false
                )
            }
        }
    }

    fun onNotificationPermissionResult(
        granted: Boolean
    ) {
        scheduleIdlePrompt.setEnabled(
            enabled = granted
        )

        _uiState.update { state ->
            state.copy(
                remindersEnabled =
                    granted,
                shouldRequestPermission =
                    false
            )
        }
    }
}