package com.julesmeister.deutschcraft.feature.mission.domain.recommendation

import com.julesmeister.deutschcraft.core.cefr.model.CefrLevel

/*
 * All information used to select the next scenario.
 *
 * Recent conversation text is only one signal. It must not
 * overpower CEFR relevance, adaptive weaknesses, and scenario
 * diversity.
 */
data class MissionRecommendationContext(
    val cefrLevel: CefrLevel,

    val recentConversationText: String,

    /*
     * Recently offered scenarios, including dismissed offers.
     */
    val recentlyOfferedScenarioTypes:
    List<MissionScenarioType> =
        emptyList(),

    /*
     * Recently completed scenarios.
     */
    val recentlyCompletedScenarioTypes:
    List<MissionScenarioType> =
        emptyList(),

    /*
     * Current adaptive-learning focus keys or titles.
     *
     * Examples:
     *
     * communication:giving_reasons
     * communication:comparing_options
     * grammar:dative_case
     */
    val adaptiveLearningFocuses:
    List<String> =
        emptyList(),

    /*
     * A scenario currently active in this conversation must
     * never be recommended again.
     */
    val activeScenarioType:
    MissionScenarioType? =
        null
)