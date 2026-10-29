package com.julesmeister.deutschcraft.feature.besser.domain.selector

import com.julesmeister.deutschcraft.feature.besser.domain.model.BesserChallengeSelectionRequest
import com.julesmeister.deutschcraft.feature.besser.domain.model.BesserChallengeSelectionResult

/*
 * AI-independent contract.
 *
 * The domain layer does not care whether the implementation
 * eventually uses:
 *
 * - Qwen
 * - another on-device model
 * - a remote model
 *
 * DeutschCraft currently intends to use Qwen.
 */
interface BesserChallengeSelector {

    suspend fun select(
        request: BesserChallengeSelectionRequest
    ): BesserChallengeSelectionResult
}