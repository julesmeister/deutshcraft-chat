// app/src/main/java/com/julesmeister/deutschcraft/feature/besser/data/repository/LocalMissionCapabilityEvidenceRepository.kt

package com.julesmeister.deutschcraft.feature.besser.data.repository

import com.julesmeister.deutschcraft.core.database.dao.MissionCapabilityEvidenceDao
import com.julesmeister.deutschcraft.core.database.entity.besser.MissionCapabilityEvidenceEntity
import com.julesmeister.deutschcraft.feature.besser.domain.mastery.MissionCapabilityEvidence
import com.julesmeister.deutschcraft.feature.besser.domain.repository.MissionCapabilityEvidenceRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Singleton
class LocalMissionCapabilityEvidenceRepository
@Inject constructor(
    private val dao:
    MissionCapabilityEvidenceDao
) : MissionCapabilityEvidenceRepository {

    override suspend fun save(
        evidence:
        MissionCapabilityEvidence
    ) {
        validate(
            evidence =
                evidence
        )

        dao.upsert(
            evidence =
                evidence.toPersistenceEntity()
        )
    }

    override suspend fun saveAll(
        evidence:
        List<MissionCapabilityEvidence>
    ) {
        if (evidence.isEmpty()) {
            return
        }

        evidence.forEach { item ->
            validate(
                evidence =
                    item
            )
        }

        val entities =
            evidence
                .distinctBy { item ->
                    item.missionId to
                            item.capabilityId
                }
                .map { item ->
                    item.toPersistenceEntity()
                }

        dao.upsertAll(
            evidence =
                entities
        )
    }

    override suspend fun getForMission(
        missionId: String
    ): List<MissionCapabilityEvidence> {

        val normalizedMissionId =
            missionId.trim()

        if (normalizedMissionId.isBlank()) {
            return emptyList()
        }

        return dao
            .getForMission(
                missionId =
                    normalizedMissionId
            )
            .map { entity ->
                entity.toDomainModel()
            }
    }

    override fun observeForCapability(
        capabilityId: String
    ): Flow<List<MissionCapabilityEvidence>> {

        val normalizedCapabilityId =
            capabilityId.trim()

        require(
            normalizedCapabilityId.isNotBlank()
        ) {
            "Capability ID cannot be blank."
        }

        return dao
            .observeForCapability(
                capabilityId =
                    normalizedCapabilityId
            )
            .map { entities ->
                entities.map { entity ->
                    entity.toDomainModel()
                }
            }
    }

    override suspend fun getForCapabilities(
        capabilityIds:
        Collection<String>
    ): Map<String, List<MissionCapabilityEvidence>> {

        val normalizedCapabilityIds =
            capabilityIds
                .asSequence()
                .map { capabilityId ->
                    capabilityId.trim()
                }
                .filter { capabilityId ->
                    capabilityId.isNotBlank()
                }
                .distinct()
                .toList()

        if (normalizedCapabilityIds.isEmpty()) {
            return emptyMap()
        }

        val persistedEvidence =
            dao.getForCapabilities(
                capabilityIds =
                    normalizedCapabilityIds
            )

        val domainEvidence =
            persistedEvidence.map { entity ->
                entity.toDomainModel()
            }

        val evidenceByCapabilityId =
            domainEvidence.groupBy { item ->
                item.capabilityId
            }

        return buildMap {
            normalizedCapabilityIds.forEach { capabilityId ->
                put(
                    capabilityId,
                    evidenceByCapabilityId[
                        capabilityId
                    ]
                        .orEmpty()
                )
            }
        }
    }

    override suspend fun getRecentForCapability(
        capabilityId: String,
        limit: Int
    ): List<MissionCapabilityEvidence> {

        val normalizedCapabilityId =
            capabilityId.trim()

        if (
            normalizedCapabilityId.isBlank() ||
            limit <=
            0
        ) {
            return emptyList()
        }

        return dao
            .getRecentForCapability(
                capabilityId =
                    normalizedCapabilityId,

                limit =
                    limit
            )
            .map { entity ->
                entity.toDomainModel()
            }
    }

    override suspend fun deleteForMission(
        missionId: String
    ) {
        val normalizedMissionId =
            missionId.trim()

        if (normalizedMissionId.isBlank()) {
            return
        }

        dao.deleteForMission(
            missionId =
                normalizedMissionId
        )
    }

    override suspend fun deleteForConversation(
        conversationId: String
    ) {
        val normalizedConversationId =
            conversationId.trim()

        if (normalizedConversationId.isBlank()) {
            return
        }

        dao.deleteForConversation(
            conversationId =
                normalizedConversationId
        )
    }

    private fun validate(
        evidence:
        MissionCapabilityEvidence
    ) {
        require(
            evidence.missionId.isNotBlank()
        ) {
            "Mission ID cannot be blank."
        }

        require(
            evidence.conversationId.isNotBlank()
        ) {
            "Conversation ID cannot be blank."
        }

        require(
            evidence.capabilityId.isNotBlank()
        ) {
            "Capability ID cannot be blank."
        }

        require(
            evidence.evaluatedAt >=
                    0L
        ) {
            "Evaluation timestamp cannot be negative."
        }
    }

    private fun MissionCapabilityEvidence
            .toPersistenceEntity():
            MissionCapabilityEvidenceEntity {

        return MissionCapabilityEvidenceEntity(
            missionId =
                missionId.trim(),

            conversationId =
                conversationId.trim(),

            capabilityId =
                capabilityId.trim(),

            overallScore =
                safeOverallScore,

            independenceScore =
                safeIndependenceScore,

            objectiveCompletionPercentage =
                safeObjectiveCompletionPercentage,

            evidenceScore =
                safeEvidenceScore,

            confidence =
                safeConfidence,

            demonstrated =
                demonstrated,

            evaluatedAt =
                evaluatedAt
        )
    }

    private fun MissionCapabilityEvidenceEntity
            .toDomainModel():
            MissionCapabilityEvidence {

        return MissionCapabilityEvidence(
            missionId =
                missionId,

            conversationId =
                conversationId,

            capabilityId =
                capabilityId,

            overallScore =
                overallScore,

            independenceScore =
                independenceScore,

            objectiveCompletionPercentage =
                objectiveCompletionPercentage,

            evidenceScore =
                evidenceScore,

            confidence =
                confidence,

            demonstrated =
                demonstrated,

            evaluatedAt =
                evaluatedAt
        )
    }
}