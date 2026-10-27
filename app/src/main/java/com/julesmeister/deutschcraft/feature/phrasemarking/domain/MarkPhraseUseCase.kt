package com.julesmeister.deutschcraft.feature.phrasemarking.domain

import com.julesmeister.deutschcraft.feature.phrasemarking.data.MarkedPhraseExplanationGateway
import com.julesmeister.deutschcraft.feature.phrasemarking.data.MarkedPhraseRepository
import javax.inject.Inject

/*
 * Saves the mark immediately, so the bubble appears in a
 * PENDING state, then fills in the explanation.
 *
 * Returns false when the phrase was already marked on the
 * same message.
 */
class MarkPhraseUseCase
@Inject constructor(
    private val repository: MarkedPhraseRepository,
    private val gateway: MarkedPhraseExplanationGateway
) {

    suspend operator fun invoke(
        conversationId: String,
        messageId: String,
        phrase: String,
        sourceSentence: String,
        type: MarkedPhraseType
    ): Boolean {
        if (phrase.isBlank()) {
            return false
        }

        val id =
            repository.create(
                conversationId = conversationId,
                messageId = messageId,
                phrase = phrase,
                sourceSentence = sourceSentence,
                type = type
            ) ?: return false

        val explanation =
            gateway.explain(
                phrase = phrase,
                sourceSentence = sourceSentence,
                type = type
            )

        if (explanation == null) {
            repository.markFailed(id)
        } else {
            repository.saveExplanation(
                id = id,
                explanation = explanation
            )
        }

        return true
    }
}