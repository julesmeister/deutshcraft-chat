package com.julesmeister.deutschcraft.feature.learninganalytics.data

import com.julesmeister.deutschcraft.core.cefr.model.CefrLevel
import com.julesmeister.deutschcraft.core.database.dao.MissionAnalyticsDao
import com.julesmeister.deutschcraft.core.database.entity.learninganalytics.MissionAnalyticsEntity
import com.julesmeister.deutschcraft.feature.learninganalytics.domain.LearningDimension
import com.julesmeister.deutschcraft.feature.learninganalytics.domain.MissionAnalytics
import com.julesmeister.deutschcraft.feature.learninganalytics.domain.repository.MissionAnalyticsRepository
import java.time.Clock
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalMissionAnalyticsRepository
@Inject constructor(
    private val dao:
    MissionAnalyticsDao,
    private val clock:
    Clock
) : MissionAnalyticsRepository {

    override suspend fun save(
        analytics:
        MissionAnalytics
    ) {
        val now =
            clock.millis()

        dao.upsertMissionAnalytics(
            analytics =
                analytics.toEntity(
                    currentTime =
                        now
                )
        )
    }

    override suspend fun getRecentForLevel(
        cefrLevel: CefrLevel,
        limit: Int
    ): List<MissionAnalytics> {
        /*
         * The full objective analytics mapping will be added
         * when evidence collection is connected.
         *
         * For CEFR calculation, mission-level scores are
         * already sufficient.
         */
        return dao
            .getRecentAnalyticsForLevel(
                cefrLevel =
                    cefrLevel.name,
                limit =
                    limit.coerceAtLeast(
                        1
                    )
            )
            .map { persisted ->
                persisted.analytics.toDomain()
            }
    }

    private fun MissionAnalytics.toEntity(
        currentTime: Long
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
                overallScore,
            independenceScore =
                independenceScore,
            grammarScore =
                grammarScore,
            vocabularyScore =
                vocabularyScore,
            comprehensionScore =
                comprehensionScore,
            interactionScore =
                interactionScore,
            hintsUsed =
                hintsUsed,
            besserCorrectionCount =
                besserCorrectionCount,
            resolvedBesserCorrectionCount =
                resolvedBesserCorrectionCount,
            independentlyCompletedObjectiveCount =
                independentlyCompletedObjectiveCount,
            supportedObjectiveCount =
                supportedObjectiveCount,
            directlyAssistedObjectiveCount =
                directlyAssistedObjectiveCount,
            conversationScore =
                dimensionScores[
                    LearningDimension.CONVERSATION
                ]
                    ?: overallScore,
            createdAt =
                currentTime,
            completedAt =
                currentTime
        )
    }

    private fun MissionAnalyticsEntity.toDomain():
            MissionAnalytics {
        val level =
            CefrLevel.valueOf(
                cefrLevel
            )

        return MissionAnalytics(
            missionId =
                missionId,
            conversationId =
                conversationId,
            cefrLevel =
                level,
            completedObjectiveCount =
                completedObjectiveCount,
            totalObjectiveCount =
                totalObjectiveCount,
            completedRequiredObjectiveCount =
                completedRequiredObjectiveCount,
            totalRequiredObjectiveCount =
                totalRequiredObjectiveCount,
            overallScore =
                overallScore,
            independenceScore =
                independenceScore,
            grammarScore =
                grammarScore,
            vocabularyScore =
                vocabularyScore,
            comprehensionScore =
                comprehensionScore,
            interactionScore =
                interactionScore,
            hintsUsed =
                hintsUsed,
            besserCorrectionCount =
                besserCorrectionCount,
            resolvedBesserCorrectionCount =
                resolvedBesserCorrectionCount,
            independentlyCompletedObjectiveCount =
                independentlyCompletedObjectiveCount,
            supportedObjectiveCount =
                supportedObjectiveCount,
            directlyAssistedObjectiveCount =
                directlyAssistedObjectiveCount,

            /*
             * Objective-specific labels and vocabulary require
             * objective evidence. Those will be restored when
             * we connect the objective analytics table.
             */
            strongObjectiveTitles =
                emptyList(),
            improvementObjectiveTitles =
                emptyList(),
            correctlyUsedTargetVocabulary =
                emptySet(),
            unusedTargetVocabulary =
                emptySet(),

            dimensionScores =
                mapOf(
                    LearningDimension.CONVERSATION to
                            conversationScore,
                    LearningDimension.INTERACTION to
                            interactionScore,
                    LearningDimension.GRAMMAR to
                            grammarScore,
                    LearningDimension.VOCABULARY to
                            vocabularyScore,
                    LearningDimension.COMPREHENSION to
                            comprehensionScore
                )
        )
    }
}