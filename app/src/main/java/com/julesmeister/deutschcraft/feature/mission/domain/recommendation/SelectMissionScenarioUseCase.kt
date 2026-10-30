// app/src/main/java/com/julesmeister/deutschcraft/feature/mission/domain/recommendation/SelectMissionScenarioUseCase.kt

package com.julesmeister.deutschcraft.feature.mission.domain.recommendation

import javax.inject.Inject
import kotlin.math.absoluteValue

class SelectMissionScenarioUseCase
@Inject constructor(
    private val catalog:
    CefrMissionScenarioCatalog
) {

    operator fun invoke(
        context:
        MissionRecommendationContext
    ): MissionScenarioDefinition? {

        return rank(
            context =
                context,

            limit =
                1
        )
            .firstOrNull()
    }

    fun rank(
        context:
        MissionRecommendationContext,

        limit: Int =
            DEFAULT_RANKED_CANDIDATE_LIMIT
    ): List<MissionScenarioDefinition> {

        if (limit <= 0) {
            return emptyList()
        }

        val levelScenarios =
            catalog.scenariosFor(
                level =
                    context.cefrLevel
            )

        if (levelScenarios.isEmpty()) {
            return emptyList()
        }

        val hardExcludedTypes =
            buildSet {
                context.activeScenarioType
                    ?.let(
                        ::add
                    )

                addAll(
                    context
                        .recentlyOfferedScenarioTypes
                        .takeLast(
                            HARD_RECENT_OFFER_EXCLUSION_COUNT
                        )
                )

                addAll(
                    context
                        .recentlyDismissedScenarioTypes
                        .takeLast(
                            HARD_RECENT_DISMISSAL_EXCLUSION_COUNT
                        )
                )
            }

        val candidates =
            levelScenarios
                .filter { scenario ->
                    scenario.type !in
                            hardExcludedTypes
                }
                .ifEmpty {
                    levelScenarios.filter { scenario ->
                        scenario.type !=
                                context.activeScenarioType &&
                                scenario.type !in
                                context
                                    .recentlyDismissedScenarioTypes
                                    .takeLast(
                                        HARD_RECENT_DISMISSAL_EXCLUSION_COUNT
                                    )
                    }
                }
                .ifEmpty {
                    levelScenarios.filter { scenario ->
                        scenario.type !=
                                context.activeScenarioType
                    }
                }

        val normalizedConversation =
            normalize(
                value =
                    context.recentConversationText
            )

        val normalizedLearningSignals =
            context
                .positiveLearningSignals
                .map(
                    ::normalize
                )

        val normalizedMasteredCapabilities =
            context
                .masteredCapabilities
                .map(
                    ::normalize
                )

        return candidates
            .map { scenario ->
                ScoredScenario(
                    scenario =
                        scenario,

                    score =
                        calculateScore(
                            scenario =
                                scenario,

                            normalizedConversation =
                                normalizedConversation,

                            normalizedLearningSignals =
                                normalizedLearningSignals,

                            normalizedMasteredCapabilities =
                                normalizedMasteredCapabilities,

                            context =
                                context
                        ),

                    tieBreaker =
                        stableVarietyValue(
                            scenario =
                                scenario,

                            context =
                                context
                        )
                )
            }
            .sortedWith(
                compareByDescending<ScoredScenario> { scored ->
                    scored.score
                }
                    .thenBy { scored ->
                        scored.tieBreaker
                    }
            )
            .map(
                ScoredScenario::scenario
            )
            .take(
                limit
            )
    }

    private fun calculateScore(
        scenario:
        MissionScenarioDefinition,

        normalizedConversation:
        String,

        normalizedLearningSignals:
        List<String>,

        normalizedMasteredCapabilities:
        List<String>,

        context:
        MissionRecommendationContext
    ): Int {

        return CEFR_CURRICULUM_SCORE +
                calculateLearningRelevanceScore(
                    scenario =
                        scenario,

                    normalizedLearningSignals =
                        normalizedLearningSignals
                ) +
                calculateConversationScore(
                    scenario =
                        scenario,

                    normalizedConversation =
                        normalizedConversation
                ) +
                calculateContextNoveltyBonus(
                    scenario =
                        scenario,

                    context =
                        context
                ) -
                calculateMasteredCapabilityPenalty(
                    scenario =
                        scenario,

                    normalizedMasteredCapabilities =
                        normalizedMasteredCapabilities
                ) -
                calculateScenarioRepetitionPenalty(
                    scenario =
                        scenario,

                    context =
                        context
                ) -
                calculateContextRepetitionPenalty(
                    scenario =
                        scenario,

                    context =
                        context
                ) -
                calculateDismissalPenalty(
                    scenario =
                        scenario,

                    context =
                        context
                )
    }

    private fun calculateLearningRelevanceScore(
        scenario:
        MissionScenarioDefinition,

        normalizedLearningSignals:
        List<String>
    ): Int {

        if (normalizedLearningSignals.isEmpty()) {
            return 0
        }

        val scenarioSignals =
            scenario.normalizedLearningSignals()

        val matchingSignalCount =
            normalizedLearningSignals.count { learningSignal ->
                scenarioSignals.any { scenarioSignal ->
                    signalsOverlap(
                        first =
                            learningSignal,

                        second =
                            scenarioSignal
                    )
                } ||
                        adaptiveScenarioMatches(
                            scenario =
                                scenario,

                            normalizedFocus =
                                learningSignal
                        )
            }

        return (
                matchingSignalCount *
                        LEARNING_SIGNAL_SCORE
                )
            .coerceAtMost(
                maximumValue =
                    MAXIMUM_LEARNING_RELEVANCE_SCORE
            )
    }

    private fun calculateContextNoveltyBonus(
        scenario:
        MissionScenarioDefinition,

        context:
        MissionRecommendationContext
    ): Int {

        val category =
            scenario.type.contextCategory

        val recentCategories =
            context
                .recentlyUsedContextCategories
                .takeLast(
                    CONTEXT_HISTORY_WINDOW
                )

        if (category !in recentCategories) {
            return UNUSED_CONTEXT_BONUS
        }

        val distance =
            reverseDistanceFromEnd(
                values =
                    recentCategories,

                target =
                    category
            )
                ?: return UNUSED_CONTEXT_BONUS

        return (
                distance *
                        CONTEXT_DISTANCE_BONUS
                )
            .coerceAtMost(
                maximumValue =
                    MAXIMUM_CONTEXT_DISTANCE_BONUS
            )
    }

    private fun calculateScenarioRepetitionPenalty(
        scenario:
        MissionScenarioDefinition,

        context:
        MissionRecommendationContext
    ): Int {

        val offeredPenalty =
            recencyPenalty(
                values =
                    context.recentlyOfferedScenarioTypes,

                target =
                    scenario.type,

                maximumPenalty =
                    RECENT_OFFER_PENALTY,

                reductionPerPosition =
                    OFFER_DISTANCE_REDUCTION
            )

        val completedPenalty =
            recencyPenalty(
                values =
                    context.recentlyCompletedScenarioTypes,

                target =
                    scenario.type,

                maximumPenalty =
                    RECENT_COMPLETION_PENALTY,

                reductionPerPosition =
                    COMPLETION_DISTANCE_REDUCTION
            )

        return offeredPenalty +
                completedPenalty
    }

    private fun calculateContextRepetitionPenalty(
        scenario:
        MissionScenarioDefinition,

        context:
        MissionRecommendationContext
    ): Int {

        val category =
            scenario.type.contextCategory

        return recencyPenalty(
            values =
                context.recentlyUsedContextCategories,

            target =
                category,

            maximumPenalty =
                RECENT_CONTEXT_PENALTY,

            reductionPerPosition =
                CONTEXT_DISTANCE_REDUCTION
        )
    }

    private fun calculateDismissalPenalty(
        scenario:
        MissionScenarioDefinition,

        context:
        MissionRecommendationContext
    ): Int {

        val scenarioPenalty =
            recencyPenalty(
                values =
                    context.recentlyDismissedScenarioTypes,

                target =
                    scenario.type,

                maximumPenalty =
                    RECENT_DISMISSAL_PENALTY,

                reductionPerPosition =
                    DISMISSAL_DISTANCE_REDUCTION
            )

        val categoryPenalty =
            recencyPenalty(
                values =
                    context.recentlyDismissedContextCategories,

                target =
                    scenario.type.contextCategory,

                maximumPenalty =
                    RECENT_DISMISSED_CONTEXT_PENALTY,

                reductionPerPosition =
                    DISMISSED_CONTEXT_DISTANCE_REDUCTION
            )

        return scenarioPenalty +
                categoryPenalty
    }

    private fun calculateMasteredCapabilityPenalty(
        scenario:
        MissionScenarioDefinition,

        normalizedMasteredCapabilities:
        List<String>
    ): Int {

        if (normalizedMasteredCapabilities.isEmpty()) {
            return 0
        }

        val scenarioSignals =
            scenario.normalizedLearningSignals()

        val masteredMatchCount =
            normalizedMasteredCapabilities.count { mastered ->
                scenarioSignals.any { signal ->
                    signalsOverlap(
                        first =
                            mastered,

                        second =
                            signal
                    )
                }
            }

        return (
                masteredMatchCount *
                        MASTERED_SIGNAL_PENALTY
                )
            .coerceAtMost(
                maximumValue =
                    MAXIMUM_MASTERED_PENALTY
            )
    }

    private fun calculateConversationScore(
        scenario:
        MissionScenarioDefinition,

        normalizedConversation:
        String
    ): Int {

        if (normalizedConversation.isBlank()) {
            return 0
        }

        val keywordMatches =
            scenario
                .topicKeywords
                .count { keyword ->
                    normalizedConversation.contains(
                        normalize(
                            value =
                                keyword
                        )
                    )
                }

        return (
                keywordMatches *
                        CONVERSATION_KEYWORD_SCORE
                )
            .coerceAtMost(
                maximumValue =
                    MAXIMUM_CONVERSATION_SCORE
            )
    }

    private fun MissionScenarioDefinition
            .normalizedLearningSignals():
            Set<String> {

        return (
                targetSkills +
                        objectiveBlueprints +
                        setOf(
                            title,
                            description,
                            type.name
                        )
                )
            .asSequence()
            .map(
                ::normalize
            )
            .filter(
                String::isNotBlank
            )
            .toSet()
    }

    private fun signalsOverlap(
        first: String,
        second: String
    ): Boolean {

        val firstTokens =
            meaningfulTokens(
                value =
                    first
            )

        val secondTokens =
            meaningfulTokens(
                value =
                    second
            )

        if (
            firstTokens.isEmpty() ||
            secondTokens.isEmpty()
        ) {
            return false
        }

        return firstTokens
            .intersect(
                secondTokens
            )
            .isNotEmpty()
    }

    private fun meaningfulTokens(
        value: String
    ): Set<String> {

        return normalize(
            value =
                value
        )
            .split(
                " "
            )
            .asSequence()
            .filter { token ->
                token.length >=
                        MINIMUM_SIGNAL_TOKEN_LENGTH
            }
            .filterNot { token ->
                token in
                        SIGNAL_STOP_WORDS
            }
            .toSet()
    }

    private fun adaptiveScenarioMatches(
        scenario:
        MissionScenarioDefinition,

        normalizedFocus:
        String
    ): Boolean {

        return when {
            normalizedFocus.containsAny(
                "giving reasons",
                "give simple reason",
                "develop opinion",
                "justify decision",
                "cause consequence",
                "begrund",
                "reason"
            ) ->
                scenario.type in
                        REASONING_SCENARIOS

            normalizedFocus.containsAny(
                "expressing opinions",
                "qualify opinion",
                "support claim",
                "counterargument",
                "meinung",
                "opinion"
            ) ->
                scenario.type in
                        OPINION_SCENARIOS

            normalizedFocus.containsAny(
                "comparing options",
                "contrast perspectives",
                "evaluate alternatives",
                "advantages disadvantages",
                "vergleich",
                "compare"
            ) ->
                scenario.type in
                        COMPARISON_SCENARIOS

            normalizedFocus.containsAny(
                "storytelling",
                "describing experiences",
                "narrate connected events",
                "sequence events",
                "geschichte",
                "erzahlen"
            ) ->
                scenario.type in
                        STORYTELLING_SCENARIOS

            normalizedFocus.containsAny(
                "polite interaction",
                "handle disagreement",
                "negotiate disagreement",
                "express reservation",
                "hoflich",
                "polite"
            ) ->
                scenario.type in
                        POLITE_INTERACTION_SCENARIOS

            normalizedFocus.contains(
                other =
                    "dative",
                ignoreCase =
                    true
            ) ->
                scenario.type in
                        LOCATION_AND_SERVICE_SCENARIOS

            else ->
                false
        }
    }

    private fun <T> recencyPenalty(
        values: List<T>,
        target: T,
        maximumPenalty: Int,
        reductionPerPosition: Int
    ): Int {

        val distance =
            reverseDistanceFromEnd(
                values =
                    values,

                target =
                    target
            )
                ?: return 0

        return (
                maximumPenalty -
                        distance *
                        reductionPerPosition
                )
            .coerceAtLeast(
                minimumValue =
                    0
            )
    }

    private fun <T> reverseDistanceFromEnd(
        values: List<T>,
        target: T
    ): Int? {

        val index =
            values
                .asReversed()
                .indexOf(
                    target
                )

        return index.takeIf { value ->
            value >=
                    0
        }
    }

    private fun stableVarietyValue(
        scenario:
        MissionScenarioDefinition,

        context:
        MissionRecommendationContext
    ): Int {

        val seed =
            buildString {
                append(
                    context.cefrLevel.name
                )

                append(
                    ":"
                )

                append(
                    context
                        .recentlyOfferedScenarioTypes
                        .joinToString(
                            separator =
                                ","
                        )
                )

                append(
                    ":"
                )

                append(
                    context
                        .recentlyDismissedScenarioTypes
                        .joinToString(
                            separator =
                                ","
                        )
                )

                append(
                    ":"
                )

                append(
                    context.freshnessFingerprint
                )

                append(
                    ":"
                )

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
                UMLAUT_A_REGEX,
                "ae"
            )
            .replace(
                UMLAUT_O_REGEX,
                "oe"
            )
            .replace(
                UMLAUT_U_REGEX,
                "ue"
            )
            .replace(
                SHARP_S_REGEX,
                "ss"
            )
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

    private fun String.containsAny(
        vararg values: String
    ): Boolean {

        return values.any { value ->
            contains(
                other =
                    value,

                ignoreCase =
                    true
            )
        }
    }

    private data class ScoredScenario(
        val scenario:
        MissionScenarioDefinition,

        val score: Int,

        val tieBreaker: Int
    )

    private companion object {

        const val DEFAULT_RANKED_CANDIDATE_LIMIT =
            6

        const val HARD_RECENT_OFFER_EXCLUSION_COUNT =
            2

        const val HARD_RECENT_DISMISSAL_EXCLUSION_COUNT =
            6

        const val CONTEXT_HISTORY_WINDOW =
            8

        const val CEFR_CURRICULUM_SCORE =
            40

        const val LEARNING_SIGNAL_SCORE =
            18

        const val MAXIMUM_LEARNING_RELEVANCE_SCORE =
            54

        const val UNUSED_CONTEXT_BONUS =
            24

        const val CONTEXT_DISTANCE_BONUS =
            4

        const val MAXIMUM_CONTEXT_DISTANCE_BONUS =
            20

        const val MASTERED_SIGNAL_PENALTY =
            14

        const val MAXIMUM_MASTERED_PENALTY =
            42

        const val CONVERSATION_KEYWORD_SCORE =
            2

        const val MAXIMUM_CONVERSATION_SCORE =
            6

        const val RECENT_OFFER_PENALTY =
            100

        const val OFFER_DISTANCE_REDUCTION =
            14

        const val RECENT_COMPLETION_PENALTY =
            72

        const val COMPLETION_DISTANCE_REDUCTION =
            10

        const val RECENT_CONTEXT_PENALTY =
            48

        const val CONTEXT_DISTANCE_REDUCTION =
            7

        const val RECENT_DISMISSAL_PENALTY =
            140

        const val DISMISSAL_DISTANCE_REDUCTION =
            16

        const val RECENT_DISMISSED_CONTEXT_PENALTY =
            56

        const val DISMISSED_CONTEXT_DISTANCE_REDUCTION =
            8

        const val MINIMUM_SIGNAL_TOKEN_LENGTH =
            4

        val NON_ALPHANUMERIC_REGEX =
            Regex(
                "[^\\p{L}\\p{N}]+"
            )

        val MULTIPLE_WHITESPACE_REGEX =
            Regex(
                "\\s+"
            )

        val UMLAUT_A_REGEX =
            Regex(
                "ä"
            )

        val UMLAUT_O_REGEX =
            Regex(
                "ö"
            )

        val UMLAUT_U_REGEX =
            Regex(
                "ü"
            )

        val SHARP_S_REGEX =
            Regex(
                "ß"
            )

        val SIGNAL_STOP_WORDS =
            setOf(
                "next",
                "step",
                "progression",
                "ready",
                "developing",
                "mastered",
                "category",
                "cefr",
                "score",
                "communication",
                "capability",
                "skill",
                "learner",
                "natural",
                "moment"
            )

        val REASONING_SCENARIOS =
            setOf(
                MissionScenarioType.PLANNING_A_TRIP,
                MissionScenarioType.MAKING_SOCIAL_PLANS,
                MissionScenarioType.JOB_INTERVIEW,
                MissionScenarioType.GIVING_REASONS,
                MissionScenarioType.MAKING_A_DECISION,
                MissionScenarioType.NEGOTIATION,
                MissionScenarioType.DEFENDING_AN_OPINION,
                MissionScenarioType.STRATEGIC_DISCUSSION,
                MissionScenarioType.ACADEMIC_DEBATE
            )

        val OPINION_SCENARIOS =
            setOf(
                MissionScenarioType.EXPRESSING_AN_OPINION,
                MissionScenarioType.MAKING_A_DECISION,
                MissionScenarioType.DEFENDING_AN_OPINION,
                MissionScenarioType.DISCUSSING_A_SOCIAL_TOPIC,
                MissionScenarioType.PARTICIPATING_IN_A_DEBATE,
                MissionScenarioType.ACADEMIC_DEBATE
            )

        val COMPARISON_SCENARIOS =
            setOf(
                MissionScenarioType.COMPARING_OPTIONS,
                MissionScenarioType.MAKING_A_DECISION,
                MissionScenarioType.EXPLAINING_ADVANTAGES_AND_DISADVANTAGES,
                MissionScenarioType.COMPARING_VIEWPOINTS,
                MissionScenarioType.STRATEGIC_DISCUSSION
            )

        val STORYTELLING_SCENARIOS =
            setOf(
                MissionScenarioType.TELLING_A_STORY,
                MissionScenarioType.DESCRIBING_A_WEEKEND,
                MissionScenarioType.DISCUSSING_TRAVEL_EXPERIENCES,
                MissionScenarioType.TALKING_ABOUT_PAST_EXPERIENCES
            )

        val POLITE_INTERACTION_SCENARIOS =
            setOf(
                MissionScenarioType.HOTEL_CHECK_IN,
                MissionScenarioType.RETURNING_A_PRODUCT,
                MissionScenarioType.MAKING_A_COMPLAINT,
                MissionScenarioType.FORMAL_COMPLAINT,
                MissionScenarioType.FORMAL_INTERVIEW,
                MissionScenarioType.NEGOTIATION,
                MissionScenarioType.DEALING_WITH_A_MISUNDERSTANDING
            )

        val LOCATION_AND_SERVICE_SCENARIOS =
            setOf(
                MissionScenarioType.ASKING_FOR_DIRECTIONS,
                MissionScenarioType.HOTEL_CHECK_IN,
                MissionScenarioType.DOCTOR_APPOINTMENT,
                MissionScenarioType.ARRANGING_A_SERVICE,
                MissionScenarioType.PUBLIC_TRANSPORT
            )
    }
}