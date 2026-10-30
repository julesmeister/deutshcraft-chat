package com.julesmeister.deutschcraft.feature.besser.domain.evaluation

import com.julesmeister.deutschcraft.feature.besser.domain.BesserChallenge
import com.julesmeister.deutschcraft.feature.besser.domain.BesserRefinementEvaluation

interface BesserRefinementEvaluator {

    suspend fun evaluate(
        challenge: BesserChallenge,
        refinedText: String
    ): BesserRefinementEvaluation
}