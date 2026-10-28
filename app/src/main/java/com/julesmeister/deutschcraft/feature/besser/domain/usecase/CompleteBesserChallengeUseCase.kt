package com.julesmeister.deutschcraft.feature.besser.domain.usecase

import com.julesmeister.deutschcraft.feature.besser.domain.AssistanceLevel
import com.julesmeister.deutschcraft.feature.besser.domain.repository.BesserChallengeRepository
import javax.inject.Inject

class CompleteBesserChallengeUseCase @Inject constructor(
    private val besserChallengeRepository:
    BesserChallengeRepository
) {

    suspend operator fun invoke(
        challengeId: String,
        refinedMessageContent: String,
        actualAssistanceLevel: AssistanceLevel
    ): Result<Unit> {
        return runCatching {
            require(
                challengeId.isNotBlank()
            ) {
                "Challenge ID cannot be blank."
            }

            val normalizedRefinedContent =
                refinedMessageContent.trim()

            require(
                normalizedRefinedContent.isNotBlank()
            ) {
                "Refined message content cannot be blank."
            }

            val challenge =
                checkNotNull(
                    besserChallengeRepository.getById(
                        challengeId =
                            challengeId
                    )
                ) {
                    "Besser challenge was not found."
                }

            check(
                challenge.canComplete
            ) {
                "Only an active challenge can be completed."
            }

            check(
                normalizedRefinedContent !=
                        challenge.originalMessageContent.trim()
            ) {
                "The refined message must differ from the original message."
            }

            val completed =
                besserChallengeRepository.complete(
                    challengeId =
                        challengeId,
                    refinedMessageContent =
                        normalizedRefinedContent,
                    actualAssistanceLevel =
                        actualAssistanceLevel,
                    completedAt =
                        System.currentTimeMillis()
                )

            check(
                completed
            ) {
                "Besser challenge could not be completed."
            }
        }
    }
}