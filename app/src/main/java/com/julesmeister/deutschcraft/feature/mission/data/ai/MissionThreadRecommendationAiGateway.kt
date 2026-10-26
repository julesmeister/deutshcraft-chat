package com.julesmeister.deutschcraft.feature.mission.data.ai

import com.julesmeister.deutschcraft.core.cefr.model.CefrLevel
import com.julesmeister.deutschcraft.feature.mission.domain.recommendation.MissionScenarioDefinition
import com.julesmeister.deutschcraft.feature.mission.domain.recommendation.MissionThreadRecommendation

/*
 * AI boundary that expands a scenario already selected by
 * the application.
 *
 * The AI must not choose or replace the semantic scenario.
 */
interface MissionThreadRecommendationAiGateway {

    suspend fun generateRecommendation(
        cefrLevel: CefrLevel,
        selectedScenario:
        MissionScenarioDefinition
    ): MissionThreadRecommendation?
}