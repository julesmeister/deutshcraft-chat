// app/src/main/java/com/julesmeister/deutschcraft/feature/besser/domain/repository/BesserChallengeRepository.kt

package com.julesmeister.deutschcraft.feature.besser.domain.repository

import com.julesmeister.deutschcraft.feature.besser.domain.AssistanceLevel
import com.julesmeister.deutschcraft.feature.besser.domain.BesserChallenge
import kotlinx.coroutines.flow.Flow

interface BesserChallengeRepository {

    suspend fun save(
        challenge: BesserChallenge
    )

    suspend fun getById(
        challengeId: String
    ): BesserChallenge?

    fun observeById(
        challengeId: String
    ): Flow<BesserChallenge?>

    fun observeForMessage(
        messageId: String
    ): Flow<List<BesserChallenge>>

    fun observeActiveForMessage(
        messageId: String
    ): Flow<BesserChallenge?>

    fun observeForConversation(
        conversationId: String
    ): Flow<List<BesserChallenge>>

    fun observeActiveForConversation(
        conversationId: String
    ): Flow<List<BesserChallenge>>

    suspend fun getActiveForMessages(
        messageIds: List<String>
    ): List<BesserChallenge>

    fun observeForCapability(
        capabilityId: String
    ): Flow<List<BesserChallenge>>

    suspend fun accept(
        challengeId: String,
        acceptedAt: Long
    ): Boolean

    suspend fun skip(
        challengeId: String,
        skippedAt: Long
    ): Boolean

    suspend fun complete(
        challengeId: String,
        refinedMessageContent: String,
        actualAssistanceLevel: AssistanceLevel,
        evaluationScore: Int?,
        evaluationFeedback: String?,
        evaluationEvidence: String?,
        completedAt: Long
    ): Boolean

    suspend fun revealNextHint(
        challengeId: String,
        updatedAt: Long
    ): Boolean

    suspend fun countActiveForMessage(
        messageId: String
    ): Int

    suspend fun deleteForMessage(
        messageId: String
    )

    suspend fun deleteForConversation(
        conversationId: String
    )
}