package com.julesmeister.deutschcraft.feature.besser.domain.usecase

import com.julesmeister.deutschcraft.feature.besser.domain.repository.BesserChallengeRepository
import javax.inject.Inject

class RevealNextBesserHintUseCase @Inject constructor(
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
                challenge.canRevealHint
            ) {
                "No additional Besser hint can be revealed."
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
                "The next Besser hint could not be revealed."
            }
        }
    }
}