package com.julesmeister.deutschcraft.feature.archivedthreads.presentation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.julesmeister.deutschcraft.feature.archivedthreads.data.ArchivedThreadsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class ArchivedThreadsViewModel @Inject constructor(
    private val repository: ArchivedThreadsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        ArchivedThreadsUiState()
    )

    val uiState: StateFlow<ArchivedThreadsUiState> =
        _uiState.asStateFlow()

    init {
        observeArchivedThreads()
    }

    fun restoreThread(
        conversationId: String
    ) {
        if (_uiState.value.processingThreadId != null) {
            return
        }

        viewModelScope.launch {
            _uiState.update { currentState ->
                currentState.copy(
                    processingThreadId = conversationId,
                    errorMessage = null
                )
            }

            try {
                repository.restoreThread(
                    conversationId = conversationId
                )

                _uiState.update { currentState ->
                    currentState.copy(
                        processingThreadId = null
                    )
                }
            } catch (
                cancellationException: CancellationException
            ) {
                throw cancellationException
            } catch (throwable: Throwable) {
                Log.e(
                    TAG,
                    "Failed to restore conversation.",
                    throwable
                )

                _uiState.update { currentState ->
                    currentState.copy(
                        processingThreadId = null,
                        errorMessage = throwable.message
                            ?: "The conversation could not be restored."
                    )
                }
            }
        }
    }

    fun deleteThreadPermanently(
        conversationId: String
    ) {
        if (_uiState.value.processingThreadId != null) {
            return
        }

        viewModelScope.launch {
            _uiState.update { currentState ->
                currentState.copy(
                    processingThreadId = conversationId,
                    errorMessage = null
                )
            }

            try {
                repository.deleteThreadPermanently(
                    conversationId = conversationId
                )

                _uiState.update { currentState ->
                    currentState.copy(
                        processingThreadId = null
                    )
                }
            } catch (
                cancellationException: CancellationException
            ) {
                throw cancellationException
            } catch (throwable: Throwable) {
                Log.e(
                    TAG,
                    "Failed to delete archived conversation.",
                    throwable
                )

                _uiState.update { currentState ->
                    currentState.copy(
                        processingThreadId = null,
                        errorMessage = throwable.message
                            ?: "The conversation could not be deleted."
                    )
                }
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

    private fun observeArchivedThreads() {
        viewModelScope.launch {
            repository
                .observeArchivedThreads()
                .catch { throwable ->
                    if (throwable is CancellationException) {
                        throw throwable
                    }

                    Log.e(
                        TAG,
                        "Failed to observe archived conversations.",
                        throwable
                    )

                    _uiState.update { currentState ->
                        currentState.copy(
                            isLoading = false,
                            errorMessage = throwable.message
                                ?: "Archived conversations could not be loaded."
                        )
                    }
                }
                .collect { threads ->
                    _uiState.update { currentState ->
                        currentState.copy(
                            threads = threads,
                            isLoading = false
                        )
                    }
                }
        }
    }

    private companion object {
        const val TAG = "ArchivedThreadsVM"
    }
}