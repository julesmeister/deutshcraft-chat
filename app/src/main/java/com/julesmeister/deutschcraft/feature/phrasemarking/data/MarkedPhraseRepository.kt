package com.julesmeister.deutschcraft.feature.phrasemarking.data

import com.julesmeister.deutschcraft.core.database.dao.MarkedPhraseDao
import com.julesmeister.deutschcraft.core.database.entity.learning.MarkedPhraseEntity
import com.julesmeister.deutschcraft.feature.phrasemarking.domain.ExplanationStatus
import com.julesmeister.deutschcraft.feature.phrasemarking.domain.MarkedPhrase
import com.julesmeister.deutschcraft.feature.phrasemarking.domain.MarkedPhraseType
import com.julesmeister.deutschcraft.feature.phrasemarking.domain.PhraseExplanation
import java.time.Clock
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Singleton
class MarkedPhraseRepository
@Inject constructor(
    private val dao: MarkedPhraseDao,
    private val clock: Clock
) {

    fun observe(
        conversationId: String
    ): Flow<List<MarkedPhrase>> {
        return dao
            .observeForConversation(conversationId)
            .map { entities ->
                entities.mapNotNull { entity -> entity.toDomainOrNull() }
            }
    }

    /*
     * Returns the new ID, or null when the phrase was
     * already marked on this message.
     */
    suspend fun create(
        conversationId: String,
        messageId: String,
        phrase: String,
        sourceSentence: String,
        type: MarkedPhraseType
    ): String? {
        val now = clock.millis()
        val id = UUID.randomUUID().toString()

        val result =
            dao.insert(
                MarkedPhraseEntity(
                    id = id,
                    conversationId = conversationId,
                    messageId = messageId,
                    phrase = phrase.trim(),
                    sourceSentence = sourceSentence.trim(),
                    markType = type.name,
                    meaning = null,
                    explanation = null,
                    example = null,
                    explanationStatus = ExplanationStatus.PENDING.name,
                    createdAt = now,
                    updatedAt = now
                )
            )

        return if (result == -1L) null else id
    }

    suspend fun saveExplanation(
        id: String,
        explanation: PhraseExplanation
    ) {
        dao.updateExplanation(
            id = id,
            meaning = explanation.meaning,
            explanation = explanation.explanation,
            example = explanation.example,
            status = ExplanationStatus.READY.name,
            updatedAt = clock.millis()
        )
    }

    suspend fun markFailed(id: String) {
        dao.updateStatus(
            id = id,
            status = ExplanationStatus.FAILED.name,
            updatedAt = clock.millis()
        )
    }

    suspend fun delete(id: String) {
        dao.delete(id)
    }

    private fun MarkedPhraseEntity.toDomainOrNull(): MarkedPhrase? {
        val type =
            runCatching { MarkedPhraseType.valueOf(markType) }
                .getOrNull()
                ?: return null

        val status =
            runCatching { ExplanationStatus.valueOf(explanationStatus) }
                .getOrDefault(ExplanationStatus.FAILED)

        return MarkedPhrase(
            id = id,
            conversationId = conversationId,
            messageId = messageId,
            phrase = phrase,
            sourceSentence = sourceSentence,
            type = type,
            meaning = meaning,
            explanation = explanation,
            example = example,
            status = status,
            createdAt = createdAt
        )
    }
}