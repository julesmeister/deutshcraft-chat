package com.julesmeister.deutschcraft.feature.besser.application

import com.julesmeister.deutschcraft.feature.besser.domain.usecase.GenerateContextualBesserChallengeUseCase
import com.julesmeister.deutschcraft.feature.besser.presentation.BesserChallengeUiModel
import com.julesmeister.deutschcraft.feature.besser.presentation.toUiModelOrNull
import javax.inject.Inject

/*
 * Coordinates challenge generation and presentation mapping.
 *
 * ChatViewModel can call this after the user message has been
 * persisted successfully.
 */
class ContextualBesserChallengeCoordinator @Inject constructor(
    private val generateChallenge:
    GenerateContextualBesserChallengeUseCase
) {

    suspend fun generate(
        messageId: String,
        learnerMessage: String,
        currentCefrLevel: String,
        targetCefrLevel: String,
        missionTitle: String?,
        missionObjective: String?,
        detectedCategories: Set<String> =
            emptySet(),
        recentlyChallengedCapabilityIds: Set<String> =
            emptySet()
    ): BesserChallengeUiModel? {
        if (
            messageId.isBlank() ||
            learnerMessage.isBlank()
        ) {
            return null
        }

        val result =
            generateChallenge(
                learnerMessage =
                    learnerMessage,
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

        return result.toUiModelOrNull(
            messageId =
                messageId
        )
    }
}