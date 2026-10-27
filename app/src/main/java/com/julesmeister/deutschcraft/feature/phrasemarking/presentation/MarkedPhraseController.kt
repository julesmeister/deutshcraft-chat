package com.julesmeister.deutschcraft.feature.phrasemarking.presentation

import android.util.Log
import com.julesmeister.deutschcraft.feature.phrasemarking.data.MarkedPhraseRepository
import com.julesmeister.deutschcraft.feature.phrasemarking.domain.MarkPhraseUseCase
import com.julesmeister.deutschcraft.feature.phrasemarking.domain.MarkedPhrase
import com.julesmeister.deutschcraft.feature.phrasemarking.domain.MarkedPhraseType
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/*
 * Owns marked-phrase state for one open conversation.
 *
 * phrasesByMessage maps messageId -> phrases marked on that
 * message, so ChatMessageList can attach bubbles under it.
 */
class MarkedPhraseController
@Inject constructor(
    private val repository: MarkedPhraseRepository,
    private val markPhrase: MarkPhraseUseCase
) {

    private val _phrasesByMessage =
        MutableStateFlow<Map<String, List<MarkedPhrase>>>(
            emptyMap()
        )

    val phrasesByMessage:
            StateFlow<Map<String, List<MarkedPhrase>>> =
        _phrasesByMessage.asStateFlow()

    private var observationJob: Job? = null

    fun start(
        conversationId: String,
        scope: CoroutineScope
    ) {
        observationJob?.cancel()

        observationJob =
            scope.launch {
                try {
                    repository
                        .observe(conversationId)
                        .collect { phrases ->
                            _phrasesByMessage.value =
                                phrases.groupBy { phrase ->
                                    phrase.messageId
                                }
                        }
                } catch (cancellation: CancellationException) {
                    throw cancellation
                } catch (throwable: Throwable) {
                    Log.e(
                        CONTROLLER_TAG,
                        "Marked phrase observation failed.",
                        throwable
                    )
                }
            }
    }

    fun mark(
        conversationId: String,
        messageId: String,
        phrase: String,
        sourceSentence: String,
        type: MarkedPhraseType,
        scope: CoroutineScope
    ) {
        scope.launch {
            try {
                markPhrase(
                    conversationId = conversationId,
                    messageId = messageId,
                    phrase = phrase,
                    sourceSentence = sourceSentence,
                    type = type
                )
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (throwable: Throwable) {
                Log.e(
                    CONTROLLER_TAG,
                    "Phrase could not be marked.",
                    throwable
                )
            }
        }
    }

    fun remove(
        id: String,
        scope: CoroutineScope
    ) {
        scope.launch {
            try {
                repository.delete(id)
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (throwable: Throwable) {
                Log.e(
                    CONTROLLER_TAG,
                    "Phrase could not be removed.",
                    throwable
                )
            }
        }
    }

    fun stop() {
        observationJob?.cancel()
        observationJob = null
    }

    private companion object {
        const val CONTROLLER_TAG = "MarkedPhrases"
    }
}