package com.julesmeister.deutschcraft.feature.settings.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.julesmeister.deutschcraft.feature.settings.data.SettingsRepository
import com.julesmeister.deutschcraft.feature.settings.domain.UserSettings
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repository:
    SettingsRepository
) : ViewModel() {

    val settings: StateFlow<UserSettings> =
        repository
            .settings
            .stateIn(
                scope =
                    viewModelScope,
                started =
                    SharingStarted.WhileSubscribed(
                        stopTimeoutMillis =
                            STATE_STOP_TIMEOUT_MILLIS
                    ),
                initialValue =
                    UserSettings()
            )

    fun onAction(
        action: SettingsUiAction
    ) {
        viewModelScope.launch {
            when (action) {
                is SettingsUiAction.ThemeChanged -> {
                    repository.setAppTheme(
                        theme =
                            action.theme
                    )
                }

                is SettingsUiAction.ProgressLanguageChanged -> {
                    repository.setProgressLanguage(
                        language =
                            action.language
                    )
                }

                is SettingsUiAction.FontScaleChanged -> {
                    repository.setFontScale(
                        option =
                            action.option
                    )
                }

                is SettingsUiAction.ReduceAnimationsChanged -> {
                    repository.setReduceAnimations(
                        enabled =
                            action.enabled
                    )
                }

                is SettingsUiAction.ShowMissionHintsChanged -> {
                    repository.setShowMissionHints(
                        enabled =
                            action.enabled
                    )
                }

                is SettingsUiAction.ShowExamplePhrasesChanged -> {
                    repository.setShowExamplePhrases(
                        enabled =
                            action.enabled
                    )
                }

                is SettingsUiAction.ShowTargetVocabularyChanged -> {
                    repository.setShowTargetVocabulary(
                        enabled =
                            action.enabled
                    )
                }
            }
        }
    }

    private companion object {

        const val STATE_STOP_TIMEOUT_MILLIS =
            5_000L
    }
}