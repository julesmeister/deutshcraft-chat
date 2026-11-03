package com.julesmeister.deutschcraft.feature.chat.presentation.progress

import com.julesmeister.deutschcraft.core.cefr.model.CefrLevel
import com.julesmeister.deutschcraft.feature.chat.presentation.theme.ChatPresentationTokens
import com.julesmeister.deutschcraft.feature.mission.domain.recommendation.MissionLearningContext
import com.julesmeister.deutschcraft.feature.mission.domain.usecase.BuildMissionLearningContextUseCase
import javax.inject.Inject

class SelectImmediateLevelNeedUseCase
@Inject constructor(
    private val buildMissionLearningContext:
    BuildMissionLearningContextUseCase
) {

    suspend operator fun invoke(
        currentLevel: CefrLevel
    ): ChatImmediateLevelNeedUiModel? {
        val context =
            buildMissionLearningContext(
                cefrLevel =
                    currentLevel
            )

        return selectProgressionNeed(
            context =
                context,

            currentLevel =
                currentLevel
        )
            ?: selectDevelopingNeed(
                context =
                    context,

                currentLevel =
                    currentLevel
            )
            ?: selectReinforcementNeed(
                context =
                    context,

                currentLevel =
                    currentLevel
            )
            ?: fallbackNeed(
                currentLevel =
                    currentLevel
            )
    }

    private fun selectProgressionNeed(
        context: MissionLearningContext,
        currentLevel: CefrLevel
    ): ChatImmediateLevelNeedUiModel? {
        val targetLevel =
            currentLevel.nextLevel()

        val parsedSignals:
                List<CapabilitySignal> =
            context
                .progressionReadyCapabilities
                .mapNotNull { rawSignal: String ->
                    parseCapabilitySignal(
                        rawSignal =
                            rawSignal
                    )
                }

        val selected =
            parsedSignals
                .firstOrNull { signal: CapabilitySignal ->
                    signal.cefrLevel.equals(
                        other =
                            targetLevel.name,

                        ignoreCase =
                            true
                    ) &&
                            signal.prefix ==
                            ChatPresentationTokens.SelectImmediateLevelNeedUseCase.NextStepPrefix
                }
                ?: parsedSignals
                    .firstOrNull { signal: CapabilitySignal ->
                        signal.cefrLevel.equals(
                            other =
                                targetLevel.name,

                            ignoreCase =
                                true
                        )
                    }
                ?: parsedSignals.firstOrNull()

        return selected?.toUiModel(
            source =
                ImmediateNeedSource.PROGRESSION
        )
    }

    private fun selectDevelopingNeed(
        context: MissionLearningContext,
        currentLevel: CefrLevel
    ): ChatImmediateLevelNeedUiModel? {
        val targetLevel =
            currentLevel.nextLevel()

        val parsedSignals:
                List<CapabilitySignal> =
            context
                .developingCapabilities
                .mapNotNull { rawSignal: String ->
                    parseCapabilitySignal(
                        rawSignal =
                            rawSignal
                    )
                }

        val selected =
            parsedSignals
                .firstOrNull { signal: CapabilitySignal ->
                    signal.cefrLevel.equals(
                        other =
                            targetLevel.name,

                        ignoreCase =
                            true
                    )
                }
                ?: parsedSignals.firstOrNull()

        return selected?.toUiModel(
            source =
                ImmediateNeedSource.DEVELOPING
        )
    }

    private fun selectReinforcementNeed(
        context: MissionLearningContext,
        currentLevel: CefrLevel
    ): ChatImmediateLevelNeedUiModel? {
        val rawSignal =
            context
                .reinforcementFocuses
                .firstOrNull()
                ?: return null

        val signal =
            parseReinforcementSignal(
                rawSignal =
                    rawSignal
            )
                ?: return null

        val inferredCapabilityId =
            capabilityIdForFocus(
                focusKey =
                    signal.focusKey,

                title =
                    signal.title,

                currentLevel =
                    currentLevel
            )

        val support =
            BuiltInCapabilityTeachingSupport
                .requireOrFallback(
                    capabilityId =
                        inferredCapabilityId,

                    capabilityName =
                        signal.title,

                    capabilityDescription =
                        signal.instruction
                )

        return ChatImmediateLevelNeedUiModel(
            key =
                "reinforcement:${signal.focusKey}",

            capabilityId =
                inferredCapabilityId,

            title =
                support.learnerTitle,

            explanation =
                support.explanation,

            whyNow =
                signal.instruction.ifBlank {
                    "DeutschCraft has repeatedly observed this pattern " +
                            "in your recent conversations."
                },

            grammarTargets =
                support.grammarTargets,

            vocabularyTargets =
                support.vocabularyTargets,

            examplePhrase =
                support.examplePhrase,

            masteryPercentage =
                null,

            statusLabel =
                "Needs reinforcement"
        )
    }

    private fun CapabilitySignal.toUiModel(
        source: ImmediateNeedSource
    ): ChatImmediateLevelNeedUiModel {
        val support =
            BuiltInCapabilityTeachingSupport
                .requireOrFallback(
                    capabilityId =
                        capabilityId,

                    capabilityName =
                        name,

                    capabilityDescription =
                        name
                )

        return ChatImmediateLevelNeedUiModel(
            key =
                "$prefix:$capabilityId",

            capabilityId =
                capabilityId,

            title =
                support.learnerTitle,

            explanation =
                support.explanation,

            whyNow =
                when (source) {
                    ImmediateNeedSource.PROGRESSION -> {
                        if (
                            prefix ==
                            ChatPresentationTokens.SelectImmediateLevelNeedUseCase.NextStepPrefix
                        ) {
                            "Your prerequisite skill is established, " +
                                    "so this is the most useful next step " +
                                    "toward $cefrLevel."
                        } else {
                            "Your prerequisite skills are strong enough " +
                                    "to begin developing this $cefrLevel requirement."
                        }
                    }

                    ImmediateNeedSource.DEVELOPING -> {
                        "You have already practised this requirement, " +
                                "but it is not yet reliable enough for progression."
                    }
                },

            grammarTargets =
                support.grammarTargets,

            vocabularyTargets =
                support.vocabularyTargets,

            examplePhrase =
                support.examplePhrase,

            masteryPercentage =
                score,

            statusLabel =
                when (source) {
                    ImmediateNeedSource.PROGRESSION ->
                        "Ready to develop"

                    ImmediateNeedSource.DEVELOPING ->
                        when (score) {
                            in 0..39 ->
                                "Needs focused practice"

                            in 40..64 ->
                                "Developing"

                            else ->
                                "Almost reliable"
                        }
                }
        )
    }

    private fun fallbackNeed(
        currentLevel: CefrLevel
    ): ChatImmediateLevelNeedUiModel {
        val capabilityId =
            when (currentLevel) {
                CefrLevel.A1 ->
                    "a2-give-simple-reason"

                CefrLevel.A2 ->
                    "b1-develop-opinion"

                CefrLevel.B1 ->
                    "b2-qualify-opinion"

                CefrLevel.B2 ->
                    "c1-develop-nuanced-argument"

                CefrLevel.C1 ->
                    "c2-fine-shades-meaning"

                CefrLevel.C2 ->
                    "c2-consolidate-discourse-control"
            }

        val support =
            BuiltInCapabilityTeachingSupport
                .requireOrFallback(
                    capabilityId =
                        capabilityId,

                    capabilityName =
                        fallbackCapabilityName(
                            capabilityId =
                                capabilityId
                        ),

                    capabilityDescription =
                        "Practise this communication requirement " +
                                "in natural conversation."
                )

        return ChatImmediateLevelNeedUiModel(
            key =
                "fallback:$capabilityId",

            capabilityId =
                capabilityId,

            title =
                support.learnerTitle,

            explanation =
                support.explanation,

            whyNow =
                if (
                    currentLevel ==
                    CefrLevel.C2
                ) {
                    "You are already at the highest CEFR level. " +
                            "This helps maintain advanced flexibility and precision."
                } else {
                    "DeutschCraft needs more personal evidence, so this " +
                            "is the most relevant general next-level requirement."
                },

            grammarTargets =
                support.grammarTargets,

            vocabularyTargets =
                support.vocabularyTargets,

            examplePhrase =
                support.examplePhrase,

            masteryPercentage =
                null,

            statusLabel =
                if (
                    currentLevel ==
                    CefrLevel.C2
                ) {
                    "Maintain"
                } else {
                    "Build evidence"
                }
        )
    }

    private fun parseCapabilitySignal(
        rawSignal: String
    ): CapabilitySignal? {
        /*
         * Expected format:
         *
         * next_step:b2-qualify-opinion
         * | Qualify an opinion
         * | category=pragmatics
         * | cefr=B2
         * | score=82%
         */
        val parts:
                List<String> =
            rawSignal
                .split(
                    "|"
                )
                .map { part: String ->
                    part.trim()
                }

        if (
            parts.size <
            ChatPresentationTokens.SelectImmediateLevelNeedUseCase.MinimumCapabilitySignalPartCount
        ) {
            return null
        }

        val identifierPart:
                String =
            parts[0]

        val identifierSeparatorIndex =
            identifierPart.indexOf(
                ':'
            )

        if (
            identifierSeparatorIndex <=
            0 ||
            identifierSeparatorIndex >=
            identifierPart.lastIndex
        ) {
            return null
        }

        val prefix =
            identifierPart
                .substring(
                    startIndex =
                        0,

                    endIndex =
                        identifierSeparatorIndex
                )
                .trim()

        val capabilityId =
            identifierPart
                .substring(
                    startIndex =
                        identifierSeparatorIndex +
                                1
                )
                .trim()

        val name =
            parts[1]
                .trim()

        if (
            prefix.isBlank() ||
            capabilityId.isBlank() ||
            name.isBlank()
        ) {
            return null
        }

        var cefrLevel =
            ""

        var score =
            0

        parts
            .drop(
                ChatPresentationTokens.SelectImmediateLevelNeedUseCase.MinimumCapabilitySignalPartCount
            )
            .forEach { part: String ->
                when {
                    part.startsWith(
                        prefix =
                            ChatPresentationTokens.SelectImmediateLevelNeedUseCase.CefrFieldPrefix,

                        ignoreCase =
                            true
                    ) -> {
                        cefrLevel =
                            part
                                .substringAfter(
                                    delimiter =
                                        '='
                                )
                                .trim()
                    }

                    part.startsWith(
                        prefix =
                            ChatPresentationTokens.SelectImmediateLevelNeedUseCase.ScoreFieldPrefix,

                        ignoreCase =
                            true
                    ) -> {
                        score =
                            part
                                .substringAfter(
                                    delimiter =
                                        '='
                                )
                                .removeSuffix(
                                    "%"
                                )
                                .trim()
                                .toIntOrNull()
                                ?.coerceIn(
                                    minimumValue =
                                        0,

                                    maximumValue =
                                        100
                                )
                                ?: 0
                    }
                }
            }

        return CapabilitySignal(
            prefix =
                prefix,

            capabilityId =
                capabilityId,

            name =
                name,

            cefrLevel =
                cefrLevel,

            score =
                score
        )
    }

    private fun parseReinforcementSignal(
        rawSignal: String
    ): ReinforcementSignal? {
        /*
         * Expected format:
         *
         * communication:giving_reasons
         * | Giving reasons
         * | Ask questions that encourage ...
         *
         * Do not use String.split with both a named delimiter
         * and a named limit. Using substring boundaries here is
         * also safer because the instruction itself may contain
         * arbitrary punctuation.
         */
        val firstSeparatorIndex =
            rawSignal.indexOf(
                ChatPresentationTokens.SelectImmediateLevelNeedUseCase.ReinforcementSeparator
            )

        if (
            firstSeparatorIndex <
            0
        ) {
            return null
        }

        val secondSeparatorIndex =
            rawSignal.indexOf(
                ChatPresentationTokens.SelectImmediateLevelNeedUseCase.ReinforcementSeparator,

                startIndex =
                    firstSeparatorIndex +
                            ChatPresentationTokens.SelectImmediateLevelNeedUseCase.ReinforcementSeparator.length
            )

        val focusKey =
            rawSignal
                .substring(
                    startIndex =
                        0,

                    endIndex =
                        firstSeparatorIndex
                )
                .trim()

        val title =
            if (
                secondSeparatorIndex >=
                0
            ) {
                rawSignal
                    .substring(
                        startIndex =
                            firstSeparatorIndex +
                                    ChatPresentationTokens.SelectImmediateLevelNeedUseCase.ReinforcementSeparator.length,

                        endIndex =
                            secondSeparatorIndex
                    )
                    .trim()
            } else {
                rawSignal
                    .substring(
                        startIndex =
                            firstSeparatorIndex +
                                    ChatPresentationTokens.SelectImmediateLevelNeedUseCase.ReinforcementSeparator.length
                    )
                    .trim()
            }

        val instruction =
            if (
                secondSeparatorIndex >=
                0
            ) {
                rawSignal
                    .substring(
                        startIndex =
                            secondSeparatorIndex +
                                    ChatPresentationTokens.SelectImmediateLevelNeedUseCase.ReinforcementSeparator.length
                    )
                    .trim()
            } else {
                ""
            }

        if (
            focusKey.isBlank() ||
            title.isBlank()
        ) {
            return null
        }

        return ReinforcementSignal(
            focusKey =
                focusKey,

            title =
                title,

            instruction =
                instruction
        )
    }

    private fun capabilityIdForFocus(
        focusKey: String,
        title: String,
        currentLevel: CefrLevel
    ): String {
        val normalized =
            "$focusKey $title"
                .lowercase()

        return when {
            normalized.contains(
                "giving_reasons"
            ) ||
                    normalized.contains(
                        "giving reasons"
                    ) ||
                    normalized.contains(
                        "give reason"
                    ) -> {
                when (currentLevel) {
                    CefrLevel.A1 ->
                        "a2-give-simple-reason"

                    CefrLevel.A2 ->
                        "b1-develop-opinion"

                    CefrLevel.B1 ->
                        "b2-multi-step-argument"

                    CefrLevel.B2 ->
                        "c1-develop-nuanced-argument"

                    CefrLevel.C1,
                    CefrLevel.C2 ->
                        "c2-adapt-rhetorical-strategy"
                }
            }

            normalized.contains(
                "word_order"
            ) ||
                    normalized.contains(
                        "word order"
                    ) ||
                    normalized.contains(
                        "subordinate"
                    ) ||
                    normalized.contains(
                        "nebensatz"
                    ) -> {
                when (currentLevel) {
                    CefrLevel.A1,
                    CefrLevel.A2 ->
                        "a2-give-simple-reason"

                    CefrLevel.B1 ->
                        "b2-qualify-opinion"

                    CefrLevel.B2 ->
                        "c1-develop-nuanced-argument"

                    CefrLevel.C1,
                    CefrLevel.C2 ->
                        "c2-control-style"
                }
            }

            normalized.contains(
                "comparison"
            ) ||
                    normalized.contains(
                        "compare"
                    ) ||
                    normalized.contains(
                        "advantages"
                    ) ||
                    normalized.contains(
                        "disadvantages"
                    ) -> {
                when (currentLevel) {
                    CefrLevel.A1 ->
                        "a2-make-comparison"

                    CefrLevel.A2 ->
                        "b1-discuss-advantages-disadvantages"

                    CefrLevel.B1 ->
                        "b2-evaluate-alternatives"

                    CefrLevel.B2 ->
                        "c1-fine-grained-evaluation"

                    CefrLevel.C1,
                    CefrLevel.C2 ->
                        "c2-fine-shades-meaning"
                }
            }

            normalized.contains(
                "uncertainty"
            ) ||
                    normalized.contains(
                        "probability"
                    ) ||
                    normalized.contains(
                        "hedge"
                    ) -> {
                when (currentLevel) {
                    CefrLevel.A1,
                    CefrLevel.A2 ->
                        "b1-express-uncertainty"

                    CefrLevel.B1 ->
                        "b2-hedge-claim"

                    CefrLevel.B2 ->
                        "c1-express-reservation"

                    CefrLevel.C1,
                    CefrLevel.C2 ->
                        "c2-fine-shades-meaning"
                }
            }

            normalized.contains(
                "disagreement"
            ) ||
                    normalized.contains(
                        "counterargument"
                    ) -> {
                when (currentLevel) {
                    CefrLevel.A1,
                    CefrLevel.A2 ->
                        "b1-handle-disagreement"

                    CefrLevel.B1 ->
                        "b2-acknowledge-counterargument"

                    CefrLevel.B2 ->
                        "c1-subtle-agreement-disagreement"

                    CefrLevel.C1,
                    CefrLevel.C2 ->
                        "c2-sophisticated-disagreement"
                }
            }

            normalized.contains(
                "past"
            ) ||
                    normalized.contains(
                        "narrat"
                    ) ||
                    normalized.contains(
                        "sequence"
                    ) -> {
                when (currentLevel) {
                    CefrLevel.A1 ->
                        "a2-sequence-events"

                    CefrLevel.A2,
                    CefrLevel.B1 ->
                        "b1-narrate-connected-events"

                    CefrLevel.B2,
                    CefrLevel.C1,
                    CefrLevel.C2 ->
                        "c1-reframe-argument"
                }
            }

            normalized.contains(
                "opinion"
            ) -> {
                when (currentLevel) {
                    CefrLevel.A1 ->
                        "a2-explain-preference"

                    CefrLevel.A2 ->
                        "b1-develop-opinion"

                    CefrLevel.B1 ->
                        "b2-qualify-opinion"

                    CefrLevel.B2 ->
                        "c1-develop-nuanced-argument"

                    CefrLevel.C1,
                    CefrLevel.C2 ->
                        "c2-adapt-rhetorical-strategy"
                }
            }

            normalized.contains(
                "vocabulary"
            ) ||
                    normalized.contains(
                        "wortschatz"
                    ) -> {
                when (currentLevel) {
                    CefrLevel.A1 ->
                        "a2-add-supporting-detail"

                    CefrLevel.A2 ->
                        "b1-develop-opinion"

                    CefrLevel.B1 ->
                        "b2-support-claim-evidence"

                    CefrLevel.B2 ->
                        "c1-develop-nuanced-argument"

                    CefrLevel.C1,
                    CefrLevel.C2 ->
                        "c2-idiomatic-nuance"
                }
            }

            else -> {
                when (currentLevel) {
                    CefrLevel.A1 ->
                        "a2-give-simple-reason"

                    CefrLevel.A2 ->
                        "b1-develop-opinion"

                    CefrLevel.B1 ->
                        "b2-qualify-opinion"

                    CefrLevel.B2 ->
                        "c1-develop-nuanced-argument"

                    CefrLevel.C1 ->
                        "c2-fine-shades-meaning"

                    CefrLevel.C2 ->
                        "c2-consolidate-discourse-control"
                }
            }
        }
    }

    private fun fallbackCapabilityName(
        capabilityId: String
    ): String {
        val displayValue =
            capabilityId
                .substringAfter(
                    delimiter =
                        "-"
                )
                .replace(
                    oldChar =
                        '-',

                    newChar =
                        ' '
                )

        if (
            displayValue.isBlank()
        ) {
            return "Next communication skill"
        }

        return displayValue
            .replaceFirstChar { character: Char ->
                character.uppercaseChar()
            }
    }

    private fun CefrLevel.nextLevel():
            CefrLevel {
        return when (this) {
            CefrLevel.A1 ->
                CefrLevel.A2

            CefrLevel.A2 ->
                CefrLevel.B1

            CefrLevel.B1 ->
                CefrLevel.B2

            CefrLevel.B2 ->
                CefrLevel.C1

            CefrLevel.C1 ->
                CefrLevel.C2

            CefrLevel.C2 ->
                CefrLevel.C2
        }
    }

    private data class CapabilitySignal(
        val prefix: String,
        val capabilityId: String,
        val name: String,
        val cefrLevel: String,
        val score: Int
    )

    private data class ReinforcementSignal(
        val focusKey: String,
        val title: String,
        val instruction: String
    )

    private enum class ImmediateNeedSource {
        PROGRESSION,
        DEVELOPING
    }

}
