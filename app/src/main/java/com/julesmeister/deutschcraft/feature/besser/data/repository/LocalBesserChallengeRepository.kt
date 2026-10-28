package com.julesmeister.deutschcraft.feature.besser.data.repository

import com.julesmeister.deutschcraft.core.database.dao.BesserChallengeDao
import com.julesmeister.deutschcraft.feature.besser.data.mapper.toDomain
import com.julesmeister.deutschcraft.feature.besser.data.mapper.toEntity
import com.julesmeister.deutschcraft.feature.besser.data.mapper.toHintEntities
import com.julesmeister.deutschcraft.feature.besser.domain.AssistanceLevel
import com.julesmeister.deutschcraft.feature.besser.domain.BesserChallenge
import com.julesmeister.deutschcraft.feature.besser.domain.repository.BesserChallengeRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class LocalBesserChallengeRepository @Inject constructor(
    private val besserChallengeDao:
    BesserChallengeDao
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

        besserChallengeDao.upsertChallengeWithHints(
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
                .filter(
                    String::isNotBlank
                )
                .distinct()

        if (normalizedMessageIds.isEmpty()) {
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
        actualAssistanceLevel: AssistanceLevel,
        completedAt: Long
    ): Boolean {
        require(
            refinedMessageContent.isNotBlank()
        ) {
            "Refined message content cannot be blank."
        }

        return besserChallengeDao.complete(
            challengeId =
                challengeId,
            refinedMessageContent =
                refinedMessageContent.trim(),
            actualAssistanceLevel =
                actualAssistanceLevel.name,
            completedAt =
                completedAt
        ) == 1
    }

    override suspend fun revealNextHint(
        challengeId: String,
        updatedAt: Long
    ): Boolean {
        val beforeReveal =
            besserChallengeDao.getRevealedHintCount(
                challengeId =
                    challengeId
            ) ?: return false

        val changedRows =
            besserChallengeDao.revealNextHint(
                challengeId =
                    challengeId,
                updatedAt =
                    updatedAt
            )

        if (changedRows != 1) {
            return false
        }

        val afterReveal =
            besserChallengeDao.getRevealedHintCount(
                challengeId =
                    challengeId
            ) ?: return false

        return afterReveal > beforeReveal
    }

    override suspend fun countActiveForMessage(
        messageId: String
    ): Int {
        return besserChallengeDao.countActiveForMessage(
            messageId =
                messageId
        )
    }

    override suspend fun deleteForMessage(
        messageId: String
    ) {
        besserChallengeDao.deleteForMessage(
            messageId =
                messageId
        )
    }

    override suspend fun deleteForConversation(
        conversationId: String
    ) {
        besserChallengeDao.deleteForConversation(
            conversationId =
                conversationId
        )
    }
}