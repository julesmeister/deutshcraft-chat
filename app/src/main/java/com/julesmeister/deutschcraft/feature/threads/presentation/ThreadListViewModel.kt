package com.julesmeister.deutschcraft.feature.threads.presentation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.julesmeister.deutschcraft.feature.threads.domain.ConversationThread
import com.julesmeister.deutschcraft.feature.threads.domain.ThreadRepository
import com.julesmeister.deutschcraft.feature.threads.domain.ThreadSection
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Clock
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class ThreadListViewModel @Inject constructor(
    private val threadRepository:
    ThreadRepository,
    private val clock: Clock
) : ViewModel() {

    private val _uiState =
        MutableStateFlow(
            ThreadListUiState()
        )

    val uiState:
            StateFlow<ThreadListUiState> =
        _uiState.asStateFlow()

    init {
        observeThreads()
    }

    fun dismissError() {
        _uiState.update { currentState ->
            currentState.copy(
                errorMessage = null
            )
        }
    }

    fun archiveThread(
        conversationId: String
    ) {
        viewModelScope.launch {
            try {
                threadRepository.archiveThread(
                    conversationId =
                        conversationId
                )
            } catch (
                cancellationException:
                CancellationException
            ) {
                throw cancellationException
            } catch (throwable: Throwable) {
                Log.e(
                    VIEW_MODEL_TAG,
                    "Failed to archive conversation " +
                            conversationId,
                    throwable
                )

                showError(
                    message =
                        "Die Unterhaltung konnte " +
                                "nicht archiviert werden."
                )
            }
        }
    }

    fun deleteThread(
        conversationId: String
    ) {
        viewModelScope.launch {
            try {
                threadRepository.deleteThread(
                    conversationId =
                        conversationId
                )
            } catch (
                cancellationException:
                CancellationException
            ) {
                throw cancellationException
            } catch (throwable: Throwable) {
                Log.e(
                    VIEW_MODEL_TAG,
                    "Failed to delete conversation " +
                            conversationId,
                    throwable
                )

                showError(
                    message =
                        "Die Unterhaltung konnte " +
                                "nicht gelöscht werden."
                )
            }
        }
    }

    private fun observeThreads() {
        viewModelScope.launch {
            try {
                threadRepository
                    .observeThreads()
                    .collect { threads ->
                        _uiState.update {
                                currentState ->
                            currentState.copy(
                                sections =
                                    groupThreadsByDate(
                                        threads
                                    ),
                                isLoading = false
                            )
                        }
                    }
            } catch (
                cancellationException:
                CancellationException
            ) {
                throw cancellationException
            } catch (throwable: Throwable) {
                Log.e(
                    VIEW_MODEL_TAG,
                    "Failed to observe conversations.",
                    throwable
                )

                _uiState.update { currentState ->
                    currentState.copy(
                        isLoading = false,
                        errorMessage =
                            "Die Unterhaltungen " +
                                    "konnten nicht geladen " +
                                    "werden."
                    )
                }
            }
        }
    }

    private fun groupThreadsByDate(
        threads: List<ConversationThread>
    ): List<ThreadSection> {
        if (threads.isEmpty()) {
            return emptyList()
        }

        val today = currentLocalDate()
        val todayEpochDay = today.toEpochDay()
        val yesterdayEpochDay =
            today.minusDays(1).toEpochDay()

        return threads
            .groupBy { thread ->
                thread.epochDay
            }
            .toSortedMap(
                compareByDescending { epochDay ->
                    epochDay
                }
            )
            .map { (epochDay, dailyThreads) ->
                ThreadSection(
                    id = epochDay,
                    title = createSectionTitle(
                        epochDay = epochDay,
                        todayEpochDay =
                            todayEpochDay,
                        yesterdayEpochDay =
                            yesterdayEpochDay
                    ),
                    threads = dailyThreads
                        .sortedByDescending {
                                thread ->
                            thread.updatedAt
                        }
                )
            }
    }

    private fun createSectionTitle(
        epochDay: Long,
        todayEpochDay: Long,
        yesterdayEpochDay: Long
    ): String {
        return when (epochDay) {
            todayEpochDay -> {
                TODAY_LABEL
            }

            yesterdayEpochDay -> {
                YESTERDAY_LABEL
            }

            else -> {
                formatDate(
                    epochDay = epochDay
                )
            }
        }
    }

    private fun formatDate(
        epochDay: Long
    ): String {
        val date =
            LocalDate.ofEpochDay(epochDay)

        return date.format(
            DATE_FORMATTER
        )
    }

    private fun currentLocalDate():
            LocalDate {
        return clock
            .instant()
            .atZone(clock.zone)
            .toLocalDate()
    }

    private fun showError(
        message: String
    ) {
        _uiState.update { currentState ->
            currentState.copy(
                errorMessage = message
            )
        }
    }

    private companion object {
        const val VIEW_MODEL_TAG =
            "ThreadListViewModel"

        const val TODAY_LABEL =
            "Heute"

        const val YESTERDAY_LABEL =
            "Gestern"

        val DATE_FORMATTER:
                DateTimeFormatter =
            DateTimeFormatter
                .ofLocalizedDate(
                    FormatStyle.LONG
                )
                .withLocale(
                    Locale.GERMAN
                )
    }
}