package com.julesmeister.deutschcraft.feature.mission.data.recommendation

import android.util.Log
import com.julesmeister.deutschcraft.core.cefr.model.CefrLevel
import com.julesmeister.deutschcraft.core.database.dao.MissionThreadRecommendationDao
import com.julesmeister.deutschcraft.core.database.entity.mission.recommendation.MissionThreadRecommendationEntity
import com.julesmeister.deutschcraft.core.database.entity.mission.recommendation.MissionThreadRecommendationObjectiveEntity
import com.julesmeister.deutschcraft.core.database.entity.mission.recommendation.MissionThreadRecommendationWithObjectives
import com.julesmeister.deutschcraft.feature.mission.domain.MissionDifficulty
import com.julesmeister.deutschcraft.feature.mission.domain.generated.GeneratedMissionDraft
import com.julesmeister.deutschcraft.feature.mission.domain.generated.GeneratedMissionObjectiveDraft
import com.julesmeister.deutschcraft.feature.mission.domain.generated.MissionOpportunity
import com.julesmeister.deutschcraft.feature.mission.domain.recommendation.MissionScenarioType
import com.julesmeister.deutschcraft.feature.mission.domain.recommendation.MissionThreadRecommendation
import com.julesmeister.deutschcraft.feature.mission.domain.recommendation.MissionThreadRecommendationStore
import java.time.Clock
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.SerializationException
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Singleton
class RoomMissionThreadRecommendationStore
@Inject constructor(
    private val dao:
    MissionThreadRecommendationDao,
    private val clock:
    Clock,
    private val json:
    Json
) : MissionThreadRecommendationStore {

    override fun observeNextEligible(
        cefrLevel: CefrLevel
    ): Flow<MissionThreadRecommendation?> {
        /*
         * Room re-runs the query whenever recommendation or
         * objective rows are changed.
         *
         * This means skipping recommendation A immediately
         * emits recommendation B without waiting for Qwen.
         */
        return dao
            .observeNextEligibleRecommendation(
                cefrLevel =
                    cefrLevel.name,
                currentTime =
                    clock.millis()
            )
            .map { persistedRecommendation ->
                persistedRecommendation
                    ?.toDomainOrNull()
            }
    }

    override suspend fun getNextEligible(
        cefrLevel: CefrLevel
    ): MissionThreadRecommendation? {
        return dao
            .getNextEligibleRecommendation(
                cefrLevel =
                    cefrLevel.name,
                currentTime =
                    clock.millis()
            )
            ?.toDomainOrNull()
    }

    override suspend fun getEligibleRecommendations(
        cefrLevel: CefrLevel,
        limit: Int
    ): List<MissionThreadRecommendation> {
        if (limit <= 0) {
            return emptyList()
        }

        return dao
            .getEligibleRecommendations(
                cefrLevel =
                    cefrLevel.name,
                currentTime =
                    clock.millis(),
                limit =
                    limit
            )
            .mapNotNull { persistedRecommendation ->
                persistedRecommendation
                    .toDomainOrNull()
            }
    }

    override suspend fun countEligible(
        cefrLevel: CefrLevel
    ): Int {
        return dao.countEligibleRecommendations(
            cefrLevel =
                cefrLevel.name,
            currentTime =
                clock.millis()
        )
    }

    override suspend fun getRecentScenarioTypes(
        cefrLevel: CefrLevel
    ): Set<MissionScenarioType> {
        val minimumCreatedAt =
            clock.millis() -
                    RECENT_HISTORY_DURATION_MILLIS

        return dao
            .getRecentScenarioTypes(
                cefrLevel =
                    cefrLevel.name,
                minimumCreatedAt =
                    minimumCreatedAt
            )
            .mapNotNull { storedScenarioType ->
                storedScenarioType
                    .toMissionScenarioTypeOrNull()
            }
            .toSet()
    }

    override suspend fun hasActiveScenario(
        cefrLevel: CefrLevel,
        scenarioType: MissionScenarioType
    ): Boolean {
        return dao.hasActiveScenario(
            cefrLevel =
                cefrLevel.name,
            scenarioType =
                scenarioType.name
        )
    }

    override suspend fun save(
        recommendation:
        MissionThreadRecommendation
    ) {
        validateBeforeSaving(
            recommendation =
                recommendation
        )

        val now =
            clock.millis()

        val recommendationEntity =
            recommendation.toEntity(
                currentTime =
                    now
            )

        val objectiveEntities =
            recommendation
                .opportunity
                .mission
                .objectives
                .mapIndexed { index, objective ->
                    objective.toEntity(
                        recommendationId =
                            recommendation.id,
                        sequenceNumber =
                            index
                    )
                }

        dao.insertCompleteRecommendation(
            recommendation =
                recommendationEntity,
            objectives =
                objectiveEntities
        )

        Log.d(
            STORE_TAG,
            buildString {
                append("Persisted mission recommendation. ")
                append("id=")
                append(recommendation.id)
                append(", scenario=")
                append(recommendation.scenarioType)
                append(", level=")
                append(
                    recommendation
                        .opportunity
                        .mission
                        .difficulty
                        .name
                )
                append(", objectiveCount=")
                append(objectiveEntities.size)
            }
        )
    }

    override suspend fun markDisplayed(
        recommendationId: String
    ) {
        if (recommendationId.isBlank()) {
            return
        }

        dao.markDisplayed(
            recommendationId =
                recommendationId,
            displayedAt =
                clock.millis()
        )
    }

    override suspend fun markSkipped(
        recommendationId: String
    ) {
        if (recommendationId.isBlank()) {
            return
        }

        val changedRows =
            dao.markSkipped(
                recommendationId =
                    recommendationId,
                skippedAt =
                    clock.millis()
            )

        Log.d(
            STORE_TAG,
            "Marked recommendation skipped. " +
                    "id=$recommendationId, rows=$changedRows"
        )
    }

    override suspend fun markStarted(
        recommendationId: String,
        conversationId: String,
        conversationMissionId: String
    ) {
        require(
            recommendationId.isNotBlank()
        ) {
            "Recommendation ID must not be blank."
        }

        require(
            conversationId.isNotBlank()
        ) {
            "Conversation ID must not be blank."
        }

        require(
            conversationMissionId.isNotBlank()
        ) {
            "Conversation mission ID must not be blank."
        }

        val changedRows =
            dao.markStarted(
                recommendationId =
                    recommendationId,
                conversationId =
                    conversationId,
                conversationMissionId =
                    conversationMissionId,
                startedAt =
                    clock.millis()
            )

        check(
            changedRows > 0
        ) {
            "Recommendation could not be marked STARTED: " +
                    recommendationId
        }
    }

    override suspend fun markCompletedByMissionId(
        conversationMissionId: String
    ) {
        if (conversationMissionId.isBlank()) {
            return
        }

        dao.markCompletedByMissionId(
            conversationMissionId =
                conversationMissionId,
            completedAt =
                clock.millis()
        )
    }

    override suspend fun performMaintenance(
        cefrLevel: CefrLevel
    ) {
        val now =
            clock.millis()

        val expiredRows =
            dao.expireOldRecommendations(
                currentTime =
                    now
            )

        val otherLevelRows =
            dao.expireOtherLevels(
                cefrLevel =
                    cefrLevel.name,
                currentTime =
                    now
            )

        val deletedRows =
            dao.deleteOldInactiveRecommendations(
                deleteBefore =
                    now -
                            INACTIVE_RETENTION_DURATION_MILLIS
            )

        if (
            expiredRows > 0 ||
            otherLevelRows > 0 ||
            deletedRows > 0
        ) {
            Log.d(
                STORE_TAG,
                buildString {
                    append(
                        "Recommendation maintenance finished. "
                    )
                    append("expired=")
                    append(expiredRows)
                    append(", otherLevel=")
                    append(otherLevelRows)
                    append(", deleted=")
                    append(deletedRows)
                }
            )
        }
    }

    private fun MissionThreadRecommendation.toEntity(
        currentTime: Long
    ): MissionThreadRecommendationEntity {
        val mission =
            opportunity.mission

        return MissionThreadRecommendationEntity(
            id =
                id,
            scenarioType =
                scenarioType.name,
            cefrLevel =
                mission.difficulty.name,
            title =
                title.trim(),
            previewText =
                previewText.trim(),
            confidence =
                opportunity
                    .confidence
                    .coerceIn(
                        minimumValue =
                            0f,
                        maximumValue =
                            1f
                    ),
            reason =
                opportunity.reason.trim(),
            description =
                mission.description.trim(),
            scenario =
                mission.scenario.trim(),
            learnerRole =
                mission.learnerRole.trim(),
            assistantRole =
                mission.assistantRole.trim(),
            icon =
                mission.icon
                    .trim()
                    .ifBlank {
                        DEFAULT_MISSION_ICON
                    },
            status =
                RecommendationStatus.READY.name,
            createdAt =
                currentTime,
            updatedAt =
                currentTime,
            displayedAt =
                null,
            skippedAt =
                null,
            startedAt =
                null,
            completedAt =
                null,
            expiresAt =
                currentTime +
                        RECOMMENDATION_EXPIRY_DURATION_MILLIS,
            conversationId =
                null,
            conversationMissionId =
                null
        )
    }

    private fun GeneratedMissionObjectiveDraft.toEntity(
        recommendationId: String,
        sequenceNumber: Int
    ): MissionThreadRecommendationObjectiveEntity {
        return MissionThreadRecommendationObjectiveEntity(
            id =
                UUID.randomUUID().toString(),
            recommendationId =
                recommendationId,
            sequenceNumber =
                sequenceNumber,
            title =
                title.trim(),
            description =
                description.trim(),
            completionHint =
                completionHint.trim(),
            coachingHintsJson =
                encodeStringList(
                    values =
                        sanitizedCoachingHints
                ),
            targetVocabularyJson =
                encodeStringList(
                    values =
                        sanitizedTargetVocabulary
                ),
            examplePhrasesJson =
                encodeStringList(
                    values =
                        sanitizedExamplePhrases
                ),
            isRequired =
                isRequired
        )
    }

    private fun MissionThreadRecommendationWithObjectives
            .toDomainOrNull():
            MissionThreadRecommendation? {
        val recommendationEntity =
            recommendation

        val scenarioType =
            recommendationEntity
                .scenarioType
                .toMissionScenarioTypeOrNull()
                ?: return null

        val difficulty =
            recommendationEntity
                .cefrLevel
                .toMissionDifficultyOrNull()
                ?: return null

        val domainObjectives =
            objectives
                .sortedBy { objective ->
                    objective.sequenceNumber
                }
                .mapNotNull { objective ->
                    objective.toDomainOrNull()
                }

        if (
            domainObjectives.size !in
            MINIMUM_OBJECTIVE_COUNT..
            MAXIMUM_OBJECTIVE_COUNT
        ) {
            Log.w(
                STORE_TAG,
                buildString {
                    append(
                        "Persisted recommendation has an "
                    )
                    append(
                        "invalid objective count. id="
                    )
                    append(recommendationEntity.id)
                    append(", count=")
                    append(domainObjectives.size)
                }
            )

            return null
        }

        val draft =
            GeneratedMissionDraft(
                title =
                    recommendationEntity.title,
                description =
                    recommendationEntity.description,
                scenario =
                    recommendationEntity.scenario,
                learnerRole =
                    recommendationEntity.learnerRole,
                assistantRole =
                    recommendationEntity.assistantRole,
                difficulty =
                    difficulty,
                icon =
                    recommendationEntity.icon,
                objectives =
                    domainObjectives
            )

        val opportunity =
            MissionOpportunity(
                confidence =
                    recommendationEntity
                        .confidence
                        .coerceIn(
                            minimumValue =
                                0f,
                            maximumValue =
                                1f
                        ),
                reason =
                    recommendationEntity.reason,
                mission =
                    draft
            )

        return MissionThreadRecommendation(
            id =
                recommendationEntity.id,
            scenarioType =
                scenarioType,
            title =
                recommendationEntity.title,
            previewText =
                recommendationEntity.previewText,
            opportunity =
                opportunity
        )
    }

    private fun MissionThreadRecommendationObjectiveEntity
            .toDomainOrNull():
            GeneratedMissionObjectiveDraft? {
        if (
            title.isBlank() ||
            description.isBlank() ||
            completionHint.isBlank()
        ) {
            return null
        }

        val coachingHints =
            decodeStringList(
                storedValue =
                    coachingHintsJson
            )

        val targetVocabulary =
            decodeStringList(
                storedValue =
                    targetVocabularyJson
            )

        val examplePhrases =
            decodeStringList(
                storedValue =
                    examplePhrasesJson
            )

        if (
            coachingHints.size !=
            REQUIRED_COACHING_HINT_COUNT
        ) {
            return null
        }

        if (
            targetVocabulary.size !in
            MINIMUM_TARGET_VOCABULARY_COUNT..
            MAXIMUM_TARGET_VOCABULARY_COUNT
        ) {
            return null
        }

        if (
            examplePhrases.size !=
            REQUIRED_EXAMPLE_PHRASE_COUNT
        ) {
            return null
        }

        return GeneratedMissionObjectiveDraft(
            title =
                title,
            description =
                description,
            completionHint =
                completionHint,
            coachingHints =
                coachingHints,
            targetVocabulary =
                targetVocabulary,
            examplePhrases =
                examplePhrases,
            isRequired =
                isRequired
        )
    }

    private fun validateBeforeSaving(
        recommendation:
        MissionThreadRecommendation
    ) {
        require(
            recommendation.id.isNotBlank()
        ) {
            "Recommendation ID must not be blank."
        }

        require(
            recommendation.title.isNotBlank()
        ) {
            "Recommendation title must not be blank."
        }

        require(
            recommendation.previewText.isNotBlank()
        ) {
            "Recommendation preview must not be blank."
        }

        val objectives =
            recommendation
                .opportunity
                .mission
                .objectives

        require(
            objectives.size in
                    MINIMUM_OBJECTIVE_COUNT..
                    MAXIMUM_OBJECTIVE_COUNT
        ) {
            "Recommendation must contain between " +
                    "$MINIMUM_OBJECTIVE_COUNT and " +
                    "$MAXIMUM_OBJECTIVE_COUNT objectives."
        }

        require(
            objectives.any { objective ->
                objective.isRequired
            }
        ) {
            "Recommendation must contain at least one " +
                    "required objective."
        }
    }

    private fun encodeStringList(
        values: List<String>
    ): String {
        return json.encodeToString(
            value =
                values
                    .map(String::trim)
                    .filter(String::isNotBlank)
                    .distinct()
        )
    }

    private fun decodeStringList(
        storedValue: String
    ): List<String> {
        if (storedValue.isBlank()) {
            return emptyList()
        }

        return try {
            json.decodeFromString<List<String>>(
                string =
                    storedValue
            )
                .map(String::trim)
                .filter(String::isNotBlank)
                .distinct()
        } catch (
            serializationException:
            SerializationException
        ) {
            Log.e(
                STORE_TAG,
                "Could not decode a persisted recommendation " +
                        "string list.",
                serializationException
            )

            emptyList()
        } catch (
            illegalArgumentException:
            IllegalArgumentException
        ) {
            Log.e(
                STORE_TAG,
                "Persisted recommendation list JSON was " +
                        "invalid.",
                illegalArgumentException
            )

            emptyList()
        }
    }

    private fun String.toMissionScenarioTypeOrNull():
            MissionScenarioType? {
        return runCatching {
            MissionScenarioType.valueOf(
                trim().uppercase()
            )
        }.getOrNull()
    }

    private fun String.toMissionDifficultyOrNull():
            MissionDifficulty? {
        return runCatching {
            MissionDifficulty.valueOf(
                trim().uppercase()
            )
        }.getOrNull()
    }

    private enum class RecommendationStatus {
        READY,
        DISPLAYED,
        SKIPPED,
        STARTED,
        COMPLETED,
        EXPIRED,
        INVALID
    }

    private companion object {
        const val STORE_TAG =
            "MissionRecommendationStore"

        const val DEFAULT_MISSION_ICON =
            "🎯"

        const val MINIMUM_OBJECTIVE_COUNT =
            8

        const val MAXIMUM_OBJECTIVE_COUNT =
            10

        const val REQUIRED_COACHING_HINT_COUNT =
            3

        const val MINIMUM_TARGET_VOCABULARY_COUNT =
            2

        const val MAXIMUM_TARGET_VOCABULARY_COUNT =
            4

        const val REQUIRED_EXAMPLE_PHRASE_COUNT =
            2

        /*
         * READY recommendations remain eligible for seven
         * days.
         */
        const val RECOMMENDATION_EXPIRY_DURATION_MILLIS =
            7L *
                    24L *
                    60L *
                    60L *
                    1_000L

        /*
         * Scenario identities remain excluded for thirty
         * days.
         */
        const val RECENT_HISTORY_DURATION_MILLIS =
            30L *
                    24L *
                    60L *
                    60L *
                    1_000L

        /*
         * Old skipped, expired, and invalid rows are removed
         * after sixty days.
         */
        const val INACTIVE_RETENTION_DURATION_MILLIS =
            60L *
                    24L *
                    60L *
                    60L *
                    1_000L
    }
}