package com.julesmeister.deutschcraft.feature.mission.data.cache

import com.julesmeister.deutschcraft.feature.mission.domain.recommendation.MissionThreadRecommendation
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MissionThreadRecommendationCache @Inject constructor() {

    private var cachedRecommendation:
            MissionThreadRecommendation? =
        null

    @Synchronized
    fun get():
            MissionThreadRecommendation? {
        return cachedRecommendation
    }

    @Synchronized
    fun replace(
        recommendation:
        MissionThreadRecommendation
    ) {
        cachedRecommendation =
            recommendation
    }

    @Synchronized
    fun clear() {
        cachedRecommendation =
            null
    }
}