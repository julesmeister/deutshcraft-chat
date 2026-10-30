package com.julesmeister.deutschcraft.feature.besser.domain.usecase

import com.julesmeister.deutschcraft.feature.besser.domain.BesserRefinementEvaluation
import com.julesmeister.deutschcraft.feature.besser.domain.evaluation.BesserRefinementEvaluator
import com.julesmeister.deutschcraft.feature.besser.domain.repository.BesserChallengeRepository
import javax.inject.Inject

class EvaluateBesserRefinementUseCase
@Inject constructor(
    private val besserChallengeRepository:
    BesserChallengeRepository,

    private val evaluator:
    BesserRefinementEvaluator
) {

    suspend operator fun invoke(
        challengeId: String,
        refinedText: String
    ): Result<BesserRefinementEvaluation> {
        return runCatching {
            require(
                challengeId.isNotBlank()
            ) {
                "Challenge ID cannot be blank."
            }

            val normalizedText =
                refinedText.trim()

            require(
                normalizedText.isNotBlank()
            ) {
                "Refined text cannot be blank."
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
                "Only an active challenge can be evaluated."
            }

            evaluator.evaluate(
                challenge =
                    challenge,
                refinedText =
                    normalizedText
            )
        }
    }
}