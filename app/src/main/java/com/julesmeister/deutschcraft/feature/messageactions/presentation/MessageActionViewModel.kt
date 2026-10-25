package com.julesmeister.deutschcraft.feature.messageactions.presentation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.julesmeister.deutschcraft.feature.messageactions.data.SentenceInsightGateway
import com.julesmeister.deutschcraft.feature.messageactions.domain.SentenceAnalysisType
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class MessageActionViewModel @Inject constructor(
    private val sentenceInsightGateway:
    SentenceInsightGateway
) : ViewModel() {

    private val _uiState =
        MutableStateFlow(
            MessageActionState()
        )

    val uiState: StateFlow<MessageActionState> =
        _uiState.asStateFlow()

    private var analysisJob: Job? = null

    fun analyzeSentence(
        sentence: String,
        action: SentenceAnalysisType
    ) {
        val cleanedSentence =
            sentence.trim()

        if (cleanedSentence.isBlank()) {
            _uiState.update { currentState ->
                currentState.copy(
                    selectedAction = action,
                    isLoading = false,
                    errorMessage =
                        "The selected message is empty."
                )
            }

            return
        }

        val currentState = _uiState.value

        /*
         * Reuse the previous response when another action
         * is selected for the same sentence.
         *
         * Qwen returns Translation, Explanation, and all
         * Rephrases together, so only one request is needed.
         */
        if (
            currentState.analyzedSentence ==
            cleanedSentence &&
            currentState.insight != null
        ) {
            _uiState.update { state ->
                state.copy(
                    selectedAction = action,
                    errorMessage = null
                )
            }

            return
        }

        analysisJob?.cancel()

        analysisJob =
            viewModelScope.launch {
                _uiState.update { state ->
                    state.copy(
                        selectedAction = action,
                        analyzedSentence =
                            cleanedSentence,
                        insight = null,
                        isLoading = true,
                        errorMessage = null
                    )
                }

                try {
                    val insight =
                        sentenceInsightGateway
                            .analyzeSentence(
                                sentence =
                                    cleanedSentence
                            )

                    _uiState.update { state ->
                        state.copy(
                            insight = insight,
                            isLoading = false,
                            errorMessage = null
                        )
                    }
                } catch (
                    cancellationException:
                    CancellationException
                ) {
                    throw cancellationException
                } catch (throwable: Throwable) {
                    Log.e(
                        MESSAGE_ACTION_TAG,
                        "Failed to analyze sentence.",
                        throwable
                    )

                    _uiState.update { state ->
                        state.copy(
                            isLoading = false,
                            errorMessage =
                                throwable.message
                                    ?: "The sentence could not be analyzed."
                        )
                    }
                }
            }
    }

    fun selectAction(
        action: SentenceAnalysisType
    ) {
        _uiState.update { currentState ->
            currentState.copy(
                selectedAction = action
            )
        }
    }

    fun reset() {
        analysisJob?.cancel()
        analysisJob = null

        _uiState.value =
            MessageActionState()
    }

    private companion object {
        const val MESSAGE_ACTION_TAG =
            "MessageActionVM"
    }
}