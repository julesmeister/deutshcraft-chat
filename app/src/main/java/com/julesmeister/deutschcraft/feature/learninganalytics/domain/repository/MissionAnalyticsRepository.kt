package com.julesmeister.deutschcraft.feature.learninganalytics.domain.repository

import com.julesmeister.deutschcraft.core.cefr.model.CefrLevel
import com.julesmeister.deutschcraft.feature.learninganalytics.domain.MissionAnalytics
import com.julesmeister.deutschcraft.feature.learninganalytics.domain.MissionCompletionEvidence
import kotlinx.coroutines.flow.Flow

/*
 * Persistent source of truth for finalized mission analytics.
 */
interface MissionAnalyticsRepository {

    /*
     * Saves one finalized mission result and the objective
     * evidence used to calculate it.
     */
    suspend fun save(
        analytics:
        MissionAnalytics,
        evidence:
        MissionCompletionEvidence
    )

    /*
     * Returns analytics for one mission when they have already
     * been generated.
     */
    suspend fun getByMissionId(
        missionId: String
    ): MissionAnalytics?

    /*
     * Observes analytics belonging to one conversation.
     */
    fun observeConversationAnalytics(
        conversationId: String
    ): Flow<List<MissionAnalytics>>

    /*
     * Returns recent analytics for one CEFR level.
     *
     * Used by CEFR mastery and next-level readiness.
     */
    suspend fun getRecentForLevel(
        cefrLevel: CefrLevel,
        limit: Int
    ): List<MissionAnalytics>

    /*
     * Returns the number of finalized mission analytics rows
     * for one CEFR level.
     */
    suspend fun countForLevel(
        cefrLevel: CefrLevel
    ): Int

    suspend fun delete(
        missionId: String
    )
}