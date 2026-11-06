// app/src/main/java/com/julesmeister/deutschcraft/feature/besser/data/repository/LocalBesserChallengeRepository.kt

package com.julesmeister.deutschcraft.feature.besser.data.repository

import androidx.room.withTransaction
import com.julesmeister.deutschcraft.core.database.DeutschCraftDatabase
import com.julesmeister.deutschcraft.core.database.dao.BesserChallengeDao
import com.julesmeister.deutschcraft.core.database.dao.MessageDao
import com.julesmeister.deutschcraft.core.database.entity.SyncStatusEntity
import com.julesmeister.deutschcraft.feature.besser.data.mapper.toDomain
import com.julesmeister.deutschcraft.feature.besser.data.mapper.toEntity
import com.julesmeister.deutschcraft.feature.besser.data.mapper.toHintEntities
import com.julesmeister.deutschcraft.feature.besser.domain.AssistanceLevel
import com.julesmeister.deutschcraft.feature.besser.domain.BesserChallenge
import com.julesmeister.deutschcraft.feature.besser.domain.repository.BesserChallengeRepository
import com.julesmeister.deutschcraft.feature.sync.scheduling.SyncScheduler
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class LocalBesserChallengeRepository
@Inject constructor(
    private val database:
    DeutschCraftDatabase,

    private val besserChallengeDao:
    BesserChallengeDao,

    private val messageDao:
    MessageDao,

    private val syncScheduler:
    SyncScheduler
) : BesserChallengeRepository {

    override suspend fun save(
        challenge: BesserChallenge
    ) {
        require(
            challenge.id.isNotBlank()
        ) {
            "Besser challenge ID cannot be blank."
        }

        require(
            challenge.messageId.isNotBlank()
        ) {
            "Besser challenge message ID cannot be blank."
        }

        require(
            challenge.conversationId.isNotBlank()
        ) {
            "Besser challenge conversation ID cannot be blank."
        }

        require(
            challenge.capabilityId.isNotBlank()
        ) {
            "Besser challenge capability ID cannot be blank."
        }

        require(
            challenge.instruction.isNotBlank()
        ) {
            "Besser challenge instruction cannot be blank."
        }

        besserChallengeDao
            .upsertChallengeWithHints(
                challenge =
                    challenge.toEntity(),
                hints =
                    challenge.toHintEntities()
            )
    }

    override suspend fun getById(
        challengeId: String
    ): BesserChallenge? {
        return besserChallengeDao
            .getById(
                challengeId =
                    challengeId
            )
            ?.toDomain()
    }

    override fun observeById(
        challengeId: String
    ): Flow<BesserChallenge?> {
        return besserChallengeDao
            .observeById(
                challengeId =
                    challengeId
            )
            .map { relation ->
                relation?.toDomain()
            }
    }

    override fun observeForMessage(
        messageId: String
    ): Flow<List<BesserChallenge>> {
        return besserChallengeDao
            .observeForMessage(
                messageId =
                    messageId
            )
            .map { relations ->
                relations.map { relation ->
                    relation.toDomain()
                }
            }
    }

    override fun observeActiveForMessage(
        messageId: String
    ): Flow<BesserChallenge?> {
        return besserChallengeDao
            .observeActiveForMessage(
                messageId =
                    messageId
            )
            .map { relation ->
                relation?.toDomain()
            }
    }

    override fun observeForConversation(
        conversationId: String
    ): Flow<List<BesserChallenge>> {
        return besserChallengeDao
            .observeForConversation(
                conversationId =
                    conversationId
            )
            .map { relations ->
                relations.map { relation ->
                    relation.toDomain()
                }
            }
    }

    override fun observeActiveForConversation(
        conversationId: String
    ): Flow<List<BesserChallenge>> {
        return besserChallengeDao
            .observeActiveForConversation(
                conversationId =
                    conversationId
            )
            .map { relations ->
                relations.map { relation ->
                    relation.toDomain()
                }
            }
    }

    override suspend fun getActiveForMessages(
        messageIds: List<String>
    ): List<BesserChallenge> {
        val normalizedMessageIds =
            messageIds
                .map(
                    String::trim
                )
                .filter(
                    String::isNotBlank
                )
                .distinct()

        if (
            normalizedMessageIds.isEmpty()
        ) {
            return emptyList()
        }

        return besserChallengeDao
            .getActiveForMessages(
                messageIds =
                    normalizedMessageIds
            )
            .map { entity ->
                entity.toDomain(
                    hints =
                        emptyList()
                )
            }
    }

    override fun observeForCapability(
        capabilityId: String
    ): Flow<List<BesserChallenge>> {
        return besserChallengeDao
            .observeForCapability(
                capabilityId =
                    capabilityId
            )
            .map { entities ->
                entities.map { entity ->
                    entity.toDomain(
                        hints =
                            emptyList()
                    )
                }
            }
    }

    override suspend fun accept(
        challengeId: String,
        acceptedAt: Long
    ): Boolean {
        return besserChallengeDao.accept(
            challengeId =
                challengeId,
            acceptedAt =
                acceptedAt
        ) == 1
    }

    override suspend fun skip(
        challengeId: String,
        skippedAt: Long
    ): Boolean {
        return besserChallengeDao.skip(
            challengeId =
                challengeId,
            skippedAt =
                skippedAt
        ) == 1
    }

    override suspend fun complete(
        challengeId: String,
        refinedMessageContent: String,
        actualAssistanceLevel:
        AssistanceLevel,
        evaluationScore: Int?,
        evaluationFeedback: String?,
        evaluationEvidence: String?,
        completedAt: Long
    ): Boolean {
        require(
            challengeId.isNotBlank()
        ) {
            "Challenge ID cannot be blank."
        }

        val normalizedRefinedContent =
            refinedMessageContent.trim()

        require(
            normalizedRefinedContent.isNotBlank()
        ) {
            "Refined message content cannot be blank."
        }

        val normalizedEvaluationScore =
            evaluationScore?.coerceIn(
                minimumValue =
                    0,
                maximumValue =
                    100
            )

        val normalizedEvaluationFeedback =
            evaluationFeedback
                ?.trim()
                ?.takeIf(
                    String::isNotBlank
                )

        val normalizedEvaluationEvidence =
            evaluationEvidence
                ?.trim()
                ?.takeIf(
                    String::isNotBlank
                )

        val wasCompleted =
            database.withTransaction {
                val challenge =
                    besserChallengeDao
                        .getById(
                            challengeId =
                                challengeId
                        )
                        ?.toDomain()
                        ?: return@withTransaction false

                if (!challenge.canComplete) {
                    return@withTransaction false
                }

                val updatedMessageRows =
                    messageDao
                        .replaceLearnerMessageContent(
                            messageId =
                                challenge.messageId,
                            refinedContent =
                                normalizedRefinedContent,
                            updatedAt =
                                completedAt,
                            syncStatus =
                                SyncStatusEntity.PENDING_UPLOAD
                        )

                check(
                    updatedMessageRows == 1
                ) {
                    "The source learner message could not be updated."
                }

                val completedChallengeRows =
                    besserChallengeDao.complete(
                        challengeId =
                            challengeId,
                        refinedMessageContent =
                            normalizedRefinedContent,
                        actualAssistanceLevel =
                            actualAssistanceLevel.name,
                        evaluationScore =
                            normalizedEvaluationScore,
                        evaluationFeedback =
                            normalizedEvaluationFeedback,
                        evaluationEvidence =
                            normalizedEvaluationEvidence,
                        completedAt =
                            completedAt
                    )

                check(
                    completedChallengeRows == 1
                ) {
                    "The Besser challenge could not be completed."
                }

                true
            }

        /*
         * Schedule synchronization only after the Room
         * transaction has committed successfully.
         *
         * The message itself is the synchronized object here.
         * Besser challenge persistence is still local in Phase
         * 3.1, but the refined learner message is marked
         * PENDING_UPLOAD inside the transaction above.
         */
        if (wasCompleted) {
            syncScheduler.requestImmediateSync()
        }

        return wasCompleted
    }

    override suspend fun revealNextHint(
        challengeId: String,
        updatedAt: Long
    ): Boolean {
        val beforeReveal =
            besserChallengeDao
                .getRevealedHintCount(
                    challengeId =
                        challengeId
                )
                ?: return false

        val changedRows =
            besserChallengeDao
                .revealNextHint(
                    challengeId =
                        challengeId,
                    updatedAt =
                        updatedAt
                )

        if (changedRows != 1) {
            return false
        }

        val afterReveal =
            besserChallengeDao
                .getRevealedHintCount(
                    challengeId =
                        challengeId
                )
                ?: return false

        return afterReveal >
                beforeReveal
    }

    override suspend fun countActiveForMessage(
        messageId: String
    ): Int {
        return besserChallengeDao
            .countActiveForMessage(
                messageId =
                    messageId
            )
    }

    override suspend fun deleteForMessage(
        messageId: String
    ) {
        besserChallengeDao
            .deleteForMessage(
                messageId =
                    messageId
            )
    }

    override suspend fun deleteForConversation(
        conversationId: String
    ) {
        besserChallengeDao
            .deleteForConversation(
                conversationId =
                    conversationId
            )
    }
}