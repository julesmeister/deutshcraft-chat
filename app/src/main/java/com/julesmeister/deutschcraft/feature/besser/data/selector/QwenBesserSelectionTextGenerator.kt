package com.julesmeister.deutschcraft.feature.besser.data.selector

import android.util.Log
import com.julesmeister.deutschcraft.feature.chat.data.ai.DeutschCraftAiGateway
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class QwenBesserSelectionTextGenerator @Inject constructor(
    private val aiGateway:
    DeutschCraftAiGateway
) : BesserSelectionTextGenerator {

    override suspend fun generate(
        prompt: String
    ): String {
        val cleanPrompt =
            prompt.trim()

        require(
            cleanPrompt.isNotBlank()
        ) {
            "Besser selection prompt must not be blank."
        }

        Log.d(
            GENERATOR_TAG,
            buildString {
                append(
                    "Requesting structured Besser decision. "
                )
                append(
                    "promptChars="
                )
                append(
                    cleanPrompt.length
                )
            }
        )

        val response =
            aiGateway.generateStructuredText(
                systemPrompt =
                    BESSER_SELECTOR_SYSTEM_PROMPT,
                userPrompt =
                    cleanPrompt
            )
                .trim()

        Log.d(
            GENERATOR_TAG,
            buildString {
                append(
                    "Structured Besser response received. "
                )
                append(
                    "responseChars="
                )
                append(
                    response.length
                )
            }
        )

        return response
    }

    private companion object {

        const val GENERATOR_TAG =
            "BesserTextGenerator"

        const val BESSER_SELECTOR_SYSTEM_PROMPT =
            """
You are a structured decision engine inside DeutschCraft.

Your only task is to determine whether the learner's latest
message contains a natural opportunity to practise one of the
capabilities supplied in the user prompt.

You are not Emil.

You are not participating in the learner's conversation.

Do not answer the learner conversationally.

Do not praise, greet, correct, or ask the learner a question
unless that text appears inside the requested JSON instruction
or hint fields.

You may select only one capabilityId appearing in the supplied
candidate list.

Never invent a capability ID.

If none of the supplied capabilities naturally improves what
the learner was trying to communicate, return NO_CHALLENGE.

Return exactly one valid JSON object matching one of the schemas
provided in the user prompt.

The JSON object must start with { and end with }.

Do not use Markdown.

Do not use code fences.

Do not add commentary before the JSON.

Do not add commentary after the JSON.
"""
    }
}