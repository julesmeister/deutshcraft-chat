package com.julesmeister.deutschcraft.feature.learninganalytics.data

import com.julesmeister.deutschcraft.core.database.dao.MissionObjectiveHintUsageDao
import com.julesmeister.deutschcraft.core.database.entity.learninganalytics.MissionObjectiveHintUsageEntity
import com.julesmeister.deutschcraft.feature.learninganalytics.domain.repository.MissionObjectiveHintUsage
import com.julesmeister.deutschcraft.feature.learninganalytics.domain.repository.MissionObjectiveHintUsageRepository
import java.time.Clock
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/*
 * Room-backed implementation of objective hint tracking.
 *
 * The combination of:
 *
 * conversationMissionId
 * objectiveId
 * hintLevel
 *
 * is unique in Room. Revealing the same hint repeatedly does
 * not count as additional learning evidence.
 */
@Singleton
class LocalMissionObjectiveHintUsageRepository
@Inject constructor(
    private val dao:
    MissionObjectiveHintUsageDao,
    private val clock:
    Clock
) : MissionObjectiveHintUsageRepository {

    override suspend fun recordHint(
        conversationMissionId: String,
        objectiveId: String,
        hintLevel: Int,
        hintText: String
    ): Boolean {
        validateHintUsage(
            conversationMissionId =
                conversationMissionId,
            objectiveId =
                objectiveId,
            hintLevel =
                hintLevel,
            hintText =
                hintText
        )

        val insertionResult =
            dao.insertHintUsage(
                usage =
                    MissionObjectiveHintUsageEntity(
                        conversationMissionId =
                            conversationMissionId,
                        objectiveId =
                            objectiveId,
                        hintLevel =
                            hintLevel,
                        hintText =
                            hintText.trim(),
                        revealedAt =
                            clock.millis()
                    )
            )

        /*
         * Room returns -1 when OnConflictStrategy.IGNORE
         * rejects an existing composite primary key.
         */
        return insertionResult !=
                INSERT_IGNORED_RESULT
    }

    override suspend fun getHintsUsed(
        conversationMissionId: String,
        objectiveId: String
    ): Int {
        validateIdentifiers(
            conversationMissionId =
                conversationMissionId,
            objectiveId =
                objectiveId
        )

        return dao
            .countHintsUsed(
                conversationMissionId =
                    conversationMissionId,
                objectiveId =
                    objectiveId
            )
            .coerceIn(
                minimumValue =
                    NO_HINT_LEVEL,
                maximumValue =
                    MAXIMUM_HINT_LEVEL
            )
    }

    override suspend fun getHighestHintLevel(
        conversationMissionId: String,
        objectiveId: String
    ): Int {
        validateIdentifiers(
            conversationMissionId =
                conversationMissionId,
            objectiveId =
                objectiveId
        )

        return dao
            .getHighestHintLevel(
                conversationMissionId =
                    conversationMissionId,
                objectiveId =
                    objectiveId
            )
            .coerceIn(
                minimumValue =
                    NO_HINT_LEVEL,
                maximumValue =
                    MAXIMUM_HINT_LEVEL
            )
    }

    override suspend fun getObjectiveHintUsage(
        conversationMissionId: String,
        objectiveId: String
    ): List<MissionObjectiveHintUsage> {
        validateIdentifiers(
            conversationMissionId =
                conversationMissionId,
            objectiveId =
                objectiveId
        )

        return dao
            .getObjectiveHintUsage(
                conversationMissionId =
                    conversationMissionId,
                objectiveId =
                    objectiveId
            )
            .map { entity ->
                entity.toDomain()
            }
    }

    override suspend fun getMissionHintUsage(
        conversationMissionId: String
    ): List<MissionObjectiveHintUsage> {
        require(
            conversationMissionId.isNotBlank()
        ) {
            CONVERSATION_MISSION_ID_ERROR
        }

        return dao
            .getMissionHintUsage(
                conversationMissionId =
                    conversationMissionId
            )
            .map { entity ->
                entity.toDomain()
            }
    }

    override fun observeMissionHintUsage(
        conversationMissionId: String
    ): Flow<List<MissionObjectiveHintUsage>> {
        require(
            conversationMissionId.isNotBlank()
        ) {
            CONVERSATION_MISSION_ID_ERROR
        }

        return dao
            .observeMissionHintUsage(
                conversationMissionId =
                    conversationMissionId
            )
            .map { entities ->
                entities.map { entity ->
                    entity.toDomain()
                }
            }
    }

    override suspend fun countMissionHintsUsed(
        conversationMissionId: String
    ): Int {
        require(
            conversationMissionId.isNotBlank()
        ) {
            CONVERSATION_MISSION_ID_ERROR
        }

        return dao
            .countMissionHintsUsed(
                conversationMissionId =
                    conversationMissionId
            )
            .coerceAtLeast(
                minimumValue =
                    0
            )
    }

    override suspend fun countObjectivesUsingHints(
        conversationMissionId: String
    ): Int {
        require(
            conversationMissionId.isNotBlank()
        ) {
            CONVERSATION_MISSION_ID_ERROR
        }

        return dao
            .countObjectivesUsingHints(
                conversationMissionId =
                    conversationMissionId
            )
            .coerceAtLeast(
                minimumValue =
                    0
            )
    }

    override suspend fun countDirectlyAssistedObjectives(
        conversationMissionId: String
    ): Int {
        require(
            conversationMissionId.isNotBlank()
        ) {
            CONVERSATION_MISSION_ID_ERROR
        }

        return dao
            .countDirectlyAssistedObjectives(
                conversationMissionId =
                    conversationMissionId
            )
            .coerceAtLeast(
                minimumValue =
                    0
            )
    }

    override suspend fun deleteMissionHintUsage(
        conversationMissionId: String
    ) {
        require(
            conversationMissionId.isNotBlank()
        ) {
            CONVERSATION_MISSION_ID_ERROR
        }

        dao.deleteMissionHintUsage(
            conversationMissionId =
                conversationMissionId
        )
    }

    private fun validateHintUsage(
        conversationMissionId: String,
        objectiveId: String,
        hintLevel: Int,
        hintText: String
    ) {
        validateIdentifiers(
            conversationMissionId =
                conversationMissionId,
            objectiveId =
                objectiveId
        )

        require(
            hintLevel in
                    MINIMUM_HINT_LEVEL..
                    MAXIMUM_HINT_LEVEL
        ) {
            HINT_LEVEL_ERROR
        }

        require(
            hintText.isNotBlank()
        ) {
            HINT_TEXT_ERROR
        }
    }

    private fun validateIdentifiers(
        conversationMissionId: String,
        objectiveId: String
    ) {
        require(
            conversationMissionId.isNotBlank()
        ) {
            CONVERSATION_MISSION_ID_ERROR
        }

        require(
            objectiveId.isNotBlank()
        ) {
            OBJECTIVE_ID_ERROR
        }
    }

    private fun MissionObjectiveHintUsageEntity.toDomain():
            MissionObjectiveHintUsage {
        return MissionObjectiveHintUsage(
            conversationMissionId =
                conversationMissionId,
            objectiveId =
                objectiveId,
            hintLevel =
                hintLevel,
            hintText =
                hintText,
            revealedAt =
                revealedAt
        )
    }

    private companion object {
        const val NO_HINT_LEVEL =
            0

        const val MINIMUM_HINT_LEVEL =
            1

        const val MAXIMUM_HINT_LEVEL =
            3

        const val INSERT_IGNORED_RESULT =
            -1L

        const val CONVERSATION_MISSION_ID_ERROR =
            "Conversation mission ID must not be blank."

        const val OBJECTIVE_ID_ERROR =
            "Objective ID must not be blank."

        const val HINT_LEVEL_ERROR =
            "Hint level must be between 1 and 3."

        const val HINT_TEXT_ERROR =
            "Hint text must not be blank."
    }
}