package com.julesmeister.deutschcraft.feature.learninganalytics.domain.repository

import com.julesmeister.deutschcraft.core.cefr.model.CefrLevel
import com.julesmeister.deutschcraft.feature.learninganalytics.domain.MissionAnalytics

interface MissionAnalyticsRepository {

    suspend fun save(
        analytics:
        MissionAnalytics
    )

    suspend fun getRecentForLevel(
        cefrLevel: CefrLevel,
        limit: Int
    ): List<MissionAnalytics>
}