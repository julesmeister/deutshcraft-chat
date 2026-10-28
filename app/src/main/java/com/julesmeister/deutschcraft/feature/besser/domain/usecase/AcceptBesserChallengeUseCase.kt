package com.julesmeister.deutschcraft.feature.besser.domain.usecase

import com.julesmeister.deutschcraft.feature.besser.domain.ChallengeStatus
import com.julesmeister.deutschcraft.feature.besser.domain.repository.BesserChallengeRepository
import javax.inject.Inject

class AcceptBesserChallengeUseCase @Inject constructor(
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
                challenge.status ==
                        ChallengeStatus.AVAILABLE
            ) {
                "Only an available challenge can be accepted."
            }

            val accepted =
                besserChallengeRepository.accept(
                    challengeId =
                        challengeId,
                    acceptedAt =
                        System.currentTimeMillis()
                )

            check(
                accepted
            ) {
                "Besser challenge could not be accepted."
            }
        }
    }
}