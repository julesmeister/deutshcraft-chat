// app/src/main/java/com/julesmeister/deutschcraft/feature/mission/data/recommendation/RoomMissionThreadRecommendationStore.kt

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
import com.julesmeister.deutschcraft.feature.mission.domain.recommendation.MissionLearningContext
import com.julesmeister.deutschcraft.feature.mission.domain.recommendation.MissionRecommendationHistory
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
        cefrLevel:
        CefrLevel
    ): Flow<MissionThreadRecommendation?> {

        return dao
            .observeNextEligibleRecommendation(
                cefrLevel =
                    cefrLevel.name,

                currentTime =
                    clock.millis()
            )
            .map { persistedRecommendation ->
                persistedRecommendation
                    ?.toRecommendationDomainOrNull()
            }
    }

    override suspend fun getNextEligible(
        cefrLevel:
        CefrLevel
    ): MissionThreadRecommendation? {

        return dao
            .getNextEligibleRecommendation(
                cefrLevel =
                    cefrLevel.name,

                currentTime =
                    clock.millis()
            )
            ?.toRecommendationDomainOrNull()
    }

    override suspend fun getEligibleRecommendations(
        cefrLevel:
        CefrLevel,

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
                    .toRecommendationDomainOrNull()
            }
    }

    override suspend fun countEligible(
        cefrLevel:
        CefrLevel
    ): Int {

        return dao.countEligibleRecommendations(
            cefrLevel =
                cefrLevel.name,

            currentTime =
                clock.millis()
        )
    }

    override suspend fun getRecommendationHistory(
        cefrLevel:
        CefrLevel
    ): MissionRecommendationHistory {

        val now =
            clock.millis()

        val minimumHistoryTime =
            now -
                    RECENT_HISTORY_DURATION_MILLIS

        val recentScenarioTypes =
            dao
                .getRecentScenarioTypes(
                    cefrLevel =
                        cefrLevel.name,

                    minimumCreatedAt =
                        minimumHistoryTime
                )
                .mapNotNull { storedScenarioType ->
                    storedScenarioType
                        .toMissionScenarioTypeOrNull()
                }

        val completedScenarioTypes =
            dao
                .getRecentlyCompletedScenarioTypes(
                    cefrLevel =
                        cefrLevel.name,

                    minimumCompletedAt =
                        minimumHistoryTime
                )
                .mapNotNull { storedScenarioType ->
                    storedScenarioType
                        .toMissionScenarioTypeOrNull()
                }

        val dismissedScenarioTypes =
            dao
                .getRecentlyDismissedScenarioTypes(
                    cefrLevel =
                        cefrLevel.name,

                    minimumSkippedAt =
                        minimumHistoryTime
                )
                .mapNotNull { storedScenarioType ->
                    storedScenarioType
                        .toMissionScenarioTypeOrNull()
                }

        return MissionRecommendationHistory(
            scenarioTypes =
                recentScenarioTypes,

            completedScenarioTypes =
                completedScenarioTypes,

            dismissedScenarioTypes =
                dismissedScenarioTypes
        )
    }

    override suspend fun getRecentScenarioTypes(
        cefrLevel:
        CefrLevel
    ): Set<MissionScenarioType> {

        return getRecommendationHistory(
            cefrLevel =
                cefrLevel
        )
            .scenarioTypes
            .toSet()
    }

    override suspend fun hasActiveScenario(
        cefrLevel:
        CefrLevel,

        scenarioType:
        MissionScenarioType
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
    }

    override suspend fun markDisplayed(
        recommendationId:
        String
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
        recommendationId:
        String
    ) {
        if (recommendationId.isBlank()) {
            return
        }

        dao.markSkipped(
            recommendationId =
                recommendationId,

            skippedAt =
                clock.millis()
        )
    }

    override suspend fun markStale(
        recommendationId:
        String
    ) {
        if (recommendationId.isBlank()) {
            return
        }

        dao.markStale(
            recommendationId =
                recommendationId,

            staleAt =
                clock.millis()
        )
    }

    override suspend fun invalidateStaleRecommendations(
        cefrLevel:
        CefrLevel,

        currentLearningContext:
        MissionLearningContext
    ): Int {

        val eligibleRecommendations =
            dao.getAllEligibleRecommendations(
                cefrLevel =
                    cefrLevel.name,

                currentTime =
                    clock.millis()
            )

        var invalidatedCount =
            0

        eligibleRecommendations.forEach { persistedRecommendation ->
            val recommendation =
                persistedRecommendation
                    .toRecommendationDomainOrNull()

            if (recommendation == null) {
                val changedRows =
                    dao.markStale(
                        recommendationId =
                            persistedRecommendation
                                .recommendation
                                .id,

                        staleAt =
                            clock.millis()
                    )

                invalidatedCount +=
                    changedRows

                return@forEach
            }

            val shouldInvalidate =
                !recommendation
                    .learningContext
                    .materiallyMatches(
                        other =
                            currentLearningContext
                    )

            if (shouldInvalidate) {
                val changedRows =
                    dao.markStale(
                        recommendationId =
                            recommendation.id,

                        staleAt =
                            clock.millis()
                    )

                invalidatedCount +=
                    changedRows
            }
        }

        return invalidatedCount
    }

    override suspend fun markStarted(
        recommendationId:
        String,

        conversationId:
        String,

        conversationMissionId:
        String
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
            changedRows >
                    0
        ) {
            "Recommendation could not be marked STARTED: " +
                    recommendationId
        }
    }

    override suspend fun markCompletedByMissionId(
        conversationMissionId:
        String
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
        cefrLevel:
        CefrLevel
    ) {
        val now =
            clock.millis()

        dao.expireOldRecommendations(
            currentTime =
                now
        )

        dao.expireOtherLevels(
            cefrLevel =
                cefrLevel.name,

            currentTime =
                now
        )

        dao.deleteOldInactiveRecommendations(
            deleteBefore =
                now -
                        INACTIVE_RETENTION_DURATION_MILLIS
        )
    }

    private fun MissionThreadRecommendation.toEntity(
        currentTime:
        Long
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
                mission
                    .icon
                    .trim()
                    .ifBlank {
                        DEFAULT_MISSION_ICON
                    },

            targetCapabilityIdsJson =
                encodeStringList(
                    values =
                        targetCapabilityIds.toList()
                ),

            adaptiveFocusKeysJson =
                encodeStringList(
                    values =
                        adaptiveFocusKeys.toList()
                ),

            masteredCapabilityIdsJson =
                encodeStringList(
                    values =
                        masteredCapabilityIds.toList()
                ),

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
        recommendationId:
        String,

        sequenceNumber:
        Int
    ): MissionThreadRecommendationObjectiveEntity {

        return MissionThreadRecommendationObjectiveEntity(
            id =
                UUID
                    .randomUUID()
                    .toString(),

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
            .toRecommendationDomainOrNull():
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
                    objective
                        .toObjectiveDomainOrNull()
                }

        if (
            domainObjectives.size !in
            MINIMUM_OBJECTIVE_COUNT..
            MAXIMUM_OBJECTIVE_COUNT
        ) {
            return null
        }

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
            )

        val targetCapabilityIds =
            decodeStringList(
                storedValue =
                    recommendationEntity
                        .targetCapabilityIdsJson
            )

        val adaptiveFocusKeys =
            decodeStringList(
                storedValue =
                    recommendationEntity
                        .adaptiveFocusKeysJson
            )

        val masteredCapabilityIds =
            decodeStringList(
                storedValue =
                    recommendationEntity
                        .masteredCapabilityIdsJson
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
                opportunity,

            learningContext =
                MissionLearningContext(
                    reinforcementFocuses =
                        adaptiveFocusKeys,

                    progressionReadyCapabilities =
                        targetCapabilityIds
                            .map { capabilityId ->
                                "progression_ready:" +
                                        capabilityId
                            },

                    developingCapabilities =
                        emptyList(),

                    masteredCapabilities =
                        masteredCapabilityIds
                            .map { capabilityId ->
                                "mastered:" +
                                        capabilityId
                            }
                )
        )
    }

    private fun MissionThreadRecommendationObjectiveEntity
            .toObjectiveDomainOrNull():
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
            "Recommendation preview text must not be blank."
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
            "Recommendation must contain a required objective."
        }
    }

    private fun encodeStringList(
        values:
        List<String>
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

    private fun decodeStringList(
        storedValue:
        String
    ): List<String> {

        if (storedValue.isBlank()) {
            return emptyList()
        }

        return try {
            json.decodeFromString<List<String>>(
                string =
                    storedValue
            )
                .asSequence()
                .map(
                    String::trim
                )
                .filter(
                    String::isNotBlank
                )
                .distinct()
                .toList()
        } catch (
            serializationException:
            SerializationException
        ) {
            Log.e(
                STORE_TAG,
                "Could not decode persisted string list.",
                serializationException
            )

            emptyList()
        } catch (
            illegalArgumentException:
            IllegalArgumentException
        ) {
            Log.e(
                STORE_TAG,
                "Persisted string list was invalid.",
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
        }
            .getOrNull()
    }

    private fun String.toMissionDifficultyOrNull():
            MissionDifficulty? {

        return runCatching {
            MissionDifficulty.valueOf(
                trim().uppercase()
            )
        }
            .getOrNull()
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

        const val RECOMMENDATION_EXPIRY_DURATION_MILLIS =
            7L *
                    24L *
                    60L *
                    60L *
                    1_000L

        const val RECENT_HISTORY_DURATION_MILLIS =
            30L *
                    24L *
                    60L *
                    60L *
                    1_000L

        const val INACTIVE_RETENTION_DURATION_MILLIS =
            60L *
                    24L *
                    60L *
                    60L *
                    1_000L
    }
}