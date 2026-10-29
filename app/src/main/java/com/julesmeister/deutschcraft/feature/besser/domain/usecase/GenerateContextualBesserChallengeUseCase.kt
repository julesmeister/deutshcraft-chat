package com.julesmeister.deutschcraft.feature.besser.domain.usecase

import com.julesmeister.deutschcraft.feature.besser.domain.model.BesserChallengeSelectionResult
import javax.inject.Inject

/*
 * Application-facing entry point for generating a contextual
 * Besser Challenge after a learner sends a message.
 *
 * This wraps the complete two-stage selection pipeline:
 *
 * 1. Local Room-backed capability shortlist
 * 2. Qwen contextual selection
 */
class GenerateContextualBesserChallengeUseCase @Inject constructor(
    private val selectContextualChallenge:
    SelectContextualBesserChallengeUseCase
) {

    suspend operator fun invoke(
        learnerMessage: String,
        currentCefrLevel: String,
        targetCefrLevel: String,
        missionTitle: String?,
        missionObjective: String?,
        detectedCategories: Set<String> =
            emptySet(),
        recentlyChallengedCapabilityIds: Set<String> =
            emptySet()
    ): BesserChallengeSelectionResult {
        val cleanMessage =
            learnerMessage.trim()

        if (
            !isEligibleMessage(
                message =
                    cleanMessage
            )
        ) {
            return BesserChallengeSelectionResult.NoChallenge(
                reason =
                    "Learner message was too short for a contextual challenge"
            )
        }

        return selectContextualChallenge(
            userMessage =
                cleanMessage,
            currentCefrLevel =
                currentCefrLevel,
            targetCefrLevel =
                targetCefrLevel,
            missionTitle =
                missionTitle,
            missionObjective =
                missionObjective,
            detectedCategories =
                detectedCategories,
            recentlyChallengedCapabilityIds =
                recentlyChallengedCapabilityIds
        )
    }

    private fun isEligibleMessage(
        message: String
    ): Boolean {
        if (message.isBlank()) {
            return false
        }

        val meaningfulWords =
            WORD_PATTERN
                .findAll(
                    message
                )
                .map { match ->
                    match.value
                }
                .filter { word ->
                    word.length >=
                            MINIMUM_WORD_LENGTH
                }
                .count()

        return meaningfulWords >=
                MINIMUM_MEANINGFUL_WORD_COUNT
    }
}

private val WORD_PATTERN =
    Regex(
        pattern =
            """[\p{L}\p{N}äöüßÄÖÜ]+"""
    )

private const val MINIMUM_WORD_LENGTH =
    2

private const val MINIMUM_MEANINGFUL_WORD_COUNT =
    4