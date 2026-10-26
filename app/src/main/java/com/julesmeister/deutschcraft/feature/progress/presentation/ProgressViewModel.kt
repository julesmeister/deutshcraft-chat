package com.julesmeister.deutschcraft.feature.progress.presentation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.julesmeister.deutschcraft.feature.mission.domain.ConversationMission
import com.julesmeister.deutschcraft.feature.mission.domain.repository.MissionRepository
import com.julesmeister.deutschcraft.feature.progress.domain.ProgressRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class ProgressViewModel @Inject constructor(
    private val progressRepository:
    ProgressRepository,
    private val missionRepository:
    MissionRepository
) : ViewModel() {

    private val _uiState =
        MutableStateFlow(
            ProgressUiState()
        )

    val uiState =
        _uiState.asStateFlow()

    init {
        observeProgress()
        observeCompletedMissions()
    }

    private fun observeProgress() {
        viewModelScope.launch {
            progressRepository
                .observeProgress()
                .catch { throwable ->
                    if (
                        throwable is
                                CancellationException
                    ) {
                        throw throwable
                    }

                    Log.e(
                        VIEW_MODEL_TAG,
                        "Progress observation failed.",
                        throwable
                    )

                    _uiState.update { state ->
                        state.copy(
                            isProgressLoading =
                                false,
                            progressErrorMessage =
                                throwable.message
                                    ?: PROGRESS_LOAD_ERROR
                        )
                    }
                }
                .collect { progress ->
                    _uiState.update { state ->
                        state.copy(
                            progress =
                                progress,
                            isProgressLoading =
                                false,
                            progressErrorMessage =
                                null
                        )
                    }
                }
        }
    }

    private fun observeCompletedMissions() {
        viewModelScope.launch {
            missionRepository
                .observeCompletedMissions()
                .catch { throwable ->
                    if (
                        throwable is
                                CancellationException
                    ) {
                        throw throwable
                    }

                    Log.e(
                        VIEW_MODEL_TAG,
                        "Completed mission observation failed.",
                        throwable
                    )

                    _uiState.update { state ->
                        state.copy(
                            areMissionsLoading =
                                false,
                            missionErrorMessage =
                                throwable.message
                                    ?: MISSION_LOAD_ERROR
                        )
                    }
                }
                .collect { missions ->
                    val completedMissions =
                        missions.map(
                            ::toCompletedMissionUiModel
                        )

                    _uiState.update { state ->
                        state.copy(
                            completedMissions =
                                completedMissions,
                            areMissionsLoading =
                                false,
                            missionErrorMessage =
                                null
                        )
                    }
                }
        }
    }

    private fun toCompletedMissionUiModel(
        mission: ConversationMission
    ): CompletedMissionUiModel {
        val completedObjectiveCount =
            mission
                .objectiveProgress
                .count { progress ->
                    progress.isCompleted
                }

        return CompletedMissionUiModel(
            id =
                mission.id,
            title =
                mission.template.title,
            description =
                mission.template.description,
            icon =
                mission.template.icon,
            completedAt =
                mission.completedAt
                    ?: mission.startedAt
                    ?: 0L,
            completedObjectiveCount =
                completedObjectiveCount,
            totalObjectiveCount =
                mission
                    .objectiveProgress
                    .size
        )
    }

    private companion object {
        const val VIEW_MODEL_TAG =
            "ProgressViewModel"

        const val PROGRESS_LOAD_ERROR =
            "Deine CEFR-Daten konnten nicht " +
                    "geladen werden."

        const val MISSION_LOAD_ERROR =
            "Abgeschlossene Missionen konnten " +
                    "nicht geladen werden."
    }
}