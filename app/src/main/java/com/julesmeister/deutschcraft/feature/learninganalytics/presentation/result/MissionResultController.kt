package com.julesmeister.deutschcraft.feature.learninganalytics.presentation.result

import com.julesmeister.deutschcraft.feature.learninganalytics.domain.MissionAnalytics
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

@Singleton
class MissionResultController
@Inject constructor() {

    private val _uiState =
        MutableStateFlow(
            MissionResultUiState()
        )

    val uiState:
            StateFlow<MissionResultUiState> =
        _uiState.asStateFlow()

    fun showResult(
        missionTitle: String,
        analytics:
        MissionAnalytics
    ) {
        _uiState.value =
            MissionResultUiState.from(
                missionTitle =
                    missionTitle,
                analytics =
                    analytics
            )
    }

    fun dismiss() {
        _uiState.value =
            _uiState
                .value
                .copy(
                    isVisible =
                        false
                )
    }

    fun clear() {
        _uiState.value =
            MissionResultUiState()
    }
}