// app/src/main/java/com/julesmeister/deutschcraft/feature/chat/presentation/ChatMessageSender.kt

package com.julesmeister.deutschcraft.feature.chat.presentation

import android.util.Log
import com.julesmeister.deutschcraft.feature.chat.data.cache.ReplySuggestionCache
import com.julesmeister.deutschcraft.feature.chat.domain.ReplySuggestion
import com.julesmeister.deutschcraft.feature.chat.domain.usecase.SendChatMessageUseCase
import com.julesmeister.deutschcraft.feature.chat.presentation.theme.ChatDesignTokens
import com.julesmeister.deutschcraft.feature.chat.presentation.theme.ChatPresentationTokens
import com.julesmeister.deutschcraft.feature.mission.domain.ConversationMission
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/*
 * Handles the conversational send pipeline.
 *
 * The critical conversational path is intentionally separated
 * from supplementary work.
 *
 * Critical path:
 *
 * learner sends
 *     ↓
 * Emil reply generated
 *     ↓
 * assistant message persisted
 *     ↓
 * composer unlocked
 *
 * Supplementary background work:
 *
 *     ├── progressive suggestion presentation
 *     └── post-send learning processing
 *
 * Neither supplementary branch should prevent the learner from
 * typing or sending the next message.
 */
class ChatMessageSender
@Inject constructor(
    private val sendChatMessage:
    SendChatMessageUseCase,

    private val replySuggestionCache:
    ReplySuggestionCache,

    private val postSendLearningProcessor:
    ChatPostSendLearningProcessor
) {

    /*
     * =======================================================
     * CRITICAL CONVERSATION SEND
     * =======================================================
     *
     * This method returns as soon as SendChatMessageUseCase has
     * completed its conversational work and Emil's message has
     * been persisted.
     *
     * ChatPostSendLearningProcessor deliberately does NOT run
     * here.
     */
    suspend fun sendCritical(
        conversationId: String,
        messageText: String,
        activeMission: ConversationMission?
    ): ChatImmediateSendResult {
        require(
            conversationId.isNotBlank()
        ) {
            "Conversation ID cannot be blank."
        }

        val normalizedMessageText =
            messageText.trim()

        require(
            normalizedMessageText.isNotBlank()
        ) {
            "Message text cannot be blank."
        }

        val sendResult =
            sendChatMessage(
                conversationId =
                    conversationId,

                messageText =
                    normalizedMessageText,

                activeMission =
                    activeMission
            )

        val assistantMessage =
            sendResult
                .assistantReply
                .message

        val suggestions =
            normalizeSuggestions(
                suggestions =
                    sendResult
                        .assistantReply
                        .suggestions
            )

        /*
         * Suggestions belonging to the previous assistant
         * response are immediately invalid.
         */
        replySuggestionCache.clearSuggestions(
            conversationId =
                conversationId
        )

        /*
         * Qwen currently returns all suggestions together.
         *
         * We therefore publish the first one immediately and
         * progressively present the remaining suggestions after
         * the assistant message is visible.
         *
         * This is progressive PRESENTATION, not Qwen network
         * streaming.
         */
        val firstSuggestions =
            suggestions.take(
                n =
                    ChatPresentationTokens.MessageSender.FirstSuggestionCount
            )

        if (firstSuggestions.isNotEmpty()) {
            replySuggestionCache
                .replaceSuggestions(
                    conversationId =
                        conversationId,

                    suggestions =
                        firstSuggestions
                )
        }

        Log.d(
            ChatPresentationTokens.MessageSender.MessageSenderTag,
            buildString {
                append(
                    "Critical conversational send completed. "
                )

                append(
                    "conversationId="
                )

                append(
                    conversationId
                )

                append(
                    ", assistantMessageId="
                )

                append(
                    assistantMessage.id
                )

                append(
                    ", suggestionCount="
                )

                append(
                    suggestions.size
                )

                append(
                    ", firstSuggestionCount="
                )

                append(
                    firstSuggestions.size
                )
            }
        )

        return ChatImmediateSendResult(
            assistantMessageId =
                assistantMessage.id,

            firstSuggestions =
                firstSuggestions,

            remainingSuggestions =
                suggestions.drop(
                    n =
                        ChatPresentationTokens.MessageSender.FirstSuggestionCount
                ),

            finalSuggestions =
                suggestions,

            warning =
                sendResult.assessmentWarning
        )
    }

    /*
     * =======================================================
     * PROGRESSIVE SUGGESTION PRESENTATION
     * =======================================================
     *
     * Starts only after Emil's visible response is ready.
     *
     * This job is completely independent from the critical
     * send job and must never lock the composer.
     */
    fun startSuggestionPresentation(
        scope: CoroutineScope,
        conversationId: String,
        assistantMessageId: String,
        remainingSuggestions:
        List<ReplySuggestion>,
        onSuggestionsUpdated:
        suspend (
            assistantMessageId: String,
            suggestions: List<ReplySuggestion>
        ) -> Unit
    ): Job {
        return scope.launch {
            try {
                remainingSuggestions
                    .forEach { suggestion ->
                        /*
                         * Small visual delay so suggestions
                         * enter progressively instead of all
                         * appearing during the same frame.
                         *
                         * Remove this delay later when true Qwen
                         * suggestion streaming is implemented.
                         */
                        delay(
                            ChatDesignTokens.ReplySuggestions.RevealDelayMillis.milliseconds
                        )

                        val updatedSuggestions =
                            replySuggestionCache
                                .appendSuggestion(
                                    conversationId =
                                        conversationId,

                                    suggestion =
                                        suggestion
                                )

                        onSuggestionsUpdated(
                            assistantMessageId,
                            updatedSuggestions
                        )
                    }

                Log.d(
                    ChatPresentationTokens.MessageSender.MessageSenderTag,
                    buildString {
                        append(
                            "Suggestion presentation completed. "
                        )

                        append(
                            "conversationId="
                        )

                        append(
                            conversationId
                        )

                        append(
                            ", assistantMessageId="
                        )

                        append(
                            assistantMessageId
                        )

                        append(
                            ", finalSuggestionCount="
                        )

                        append(
                            replySuggestionCache
                                .getSuggestions(
                                    conversationId =
                                        conversationId
                                )
                                .size
                        )
                    }
                )
            } catch (
                cancellationException:
                CancellationException
            ) {
                /*
                 * Expected when the learner sends another
                 * message before the previous suggestions have
                 * finished appearing.
                 */
                throw cancellationException
            } catch (throwable: Throwable) {
                /*
                 * Suggestions are optional.
                 *
                 * Failure must never convert a successful Emil
                 * response into a failed conversation.
                 */
                Log.e(
                    ChatPresentationTokens.MessageSender.MessageSenderTag,
                    "Progressive suggestion presentation failed.",
                    throwable
                )
            }
        }
    }

    /*
     * =======================================================
     * POST-SEND LEARNING
     * =======================================================
     *
     * Runs only after the visible conversation turn is already
     * complete.
     *
     * This may perform:
     *
     * - learning assessment
     * - Besser processing
     * - adaptive learning
     * - mission evaluation
     * - mission analytics
     *
     * None of this should keep the composer locked.
     */
    fun startPostSendLearning(
        scope: CoroutineScope,
        conversationId: String,
        submittedMessageText: String,
        activeMission: ConversationMission?,
        suggestions: List<ReplySuggestion>,
        warning: String?,
        onCompleted:
        suspend (
            result: ChatMessageSendResult
        ) -> Unit,
        onFailure:
        suspend (
            throwable: Throwable
        ) -> Unit =
            {}
    ): Job {
        return scope.launch {
            try {
                val normalizedSubmittedMessage =
                    submittedMessageText.trim()

                if (
                    normalizedSubmittedMessage.isBlank()
                ) {
                    Log.w(
                        ChatPresentationTokens.MessageSender.MessageSenderTag,
                        "Skipping post-send learning because " +
                                "the submitted message is blank."
                    )

                    return@launch
                }

                val learningResult =
                    postSendLearningProcessor.process(
                        conversationId =
                            conversationId,

                        submittedMessageText =
                            normalizedSubmittedMessage,

                        activeMission =
                            activeMission
                    )

                val result =
                    ChatMessageSendResult(
                        suggestions =
                            suggestions,

                        warning =
                            warning,

                        adaptiveLearningFeedback =
                            learningResult
                                .adaptiveLearningFeedback,

                        completedMissionTitle =
                            learningResult
                                .completedMissionTitle,

                        completedMissionAnalytics =
                            learningResult
                                .completedMissionAnalytics
                    )

                onCompleted(
                    result
                )

                Log.d(
                    ChatPresentationTokens.MessageSender.MessageSenderTag,
                    buildString {
                        append(
                            "Post-send learning completed. "
                        )

                        append(
                            "conversationId="
                        )

                        append(
                            conversationId
                        )

                        append(
                            ", suggestionCount="
                        )

                        append(
                            suggestions.size
                        )

                        append(
                            ", hasWarning="
                        )

                        append(
                            !warning.isNullOrBlank()
                        )

                        append(
                            ", missionCompleted="
                        )

                        append(
                            learningResult
                                .completedMissionAnalytics !=
                                    null
                        )
                    }
                )
            } catch (
                cancellationException:
                CancellationException
            ) {
                /*
                 * Cancellation is normal lifecycle control and
                 * must continue propagating.
                 */
                throw cancellationException
            } catch (throwable: Throwable) {
                /*
                 * Emil's response has already succeeded.
                 *
                 * Supplementary learning failure therefore must
                 * not turn the conversation itself into a
                 * failed send.
                 */
                Log.e(
                    ChatPresentationTokens.MessageSender.MessageSenderTag,
                    "Post-send learning processing failed.",
                    throwable
                )

                onFailure(
                    throwable
                )
            }
        }
    }

    /*
     * =======================================================
     * SUGGESTION CACHE
     * =======================================================
     */

    fun clearSuggestions(
        conversationId: String
    ) {
        if (conversationId.isBlank()) {
            return
        }

        replySuggestionCache
            .clearSuggestions(
                conversationId =
                    conversationId
            )
    }

    /*
     * =======================================================
     * SUGGESTION NORMALIZATION
     * =======================================================
     */

    private fun normalizeSuggestions(
        suggestions:
        List<ReplySuggestion>
    ): List<ReplySuggestion> {
        return suggestions
            .mapNotNull { suggestion ->
                val cleanText =
                    suggestion
                        .text
                        .trim()
                        .replace(
                            regex =
                                MULTIPLE_WHITESPACE_REGEX,

                            replacement =
                                " "
                        )

                if (cleanText.isBlank()) {
                    null
                } else {
                    suggestion.copy(
                        text =
                            cleanText
                    )
                }
            }
            .distinctBy { suggestion ->
                suggestion
                    .text
                    .lowercase()
            }
            .take(
                n =
                    ChatPresentationTokens.MessageSender.MaximumSuggestionCount
            )
    }

    private companion object {

        val MULTIPLE_WHITESPACE_REGEX =
            Regex(
                "\\s+"
            )
    }
}

/*
 * ===========================================================
 * IMMEDIATE SEND RESULT
 * ===========================================================
 *
 * Represents only the critical learner -> Emil conversational
 * path.
 *
 * Post-send learning results intentionally do not belong here.
 */
data class ChatImmediateSendResult(
    val assistantMessageId: String,

    val firstSuggestions:
    List<ReplySuggestion>,

    val remainingSuggestions:
    List<ReplySuggestion>,

    val finalSuggestions:
    List<ReplySuggestion>,

    val warning: String?
)
