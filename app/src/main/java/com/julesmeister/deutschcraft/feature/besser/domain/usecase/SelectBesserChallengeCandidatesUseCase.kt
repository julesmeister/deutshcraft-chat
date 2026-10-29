package com.julesmeister.deutschcraft.feature.besser.domain.usecase

import android.util.Log
import com.julesmeister.deutschcraft.core.database.dao.CapabilityDao
import com.julesmeister.deutschcraft.core.database.entity.besser.CapabilityEntity
import com.julesmeister.deutschcraft.feature.besser.domain.model.BesserCandidateSelectionRequest
import com.julesmeister.deutschcraft.feature.besser.domain.model.BesserCapabilityCandidate
import javax.inject.Inject

class SelectBesserChallengeCandidatesUseCase @Inject constructor(
    private val capabilityDao: CapabilityDao
) {

    suspend operator fun invoke(
        request: BesserCandidateSelectionRequest
    ): List<BesserCapabilityCandidate> {
        val message =
            request.userMessage.trim()

        Log.d(
            SHORTLIST_TAG,
            buildString {
                append("Starting shortlist. ")
                append("current=")
                append(request.currentCefrLevel)
                append(", target=")
                append(request.targetCefrLevel)
                append(", requestedLimit=")
                append(request.limit)
                append(", messageLength=")
                append(message.length)
            }
        )

        if (message.isBlank()) {
            Log.d(
                SHORTLIST_TAG,
                "Shortlist stopped: learner message is blank."
            )

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

        Log.d(
            SHORTLIST_TAG,
            buildString {
                append("Eligible CEFR levels=")
                append(eligibleCefrLevels)
                append(", effectiveLimit=")
                append(shortlistLimit)
            }
        )

        if (eligibleCefrLevels.isEmpty()) {
            Log.w(
                SHORTLIST_TAG,
                buildString {
                    append("Shortlist stopped: no valid CEFR levels. ")
                    append("current=")
                    append(request.currentCefrLevel)
                    append(", target=")
                    append(request.targetCefrLevel)
                }
            )

            return emptyList()
        }

        val messageTokens =
            tokenize(
                value =
                    message
            )
                .asSequence()
                .filterNot { token ->
                    token in MESSAGE_STOP_WORDS
                }
                .toSet()

        Log.d(
            SHORTLIST_TAG,
            buildString {
                append("Meaningful message tokens=")
                append(messageTokens.size)
                append(" ")
                append(messageTokens)
            }
        )

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

        Log.d(
            SHORTLIST_TAG,
            buildString {
                append("Detected categories=")
                append(normalizedCategories)
            }
        )

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

        Log.d(
            SHORTLIST_TAG,
            buildString {
                append("Recently challenged capabilities=")
                append(recentCapabilityIds.size)

                if (recentCapabilityIds.isNotEmpty()) {
                    append(" ")
                    append(recentCapabilityIds)
                }
            }
        )

        /*
         * ====================================================
         * ROOM CATALOGUE
         * ====================================================
         */

        val capabilities =
            capabilityDao.getActiveForShortlist(
                cefrLevels =
                    eligibleCefrLevels
            )

        Log.d(
            SHORTLIST_TAG,
            buildString {
                append("Room catalogue returned ")
                append(capabilities.size)
                append(" active capabilities for ")
                append(eligibleCefrLevels)
                append(".")
            }
        )

        if (capabilities.isEmpty()) {
            Log.w(
                SHORTLIST_TAG,
                buildString {
                    append("EMPTY CATALOGUE RESULT. ")
                    append("No active capabilities exist for levels=")
                    append(eligibleCefrLevels)
                    append(". Check BuiltInCapabilityCatalogue ")
                    append("and CapabilityCatalogueInitializer.")
                }
            )

            return emptyList()
        }

        /*
         * Log the CEFR distribution returned by Room.
         */
        capabilities
            .groupingBy { capability ->
                capability.cefrLevel
            }
            .eachCount()
            .forEach { (level, count) ->
                Log.d(
                    SHORTLIST_TAG,
                    "Room level=$level count=$count"
                )
            }

        /*
         * ====================================================
         * CONVERT TO CANDIDATES
         * ====================================================
         */

        val allCandidates =
            capabilities
                .asSequence()
                .filter { capability ->
                    capability.isActive
                }
                .map { capability ->
                    capability.toCandidate(
                        messageTokens =
                            messageTokens,
                        normalizedCategories =
                            normalizedCategories,
                        targetCefrLevel =
                            request.targetCefrLevel,
                        recentCapabilityIds =
                            recentCapabilityIds
                    )
                }
                .toList()

        Log.d(
            SHORTLIST_TAG,
            buildString {
                append("Candidates before eligibility filter=")
                append(allCandidates.size)
            }
        )

        /*
         * Print every candidate before local filtering.
         *
         * With the medium catalogue this is normally only the
         * current and target bands, around 20 entries.
         */
        allCandidates.forEach { candidate ->
            Log.d(
                SHORTLIST_TAG,
                buildString {
                    append("PRE ")
                    append(candidate.capabilityId)
                    append(" | level=")
                    append(candidate.cefrLevel)
                    append(" | category=")
                    append(candidate.category)
                    append(" | score=")
                    append(candidate.localPriorityScore)
                    append(" | lexical=")
                    append(candidate.lexicalMatchCount)
                    append(" | categoryMatched=")
                    append(candidate.categoryMatched)
                    append(" | targetLevel=")
                    append(candidate.isTargetLevelCapability)
                }
            )
        }

        /*
         * ====================================================
         * LOCAL ELIGIBILITY
         * ====================================================
         */

        val eligibleCandidates =
            allCandidates
                .filter { candidate ->
                    candidate.isLocallyEligible(
                        hasDetectedCategories =
                            normalizedCategories.isNotEmpty(),
                        hasMeaningfulMessageTokens =
                            messageTokens.isNotEmpty()
                    )
                }

        Log.d(
            SHORTLIST_TAG,
            buildString {
                append("Locally eligible candidates=")
                append(eligibleCandidates.size)
                append("/")
                append(allCandidates.size)
            }
        )

        if (eligibleCandidates.isEmpty()) {
            Log.w(
                SHORTLIST_TAG,
                buildString {
                    append("EMPTY LOCAL SHORTLIST. ")
                    append("Room supplied ")
                    append(capabilities.size)
                    append(" capabilities, but all were removed ")
                    append("by local eligibility. ")
                    append("messageTokens=")
                    append(messageTokens)
                    append(", categories=")
                    append(normalizedCategories)
                }
            )

            return emptyList()
        }

        /*
         * ====================================================
         * RANK + FINAL SHORTLIST
         * ====================================================
         */

        val shortlist =
            eligibleCandidates
                .sortedWith(
                    compareByDescending<BesserCapabilityCandidate> {
                            candidate ->
                        candidate.localPriorityScore
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
                    shortlistLimit
                )

        Log.i(
            SHORTLIST_TAG,
            buildString {
                append("FINAL SHORTLIST ")
                append(shortlist.size)
                append("/")
                append(capabilities.size)
                append(" | current=")
                append(request.currentCefrLevel)
                append(" | target=")
                append(request.targetCefrLevel)
            }
        )

        shortlist.forEachIndexed { index, candidate ->
            Log.i(
                SHORTLIST_TAG,
                buildString {
                    append("#")
                    append(index + 1)
                    append(" ")
                    append(candidate.capabilityId)
                    append(" | ")
                    append(candidate.name)
                    append(" | level=")
                    append(candidate.cefrLevel)
                    append(" | score=")
                    append(candidate.localPriorityScore)
                    append(" | lexical=")
                    append(candidate.lexicalMatchCount)
                    append(" | categoryMatched=")
                    append(candidate.categoryMatched)
                    append(" | targetLevel=")
                    append(candidate.isTargetLevelCapability)
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

/*
 * ===========================================================
 * Capability -> candidate
 * ===========================================================
 */

private fun CapabilityEntity.toCandidate(
    messageTokens: Set<String>,
    normalizedCategories: Set<String>,
    targetCefrLevel: String,
    recentCapabilityIds: Set<String>
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
                token in CAPABILITY_STOP_WORDS
            }
            .toSet()

    val lexicalMatches =
        messageTokens.intersect(
            capabilityTokens
        )

    val normalizedCapabilityCategory =
        normalizeCategory(
            category
        )

    val categoryMatched =
        normalizedCapabilityCategory in
                normalizedCategories

    val isTargetLevelCapability =
        normalizeCefrLevel(
            cefrLevel
        ) ==
                normalizeCefrLevel(
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
                            0
                        )
                )
            .coerceAtLeast(
                0
            ) *
                SORT_ORDER_WEIGHT

    val recentChallengePenalty =
        if (
            id in recentCapabilityIds
        ) {
            RECENT_CAPABILITY_PENALTY
        } else {
            0f
        }

    val localPriorityScore =
        (
                BASE_PRIORITY_SCORE +
                        lexicalScore +
                        categoryScore +
                        cefrScore +
                        catalogueOrderScore -
                        recentChallengePenalty
                )
            .coerceAtLeast(
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
            sortOrder
    )
}

/*
 * ===========================================================
 * Local eligibility
 * ===========================================================
 */

private fun BesserCapabilityCandidate.isLocallyEligible(
    hasDetectedCategories: Boolean,
    hasMeaningfulMessageTokens: Boolean
): Boolean {
    if (hasDetectedCategories) {
        return categoryMatched ||
                lexicalMatchCount > 0
    }

    /*
     * English catalogue descriptions generally will not share
     * many literal tokens with German learner messages.
     *
     * Target-level capabilities therefore remain eligible even
     * when lexical overlap is zero. Qwen performs the actual
     * semantic judgment afterward.
     */
    if (hasMeaningfulMessageTokens) {
        return lexicalMatchCount > 0 ||
                isTargetLevelCapability
    }

    return isTargetLevelCapability
}

/*
 * ===========================================================
 * CEFR range
 * ===========================================================
 */

private fun buildEligibleCefrLevels(
    currentCefrLevel: String,
    targetCefrLevel: String
): List<String> {
    val currentLevel =
        normalizeCefrLevel(
            currentCefrLevel
        )

    val targetLevel =
        normalizeCefrLevel(
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

/*
 * ===========================================================
 * Tokenization
 * ===========================================================
 */

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

/*
 * ===========================================================
 * Normalization
 * ===========================================================
 */

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

/*
 * ===========================================================
 * Patterns
 * ===========================================================
 */

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

/*
 * ===========================================================
 * CEFR
 * ===========================================================
 */

private val SUPPORTED_CEFR_LEVELS =
    setOf(
        "A1",
        "A2",
        "B1",
        "B2",
        "C1",
        "C2"
    )

/*
 * ===========================================================
 * Stop words
 * ===========================================================
 */

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

/*
 * ===========================================================
 * Selection limits
 * ===========================================================
 */

private const val MINIMUM_SHORTLIST_LIMIT =
    1

private const val MAXIMUM_SHORTLIST_LIMIT =
    8

private const val MINIMUM_TOKEN_LENGTH =
    2

private const val MAXIMUM_LEXICAL_MATCH_COUNT =
    5

/*
 * ===========================================================
 * Ranking weights
 * ===========================================================
 */

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

private const val MAXIMUM_SORT_ORDER_BONUS =
    20

private const val SORT_ORDER_WEIGHT =
    0.25f

private const val RECENT_CAPABILITY_PENALTY =
    40f