package com.julesmeister.deutschcraft.feature.chat.data.factory

import com.julesmeister.deutschcraft.core.cefr.model.CefrLevel
import com.julesmeister.deutschcraft.feature.chat.domain.ReplySuggestion
import java.util.UUID
import javax.inject.Inject

class ReplySuggestionFactory @Inject constructor() {

    fun create(
        cefrLevel: CefrLevel
    ): List<ReplySuggestion> {
        return suggestionTexts(
            cefrLevel = cefrLevel
        ).map { text ->
            ReplySuggestion(
                id = UUID.randomUUID().toString(),
                text = text
            )
        }
    }

    private fun suggestionTexts(
        cefrLevel: CefrLevel
    ): List<String> {
        return when (cefrLevel) {
            CefrLevel.A1 -> {
                listOf(
                    "Ja, gerne.",
                    "Warum?",
                    "Ich weiß nicht."
                )
            }

            CefrLevel.A2 -> {
                listOf(
                    "Das klingt interessant.",
                    "Kannst du mehr erzählen?",
                    "Ich denke schon."
                )
            }

            CefrLevel.B1 -> {
                listOf(
                    "Das hängt von der Situation ab.",
                    "Ich sehe das etwas anders.",
                    "Was meinst du genau?"
                )
            }

            CefrLevel.B2,
            CefrLevel.C1,
            CefrLevel.C2 -> {
                listOf(
                    "Darüber habe ich noch nicht nachgedacht.",
                    "Welche Gründe sprechen dafür?",
                    "Ich würde das differenzierter betrachten."
                )
            }
        }
    }
}