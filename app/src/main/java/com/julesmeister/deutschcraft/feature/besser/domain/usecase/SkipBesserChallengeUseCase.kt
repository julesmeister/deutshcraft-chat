package com.julesmeister.deutschcraft.feature.besser.domain.usecase

import com.julesmeister.deutschcraft.feature.besser.domain.repository.BesserChallengeRepository
import javax.inject.Inject

class SkipBesserChallengeUseCase @Inject constructor(
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
                challenge.canSkip
            ) {
                "Only an active challenge can be skipped."
            }

            val skipped =
                besserChallengeRepository.skip(
                    challengeId =
                        challengeId,
                    skippedAt =
                        System.currentTimeMillis()
                )

            check(
                skipped
            ) {
                "Besser challenge could not be skipped."
            }
        }
    }
}