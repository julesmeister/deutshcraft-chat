package com.julesmeister.deutschcraft.feature.besser.domain.usecase

import com.julesmeister.deutschcraft.feature.besser.domain.repository.BesserChallengeRepository
import javax.inject.Inject

class RevealBesserChallengeHintUseCase @Inject constructor(
    private val besserChallengeRepository:
    BesserChallengeRepository
) {

    suspend operator fun invoke(
        challengeId: String
    ): Result<Unit> {
        return runCatching {
            require(
                challengeId.isNotBlank()
            ) {
                "Challenge ID cannot be blank."
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
                challenge.status.isActive
            ) {
                "Hints cannot be revealed for an inactive challenge."
            }

            check(
                challenge.canRevealHint
            ) {
                "The challenge has no additional hints."
            }

            val revealed =
                besserChallengeRepository.revealNextHint(
                    challengeId =
                        challengeId,
                    updatedAt =
                        System.currentTimeMillis()
                )

            check(
                revealed
            ) {
                "The next Besser challenge hint could not be revealed."
            }
        }
    }
}