package com.julesmeister.deutschcraft.feature.besser.presentation

import com.julesmeister.deutschcraft.feature.besser.domain.model.BesserChallengeSelectionResult
import java.util.UUID

/*
 * Converts the contextual selector result into the presentation
 * model already consumed by BesserChallengeBubble.
 *
 * NO_CHALLENGE produces null, so no attachment is rendered
 * beneath the user message.
 */
internal fun BesserChallengeSelectionResult.toUiModelOrNull(
    messageId: String
): BesserChallengeUiModel? {
    return when (this) {
        is BesserChallengeSelectionResult.NoChallenge -> {
            null
        }

        is BesserChallengeSelectionResult.Selected -> {
            BesserChallengeUiModel(
                id =
                    UUID
                        .randomUUID()
                        .toString(),
                messageId =
                    messageId,
                capabilityId =
                    capability.capabilityId,
                capabilityName =
                    capability.name,
                instruction =
                    instruction,
                rationale =
                    rationale,
                hints =
                    hints,
                revealedHintCount =
                    0,
                confidence =
                    confidence,
                isRefinementActive =
                    false
            )
        }
    }
}