package com.julesmeister.deutschcraft.feature.mission.data.ai

import com.julesmeister.deutschcraft.core.cefr.model.CefrLevel
import com.julesmeister.deutschcraft.feature.mission.domain.recommendation.MissionThreadRecommendation

/*
 * AI boundary responsible for generating one mission
 * conversation recommendation for the thread list.
 *
 * A null result means that no valid recommendation could
 * be generated.
 */
interface MissionThreadRecommendationAiGateway {

    suspend fun generateRecommendation(
        cefrLevel: CefrLevel
    ): MissionThreadRecommendation?
}
