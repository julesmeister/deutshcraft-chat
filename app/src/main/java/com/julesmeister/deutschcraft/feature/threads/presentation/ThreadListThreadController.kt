package com.julesmeister.deutschcraft.feature.threads.presentation

import android.util.Log
import com.julesmeister.deutschcraft.feature.chat.domain.Conversation
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class ThreadListThreadController @Inject constructor(
    private val threadListObserver:
    ThreadListObserver,
    private val threadListOperations:
    ThreadListOperations
) {

    private var observationJob: Job? =
        null

    fun startObserving(
        scope: CoroutineScope,
        onSnapshot: (ThreadListsSnapshot) -> Unit,
        onFailure: (String) -> Unit
    ) {
        if (observationJob?.isActive == true) {
            return
        }

        observationJob =
            threadListObserver
                .observe()
                .onEach { snapshot ->
                    onSnapshot(
                        snapshot
                    )
                }
                .launchIn(scope)

        observationJob?.invokeOnCompletion { throwable ->
            if (
                throwable != null &&
                throwable !is CancellationException
            ) {
                Log.e(
                    CONTROLLER_TAG,
                    "Failed to observe conversations.",
                    throwable
                )

                onFailure(
                    LOAD_CONVERSATIONS_ERROR
                )
            }
        }
    }

    fun createConversation(
        scope: CoroutineScope,
        onSuccess: suspend (Conversation) -> Unit,
        onFailure: (String) -> Unit,
        onFinished: () -> Unit
    ) {
        scope.launch {
            try {
                val conversation =
                    threadListOperations
                        .createConversation()

                onSuccess(
                    conversation
                )
            } catch (
                cancellationException:
                CancellationException
            ) {
                throw cancellationException
            } catch (throwable: Throwable) {
                logFailure(
                    message =
                        "Failed to create conversation.",
                    throwable =
                        throwable
                )

                onFailure(
                    CREATE_CONVERSATION_ERROR
                )
            } finally {
                onFinished()
            }
        }
    }

    fun archiveConversation(
        conversationId: String,
        conversationTitle: String,
        scope: CoroutineScope,
        onSuccess:
        suspend (ArchiveSnackbarEvent) -> Unit,
        onFailure: (String) -> Unit
    ) {
        scope.launch {
            try {
                val event =
                    threadListOperations
                        .archiveConversation(
                            conversationId =
                                conversationId,
                            conversationTitle =
                                conversationTitle
                        )

                onSuccess(
                    event
                )
            } catch (
                cancellationException:
                CancellationException
            ) {
                throw cancellationException
            } catch (throwable: Throwable) {
                logFailure(
                    message =
                        "Failed to archive conversation " +
                                conversationId,
                    throwable =
                        throwable
                )

                onFailure(
                    ARCHIVE_CONVERSATION_ERROR
                )
            }
        }
    }

    fun restoreConversation(
        conversationId: String,
        scope: CoroutineScope,
        onFailure: (String) -> Unit
    ) {
        scope.launch {
            try {
                threadListOperations
                    .restoreConversation(
                        conversationId =
                            conversationId
                    )
            } catch (
                cancellationException:
                CancellationException
            ) {
                throw cancellationException
            } catch (throwable: Throwable) {
                logFailure(
                    message =
                        "Failed to restore conversation " +
                                conversationId,
                    throwable =
                        throwable
                )

                onFailure(
                    RESTORE_CONVERSATION_ERROR
                )
            }
        }
    }

    fun deleteConversation(
        conversationId: String,
        scope: CoroutineScope,
        onFailure: (String) -> Unit
    ) {
        scope.launch {
            try {
                threadListOperations
                    .deleteConversation(
                        conversationId =
                            conversationId
                    )
            } catch (
                cancellationException:
                CancellationException
            ) {
                throw cancellationException
            } catch (throwable: Throwable) {
                logFailure(
                    message =
                        "Failed to delete conversation " +
                                conversationId,
                    throwable =
                        throwable
                )

                onFailure(
                    DELETE_CONVERSATION_ERROR
                )
            }
        }
    }

    private fun logFailure(
        message: String,
        throwable: Throwable
    ) {
        Log.e(
            CONTROLLER_TAG,
            message,
            throwable
        )
    }

    private companion object {
        const val CONTROLLER_TAG =
            "ThreadListThreadController"

        const val CREATE_CONVERSATION_ERROR =
            "Die Unterhaltung konnte nicht " +
                    "erstellt werden."

        const val ARCHIVE_CONVERSATION_ERROR =
            "Die Unterhaltung konnte nicht " +
                    "archiviert werden."

        const val RESTORE_CONVERSATION_ERROR =
            "Die Unterhaltung konnte nicht " +
                    "wiederhergestellt werden."

        const val DELETE_CONVERSATION_ERROR =
            "Die Unterhaltung konnte nicht " +
                    "gelöscht werden."

        const val LOAD_CONVERSATIONS_ERROR =
            "Die Unterhaltungen konnten nicht " +
                    "geladen werden."
    }
}