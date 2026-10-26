package com.julesmeister.deutschcraft.feature.mission.domain.recommendation

import com.julesmeister.deutschcraft.feature.mission.domain.generated.MissionOpportunity

/*
 * Recommendation displayed as the featured mission cell in
 * the thread-list screen.
 *
 * scenarioType is the semantic identity used for repetition
 * prevention.
 *
 * Titles are not reliable identities.
 *
 * For example:
 *
 * - Kaffee am Morgen
 * - Bestellung im Café
 * - Ein Cappuccino im Café
 *
 * all belong to CAFE_ORDERING.
 */
data class MissionThreadRecommendation(
    val id: String,
    val scenarioType: MissionScenarioType,
    val title: String,
    val previewText: String,
    val opportunity: MissionOpportunity
)