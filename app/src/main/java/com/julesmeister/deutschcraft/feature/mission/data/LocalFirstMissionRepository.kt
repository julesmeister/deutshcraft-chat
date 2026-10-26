package com.julesmeister.deutschcraft.feature.mission.data

import com.julesmeister.deutschcraft.core.database.dao.MissionDao
import com.julesmeister.deutschcraft.core.database.entity.mission.ConversationMissionEntity
import com.julesmeister.deutschcraft.core.database.entity.mission.MissionObjectiveEntity
import com.julesmeister.deutschcraft.core.database.entity.mission.MissionObjectiveProgressEntity
import com.julesmeister.deutschcraft.core.database.entity.mission.MissionTemplateEntity
import com.julesmeister.deutschcraft.feature.mission.data.local.toMissionObjectiveJson
import com.julesmeister.deutschcraft.feature.mission.data.mapper.toDomain
import com.julesmeister.deutschcraft.feature.mission.domain.ConversationMission
import com.julesmeister.deutschcraft.feature.mission.domain.MissionStatus
import com.julesmeister.deutschcraft.feature.mission.domain.MissionTemplate
import com.julesmeister.deutschcraft.feature.mission.domain.repository.MissionRepository
import java.time.Clock
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json

@Singleton
class LocalFirstMissionRepository @Inject constructor(
    private val missionDao: MissionDao,
    private val clock: Clock,
    private val json: Json
) : MissionRepository {

    override fun observeMissionCatalog():
            Flow<List<MissionTemplate>> {
        return missionDao
            .observeEnabledMissionTemplates()
            .map { templates ->
                templates.map { template ->
                    val objectives =
                        missionDao
                            .getMissionObjectives(
                                missionTemplateId =
                                    template.id
                            )

                    template.toDomain(
                        objectives =
                            objectives
                    )
                }
            }
    }

    override fun observeActiveMission(
        conversationId: String
    ): Flow<ConversationMission?> {
        return missionDao
            .observeActiveMission(
                conversationId =
                    conversationId
            )
            .map { mission ->
                mission?.toDomain()
            }
    }

    override fun observeMissionHistory(
        conversationId: String
    ): Flow<List<ConversationMission>> {
        return missionDao
            .observeConversationMissionHistory(
                conversationId =
                    conversationId
            )
            .map { missions ->
                missions.map { mission ->
                    mission.toDomain()
                }
            }
    }

    override suspend fun getMissionTemplate(
        missionTemplateId: String
    ): MissionTemplate? {
        val template =
            missionDao.getMissionTemplate(
                missionTemplateId =
                    missionTemplateId
            )
                ?: return null

        val objectives =
            missionDao.getMissionObjectives(
                missionTemplateId =
                    missionTemplateId
            )

        return template.toDomain(
            objectives =
                objectives
        )
    }

    override suspend fun getConversationMission(
        conversationMissionId: String
    ): ConversationMission? {
        return missionDao
            .getConversationMission(
                conversationMissionId =
                    conversationMissionId
            )
            ?.toDomain()
    }

    override suspend fun saveMissionTemplate(
        template: MissionTemplate
    ) {
        val now =
            clock.millis()

        missionDao.upsertMissionTemplate(
            template =
                MissionTemplateEntity(
                    id =
                        template.id,
                    title =
                        template.title,
                    description =
                        template.description,
                    scenario =
                        template.scenario,
                    learnerRole =
                        template.learnerRole,
                    assistantRole =
                        template.assistantRole,
                    difficulty =
                        template.difficulty.name,
                    icon =
                        template.icon,
                    isEnabled =
                        template.isEnabled,
                    createdAt =
                        now,
                    updatedAt =
                        now
                )
        )

        missionDao.upsertMissionObjectives(
            objectives =
                template.objectives.map { objective ->
                    MissionObjectiveEntity(
                        id =
                            objective.id,
                        missionTemplateId =
                            template.id,
                        title =
                            objective.title,
                        description =
                            objective.description,
                        sequenceNumber =
                            objective.sequenceNumber,
                        isRequired =
                            objective.isRequired,
                        completionHint =
                            objective.completionHint,
                        coachingHintsJson =
                            objective
                                .coachingHints
                                .toMissionObjectiveJson(
                                    json =
                                        json
                                ),
                        targetVocabularyJson =
                            objective
                                .targetVocabulary
                                .toMissionObjectiveJson(
                                    json =
                                        json
                                ),
                        examplePhrasesJson =
                            objective
                                .examplePhrases
                                .toMissionObjectiveJson(
                                    json =
                                        json
                                )
                    )
                }
        )
    }

    override suspend fun offerMission(
        conversationId: String,
        missionTemplateId: String
    ): String {
        val template =
            getMissionTemplate(
                missionTemplateId =
                    missionTemplateId
            )
                ?: throw IllegalArgumentException(
                    "Mission template does not exist: " +
                            missionTemplateId
                )

        val now =
            clock.millis()

        val conversationMissionId =
            UUID.randomUUID().toString()

        missionDao.upsertConversationMission(
            conversationMission =
                ConversationMissionEntity(
                    id =
                        conversationMissionId,
                    conversationId =
                        conversationId,
                    missionTemplateId =
                        missionTemplateId,
                    status =
                        MissionStatus.OFFERED.name,
                    startedAt =
                        null,
                    completedAt =
                        null,
                    dismissedAt =
                        null,
                    createdAt =
                        now,
                    updatedAt =
                        now
                )
        )

        missionDao.upsertObjectiveProgressList(
            progress =
                template.objectives.map { objective ->
                    MissionObjectiveProgressEntity(
                        conversationMissionId =
                            conversationMissionId,
                        objectiveId =
                            objective.id,
                        isCompleted =
                            false,
                        completedAt =
                            null,
                        completedByMessageId =
                            null,
                        confidence =
                            0f,
                        updatedAt =
                            now
                    )
                }
        )

        return conversationMissionId
    }

    override suspend fun startMission(
        conversationMissionId: String
    ) {
        val now =
            clock.millis()

        val changedRows =
            missionDao.updateMissionStartState(
                conversationMissionId =
                    conversationMissionId,
                status =
                    MissionStatus.ACTIVE.name,
                startedAt =
                    now,
                updatedAt =
                    now
            )

        check(
            changedRows > 0
        ) {
            "Mission could not be started: " +
                    conversationMissionId
        }
    }

    override suspend fun dismissMission(
        conversationMissionId: String
    ) {
        val now =
            clock.millis()

        missionDao.updateMissionDismissalState(
            conversationMissionId =
                conversationMissionId,
            status =
                MissionStatus.DISMISSED.name,
            dismissedAt =
                now,
            updatedAt =
                now
        )
    }

    override suspend fun completeObjective(
        conversationMissionId: String,
        objectiveId: String,
        messageId: String,
        confidence: Float
    ) {
        val now =
            clock.millis()

        val changedRows =
            missionDao.completeObjective(
                conversationMissionId =
                    conversationMissionId,
                objectiveId =
                    objectiveId,
                completedAt =
                    now,
                messageId =
                    messageId,
                confidence =
                    confidence.coerceIn(
                        minimumValue =
                            0f,
                        maximumValue =
                            1f
                    ),
                updatedAt =
                    now
            )

        check(
            changedRows > 0
        ) {
            "Mission objective could not be completed: " +
                    objectiveId
        }

        val remainingRequiredObjectives =
            missionDao
                .countIncompleteRequiredObjectives(
                    conversationMissionId =
                        conversationMissionId
                )

        if (remainingRequiredObjectives == 0) {
            completeMission(
                conversationMissionId =
                    conversationMissionId
            )
        }
    }

    override suspend fun completeMission(
        conversationMissionId: String
    ) {
        val now =
            clock.millis()

        missionDao.updateMissionCompletionState(
            conversationMissionId =
                conversationMissionId,
            status =
                MissionStatus.COMPLETED.name,
            completedAt =
                now,
            updatedAt =
                now
        )
    }

    override suspend fun deleteConversationMissions(
        conversationId: String
    ) {
        missionDao.deleteConversationMissions(
            conversationId =
                conversationId
        )
    }
}