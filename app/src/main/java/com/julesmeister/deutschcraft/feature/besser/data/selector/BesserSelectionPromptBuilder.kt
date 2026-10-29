package com.julesmeister.deutschcraft.feature.besser.data.selector

import com.julesmeister.deutschcraft.feature.besser.domain.model.BesserChallengeSelectionRequest

internal object BesserSelectionPromptBuilder {

    fun build(
        request: BesserChallengeSelectionRequest
    ): String {
        val candidatesText =
            request.candidates
                .mapIndexed { index, candidate ->
                    buildString {
                        append("Candidate ")
                        append(index + 1)
                        appendLine()

                        append("capabilityId: ")
                        appendLine(
                            candidate.capabilityId
                        )

                        append("name: ")
                        appendLine(
                            candidate.name
                        )

                        append("description: ")
                        appendLine(
                            candidate.description
                        )

                        append("category: ")
                        appendLine(
                            candidate.category
                        )

                        append("cefrLevel: ")
                        appendLine(
                            candidate.cefrLevel
                        )
                    }
                }
                .joinToString(
                    separator =
                        "\n"
                )

        val missionContext =
            buildString {
                request.missionTitle
                    ?.trim()
                    ?.takeIf(
                        String::isNotBlank
                    )
                    ?.let { title ->
                        append("Mission title: ")
                        appendLine(
                            title
                        )
                    }

                request.missionObjective
                    ?.trim()
                    ?.takeIf(
                        String::isNotBlank
                    )
                    ?.let { objective ->
                        append(
                            "Mission objective: "
                        )
                        appendLine(
                            objective
                        )
                    }
            }
                .trim()

        return buildString {
            appendLine(
                "You are the contextual Besser growth selector " +
                        "for DeutschCraft."
            )

            appendLine()

            appendLine(
                "DeutschCraft teaches German through real " +
                        "conversation rather than isolated lessons."
            )

            appendLine()

            appendLine("YOUR JOB")

            appendLine()

            appendLine(
                "Determine whether the learner's latest message " +
                        "contains a natural opportunity to practise " +
                        "ONE capability from the supplied candidate list."
            )

            appendLine()

            appendLine("IMPORTANT RULES")

            appendLine()

            appendLine(
                "1. Do not create a challenge merely because a " +
                        "candidate exists."
            )

            appendLine(
                "2. Do not force unnecessary grammatical complexity."
            )

            appendLine(
                "3. The selected capability must improve what the " +
                        "learner was already trying to communicate."
            )

            appendLine(
                "4. Never select a capability outside the supplied " +
                        "candidate list."
            )

            appendLine(
                "5. If none naturally fits, return NO_CHALLENGE."
            )

            appendLine(
                "6. Prefer useful communicative growth over showing " +
                        "off sophisticated grammar."
            )

            appendLine(
                "7. For example, do not require einerseits / " +
                        "andererseits unless contrasting two sides is " +
                        "actually relevant to the learner's message."
            )

            appendLine(
                "8. The challenge instruction must tell the learner " +
                        "what to improve without writing the complete " +
                        "answer for them."
            )

            appendLine(
                "9. Hints must become progressively more helpful."
            )

            appendLine()

            append("Current CEFR: ")
            appendLine(
                request.currentCefrLevel
            )

            append("Target CEFR: ")
            appendLine(
                request.targetCefrLevel
            )

            if (missionContext.isNotBlank()) {
                appendLine()
                appendLine("MISSION CONTEXT")
                appendLine()
                appendLine(
                    missionContext
                )
            }

            appendLine()
            appendLine("LEARNER MESSAGE")
            appendLine()

            appendLine(
                request.userMessage.trim()
            )

            appendLine()
            appendLine("ALLOWED CAPABILITIES")
            appendLine()

            appendLine(
                candidatesText
            )

            appendLine()
            appendLine("OUTPUT")

            appendLine()

            appendLine(
                "Return exactly one JSON object."
            )

            appendLine()

            appendLine(
                "If no challenge is appropriate:"
            )

            appendLine()

            appendLine(
                """
                {
                  "decision": "NO_CHALLENGE",
                  "reason": "brief reason"
                }
                """.trimIndent()
            )

            appendLine()

            appendLine(
                "If a challenge is appropriate:"
            )

            appendLine()

            appendLine(
                """
                {
                  "decision": "SELECT",
                  "capabilityId": "exact capabilityId from the candidate list",
                  "rationale": "short explanation in German",
                  "instruction": "short learner-facing challenge in German",
                  "hints": [
                    "gentle first hint",
                    "more specific second hint",
                    "optional stronger third hint"
                  ],
                  "confidence": 0.85
                }
                """.trimIndent()
            )

            appendLine()

            appendLine(
                "confidence must be between 0.0 and 1.0."
            )

            appendLine(
                "Do not return Markdown."
            )

            appendLine(
                "Do not wrap the JSON in code fences."
            )

            appendLine(
                "Do not write anything before or after the JSON."
            )
        }
    }
}