// app/src/main/java/com/julesmeister/deutschcraft/feature/besser/domain/usecase/SelectBesserChallengeCandidatesUseCase.kt

package com.julesmeister.deutschcraft.feature.besser.domain.usecase

import android.util.Log
import com.julesmeister.deutschcraft.core.database.dao.CapabilityDao
import com.julesmeister.deutschcraft.core.database.entity.besser.CapabilityEntity
import com.julesmeister.deutschcraft.feature.besser.domain.mastery.CapabilityMastery
import com.julesmeister.deutschcraft.feature.besser.domain.mastery.CapabilityMasteryStatus
import com.julesmeister.deutschcraft.feature.besser.domain.model.BesserCandidateSelectionRequest
import com.julesmeister.deutschcraft.feature.besser.domain.model.BesserCapabilityCandidate
import com.julesmeister.deutschcraft.feature.besser.domain.progression.CapabilityProgressionEvaluator
import com.julesmeister.deutschcraft.feature.besser.domain.progression.CapabilityProgressionState
import javax.inject.Inject

class SelectBesserChallengeCandidatesUseCase
@Inject constructor(
    private val capabilityDao:
    CapabilityDao,

    private val getCapabilityMasteries:
    GetCapabilityMasteriesUseCase,

    private val progressionEvaluator:
    CapabilityProgressionEvaluator
) {

    suspend operator fun invoke(
        request: BesserCandidateSelectionRequest
    ): List<BesserCapabilityCandidate> {

        val message =
            request.userMessage.trim()

        if (message.isBlank()) {
            return emptyList()
        }

        val shortlistLimit =
            request.limit.coerceIn(
                minimumValue =
                    MINIMUM_SHORTLIST_LIMIT,

                maximumValue =
                    MAXIMUM_SHORTLIST_LIMIT
            )

        val eligibleCefrLevels =
            buildEligibleCefrLevels(
                currentCefrLevel =
                    request.currentCefrLevel,

                targetCefrLevel =
                    request.targetCefrLevel
            )

        if (eligibleCefrLevels.isEmpty()) {
            return emptyList()
        }

        val messageTokens =
            tokenize(
                value =
                    message
            )
                .asSequence()
                .filterNot { token ->
                    token in
                            MESSAGE_STOP_WORDS
                }
                .toSet()

        val normalizedCategories =
            request
                .detectedCategories
                .asSequence()
                .map(
                    ::normalizeCategory
                )
                .filter(
                    String::isNotBlank
                )
                .toSet()

        val recentCapabilityIds =
            request
                .recentlyChallengedCapabilityIds
                .asSequence()
                .map(
                    String::trim
                )
                .filter(
                    String::isNotBlank
                )
                .toSet()

        val capabilities =
            capabilityDao.getActiveForShortlist(
                cefrLevels =
                    eligibleCefrLevels
            )

        if (capabilities.isEmpty()) {
            Log.w(
                SHORTLIST_TAG,
                buildString {
                    append(
                        "No active capabilities for "
                    )

                    append(
                        eligibleCefrLevels
                    )
                }
            )

            return emptyList()
        }

        /*
         * Include both:
         *
         * - the locally selectable capabilities
         * - every prerequisite referenced by those capabilities
         *
         * This is important because the target-level capability
         * may depend on a prerequisite outside the current
         * shortlist's CEFR bands.
         */
        val shortlistCapabilityIds =
            capabilities
                .asSequence()
                .map { capability ->
                    capability.id
                }
                .toSet()

        val prerequisiteCapabilityIds =
            shortlistCapabilityIds
                .asSequence()
                .flatMap { capabilityId ->
                    progressionEvaluator
                        .prerequisitesFor(
                            capabilityId =
                                capabilityId
                        )
                        .asSequence()
                }
                .toSet()

        val masteryCapabilityIds =
            shortlistCapabilityIds +
                    prerequisiteCapabilityIds

        val masteryByCapabilityId =
            getCapabilityMasteries(
                capabilityIds =
                    masteryCapabilityIds
            )

        val allCandidates =
            capabilities
                .asSequence()
                .filter { capability ->
                    capability.isActive
                }
                .map { capability ->
                    val mastery =
                        masteryByCapabilityId[
                            capability.id
                        ]
                            ?: CapabilityMastery.empty(
                                capabilityId =
                                    capability.id
                            )

                    val progression =
                        progressionEvaluator.evaluate(
                            capabilityId =
                                capability.id,

                            masteryByCapabilityId =
                                masteryByCapabilityId
                        )

                    capability.toCandidate(
                        messageTokens =
                            messageTokens,

                        normalizedCategories =
                            normalizedCategories,

                        targetCefrLevel =
                            request.targetCefrLevel,

                        recentCapabilityIds =
                            recentCapabilityIds,

                        mastery =
                            mastery,

                        progression =
                            progression
                    )
                }
                .toList()

        val eligibleCandidates =
            allCandidates.filter { candidate ->
                candidate.isLocallyEligible(
                    hasDetectedCategories =
                        normalizedCategories.isNotEmpty(),

                    hasMeaningfulMessageTokens =
                        messageTokens.isNotEmpty()
                )
            }

        if (eligibleCandidates.isEmpty()) {
            return emptyList()
        }

        val shortlist =
            eligibleCandidates
                .sortedWith(
                    compareByDescending<BesserCapabilityCandidate> { candidate ->
                        candidate.localPriorityScore
                    }
                        .thenByDescending { candidate ->
                            candidate
                                .isDirectSuccessorOfMasteredCapability
                        }
                        .thenByDescending { candidate ->
                            candidate.progressionReady
                        }
                        .thenByDescending { candidate ->
                            candidate.categoryMatched
                        }
                        .thenByDescending { candidate ->
                            candidate.lexicalMatchCount
                        }
                        .thenByDescending { candidate ->
                            candidate.isTargetLevelCapability
                        }
                        .thenBy { candidate ->
                            candidate.sortOrder
                        }
                        .thenBy { candidate ->
                            candidate.name.lowercase()
                        }
                )
                .take(
                    n =
                        shortlistLimit
                )

        shortlist.forEachIndexed { index, candidate ->
            Log.i(
                SHORTLIST_TAG,
                buildString {
                    append("#")
                    append(index + 1)

                    append(" ")
                    append(candidate.capabilityId)

                    append(" | score=")
                    append(candidate.localPriorityScore)

                    append(" | mastery=")
                    append(candidate.masteryPercentage)
                    append("%")

                    append(" | status=")
                    append(candidate.masteryStatus)

                    append(" | progressionReady=")
                    append(candidate.progressionReady)

                    append(" | prerequisiteMastery=")
                    append(candidate.prerequisiteMasteryPercentage)
                    append("%")

                    append(" | successorOfMastered=")
                    append(
                        candidate
                            .isDirectSuccessorOfMasteredCapability
                    )

                    append(" | recent=")
                    append(
                        candidate.capabilityId in
                                recentCapabilityIds
                    )
                }
            )
        }

        return shortlist
    }

    private companion object {

        const val SHORTLIST_TAG =
            "BesserShortlist"
    }
}

private fun CapabilityEntity.toCandidate(
    messageTokens: Set<String>,
    normalizedCategories: Set<String>,
    targetCefrLevel: String,
    recentCapabilityIds: Set<String>,
    mastery: CapabilityMastery,
    progression: CapabilityProgressionState
): BesserCapabilityCandidate {

    val capabilityTokens =
        (
                tokenize(
                    value =
                        name
                ) +
                        tokenize(
                            value =
                                description
                        )
                )
            .asSequence()
            .filterNot { token ->
                token in
                        CAPABILITY_STOP_WORDS
            }
            .toSet()

    val lexicalMatches =
        messageTokens.intersect(
            capabilityTokens
        )

    val normalizedCapabilityCategory =
        normalizeCategory(
            category =
                category
        )

    val categoryMatched =
        normalizedCapabilityCategory in
                normalizedCategories

    val isTargetLevelCapability =
        normalizeCefrLevel(
            rawLevel =
                cefrLevel
        ) ==
                normalizeCefrLevel(
                    rawLevel =
                        targetCefrLevel
                )

    val lexicalScore =
        minOf(
            lexicalMatches.size,
            MAXIMUM_LEXICAL_MATCH_COUNT
        ) *
                LEXICAL_MATCH_WEIGHT

    val categoryScore =
        if (categoryMatched) {
            CATEGORY_MATCH_WEIGHT
        } else {
            0f
        }

    val cefrScore =
        if (isTargetLevelCapability) {
            TARGET_LEVEL_WEIGHT
        } else {
            CURRENT_LEVEL_WEIGHT
        }

    val catalogueOrderScore =
        (
                MAXIMUM_SORT_ORDER_BONUS -
                        sortOrder.coerceAtLeast(
                            minimumValue =
                                0
                        )
                )
            .coerceAtLeast(
                minimumValue =
                    0
            ) *
                SORT_ORDER_WEIGHT

    val recentChallengePenalty =
        if (
            id in
            recentCapabilityIds
        ) {
            RECENT_CAPABILITY_PENALTY
        } else {
            0f
        }

    val masteryAdjustment =
        when (mastery.status) {
            CapabilityMasteryStatus.DEVELOPING -> {
                DEVELOPING_CAPABILITY_BONUS
            }

            CapabilityMasteryStatus.PRACTISING -> {
                PRACTISING_CAPABILITY_BONUS
            }

            CapabilityMasteryStatus.MASTERED -> {
                -MASTERED_CAPABILITY_PENALTY
            }
        }

    val progressionAdjustment =
        when {
            progression
                .isDirectSuccessorOfMasteredCapability -> {
                DIRECT_SUCCESSOR_BONUS
            }

            progression.isProgressionReady &&
                    progression.hasPrerequisites -> {
                PROGRESSION_READY_BONUS
            }

            !progression.isProgressionReady &&
                    progression.hasPrerequisites -> {
                -UNREADY_PROGRESSION_PENALTY
            }

            else -> {
                0f
            }
        }

    val localPriorityScore =
        (
                BASE_PRIORITY_SCORE +
                        lexicalScore +
                        categoryScore +
                        cefrScore +
                        catalogueOrderScore +
                        masteryAdjustment +
                        progressionAdjustment -
                        recentChallengePenalty
                )
            .coerceAtLeast(
                minimumValue =
                    0f
            )

    return BesserCapabilityCandidate(
        capabilityId =
            id,

        name =
            name,

        description =
            description,

        category =
            category,

        cefrLevel =
            cefrLevel,

        localPriorityScore =
            localPriorityScore,

        lexicalMatchCount =
            lexicalMatches.size,

        categoryMatched =
            categoryMatched,

        isTargetLevelCapability =
            isTargetLevelCapability,

        sortOrder =
            sortOrder,

        masteryScore =
            mastery.safeMasteryScore,

        masteryStatus =
            mastery.status,

        masteryAttemptCount =
            mastery.attemptCount,

        masteryCompletedCount =
            mastery.completedCount,

        masterySkippedCount =
            mastery.skippedCount,

        lastChallengedAt =
            mastery.lastChallengedAt,

        progressionReady =
            progression.isProgressionReady,

        progressionPrerequisiteCount =
            progression.totalPrerequisiteCount,

        satisfiedProgressionPrerequisiteCount =
            progression.satisfiedPrerequisiteCount,

        averagePrerequisiteMastery =
            progression.safeAveragePrerequisiteMastery,

        isDirectSuccessorOfMasteredCapability =
            progression
                .isDirectSuccessorOfMasteredCapability
    )
}

private fun BesserCapabilityCandidate.isLocallyEligible(
    hasDetectedCategories: Boolean,
    hasMeaningfulMessageTokens: Boolean
): Boolean {

    if (hasDetectedCategories) {
        return categoryMatched ||
                lexicalMatchCount >
                0
    }

    if (hasMeaningfulMessageTokens) {
        return lexicalMatchCount >
                0 ||
                isTargetLevelCapability
    }

    return isTargetLevelCapability
}

private fun buildEligibleCefrLevels(
    currentCefrLevel: String,
    targetCefrLevel: String
): List<String> {

    val currentLevel =
        normalizeCefrLevel(
            rawLevel =
                currentCefrLevel
        )

    val targetLevel =
        normalizeCefrLevel(
            rawLevel =
                targetCefrLevel
        )

    val currentIsValid =
        currentLevel in
                SUPPORTED_CEFR_LEVELS

    val targetIsValid =
        targetLevel in
                SUPPORTED_CEFR_LEVELS

    return when {
        currentIsValid &&
                targetIsValid -> {
            listOf(
                currentLevel,
                targetLevel
            )
                .distinct()
        }

        currentIsValid -> {
            listOf(
                currentLevel
            )
        }

        targetIsValid -> {
            listOf(
                targetLevel
            )
        }

        else -> {
            emptyList()
        }
    }
}

private fun tokenize(
    value: String
): List<String> {

    return TOKEN_PATTERN
        .findAll(
            value.lowercase()
        )
        .map { match ->
            match.value
        }
        .filter { token ->
            token.length >=
                    MINIMUM_TOKEN_LENGTH
        }
        .toList()
}

private fun normalizeCategory(
    category: String
): String {

    return category
        .trim()
        .lowercase()
        .replace(
            CATEGORY_SEPARATOR_PATTERN,
            "_"
        )
        .trim(
            '_'
        )
}

private fun normalizeCefrLevel(
    rawLevel: String
): String {

    return rawLevel
        .trim()
        .uppercase()
}

private val TOKEN_PATTERN =
    Regex(
        pattern =
            """[\p{L}\p{N}äöüßÄÖÜ]+"""
    )

private val CATEGORY_SEPARATOR_PATTERN =
    Regex(
        pattern =
            """[\s\-/]+"""
    )

private val SUPPORTED_CEFR_LEVELS =
    setOf(
        "A1",
        "A2",
        "B1",
        "B2",
        "C1",
        "C2"
    )

private val MESSAGE_STOP_WORDS =
    setOf(
        "aber",
        "also",
        "am",
        "an",
        "auf",
        "aus",
        "bei",
        "bin",
        "bist",
        "da",
        "das",
        "dass",
        "dem",
        "den",
        "der",
        "des",
        "die",
        "du",
        "ein",
        "eine",
        "einem",
        "einen",
        "einer",
        "er",
        "es",
        "für",
        "hat",
        "haben",
        "ich",
        "im",
        "in",
        "ist",
        "mit",
        "nicht",
        "oder",
        "sie",
        "sind",
        "so",
        "und",
        "von",
        "war",
        "was",
        "wenn",
        "wie",
        "wir",
        "zu",
        "zum",
        "zur",
        "a",
        "an",
        "and",
        "are",
        "as",
        "at",
        "be",
        "but",
        "for",
        "from",
        "i",
        "in",
        "is",
        "it",
        "of",
        "on",
        "or",
        "that",
        "the",
        "this",
        "to",
        "was",
        "with"
    )

private val CAPABILITY_STOP_WORDS =
    MESSAGE_STOP_WORDS +
            setOf(
                "ability",
                "capability",
                "correctly",
                "express",
                "language",
                "learner",
                "sentence",
                "skill",
                "structure",
                "fähigkeit",
                "lerner",
                "satz",
                "sprache",
                "struktur",
                "verwenden"
            )

private const val MINIMUM_SHORTLIST_LIMIT =
    1

private const val MAXIMUM_SHORTLIST_LIMIT =
    8

private const val MINIMUM_TOKEN_LENGTH =
    2

private const val MAXIMUM_LEXICAL_MATCH_COUNT =
    5

private const val MAXIMUM_SORT_ORDER_BONUS =
    20

private const val BASE_PRIORITY_SCORE =
    10f

private const val LEXICAL_MATCH_WEIGHT =
    8f

private const val CATEGORY_MATCH_WEIGHT =
    24f

private const val TARGET_LEVEL_WEIGHT =
    12f

private const val CURRENT_LEVEL_WEIGHT =
    5f

private const val SORT_ORDER_WEIGHT =
    0.25f

private const val RECENT_CAPABILITY_PENALTY =
    40f

private const val DEVELOPING_CAPABILITY_BONUS =
    18f

private const val PRACTISING_CAPABILITY_BONUS =
    8f

private const val MASTERED_CAPABILITY_PENALTY =
    35f

private const val PROGRESSION_READY_BONUS =
    12f

private const val DIRECT_SUCCESSOR_BONUS =
    22f

private const val UNREADY_PROGRESSION_PENALTY =
    14f