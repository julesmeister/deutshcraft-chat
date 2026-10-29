package com.julesmeister.deutschcraft.feature.besser.data.selector

/*
 * Adapter boundary between the Besser selector and your
 * existing Qwen infrastructure.
 *
 * This keeps Qwen implementation details out of the Besser
 * domain.
 */
interface BesserSelectionTextGenerator {

    suspend fun generate(
        prompt: String
    ): String
}