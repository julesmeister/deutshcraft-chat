package com.julesmeister.deutschcraft.feature.adaptivelearning.data.ai

import android.util.Log
import com.julesmeister.deutschcraft.BuildConfig
import com.julesmeister.deutschcraft.core.cefr.model.CefrLevel
import com.julesmeister.deutschcraft.feature.adaptivelearning.domain.model.AdaptiveConversationSpeaker
import com.julesmeister.deutschcraft.feature.adaptivelearning.domain.model.AdaptiveConversationTurn
import com.julesmeister.deutschcraft.feature.adaptivelearning.domain.model.CefrCommunicationProfiles
import com.julesmeister.deutschcraft.feature.adaptivelearning.domain.model.CommunicationEvidence
import com.julesmeister.deutschcraft.feature.adaptivelearning.domain.model.CommunicationEvidenceBatch
import com.julesmeister.deutschcraft.feature.adaptivelearning.domain.model.CommunicationSkill
import com.julesmeister.deutschcraft.feature.adaptivelearning.domain.model.LearningEvidenceResult
import com.julesmeister.deutschcraft.feature.chat.data.ai.QwenApi
import com.julesmeister.deutschcraft.feature.chat.data.ai.QwenChatCompletionRequest
import com.julesmeister.deutschcraft.feature.chat.data.ai.QwenMessage
import com.julesmeister.deutschcraft.feature.chat.data.ai.QwenResponseFormat
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.floatOrNull
import retrofit2.HttpException

@Singleton
class QwenCommunicationEvidenceAiGateway
@Inject constructor(
    private val qwenApi: QwenApi,
    private val json: Json
) : CommunicationEvidenceAiGateway {

    override suspend fun evaluate(
        learnerMessage: String,
        cefrLevel: CefrLevel,
        recentConversation:
        List<AdaptiveConversationTurn>,
        expectedSkills:
        Set<CommunicationSkill>
    ): CommunicationEvidenceBatch {
        val safeLearnerMessage =
            learnerMessage.trim()

        if (
            safeLearnerMessage.isBlank() ||
            expectedSkills.isEmpty()
        ) {
            return CommunicationEvidenceBatch()
        }

        if (BuildConfig.QWEN_API_KEY.isBlank()) {
            Log.w(
                GATEWAY_TAG,
                "Communication evaluation skipped because " +
                        "the Qwen API key is missing."
            )

            return CommunicationEvidenceBatch()
        }

        return try {
            evaluateWithRepair(
                learnerMessage =
                    safeLearnerMessage,
                cefrLevel =
                    cefrLevel,
                recentConversation =
                    recentConversation,
                expectedSkills =
                    expectedSkills
            )
        } catch (
            cancellationException:
            CancellationException
        ) {
            throw cancellationException
        } catch (
            httpException:
            HttpException
        ) {
            Log.e(
                GATEWAY_TAG,
                "Communication evaluation HTTP request failed.",
                httpException
            )

            CommunicationEvidenceBatch()
        } catch (
            ioException:
            IOException
        ) {
            Log.e(
                GATEWAY_TAG,
                "Communication evaluation could not reach Qwen.",
                ioException
            )

            CommunicationEvidenceBatch()
        } catch (
            throwable:
            Throwable
        ) {
            /*
             * Adaptive communication evaluation is
             * supplementary. Its failure must never interrupt
             * normal conversation.
             */
            Log.e(
                GATEWAY_TAG,
                "Communication evaluation failed.",
                throwable
            )

            CommunicationEvidenceBatch()
        }
    }

    private suspend fun evaluateWithRepair(
        learnerMessage: String,
        cefrLevel: CefrLevel,
        recentConversation:
        List<AdaptiveConversationTurn>,
        expectedSkills:
        Set<CommunicationSkill>
    ): CommunicationEvidenceBatch {
        val initialContent =
            requestCompletion(
                messages =
                    buildEvaluationMessages(
                        learnerMessage =
                            learnerMessage,
                        cefrLevel =
                            cefrLevel,
                        recentConversation =
                            recentConversation,
                        expectedSkills =
                            expectedSkills
                    ),
                temperature =
                    EVALUATION_TEMPERATURE
            )

        val initialResult =
            decodeAndValidate(
                responseContent =
                    initialContent,
                learnerMessage =
                    learnerMessage,
                cefrLevel =
                    cefrLevel,
                allowedSkills =
                    expectedSkills
            )

        if (initialResult != null) {
            return initialResult
        }

        Log.w(
            GATEWAY_TAG,
            "Qwen returned invalid communication evidence. " +
                    "Retrying once."
        )

        val repairedContent =
            requestCompletion(
                messages =
                    buildRepairMessages(
                        malformedResponse =
                            initialContent,
                        learnerMessage =
                            learnerMessage,
                        cefrLevel =
                            cefrLevel,
                        expectedSkills =
                            expectedSkills
                    ),
                temperature =
                    REPAIR_TEMPERATURE
            )

        return decodeAndValidate(
            responseContent =
                repairedContent,
            learnerMessage =
                learnerMessage,
            cefrLevel =
                cefrLevel,
            allowedSkills =
                expectedSkills
        ) ?: CommunicationEvidenceBatch()
    }

    private suspend fun requestCompletion(
        messages: List<QwenMessage>,
        temperature: Double
    ): String {
        val request =
            QwenChatCompletionRequest(
                model =
                    BuildConfig.QWEN_MODEL,
                messages =
                    messages,
                enableThinking =
                    false,
                temperature =
                    temperature,
                maxTokens =
                    MAXIMUM_RESPONSE_TOKENS,
                responseFormat =
                    QwenResponseFormat(
                        type =
                            JSON_OBJECT_TYPE
                    )
            )

        val response =
            qwenApi.createChatCompletion(
                request =
                    request
            )

        return response
            .choices
            .firstOrNull()
            ?.message
            ?.content
            ?.trim()
            .orEmpty()
            .takeIf(
                String::isNotBlank
            )
            ?: throw IllegalStateException(
                "Qwen returned empty communication evidence."
            )
    }

    private fun buildEvaluationMessages(
        learnerMessage: String,
        cefrLevel: CefrLevel,
        recentConversation:
        List<AdaptiveConversationTurn>,
        expectedSkills:
        Set<CommunicationSkill>
    ): List<QwenMessage> {
        val profile =
            CefrCommunicationProfiles.forLevel(
                level =
                    cefrLevel
            )

        val skillsText =
            expectedSkills
                .sortedBy { skill ->
                    skill.name
                }
                .joinToString(
                    separator =
                        "\n"
                ) { skill ->
                    buildString {
                        append("- ")
                        append(skill.name)
                        append(": ")
                        append(skill.displayName)
                        append(". ")
                        append(skill.learnerGuidance)
                    }
                }

        val conversationText =
            formatConversation(
                turns =
                    recentConversation
            )

        return listOf(
            QwenMessage(
                role =
                    SYSTEM_ROLE,
                content =
                    createEvaluationPrompt(
                        cefrLevel =
                            cefrLevel,
                        expectedSkillsText =
                            skillsText,
                        coachingInstruction =
                            profile.coachingInstruction
                    )
            ),
            QwenMessage(
                role =
                    USER_ROLE,
                content =
                    """
                    Recent conversation:

                    $conversationText

                    Latest learner message:

                    ${learnerMessage.take(MAXIMUM_MESSAGE_LENGTH)}

                    Evaluate only communication skills that were
                    genuinely relevant to this conversational
                    opportunity.

                    Return JSON only.
                    """.trimIndent()
            )
        )
    }

    private fun createEvaluationPrompt(
        cefrLevel: CefrLevel,
        expectedSkillsText: String,
        coachingInstruction: String
    ): String {
        return """
            You evaluate CEFR communication behavior for
            DeutschCraft, a conversation-first German learning
            application.

            The learner's current CEFR level is:
            ${cefrLevel.name}

            CEFR coaching profile:

            $coachingInstruction

            Skills that may be evaluated:

            $expectedSkillsText

            Return exactly one JSON object:

            {
              "evidence": [
                {
                  "skill": "EXACT_ENUM_NAME",
                  "result": "STRUGGLED",
                  "confidence": 0.0,
                  "reason": "Short internal explanation",
                  "reinforcementInstruction": "Instruction for Emil",
                  "learnerGuidance": "Short subtle learner feedback",
                  "levelAppropriateText": "Optional German version"
                }
              ]
            }

            Allowed result values:

            - STRUGGLED
            - PARTIAL_SUCCESS
            - SUCCESS
            - NOT_OBSERVED

            Critical evaluation rules:

            - Evaluate communication, not only grammar.
            - Evaluate the latest learner message in context.
            - Assistant turns provide context but cannot
              demonstrate learner ability.
            - Do not evaluate every available skill.
            - Return at most three evidence entries.
            - Include a skill only when the conversation
              genuinely gave the learner an opportunity to
              demonstrate it.
            - Do not mark a skill STRUGGLED merely because the
              learner did not use it.
            - STRUGGLED means the conversational opportunity
              clearly required or invited the behavior and the
              learner did not demonstrate it sufficiently.
            - PARTIAL_SUCCESS means the learner attempted the
              behavior but did not fully develop it.
            - SUCCESS means the learner clearly demonstrated
              the behavior.
            - NOT_OBSERVED should normally be omitted from the
              response.
            - A grammatically correct message may still provide
              communication evidence.
            - Do not penalize a naturally short answer when the
              question only required a short answer.
            - Do not demand storytelling, comparison, or
              argumentation when the conversation did not invite
              those behaviors.
            - Minor grammar mistakes must not erase successful
              communication evidence when meaning is clear.
            - Evaluate according to CEFR ${cefrLevel.name}, not
              native-speaker expectations.
            - learnerGuidance must be brief and supportive.
            - learnerGuidance must not mention AI, scoring,
              databases, or internal evaluation.
            - reinforcementInstruction is internal guidance for
              Emil.
            - levelAppropriateText must be German.
            - levelAppropriateText must preserve the learner's
              meaning.
            - Do not invent personal facts.
            - Omit levelAppropriateText when a safe alternative
              cannot be produced.
            - Confidence must be between 0 and 1.
            - Use only exact skill enum names supplied above.
            - Return JSON only.
            - Do not use Markdown.
        """.trimIndent()
    }

    private fun buildRepairMessages(
        malformedResponse: String,
        learnerMessage: String,
        cefrLevel: CefrLevel,
        expectedSkills:
        Set<CommunicationSkill>
    ): List<QwenMessage> {
        val allowedSkillNames =
            expectedSkills
                .sortedBy { skill ->
                    skill.name
                }
                .joinToString(
                    separator =
                        ", "
                ) { skill ->
                    skill.name
                }

        return listOf(
            QwenMessage(
                role =
                    SYSTEM_ROLE,
                content =
                    """
                    Repair malformed DeutschCraft communication
                    evidence JSON.

                    Return exactly:

                    {
                      "evidence": [
                        {
                          "skill": "EXACT_ENUM_NAME",
                          "result": "SUCCESS",
                          "confidence": 0.0,
                          "reason": "Short explanation",
                          "reinforcementInstruction": "Instruction for Emil",
                          "learnerGuidance": "Brief learner guidance",
                          "levelAppropriateText": "Optional German version"
                        }
                      ]
                    }

                    Learner CEFR:
                    ${cefrLevel.name}

                    Allowed skills:
                    $allowedSkillNames

                    Allowed results:
                    STRUGGLED
                    PARTIAL_SUCCESS
                    SUCCESS
                    NOT_OBSERVED

                    Requirements:

                    - Return at most three evidence entries.
                    - Keep only contextually relevant skills.
                    - Do not mark missing irrelevant skills as
                      struggles.
                    - Use exact enum names.
                    - Confidence must be between 0 and 1.
                    - levelAppropriateText must preserve meaning.
                    - Do not invent personal information.
                    - Return JSON only.
                    - Do not use Markdown.
                    """.trimIndent()
            ),
            QwenMessage(
                role =
                    USER_ROLE,
                content =
                    """
                    Learner message:

                    ${learnerMessage.take(MAXIMUM_MESSAGE_LENGTH)}

                    Repair this response:

                    ${
                        malformedResponse.take(
                            MAXIMUM_REPAIR_INPUT_LENGTH
                        )
                    }
                    """.trimIndent()
            )
        )
    }

    private fun decodeAndValidate(
        responseContent: String,
        learnerMessage: String,
        cefrLevel: CefrLevel,
        allowedSkills:
        Set<CommunicationSkill>
    ): CommunicationEvidenceBatch? {
        val root =
            decodeRootObject(
                responseContent =
                    responseContent
            )
                ?: return null

        val evidenceArray =
            root[EVIDENCE_KEY] as? JsonArray
                ?: return null

        val evidence =
            evidenceArray
                .mapNotNull { element ->
                    parseEvidence(
                        element =
                            element,
                        learnerMessage =
                            learnerMessage,
                        cefrLevel =
                            cefrLevel,
                        allowedSkills =
                            allowedSkills
                    )
                }
                .filter { item ->
                    item.result !=
                            LearningEvidenceResult.NOT_OBSERVED
                }
                .groupBy { item ->
                    item.focusKey
                }
                .mapNotNull { (_, matchingEvidence) ->
                    matchingEvidence.maxByOrNull { item ->
                        item.normalizedConfidence
                    }
                }
                .sortedByDescending { item ->
                    item.normalizedConfidence
                }
                .take(
                    MAXIMUM_EVIDENCE_COUNT
                )

        /*
         * An empty evidence array is valid. It means the
         * message did not provide meaningful communication
         * evidence.
         */
        return CommunicationEvidenceBatch(
            evidence =
                evidence
        )
    }

    private fun parseEvidence(
        element: JsonElement,
        learnerMessage: String,
        cefrLevel: CefrLevel,
        allowedSkills:
        Set<CommunicationSkill>
    ): CommunicationEvidence? {
        val evidenceObject =
            element as? JsonObject
                ?: return null

        val skill =
            parseEnumOrNull<CommunicationSkill>(
                value =
                    evidenceObject.stringValue(
                        key =
                            SKILL_KEY
                    )
                        ?: return null
            )
                ?: return null

        if (skill !in allowedSkills) {
            return null
        }

        val result =
            parseEnumOrNull<LearningEvidenceResult>(
                value =
                    evidenceObject.stringValue(
                        key =
                            RESULT_KEY
                    )
                        ?: return null
            )
                ?: return null

        val confidence =
            evidenceObject.floatValue(
                key =
                    CONFIDENCE_KEY
            )
                ?.coerceIn(
                    minimumValue =
                        0f,
                    maximumValue =
                        1f
                )
                ?: return null

        if (
            confidence <
            MINIMUM_ACCEPTED_CONFIDENCE
        ) {
            return null
        }

        val reason =
            evidenceObject.stringValue(
                key =
                    REASON_KEY
            )
                ?: return null

        val reinforcementInstruction =
            evidenceObject.stringValue(
                key =
                    REINFORCEMENT_INSTRUCTION_KEY
            )
                ?: CommunicationEvidence
                    .defaultReinforcementInstruction(
                        skill =
                            skill
                    )

        val learnerGuidance =
            evidenceObject.stringValue(
                key =
                    LEARNER_GUIDANCE_KEY
            )
                ?: skill.learnerGuidance

        val levelAppropriateText =
            evidenceObject.stringValue(
                key =
                    LEVEL_APPROPRIATE_TEXT_KEY
            )

        return CommunicationEvidence(
            skill =
                skill,
            targetCefrLevel =
                cefrLevel,
            result =
                result,
            confidence =
                confidence,
            reason =
                reason,
            learnerText =
                learnerMessage,
            reinforcementInstruction =
                reinforcementInstruction,
            learnerGuidance =
                learnerGuidance,
            levelAppropriateText =
                levelAppropriateText
        )
    }

    private fun decodeRootObject(
        responseContent: String
    ): JsonObject? {
        val cleanedContent =
            removeMarkdownCodeFence(
                content =
                    responseContent
            )

        return try {
            json.parseToJsonElement(
                string =
                    cleanedContent
            ) as? JsonObject
        } catch (
            serializationException:
            SerializationException
        ) {
            Log.w(
                GATEWAY_TAG,
                "Qwen returned invalid communication " +
                        "evidence JSON.",
                serializationException
            )

            null
        } catch (
            illegalArgumentException:
            IllegalArgumentException
        ) {
            Log.w(
                GATEWAY_TAG,
                "Qwen returned an unsupported communication " +
                        "evidence payload.",
                illegalArgumentException
            )

            null
        }
    }

    private fun formatConversation(
        turns: List<AdaptiveConversationTurn>
    ): String {
        return turns
            .asSequence()
            .filter { turn ->
                turn.isUsable
            }
            .takeLastAsList(
                maximumCount =
                    MAXIMUM_RECENT_TURN_COUNT
            )
            .joinToString(
                separator =
                    "\n"
            ) { turn ->
                val speaker =
                    when (turn.speaker) {
                        AdaptiveConversationSpeaker.LEARNER -> {
                            "Learner"
                        }

                        AdaptiveConversationSpeaker.ASSISTANT -> {
                            "Emil"
                        }
                    }

                "$speaker: ${
                    turn
                        .sanitizedContent
                        .take(
                            MAXIMUM_TURN_LENGTH
                        )
                }"
            }
            .ifBlank {
                "No earlier conversation context is available."
            }
    }

    private fun JsonObject.stringValue(
        key: String
    ): String? {
        return (
                this[key] as? JsonPrimitive
                )
            ?.contentOrNull
            ?.sanitizeGeneratedText()
    }

    private fun JsonObject.floatValue(
        key: String
    ): Float? {
        return (
                this[key] as? JsonPrimitive
                )?.floatOrNull
    }

    private fun String.sanitizeGeneratedText():
            String? {
        return trim()
            .replace(
                MULTIPLE_WHITESPACE_REGEX,
                " "
            )
            .takeIf(
                String::isNotBlank
            )
    }

    private inline fun <reified T : Enum<T>>
            parseEnumOrNull(
        value: String
    ): T? {
        val normalizedValue =
            value
                .trim()
                .uppercase()

        return enumValues<T>()
            .firstOrNull { enumValue ->
                enumValue.name ==
                        normalizedValue
            }
    }

    private fun removeMarkdownCodeFence(
        content: String
    ): String {
        return content
            .trim()
            .removePrefix(
                "```json"
            )
            .removePrefix(
                "```JSON"
            )
            .removePrefix(
                "```"
            )
            .removeSuffix(
                "```"
            )
            .trim()
    }

    private fun <T> Sequence<T>.takeLastAsList(
        maximumCount: Int
    ): List<T> {
        return toList()
            .takeLast(
                maximumCount
            )
    }

    private companion object {
        const val GATEWAY_TAG =
            "CommunicationEvidenceAi"

        const val SYSTEM_ROLE =
            "system"

        const val USER_ROLE =
            "user"

        const val JSON_OBJECT_TYPE =
            "json_object"

        const val EVIDENCE_KEY =
            "evidence"

        const val SKILL_KEY =
            "skill"

        const val RESULT_KEY =
            "result"

        const val CONFIDENCE_KEY =
            "confidence"

        const val REASON_KEY =
            "reason"

        const val REINFORCEMENT_INSTRUCTION_KEY =
            "reinforcementInstruction"

        const val LEARNER_GUIDANCE_KEY =
            "learnerGuidance"

        const val LEVEL_APPROPRIATE_TEXT_KEY =
            "levelAppropriateText"

        const val MAXIMUM_RESPONSE_TOKENS =
            1_200

        const val MAXIMUM_REPAIR_INPUT_LENGTH =
            6_000

        const val MAXIMUM_MESSAGE_LENGTH =
            1_500

        const val MAXIMUM_RECENT_TURN_COUNT =
            10

        const val MAXIMUM_TURN_LENGTH =
            500

        const val MAXIMUM_EVIDENCE_COUNT =
            3

        const val MINIMUM_ACCEPTED_CONFIDENCE =
            0.55f

        const val EVALUATION_TEMPERATURE =
            0.15

        const val REPAIR_TEMPERATURE =
            0.05

        val MULTIPLE_WHITESPACE_REGEX =
            Regex(
                "\\s+"
            )
    }
}