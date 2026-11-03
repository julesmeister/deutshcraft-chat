// app/src/main/java/com/julesmeister/deutschcraft/feature/chat/presentation/progress/ChatLevelProgressViewModel.kt

package com.julesmeister.deutschcraft.feature.chat.presentation.progress

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.julesmeister.deutschcraft.core.cefr.model.CefrLevel
import com.julesmeister.deutschcraft.core.cefr.model.DefaultLearnerProfile
import com.julesmeister.deutschcraft.core.database.dao.LearnerProfileDao
import com.julesmeister.deutschcraft.core.database.entity.LearnerProfileEntity
import com.julesmeister.deutschcraft.feature.chat.presentation.theme.ChatPresentationTokens
import com.julesmeister.deutschcraft.feature.progress.domain.ProgressRepository
import com.julesmeister.deutschcraft.feature.progress.domain.model.ProgressSummary
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class ChatLevelProgressViewModel
@Inject constructor(
    private val learnerProfileDao:
    LearnerProfileDao,

    private val progressRepository:
    ProgressRepository,

    private val selectImmediateLevelNeed:
    SelectImmediateLevelNeedUseCase
) : ViewModel() {

    private val _uiState =
        MutableStateFlow(
            ChatLevelProgressUiState()
        )

    val uiState:
            StateFlow<ChatLevelProgressUiState> =
        _uiState.asStateFlow()

    init {
        observeLevelProgress()
    }

    private fun observeLevelProgress() {
        viewModelScope.launch {
            combine(
                learnerProfileDao.observeProfile(
                    profileId =
                        DefaultLearnerProfile.LOCAL_PROFILE_ID
                ),

                progressRepository.observeProgress()
            ) {
                    profile,
                    progress ->

                ProgressSources(
                    profile =
                        profile,

                    progress =
                        progress
                )
            }
                .mapLatest { sources ->
                    buildUiState(
                        profile =
                            sources.profile,

                        progress =
                            sources.progress
                    )
                }
                .catch { throwable ->
                    if (
                        throwable is
                                CancellationException
                    ) {
                        throw throwable
                    }

                    Log.e(
                        ChatPresentationTokens.LevelProgressViewModel.ViewModelTag,
                        "Chat level progress observation failed.",
                        throwable
                    )

                    _uiState.update { state ->
                        state.copy(
                            isLoading =
                                false
                        )
                    }
                }
                .collect { state ->
                    _uiState.value =
                        state
                }
        }
    }

    private suspend fun buildUiState(
        profile:
        LearnerProfileEntity?,

        progress:
        ProgressSummary?
    ): ChatLevelProgressUiState {
        val currentLevel =
            profile?.currentLevel
                ?: progress?.currentLevel
                ?: CefrLevel.A1

        val readinessPercentage =
            profile?.readinessPercentage
                ?: progress?.readinessPercentage
                ?: 0

        val assessedMessageCount =
            maxOf(
                profile?.assessedMessageCount
                    ?: 0,

                progress?.assessedMessageCount
                    ?: 0
            )

        val activeDayCount =
            maxOf(
                profile?.activeDayCount
                    ?: 0,

                progress?.activeDayCount
                    ?: 0
            )

        val immediateNeed =
            try {
                selectImmediateLevelNeed(
                    currentLevel =
                        currentLevel
                )
            } catch (
                cancellationException:
                CancellationException
            ) {
                throw cancellationException
            } catch (throwable: Throwable) {
                Log.e(
                    ChatPresentationTokens.LevelProgressViewModel.ViewModelTag,
                    "Immediate level need could not be selected.",
                    throwable
                )

                null
            }

        return ChatLevelProgressUiState(
            isLoading =
                profile == null &&
                        progress == null,

            currentLevel =
                currentLevel,

            readinessPercentage =
                readinessPercentage.coerceIn(
                    minimumValue = 0,
                    maximumValue = 100
                ),

            suggestedNextLevel =
                profile?.suggestedNextLevel,

            assessedMessageCount =
                assessedMessageCount.coerceAtLeast(
                    minimumValue = 0
                ),

            activeDayCount =
                activeDayCount.coerceAtLeast(
                    minimumValue = 0
                ),

            immediateNeed =
                immediateNeed
        )
    }

    private data class ProgressSources(
        val profile: LearnerProfileEntity?,
        val progress: ProgressSummary?
    )

}
