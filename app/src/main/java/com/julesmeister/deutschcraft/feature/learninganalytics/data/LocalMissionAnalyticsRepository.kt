package com.julesmeister.deutschcraft.feature.learninganalytics.data

import android.util.Log
import com.julesmeister.deutschcraft.core.cefr.model.CefrLevel
import com.julesmeister.deutschcraft.core.database.dao.MissionAnalyticsDao
import com.julesmeister.deutschcraft.core.database.entity.learninganalytics.MissionAnalyticsEntity
import com.julesmeister.deutschcraft.core.database.entity.learninganalytics.MissionAnalyticsWithObjectives
import com.julesmeister.deutschcraft.core.database.entity.learninganalytics.MissionObjectiveAnalyticsEntity
import com.julesmeister.deutschcraft.feature.learninganalytics.domain.LearningDimension
import com.julesmeister.deutschcraft.feature.learninganalytics.domain.MissionAnalytics
import com.julesmeister.deutschcraft.feature.learninganalytics.domain.MissionCompletionEvidence
import com.julesmeister.deutschcraft.feature.learninganalytics.domain.repository.MissionAnalyticsRepository
import java.time.Clock
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.SerializationException
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Singleton
class LocalMissionAnalyticsRepository
@Inject constructor(
    private val dao:
    MissionAnalyticsDao,
    private val clock:
    Clock,
    private val json:
    Json
) : MissionAnalyticsRepository {

    override suspend fun save(
        analytics:
        MissionAnalytics,
        evidence:
        MissionCompletionEvidence
    ) {
        require(
            analytics.missionId ==
                    evidence.missionId
        ) {
            "Analytics and evidence must belong to the same " +
                    "mission."
        }

        require(
            analytics.conversationId ==
                    evidence.conversationId
        ) {
            "Analytics and evidence must belong to the same " +
                    "conversation."
        }

        require(
            analytics.cefrLevel ==
                    evidence.cefrLevel
        ) {
            "Analytics and evidence must use the same CEFR " +
                    "level."
        }

        val now =
            clock.millis()

        val completedAt =
            evidence.completedAt
                ?: now

        val missionEntity =
            analytics.toEntity(
                createdAt =
                    now,
                completedAt =
                    completedAt
            )

        val objectiveEntities =
            evidence
                .objectiveEvidence
                .map { objective ->
                    MissionObjectiveAnalyticsEntity(
                        missionId =
                            evidence.missionId,
                        objectiveId =
                            objective.objectiveId,
                        title =
                            objective.title.trim(),
                        isRequired =
                            objective.isRequired,
                        wasCompleted =
                            objective.wasCompleted,
                        hintsUsed =
                            objective
                                .normalizedHintsUsed,
                        besserCorrectionCount =
                            objective
                                .besserCorrectionCount
                                .coerceAtLeast(
                                    0
                                ),
                        evaluationConfidence =
                            objective
                                .normalizedEvaluationConfidence,
                        targetVocabularyJson =
                            encodeStringCollection(
                                values =
                                    objective.targetVocabulary
                            ),
                        correctlyUsedVocabularyJson =
                            encodeStringCollection(
                                values =
                                    objective
                                        .correctlyUsedVocabulary
                            )
                    )
                }

        dao.saveCompleteAnalytics(
            analytics =
                missionEntity,
            objectives =
                objectiveEntities
        )

        Log.d(
            REPOSITORY_TAG,
            buildString {
                append("Saved mission analytics. ")
                append("missionId=")
                append(analytics.missionId)
                append(", level=")
                append(analytics.cefrLevel.name)
                append(", overall=")
                append(analytics.overallScore)
                append(", independence=")
                append(analytics.independenceScore)
                append(", objectives=")
                append(objectiveEntities.size)
            }
        )
    }

    override suspend fun getByMissionId(
        missionId: String
    ): MissionAnalytics? {
        if (missionId.isBlank()) {
            return null
        }

        return dao
            .getMissionAnalytics(
                missionId =
                    missionId
            )
            ?.toDomainOrNull()
    }

    override fun observeConversationAnalytics(
        conversationId: String
    ): Flow<List<MissionAnalytics>> {
        require(
            conversationId.isNotBlank()
        ) {
            "Conversation ID must not be blank."
        }

        return dao
            .observeConversationAnalytics(
                conversationId =
                    conversationId
            )
            .map { persistedAnalytics ->
                persistedAnalytics
                    .mapNotNull { persisted ->
                        persisted.toDomainOrNull()
                    }
            }
    }

    override suspend fun getRecentForLevel(
        cefrLevel: CefrLevel,
        limit: Int
    ): List<MissionAnalytics> {
        if (limit <= 0) {
            return emptyList()
        }

        return dao
            .getRecentAnalyticsForLevel(
                cefrLevel =
                    cefrLevel.name,
                limit =
                    limit
            )
            .mapNotNull { persisted ->
                persisted.toDomainOrNull()
            }
    }

    override suspend fun countForLevel(
        cefrLevel: CefrLevel
    ): Int {
        return dao.countMissionsForLevel(
            cefrLevel =
                cefrLevel.name
        )
    }

    override suspend fun delete(
        missionId: String
    ) {
        if (missionId.isBlank()) {
            return
        }

        dao.deleteMissionAnalytics(
            missionId =
                missionId
        )
    }

    private fun MissionAnalytics.toEntity(
        createdAt: Long,
        completedAt: Long
    ): MissionAnalyticsEntity {
        return MissionAnalyticsEntity(
            missionId =
                missionId,
            conversationId =
                conversationId,
            cefrLevel =
                cefrLevel.name,
            completedObjectiveCount =
                completedObjectiveCount,
            totalObjectiveCount =
                totalObjectiveCount,
            completedRequiredObjectiveCount =
                completedRequiredObjectiveCount,
            totalRequiredObjectiveCount =
                totalRequiredObjectiveCount,
            overallScore =
                overallScore.coercePercent(),
            independenceScore =
                independenceScore.coercePercent(),
            grammarScore =
                grammarScore.coercePercent(),
            vocabularyScore =
                vocabularyScore.coercePercent(),
            comprehensionScore =
                comprehensionScore.coercePercent(),
            interactionScore =
                interactionScore.coercePercent(),
            hintsUsed =
                hintsUsed.coerceAtLeast(
                    minimumValue =
                        0
                ),
            besserCorrectionCount =
                besserCorrectionCount.coerceAtLeast(
                    minimumValue =
                        0
                ),
            resolvedBesserCorrectionCount =
                resolvedBesserCorrectionCount
                    .coerceIn(
                        minimumValue =
                            0,
                        maximumValue =
                            besserCorrectionCount
                                .coerceAtLeast(
                                    0
                                )
                    ),
            independentlyCompletedObjectiveCount =
                independentlyCompletedObjectiveCount
                    .coerceAtLeast(
                        minimumValue =
                            0
                    ),
            supportedObjectiveCount =
                supportedObjectiveCount
                    .coerceAtLeast(
                        minimumValue =
                            0
                    ),
            directlyAssistedObjectiveCount =
                directlyAssistedObjectiveCount
                    .coerceAtLeast(
                        minimumValue =
                            0
                    ),
            conversationScore =
                (
                        dimensionScores[
                            LearningDimension.CONVERSATION
                        ]
                            ?: overallScore
                        )
                    .coercePercent(),
            createdAt =
                createdAt,
            completedAt =
                completedAt
        )
    }

    private fun MissionAnalyticsWithObjectives
            .toDomainOrNull():
            MissionAnalytics? {
        val missionEntity =
            analytics

        val cefrLevel =
            runCatching {
                CefrLevel.valueOf(
                    missionEntity.cefrLevel
                )
            }.getOrNull()
                ?: return null

        val orderedObjectives =
            objectives.sortedBy { objective ->
                objective.title
            }

        val strongObjectiveTitles =
            orderedObjectives
                .filter { objective ->
                    objective.wasCompleted &&
                            objective.hintsUsed <=
                            MAXIMUM_STRONG_HINT_COUNT &&
                            objective.evaluationConfidence >=
                            MINIMUM_STRONG_CONFIDENCE
                }
                .map { objective ->
                    objective.title
                }
                .filter(
                    String::isNotBlank
                )
                .distinct()

        val improvementObjectiveTitles =
            orderedObjectives
                .filter { objective ->
                    !objective.wasCompleted ||
                            objective.hintsUsed >=
                            DIRECT_ASSISTANCE_HINT_LEVEL ||
                            objective.evaluationConfidence <
                            MINIMUM_STRONG_CONFIDENCE
                }
                .map { objective ->
                    objective.title
                }
                .filter(
                    String::isNotBlank
                )
                .distinct()

        val availableVocabulary =
            orderedObjectives
                .flatMap { objective ->
                    decodeStringList(
                        storedValue =
                            objective.targetVocabularyJson
                    )
                }
                .toSet()

        val correctlyUsedVocabulary =
            orderedObjectives
                .flatMap { objective ->
                    decodeStringList(
                        storedValue =
                            objective
                                .correctlyUsedVocabularyJson
                    )
                }
                .toSet()

        return MissionAnalytics(
            missionId =
                missionEntity.missionId,
            conversationId =
                missionEntity.conversationId,
            cefrLevel =
                cefrLevel,
            completedObjectiveCount =
                missionEntity.completedObjectiveCount,
            totalObjectiveCount =
                missionEntity.totalObjectiveCount,
            completedRequiredObjectiveCount =
                missionEntity
                    .completedRequiredObjectiveCount,
            totalRequiredObjectiveCount =
                missionEntity
                    .totalRequiredObjectiveCount,
            overallScore =
                missionEntity.overallScore.coercePercent(),
            independenceScore =
                missionEntity
                    .independenceScore
                    .coercePercent(),
            grammarScore =
                missionEntity.grammarScore.coercePercent(),
            vocabularyScore =
                missionEntity
                    .vocabularyScore
                    .coercePercent(),
            comprehensionScore =
                missionEntity
                    .comprehensionScore
                    .coercePercent(),
            interactionScore =
                missionEntity
                    .interactionScore
                    .coercePercent(),
            hintsUsed =
                missionEntity.hintsUsed.coerceAtLeast(
                    minimumValue =
                        0
                ),
            besserCorrectionCount =
                missionEntity
                    .besserCorrectionCount
                    .coerceAtLeast(
                        minimumValue =
                            0
                    ),
            resolvedBesserCorrectionCount =
                missionEntity
                    .resolvedBesserCorrectionCount
                    .coerceAtLeast(
                        minimumValue =
                            0
                    ),
            independentlyCompletedObjectiveCount =
                missionEntity
                    .independentlyCompletedObjectiveCount
                    .coerceAtLeast(
                        minimumValue =
                            0
                    ),
            supportedObjectiveCount =
                missionEntity
                    .supportedObjectiveCount
                    .coerceAtLeast(
                        minimumValue =
                            0
                    ),
            directlyAssistedObjectiveCount =
                missionEntity
                    .directlyAssistedObjectiveCount
                    .coerceAtLeast(
                        minimumValue =
                            0
                    ),
            strongObjectiveTitles =
                strongObjectiveTitles,
            improvementObjectiveTitles =
                improvementObjectiveTitles,
            correctlyUsedTargetVocabulary =
                correctlyUsedVocabulary,
            unusedTargetVocabulary =
                availableVocabulary -
                        correctlyUsedVocabulary,
            dimensionScores =
                mapOf(
                    LearningDimension.CONVERSATION to
                            missionEntity
                                .conversationScore
                                .coercePercent(),
                    LearningDimension.INTERACTION to
                            missionEntity
                                .interactionScore
                                .coercePercent(),
                    LearningDimension.GRAMMAR to
                            missionEntity
                                .grammarScore
                                .coercePercent(),
                    LearningDimension.VOCABULARY to
                            missionEntity
                                .vocabularyScore
                                .coercePercent(),
                    LearningDimension.COMPREHENSION to
                            missionEntity
                                .comprehensionScore
                                .coercePercent()
                )
        )
    }

    private fun encodeStringCollection(
        values: Collection<String>
    ): String {
        return json.encodeToString(
            value =
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
                REPOSITORY_TAG,
                "Could not decode mission analytics JSON.",
                serializationException
            )

            emptyList()
        } catch (
            illegalArgumentException:
            IllegalArgumentException
        ) {
            Log.e(
                REPOSITORY_TAG,
                "Mission analytics JSON was invalid.",
                illegalArgumentException
            )

            emptyList()
        }
    }

    private fun Int.coercePercent():
            Int {
        return coerceIn(
            minimumValue =
                0,
            maximumValue =
                100
        )
    }

    private companion object {
        const val REPOSITORY_TAG =
            "MissionAnalyticsRepository"

        const val MAXIMUM_STRONG_HINT_COUNT =
            1

        const val DIRECT_ASSISTANCE_HINT_LEVEL =
            3

        const val MINIMUM_STRONG_CONFIDENCE =
            0.80f
    }
}