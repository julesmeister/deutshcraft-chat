package com.julesmeister.deutschcraft.feature.besser.domain.usecase

import com.julesmeister.deutschcraft.feature.besser.domain.model.BesserCandidateSelectionRequest
import com.julesmeister.deutschcraft.feature.besser.domain.model.BesserChallengeSelectionRequest
import com.julesmeister.deutschcraft.feature.besser.domain.model.BesserChallengeSelectionResult
import com.julesmeister.deutschcraft.feature.besser.domain.selector.BesserChallengeSelector
import javax.inject.Inject

class SelectContextualBesserChallengeUseCase
@Inject constructor(
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