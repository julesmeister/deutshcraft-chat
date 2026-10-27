package com.julesmeister.deutschcraft

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.julesmeister.deutschcraft.feature.settings.data.SettingsRepository
import com.julesmeister.deutschcraft.feature.settings.domain.UserSettings
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class MainViewModel @Inject constructor(
    repository: SettingsRepository
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
                            5_000L
                    ),
                initialValue =
                    UserSettings()
            )
}