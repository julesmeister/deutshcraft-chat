package com.julesmeister.deutschcraft.feature.learninganalytics.data

import com.julesmeister.deutschcraft.core.database.dao.MissionInteractionEvidenceDao
import com.julesmeister.deutschcraft.core.database.entity.learninganalytics.MissionInteractionEvidenceEntity
import com.julesmeister.deutschcraft.feature.learninganalytics.domain.MissionMessageInteractionEvidence
import com.julesmeister.deutschcraft.feature.learninganalytics.domain.repository.MissionInteractionEvidenceRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Singleton
class LocalMissionInteractionEvidenceRepository
@Inject constructor(
    private val dao:
    MissionInteractionEvidenceDao
) : MissionInteractionEvidenceRepository {

    override suspend fun save(
        evidence:
        MissionMessageInteractionEvidence
    ) {
        validateEvidence(
            evidence =
                evidence
        )

        dao.upsertInteractionEvidence(
            evidence =
                evidence.toEntity()
        )
    }

    override suspend fun getForMission(
        conversationMissionId: String
    ): List<MissionMessageInteractionEvidence> {
        require(
            conversationMissionId.isNotBlank()
        ) {
            "Conversation mission ID must not be blank."
        }

        return dao
            .getMissionInteractionEvidence(
                conversationMissionId =
                    conversationMissionId
            )
            .map { entity ->
                entity.toDomain()
            }
    }

    override suspend fun getForMessage(
        messageId: String
    ): MissionMessageInteractionEvidence? {
        if (messageId.isBlank()) {
            return null
        }

        return dao
            .getInteractionEvidenceForMessage(
                messageId =
                    messageId
            )
            ?.toDomain()
    }

    override fun observeForMission(
        conversationMissionId: String
    ): Flow<List<MissionMessageInteractionEvidence>> {
        require(
            conversationMissionId.isNotBlank()
        ) {
            "Conversation mission ID must not be blank."
        }

        return dao
            .observeMissionInteractionEvidence(
                conversationMissionId =
                    conversationMissionId
            )
            .map { entities ->
                entities.map { entity ->
                    entity.toDomain()
                }
            }
    }

    override suspend fun deleteForMission(
        conversationMissionId: String
    ) {
        if (conversationMissionId.isBlank()) {
            return
        }

        dao.deleteMissionInteractionEvidence(
            conversationMissionId =
                conversationMissionId
        )
    }

    private fun validateEvidence(
        evidence:
        MissionMessageInteractionEvidence
    ) {
        require(
            evidence.conversationMissionId.isNotBlank()
        ) {
            "Conversation mission ID must not be blank."
        }

        require(
            evidence.messageId.isNotBlank()
        ) {
            "Message ID must not be blank."
        }

        require(
            evidence.evaluatedAt >= 0L
        ) {
            "Evaluation time must not be negative."
        }
    }

    private fun MissionMessageInteractionEvidence.toEntity():
            MissionInteractionEvidenceEntity {
        return MissionInteractionEvidenceEntity(
            conversationMissionId =
                conversationMissionId,
            messageId =
                messageId,
            relevantReply =
                relevantReply,
            followUpQuestion =
                followUpQuestion,
            clarification =
                clarification,
            replyExpected =
                replyExpected,
            confidence =
                normalizedConfidence,
            evaluatedAt =
                evaluatedAt
        )
    }

    private fun MissionInteractionEvidenceEntity.toDomain():
            MissionMessageInteractionEvidence {
        return MissionMessageInteractionEvidence(
            conversationMissionId =
                conversationMissionId,
            messageId =
                messageId,
            relevantReply =
                relevantReply,
            followUpQuestion =
                followUpQuestion,
            clarification =
                clarification,
            replyExpected =
                replyExpected,
            confidence =
                confidence.coerceIn(
                    minimumValue =
                        0f,
                    maximumValue =
                        1f
                ),
            evaluatedAt =
                evaluatedAt
        )
    }
}