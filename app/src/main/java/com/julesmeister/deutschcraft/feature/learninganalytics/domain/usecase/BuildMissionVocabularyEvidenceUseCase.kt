package com.julesmeister.deutschcraft.feature.learninganalytics.domain.usecase

import android.util.Log
import com.julesmeister.deutschcraft.core.database.dao.MessageDao
import com.julesmeister.deutschcraft.core.database.entity.MessageEntity
import com.julesmeister.deutschcraft.core.database.entity.MessageSenderEntity
import com.julesmeister.deutschcraft.feature.learninganalytics.domain.MissionVocabularyEvidence
import com.julesmeister.deutschcraft.feature.mission.domain.ConversationMission
import com.julesmeister.deutschcraft.feature.mission.domain.MissionObjectiveProgress
import java.time.Clock
import javax.inject.Inject
import kotlinx.coroutines.CancellationException

/*
 * Detects mission target vocabulary that appears in learner
 * messages while the mission is active.
 *
 * This is deterministic local detection. It does not require
 * another AI request.
 *
 * Matching is:
 *
 * - Case-insensitive
 * - Punctuation-insensitive
 * - Unicode-aware
 * - Protected by simple word boundaries
 *
 * Example:
 *
 * Target:
 * Termin
 *
 * Learner:
 * Ich brauche einen Termin.
 *
 * Result:
 * Termin is demonstrated.
 *
 * Multi-word phrases are also supported.
 *
 * Target:
 * würde gern
 *
 * Learner:
 * Ich würde gern einen Kaffee bestellen.
 *
 * Result:
 * würde gern is demonstrated.
 */
class BuildMissionVocabularyEvidenceUseCase
@Inject constructor(
    private val messageDao:
    MessageDao,
    private val clock:
    Clock
) {

    suspend operator fun invoke(
        mission:
        ConversationMission
    ): MissionVocabularyEvidence {
        require(
            mission.id.isNotBlank()
        ) {
            "Conversation mission ID must not be blank."
        }

        require(
            mission.conversationId.isNotBlank()
        ) {
            "Conversation ID must not be blank."
        }

        return try {
            buildEvidence(
                mission =
                    mission
            )
        } catch (
            cancellationException:
            CancellationException
        ) {
            throw cancellationException
        } catch (throwable: Throwable) {
            /*
             * Vocabulary evidence is supplementary.
             *
             * Failure must not prevent mission completion or
             * the rest of mission analytics from being saved.
             */
            Log.e(
                USE_CASE_TAG,
                "Mission vocabulary evidence could not be " +
                        "built.",
                throwable
            )

            MissionVocabularyEvidence.EMPTY
        }
    }

    private suspend fun buildEvidence(
        mission:
        ConversationMission
    ): MissionVocabularyEvidence {
        val startedAt =
            mission.startedAt
                ?: mission.createdAt

        val completedAt =
            mission.completedAt
                ?: clock.millis()

        /*
         * Query only learner messages sent while this mission
         * was active.
         *
         * Assistant messages must not count as learner
         * vocabulary usage.
         */
        val learnerMessages =
            messageDao
                .getMessagesForSenderInWindow(
                    conversationId =
                        mission.conversationId,
                    sender =
                        MessageSenderEntity.USER,
                    startedAt =
                        startedAt,
                    completedAt =
                        completedAt
                )
                .filter { message ->
                    message.content.isNotBlank()
                }
                .sortedBy { message ->
                    message.sequenceNumber
                }

        val orderedObjectives =
            mission
                .objectiveProgress
                .sortedBy { progress ->
                    progress
                        .objective
                        .sequenceNumber
                }

        /*
         * Complete set of unique target words and phrases
         * across the mission.
         *
         * Original display text is preserved, while normalized
         * values are used only for duplicate detection.
         */
        val availableVocabulary =
            orderedObjectives
                .flatMap { progress ->
                    progress
                        .objective
                        .targetVocabulary
                }
                .asSequence()
                .map(
                    String::trim
                )
                .filter(
                    String::isNotBlank
                )
                .distinctBy { value ->
                    normalizeText(
                        value =
                            value
                    )
                }
                .toSet()

        if (
            learnerMessages.isEmpty() ||
            availableVocabulary.isEmpty()
        ) {
            return MissionVocabularyEvidence(
                availableVocabulary =
                    availableVocabulary,
                correctlyUsedVocabulary =
                    emptySet(),
                usedByObjectiveId =
                    emptyMap()
            )
        }

        val usedByObjectiveId =
            detectVocabularyByObjective(
                orderedObjectives =
                    orderedObjectives,
                learnerMessages =
                    learnerMessages
            )

        val demonstratedVocabulary =
            usedByObjectiveId
                .values
                .flatten()
                .toSet()

        Log.d(
            USE_CASE_TAG,
            buildString {
                append(
                    "Mission vocabulary evidence built. "
                )
                append(
                    "missionId="
                )
                append(
                    mission.id
                )
                append(
                    ", messages="
                )
                append(
                    learnerMessages.size
                )
                append(
                    ", available="
                )
                append(
                    availableVocabulary.size
                )
                append(
                    ", demonstrated="
                )
                append(
                    demonstratedVocabulary.size
                )
            }
        )

        return MissionVocabularyEvidence(
            availableVocabulary =
                availableVocabulary,
            correctlyUsedVocabulary =
                demonstratedVocabulary,
            usedByObjectiveId =
                usedByObjectiveId
        )
    }

    /*
     * Assigns learner messages to the objective that was
     * active when each message was sent.
     *
     * Objective A owns messages after the previous objective's
     * completion through Objective A's completion message.
     *
     * Objective B then owns messages after Objective A's
     * completion through Objective B's completion message.
     *
     * The first unfinished objective owns the remaining
     * learner messages.
     */
    private fun detectVocabularyByObjective(
        orderedObjectives:
        List<MissionObjectiveProgress>,
        learnerMessages:
        List<MessageEntity>
    ): Map<String, Set<String>> {
        val sequenceByMessageId =
            learnerMessages.associate { message ->
                message.id to
                        message.sequenceNumber
            }

        val result =
            linkedMapOf<String, Set<String>>()

        var previousCompletionSequence =
            Long.MIN_VALUE

        var openObjectiveAssigned =
            false

        orderedObjectives.forEach { progress ->
            val objective =
                progress.objective

            val targetVocabulary =
                objective
                    .targetVocabulary
                    .asSequence()
                    .map(
                        String::trim
                    )
                    .filter(
                        String::isNotBlank
                    )
                    .distinctBy { value ->
                        normalizeText(
                            value =
                                value
                        )
                    }
                    .toList()

            if (targetVocabulary.isEmpty()) {
                result[objective.id] =
                    emptySet()

                return@forEach
            }

            val completionSequence =
                progress
                    .completedByMessageId
                    ?.let { messageId ->
                        sequenceByMessageId[
                            messageId
                        ]
                    }

            val objectiveEndSequence =
                when {
                    progress.isCompleted &&
                            completionSequence != null -> {

                        completionSequence
                    }

                    !progress.isCompleted &&
                            !openObjectiveAssigned -> {

                        /*
                         * The first unfinished objective owns
                         * the remaining learner messages.
                         */
                        openObjectiveAssigned =
                            true

                        Long.MAX_VALUE
                    }

                    else -> {
                        /*
                         * A reliable message boundary could not
                         * be established for this objective.
                         */
                        result[objective.id] =
                            emptySet()

                        return@forEach
                    }
                }

            val objectiveMessages =
                learnerMessages.filter { message ->
                    message.sequenceNumber >
                            previousCompletionSequence &&
                            message.sequenceNumber <=
                            objectiveEndSequence
                }

            result[objective.id] =
                findUsedTargets(
                    targetVocabulary =
                        targetVocabulary,
                    messages =
                        objectiveMessages
                )

            previousCompletionSequence =
                objectiveEndSequence
        }

        return result
    }

    private fun findUsedTargets(
        targetVocabulary:
        List<String>,
        messages:
        List<MessageEntity>
    ): Set<String> {
        if (
            targetVocabulary.isEmpty() ||
            messages.isEmpty()
        ) {
            return emptySet()
        }

        val normalizedMessages =
            messages.map { message ->
                normalizeText(
                    value =
                        message.content
                )
            }

        return targetVocabulary
            .filter { target ->
                val normalizedTarget =
                    normalizeText(
                        value =
                            target
                    )

                if (normalizedTarget.isBlank()) {
                    false
                } else {
                    normalizedMessages.any { message ->
                        containsTarget(
                            normalizedMessage =
                                message,
                            normalizedTarget =
                                normalizedTarget
                        )
                    }
                }
            }
            .toSet()
    }

    /*
     * Surrounding both values with spaces creates simple word
     * boundaries.
     *
     * This prevents:
     *
     * target:
     * Rat
     *
     * from matching:
     * Restaurant
     *
     * while still allowing:
     *
     * target:
     * Termin
     *
     * to match:
     * Ich brauche einen Termin.
     *
     * Multi-word phrases also work because normalized spaces
     * remain inside the target.
     */
    private fun containsTarget(
        normalizedMessage: String,
        normalizedTarget: String
    ): Boolean {
        if (
            normalizedMessage.isBlank() ||
            normalizedTarget.isBlank()
        ) {
            return false
        }

        val paddedMessage =
            " $normalizedMessage "

        val paddedTarget =
            " $normalizedTarget "

        return paddedMessage.contains(
            other =
                paddedTarget
        )
    }

    private fun normalizeText(
        value: String
    ): String {
        return value
            .trim()
            .lowercase()
            .replace(
                NON_LETTER_OR_DIGIT_REGEX,
                " "
            )
            .replace(
                MULTIPLE_WHITESPACE_REGEX,
                " "
            )
            .trim()
    }

    private companion object {
        const val USE_CASE_TAG =
            "MissionVocabularyEvidence"

        val NON_LETTER_OR_DIGIT_REGEX =
            Regex(
                "[^\\p{L}\\p{N}]+"
            )

        val MULTIPLE_WHITESPACE_REGEX =
            Regex(
                "\\s+"
            )
    }
}