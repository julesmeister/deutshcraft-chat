// app/src/main/java/com/julesmeister/deutschcraft/feature/mission/data/LocalFirstMissionRepository.kt

package com.julesmeister.deutschcraft.feature.mission.data

import androidx.room.withTransaction
import com.julesmeister.deutschcraft.core.database.DeutschCraftDatabase
import com.julesmeister.deutschcraft.core.database.dao.MissionDao
import com.julesmeister.deutschcraft.core.database.entity.mission.ConversationMissionEntity
import com.julesmeister.deutschcraft.core.database.entity.mission.MissionObjectiveEntity
import com.julesmeister.deutschcraft.core.database.entity.mission.MissionObjectiveProgressEntity
import com.julesmeister.deutschcraft.core.database.entity.mission.MissionTemplateEntity
import com.julesmeister.deutschcraft.feature.mission.data.local.toMissionObjectiveJson
import com.julesmeister.deutschcraft.feature.mission.data.mapper.toDomain
import com.julesmeister.deutschcraft.feature.mission.domain.ConversationMission
import com.julesmeister.deutschcraft.feature.mission.domain.MissionObjective
import com.julesmeister.deutschcraft.feature.mission.domain.MissionStatus
import com.julesmeister.deutschcraft.feature.mission.domain.MissionTemplate
import com.julesmeister.deutschcraft.feature.mission.domain.repository.MissionRepository
import java.time.Clock
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Singleton
class LocalFirstMissionRepository
@Inject constructor(
    private val database:
    DeutschCraftDatabase,

    private val missionDao:
    MissionDao,

    private val clock:
    Clock,

    private val json:
    Json
) : MissionRepository {

    override fun observeMissionCatalog():
            Flow<List<MissionTemplate>> {

        return missionDao
            .observeEnabledMissionTemplates()
            .map { templates ->
                templates.map { template ->
                    val objectives =
                        missionDao.getMissionObjectives(
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

    override fun observeCompletedMissions():
            Flow<List<ConversationMission>> {

        return missionDao
            .observeCompletedMissions()
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

        database.withTransaction {
            saveMissionTemplateInternal(
                template =
                    template,

                now =
                    now
            )
        }
    }

    override suspend fun offerMission(
        conversationId: String,
        missionTemplateId: String,
        sourceRecommendationId: String?,
        targetCapabilityIds: Set<String>,
        adaptiveFocusKeys: Set<String>
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

        database.withTransaction {
            missionDao.upsertConversationMission(
                conversationMission =
                    ConversationMissionEntity(
                        id =
                            conversationMissionId,

                        conversationId =
                            conversationId,

                        missionTemplateId =
                            missionTemplateId,

                        rootMissionTemplateId =
                            missionTemplateId,

                        sourceRecommendationId =
                            sourceRecommendationId
                                ?.trim()
                                ?.takeIf(
                                    String::isNotBlank
                                ),

                        targetCapabilityIdsJson =
                            encodeStringSet(
                                values =
                                    targetCapabilityIds
                            ),

                        adaptiveFocusKeysJson =
                            encodeStringSet(
                                values =
                                    adaptiveFocusKeys
                            ),

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
                        objective.toEmptyProgressEntity(
                            conversationMissionId =
                                conversationMissionId,

                            updatedAt =
                                now
                        )
                    }
            )
        }

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

    override suspend fun replaceActiveMissionBranch(
        conversationMissionId: String,
        replacementTemplate: MissionTemplate
    ) {
        /*
         * Read the complete current mission before entering the
         * transaction.
         *
         * At this point template may be a generated temporary
         * branch. rootMissionTemplateId remains the permanent
         * semantic anchor.
         */
        val existingMission =
            getConversationMission(
                conversationMissionId =
                    conversationMissionId
            )
                ?: throw IllegalArgumentException(
                    "Mission does not exist: " +
                            conversationMissionId
                )

        check(
            existingMission.status ==
                    MissionStatus.ACTIVE
        ) {
            "Only an active mission can be branched."
        }

        val completedProgress =
            existingMission
                .objectiveProgress
                .filter { progress ->
                    progress.isCompleted
                }
                .sortedBy { progress ->
                    progress.objective.sequenceNumber
                }

        val now =
            clock.millis()

        /*
         * The current ConversationMission keeps its ID and
         * original root template.
         *
         * The new template contains:
         *
         * 1. cloned completed objectives
         * 2. freshly generated objectives for the new branch
         *
         * Cloning completed objectives is necessary because
         * objective rows belong to a specific template.
         */
        val mergedTemplate =
            buildMergedBranchTemplate(
                replacementTemplate =
                    replacementTemplate,

                completedProgress =
                    completedProgress
            )

        database.withTransaction {
            saveMissionTemplateInternal(
                template =
                    mergedTemplate,

                now =
                    now
            )

            /*
             * Remove progress belonging to the previous current
             * template.
             *
             * We immediately recreate completed progress against
             * the cloned completed objectives and empty progress
             * against the AI-generated branch objectives.
             */
            missionDao.deleteObjectiveProgressForMission(
                conversationMissionId =
                    conversationMissionId
            )

            val preservedObjectiveCount =
                completedProgress.size

            val newProgress =
                mergedTemplate
                    .objectives
                    .mapIndexed {
                            index,
                            objective ->

                        if (
                            index <
                            preservedObjectiveCount
                        ) {
                            val previousProgress =
                                completedProgress[
                                    index
                                ]

                            MissionObjectiveProgressEntity(
                                conversationMissionId =
                                    conversationMissionId,

                                objectiveId =
                                    objective.id,

                                isCompleted =
                                    true,

                                completedAt =
                                    previousProgress.completedAt,

                                completedByMessageId =
                                    previousProgress
                                        .completedByMessageId,

                                confidence =
                                    previousProgress
                                        .normalizedConfidence,

                                updatedAt =
                                    now
                            )
                        } else {
                            objective.toEmptyProgressEntity(
                                conversationMissionId =
                                    conversationMissionId,

                                updatedAt =
                                    now
                            )
                        }
                    }

            missionDao.upsertObjectiveProgressList(
                progress =
                    newProgress
            )

            /*
             * IMPORTANT:
             *
             * This is the renamed DAO method from the corrected
             * MissionDao.
             *
             * Do not call updateActiveMissionTemplate().
             */
            val changedRows =
                missionDao.replaceActiveMissionTemplate(
                    conversationMissionId =
                        conversationMissionId,

                    newMissionTemplateId =
                        mergedTemplate.id,

                    updatedAt =
                        now
                )

            check(
                changedRows > 0
            ) {
                "The active mission branch could not be persisted."
            }
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
            missionDao.countIncompleteRequiredObjectives(
                conversationMissionId =
                    conversationMissionId
            )

        if (
            remainingRequiredObjectives ==
            0
        ) {
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

    private suspend fun saveMissionTemplateInternal(
        template: MissionTemplate,
        now: Long
    ) {
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
                template
                    .objectives
                    .map { objective ->
                        objective.toEntity(
                            missionTemplateId =
                                template.id
                        )
                    }
        )
    }

    private fun buildMergedBranchTemplate(
        replacementTemplate: MissionTemplate,
        completedProgress:
        List<com.julesmeister.deutschcraft.feature.mission.domain.MissionObjectiveProgress>
    ): MissionTemplate {

        val templateId =
            replacementTemplate.id

        val preservedObjectives =
            completedProgress
                .mapIndexed {
                        index,
                        progress ->

                    progress
                        .objective
                        .copy(
                            id =
                                UUID
                                    .randomUUID()
                                    .toString(),

                            missionTemplateId =
                                templateId,

                            sequenceNumber =
                                index + 1
                        )
                }

        val branchObjectives =
            replacementTemplate
                .objectives
                .mapIndexed {
                        index,
                        objective ->

                    objective.copy(
                        id =
                            UUID
                                .randomUUID()
                                .toString(),

                        missionTemplateId =
                            templateId,

                        sequenceNumber =
                            preservedObjectives.size +
                                    index +
                                    1
                    )
                }

        return replacementTemplate.copy(
            objectives =
                preservedObjectives +
                        branchObjectives
        )
    }

    private fun MissionObjective.toEntity(
        missionTemplateId: String
    ): MissionObjectiveEntity {

        return MissionObjectiveEntity(
            id =
                id,

            missionTemplateId =
                missionTemplateId,

            title =
                title,

            description =
                description,

            sequenceNumber =
                sequenceNumber,

            isRequired =
                isRequired,

            completionHint =
                completionHint,

            coachingHintsJson =
                coachingHints
                    .toMissionObjectiveJson(
                        json =
                            json
                    ),

            targetVocabularyJson =
                targetVocabulary
                    .toMissionObjectiveJson(
                        json =
                            json
                    ),

            examplePhrasesJson =
                examplePhrases
                    .toMissionObjectiveJson(
                        json =
                            json
                    )
        )
    }

    private fun MissionObjective.toEmptyProgressEntity(
        conversationMissionId: String,
        updatedAt: Long
    ): MissionObjectiveProgressEntity {

        return MissionObjectiveProgressEntity(
            conversationMissionId =
                conversationMissionId,

            objectiveId =
                id,

            isCompleted =
                false,

            completedAt =
                null,

            completedByMessageId =
                null,

            confidence =
                0f,

            updatedAt =
                updatedAt
        )
    }

    private fun encodeStringSet(
        values: Set<String>
    ): String {

        return json.encodeToString(
            values
                .asSequence()
                .map(
                    String::trim
                )
                .filter(
                    String::isNotBlank
                )
                .distinct()
                .sorted()
                .toList()
        )
    }
}