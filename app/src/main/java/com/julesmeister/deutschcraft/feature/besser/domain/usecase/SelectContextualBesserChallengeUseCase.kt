package com.julesmeister.deutschcraft.feature.besser.domain.usecase

import com.julesmeister.deutschcraft.feature.besser.domain.model.BesserCandidateSelectionRequest
import com.julesmeister.deutschcraft.feature.besser.domain.model.BesserChallengeSelectionRequest
import com.julesmeister.deutschcraft.feature.besser.domain.model.BesserChallengeSelectionResult
import com.julesmeister.deutschcraft.feature.besser.domain.selector.BesserChallengeSelector
import javax.inject.Inject

/*
 * Full contextual Besser selection pipeline.
 *
 * Stage 1:
 *
 *     Room capability catalogue
 *             ↓
 *     local shortlist
 *
 * Stage 2:
 *
 *     learner message
 *     + shortlist
 *     + CEFR
 *     + mission context
 *             ↓
 *     contextual Qwen selector
 *
 * Result:
 *
 *     Selected
 *
 * or
 *
 *     NoChallenge
 */
class SelectContextualBesserChallengeUseCase @Inject constructor(
    private val selectCandidates:
    SelectBesserChallengeCandidatesUseCase,

    private val challengeSelector:
    BesserChallengeSelector
) {

    suspend operator fun invoke(
        userMessage: String,
        currentCefrLevel: String,
        targetCefrLevel: String,
        missionTitle: String? = null,
        missionObjective: String? = null,
        detectedCategories: Set<String> =
            emptySet(),
        recentlyChallengedCapabilityIds: Set<String> =
            emptySet()
    ): BesserChallengeSelectionResult {
        val cleanMessage =
            userMessage.trim()

        if (cleanMessage.isBlank()) {
            return BesserChallengeSelectionResult.NoChallenge(
                reason =
                    "Learner message was empty"
            )
        }

        /*
         * ===================================================
         * Stage 1
         * ===================================================
         *
         * Run the cheap local shortlist first.
         */
        val candidates =
            selectCandidates(
                request =
                    BesserCandidateSelectionRequest(
                        userMessage =
                            cleanMessage,
                        currentCefrLevel =
                            currentCefrLevel,
                        targetCefrLevel =
                            targetCefrLevel,
                        detectedCategories =
                            detectedCategories,
                        recentlyChallengedCapabilityIds =
                            recentlyChallengedCapabilityIds
                    )
            )

        if (candidates.isEmpty()) {
            return BesserChallengeSelectionResult.NoChallenge(
                reason =
                    "No locally relevant capability candidates"
            )
        }

        /*
         * ===================================================
         * Stage 2
         * ===================================================
         *
         * Qwen receives only this shortlist.
         */
        return challengeSelector.select(
            request =
                BesserChallengeSelectionRequest(
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
                    candidates =
                        candidates
                )
        )
    }
}