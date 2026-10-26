package com.julesmeister.deutschcraft.feature.mission.domain.recommendation

import javax.inject.Inject
import kotlin.math.absoluteValue

/*
 * Selects a diverse CEFR-appropriate scenario before Qwen
 * generates the final mission wording.
 *
 * Recommendation priorities:
 *
 * 1. CEFR compatibility
 * 2. Scenario diversity
 * 3. Adaptive-learning relevance
 * 4. Conversation relevance
 * 5. Stable variety
 *
 * Conversation keywords are intentionally weighted lower
 * than curriculum and diversity. This prevents one coffee
 * conversation from producing endless café missions.
 */
class SelectMissionScenarioUseCase
@Inject constructor(
    private val catalog:
    CefrMissionScenarioCatalog
) {

    operator fun invoke(
        context: MissionRecommendationContext
    ): MissionScenarioDefinition? {
        val levelScenarios =
            catalog.scenariosFor(
                level =
                    context.cefrLevel
            )

        if (levelScenarios.isEmpty()) {
            return null
        }

        val excludedScenarioTypes =
            buildSet {
                context.activeScenarioType
                    ?.let(
                        ::add
                    )

                addAll(
                    context
                        .recentlyOfferedScenarioTypes
                        .takeLast(
                            RECENT_OFFER_EXCLUSION_COUNT
                        )
                )

                addAll(
                    context
                        .recentlyCompletedScenarioTypes
                        .takeLast(
                            RECENT_COMPLETION_EXCLUSION_COUNT
                        )
                )
            }

        val diverseCandidates =
            levelScenarios.filter { scenario ->
                scenario.type !in
                        excludedScenarioTypes
            }

        /*
         * If every CEFR scenario is temporarily excluded,
         * relax completed-history exclusions first.
         *
         * Recently offered and active scenarios remain
         * excluded because immediately repeating an offer is
         * especially disruptive.
         */
        val candidates =
            diverseCandidates.ifEmpty {
                val stronglyExcludedTypes =
                    buildSet {
                        context.activeScenarioType
                            ?.let(
                                ::add
                            )

                        addAll(
                            context
                                .recentlyOfferedScenarioTypes
                                .takeLast(
                                    RECENT_OFFER_EXCLUSION_COUNT
                                )
                        )
                    }

                levelScenarios.filter { scenario ->
                    scenario.type !in
                            stronglyExcludedTypes
                }
            }.ifEmpty {
                levelScenarios
            }

        val normalizedConversation =
            normalize(
                value =
                    context.recentConversationText
            )

        val normalizedFocuses =
            context
                .adaptiveLearningFocuses
                .map(
                    ::normalize
                )

        return candidates
            .map { scenario ->
                val score =
                    calculateScore(
                        scenario =
                            scenario,
                        normalizedConversation =
                            normalizedConversation,
                        normalizedFocuses =
                            normalizedFocuses,
                        context =
                            context
                    )

                ScoredScenario(
                    scenario =
                        scenario,
                    score =
                        score
                )
            }
            .sortedWith(
                compareByDescending<ScoredScenario> {
                    it.score
                }
                    .thenBy {
                        stableVarietyValue(
                            scenario =
                                it.scenario,
                            context =
                                context
                        )
                    }
            )
            .firstOrNull()
            ?.scenario
    }

    private fun calculateScore(
        scenario: MissionScenarioDefinition,
        normalizedConversation: String,
        normalizedFocuses: List<String>,
        context: MissionRecommendationContext
    ): Int {
        val curriculumScore =
            CEFR_CURRICULUM_SCORE

        val adaptiveScore =
            calculateAdaptiveScore(
                scenario =
                    scenario,
                normalizedFocuses =
                    normalizedFocuses
            )

        val conversationScore =
            calculateConversationScore(
                scenario =
                    scenario,
                normalizedConversation =
                    normalizedConversation
            )

        val repetitionPenalty =
            calculateRepetitionPenalty(
                scenario =
                    scenario,
                context =
                    context
            )

        return curriculumScore +
                adaptiveScore +
                conversationScore -
                repetitionPenalty
    }

    private fun calculateAdaptiveScore(
        scenario: MissionScenarioDefinition,
        normalizedFocuses: List<String>
    ): Int {
        if (normalizedFocuses.isEmpty()) {
            return 0
        }

        val normalizedTargetSkills =
            scenario.targetSkills.map(
                ::normalize
            )

        val matchingSignals =
            normalizedFocuses.count { focus ->
                normalizedTargetSkills.any { skill ->
                    focus.contains(
                        other =
                            skill,
                        ignoreCase =
                            true
                    ) ||
                            skill.contains(
                                other =
                                    focus.substringAfterLast(
                                        delimiter =
                                            ":"
                                    ),
                                ignoreCase =
                                    true
                            )
                } ||
                        adaptiveScenarioMatches(
                            scenario =
                                scenario,
                            normalizedFocus =
                                focus
                        )
            }

        return (
                matchingSignals *
                        ADAPTIVE_SIGNAL_SCORE
                )
            .coerceAtMost(
                maximumValue =
                    MAXIMUM_ADAPTIVE_SCORE
            )
    }

    private fun adaptiveScenarioMatches(
        scenario: MissionScenarioDefinition,
        normalizedFocus: String
    ): Boolean {
        return when {
            normalizedFocus.contains(
                "giving_reasons"
            ) -> {
                scenario.type in
                        REASONING_SCENARIOS
            }

            normalizedFocus.contains(
                "expressing_opinions"
            ) -> {
                scenario.type in
                        OPINION_SCENARIOS
            }

            normalizedFocus.contains(
                "comparing_options"
            ) -> {
                scenario.type in
                        COMPARISON_SCENARIOS
            }

            normalizedFocus.contains(
                "storytelling"
            ) ||
                    normalizedFocus.contains(
                        "describing_experiences"
                    ) -> {

                scenario.type in
                        STORYTELLING_SCENARIOS
            }

            normalizedFocus.contains(
                "polite_interaction"
            ) -> {
                scenario.type in
                        POLITE_INTERACTION_SCENARIOS
            }

            normalizedFocus.contains(
                "dative"
            ) -> {
                scenario.type in
                        LOCATION_AND_SERVICE_SCENARIOS
            }

            else -> {
                false
            }
        }
    }

    private fun calculateConversationScore(
        scenario: MissionScenarioDefinition,
        normalizedConversation: String
    ): Int {
        if (normalizedConversation.isBlank()) {
            return 0
        }

        val keywordMatches =
            scenario
                .topicKeywords
                .count { keyword ->
                    normalizedConversation.contains(
                        other =
                            normalize(
                                value =
                                    keyword
                            )
                    )
                }

        /*
         * Conversation relevance is intentionally capped.
         *
         * Even if "Kaffee" is mentioned repeatedly, café
         * ordering cannot overpower diversity and the CEFR
         * curriculum.
         */
        return (
                keywordMatches *
                        CONVERSATION_KEYWORD_SCORE
                )
            .coerceAtMost(
                maximumValue =
                    MAXIMUM_CONVERSATION_SCORE
            )
    }

    private fun calculateRepetitionPenalty(
        scenario: MissionScenarioDefinition,
        context: MissionRecommendationContext
    ): Int {
        val offeredDistance =
            reverseDistanceFromEnd(
                values =
                    context
                        .recentlyOfferedScenarioTypes,
                target =
                    scenario.type
            )

        val completedDistance =
            reverseDistanceFromEnd(
                values =
                    context
                        .recentlyCompletedScenarioTypes,
                target =
                    scenario.type
            )

        val offeredPenalty =
            offeredDistance
                ?.let { distance ->
                    (
                            RECENT_OFFER_PENALTY -
                                    distance *
                                    OFFER_DISTANCE_REDUCTION
                            )
                        .coerceAtLeast(
                            minimumValue =
                                0
                        )
                }
                ?: 0

        val completedPenalty =
            completedDistance
                ?.let { distance ->
                    (
                            RECENT_COMPLETION_PENALTY -
                                    distance *
                                    COMPLETION_DISTANCE_REDUCTION
                            )
                        .coerceAtLeast(
                            minimumValue =
                                0
                        )
                }
                ?: 0

        return offeredPenalty +
                completedPenalty
    }

    private fun reverseDistanceFromEnd(
        values: List<MissionScenarioType>,
        target: MissionScenarioType
    ): Int? {
        val reversedIndex =
            values
                .asReversed()
                .indexOf(
                    target
                )

        return reversedIndex.takeIf { index ->
            index >= 0
        }
    }

    private fun stableVarietyValue(
        scenario: MissionScenarioDefinition,
        context: MissionRecommendationContext
    ): Int {
        val seed =
            buildString {
                append(
                    context.cefrLevel.name
                )
                append(":")
                append(
                    normalize(
                        context.recentConversationText
                    )
                        .take(
                            VARIETY_SEED_TEXT_LENGTH
                        )
                )
                append(":")
                append(
                    context
                        .recentlyOfferedScenarioTypes
                        .size
                )
                append(":")
                append(
                    scenario.type.name
                )
            }

        return seed
            .hashCode()
            .absoluteValue
    }

    private fun normalize(
        value: String
    ): String {
        return value
            .trim()
            .lowercase()
            .replace(
                NON_ALPHANUMERIC_REGEX,
                " "
            )
            .replace(
                MULTIPLE_WHITESPACE_REGEX,
                " "
            )
            .trim()
    }

    private data class ScoredScenario(
        val scenario:
        MissionScenarioDefinition,
        val score: Int
    )

    private companion object {
        const val RECENT_OFFER_EXCLUSION_COUNT =
            6

        const val RECENT_COMPLETION_EXCLUSION_COUNT =
            4

        const val CEFR_CURRICULUM_SCORE =
            40

        const val ADAPTIVE_SIGNAL_SCORE =
            15

        const val MAXIMUM_ADAPTIVE_SCORE =
            30

        const val CONVERSATION_KEYWORD_SCORE =
            3

        /*
         * Conversation context can contribute at most ten
         * points. It cannot defeat the forty-point CEFR
         * curriculum baseline or diversity exclusions.
         */
        const val MAXIMUM_CONVERSATION_SCORE =
            10

        const val RECENT_OFFER_PENALTY =
            100

        const val OFFER_DISTANCE_REDUCTION =
            12

        const val RECENT_COMPLETION_PENALTY =
            60

        const val COMPLETION_DISTANCE_REDUCTION =
            10

        const val VARIETY_SEED_TEXT_LENGTH =
            120

        val NON_ALPHANUMERIC_REGEX =
            Regex(
                "[^\\p{L}\\p{N}]+"
            )

        val MULTIPLE_WHITESPACE_REGEX =
            Regex(
                "\\s+"
            )

        val REASONING_SCENARIOS =
            setOf(
                MissionScenarioType
                    .MAKING_A_DECISION,
                MissionScenarioType
                    .JOB_INTERVIEW,
                MissionScenarioType
                    .DEFENDING_AN_OPINION,
                MissionScenarioType
                    .NEGOTIATION,
                MissionScenarioType
                    .STRATEGIC_DISCUSSION,
                MissionScenarioType
                    .ACADEMIC_DEBATE
            )

        val OPINION_SCENARIOS =
            setOf(
                MissionScenarioType
                    .EXPRESSING_AN_OPINION,
                MissionScenarioType
                    .MAKING_A_DECISION,
                MissionScenarioType
                    .DEFENDING_AN_OPINION,
                MissionScenarioType
                    .DISCUSSING_A_SOCIAL_TOPIC,
                MissionScenarioType
                    .PARTICIPATING_IN_A_DEBATE,
                MissionScenarioType
                    .ACADEMIC_DEBATE
            )

        val COMPARISON_SCENARIOS =
            setOf(
                MissionScenarioType
                    .COMPARING_OPTIONS,
                MissionScenarioType
                    .MAKING_A_DECISION,
                MissionScenarioType
                    .EXPLAINING_ADVANTAGES_AND_DISADVANTAGES,
                MissionScenarioType
                    .COMPARING_VIEWPOINTS,
                MissionScenarioType
                    .STRATEGIC_DISCUSSION
            )

        val STORYTELLING_SCENARIOS =
            setOf(
                MissionScenarioType
                    .TELLING_A_STORY,
                MissionScenarioType
                    .DESCRIBING_A_WEEKEND,
                MissionScenarioType
                    .DISCUSSING_TRAVEL_EXPERIENCES,
                MissionScenarioType
                    .TALKING_ABOUT_PAST_EXPERIENCES
            )

        val POLITE_INTERACTION_SCENARIOS =
            setOf(
                MissionScenarioType
                    .RESTAURANT_ORDERING,
                MissionScenarioType
                    .HOTEL_CHECK_IN,
                MissionScenarioType
                    .RETURNING_A_PRODUCT,
                MissionScenarioType
                    .MAKING_A_COMPLAINT,
                MissionScenarioType
                    .FORMAL_COMPLAINT,
                MissionScenarioType
                    .FORMAL_INTERVIEW
            )

        val LOCATION_AND_SERVICE_SCENARIOS =
            setOf(
                MissionScenarioType
                    .ASKING_FOR_DIRECTIONS,
                MissionScenarioType
                    .HOTEL_CHECK_IN,
                MissionScenarioType
                    .RESTAURANT_ORDERING,
                MissionScenarioType
                    .DOCTOR_APPOINTMENT,
                MissionScenarioType
                    .ARRANGING_A_SERVICE
            )
    }
}