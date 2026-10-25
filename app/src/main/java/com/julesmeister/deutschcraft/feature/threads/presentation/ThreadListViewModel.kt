package com.julesmeister.deutschcraft.feature.threads.presentation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.julesmeister.deutschcraft.feature.chat.domain.ChatRepository
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
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class ThreadListViewModel @Inject constructor(
    private val threadRepository: ThreadRepository,
    private val chatRepository: ChatRepository,
    private val clock: Clock
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        ThreadListUiState()
    )

    val uiState: StateFlow<ThreadListUiState> =
        _uiState.asStateFlow()

    /*
     * Navigation is a one-time event rather than
     * persistent screen state.
     *
     * A Channel prevents navigation from happening
     * again after configuration changes or recomposition.
     */
    private val navigationEventChannel =
        Channel<ThreadListNavigationEvent>(
            capacity = Channel.BUFFERED
        )

    val navigationEvents:
            Flow<ThreadListNavigationEvent> =
        navigationEventChannel.receiveAsFlow()

    /*
     * A successful archive operation emits a one-time
     * Snackbar event.
     *
     * The Route collects this event and displays:
     *
     * "Conversation title" archived    UNDO
     */
    private val _archiveSnackbarEvents =
        MutableSharedFlow<ArchiveSnackbarEvent>(
            extraBufferCapacity = 1
        )

    val archiveSnackbarEvents =
        _archiveSnackbarEvents.asSharedFlow()

    init {
        observeThreads()
    }

    /*
     * Creates a real conversation in Room and emits
     * its ID so ThreadListRoute can navigate to Chat.
     */
    fun createThread() {
        if (_uiState.value.isCreatingThread) {
            return
        }

        _uiState.update { currentState ->
            currentState.copy(
                isCreatingThread = true,
                errorMessage = null
            )
        }

        viewModelScope.launch {
            try {
                val conversation =
                    chatRepository.createConversation()

                navigationEventChannel.send(
                    ThreadListNavigationEvent
                        .OpenConversation(
                            conversationId =
                                conversation.id
                        )
                )
            } catch (
                cancellationException: CancellationException
            ) {
                throw cancellationException
            } catch (throwable: Throwable) {
                Log.e(
                    VIEW_MODEL_TAG,
                    "Failed to create conversation.",
                    throwable
                )

                showError(
                    message =
                        "Die Unterhaltung konnte " +
                                "nicht erstellt werden."
                )
            } finally {
                _uiState.update { currentState ->
                    currentState.copy(
                        isCreatingThread = false
                    )
                }
            }
        }
    }

    /*
     * Archives a conversation.
     *
     * The conversation title is retrieved before Room
     * removes the item from the active conversation flow.
     *
     * After the archive succeeds, a Snackbar event is
     * emitted so the user can undo the operation.
     */
    fun archiveThread(
        conversationId: String
    ) {
        val conversationTitle =
            findActiveThread(
                conversationId = conversationId
            )?.title ?: DEFAULT_CONVERSATION_TITLE

        viewModelScope.launch {
            try {
                threadRepository.archiveThread(
                    conversationId = conversationId
                )

                _archiveSnackbarEvents.emit(
                    ArchiveSnackbarEvent(
                        conversationId = conversationId,
                        conversationTitle =
                            conversationTitle
                    )
                )
            } catch (
                cancellationException: CancellationException
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

    /*
     * Restores an archived conversation.
     *
     * This is used both by the archived conversations
     * screen and by the UNDO Snackbar action.
     */
    fun restoreThread(
        conversationId: String
    ) {
        viewModelScope.launch {
            try {
                threadRepository.restoreThread(
                    conversationId = conversationId
                )
            } catch (
                cancellationException: CancellationException
            ) {
                throw cancellationException
            } catch (throwable: Throwable) {
                Log.e(
                    VIEW_MODEL_TAG,
                    "Failed to restore conversation " +
                            conversationId,
                    throwable
                )

                showError(
                    message =
                        "Die Unterhaltung konnte nicht " +
                                "wiederhergestellt werden."
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
                    conversationId = conversationId
                )
            } catch (
                cancellationException: CancellationException
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

    fun dismissError() {
        _uiState.update { currentState ->
            currentState.copy(
                errorMessage = null
            )
        }
    }

    /*
     * Finds the active thread before it disappears after
     * archiving.
     *
     * We need its title for the Snackbar message.
     */
    private fun findActiveThread(
        conversationId: String
    ): ConversationThread? {
        return _uiState.value.sections
            .asSequence()
            .flatMap { section ->
                section.threads.asSequence()
            }
            .firstOrNull { thread ->
                thread.id == conversationId
            }
    }

    private fun observeThreads() {
        viewModelScope.launch {
            try {
                combine(
                    threadRepository.observeThreads(),
                    threadRepository.observeArchivedThreads()
                ) { activeThreads, archivedThreads ->
                    activeThreads to archivedThreads
                }.collect {
                        (
                            activeThreads,
                            archivedThreads
                        ) ->

                    _uiState.update { currentState ->
                        currentState.copy(
                            sections =
                                groupThreadsByDate(
                                    threads = activeThreads
                                ),
                            archivedThreads =
                                archivedThreads,
                            isLoading = false
                        )
                    }
                }
            } catch (
                cancellationException: CancellationException
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
                            "Die Unterhaltungen konnten " +
                                    "nicht geladen werden."
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

        val today =
            currentLocalDate()

        val todayEpochDay =
            today.toEpochDay()

        val yesterdayEpochDay =
            today
                .minusDays(1)
                .toEpochDay()

        return threads
            .groupBy { thread ->
                thread.epochDay
            }
            .toSortedMap(
                compareByDescending { epochDay ->
                    epochDay
                }
            )
            .map { entry ->
                val epochDay =
                    entry.key

                val dailyThreads =
                    entry.value

                ThreadSection(
                    id = epochDay,
                    title = createSectionTitle(
                        epochDay = epochDay,
                        todayEpochDay =
                            todayEpochDay,
                        yesterdayEpochDay =
                            yesterdayEpochDay
                    ),
                    threads =
                        dailyThreads
                            .sortedByDescending { thread ->
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
            LocalDate.ofEpochDay(
                epochDay
            )

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

        const val DEFAULT_CONVERSATION_TITLE =
            "Unterhaltung"

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