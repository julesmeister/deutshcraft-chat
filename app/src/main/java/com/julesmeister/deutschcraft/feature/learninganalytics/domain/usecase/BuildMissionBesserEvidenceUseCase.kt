package com.julesmeister.deutschcraft.feature.learninganalytics.domain.usecase

import android.util.Log
import com.julesmeister.deutschcraft.core.database.dao.AdaptiveLearningMistakeDao
import com.julesmeister.deutschcraft.core.database.dao.MessageDao
import com.julesmeister.deutschcraft.core.database.entity.DetectedMistakeEntity
import com.julesmeister.deutschcraft.core.database.entity.MessageEntity
import com.julesmeister.deutschcraft.core.database.entity.MessageSenderEntity
import com.julesmeister.deutschcraft.feature.learninganalytics.domain.MissionBesserEvidence
import com.julesmeister.deutschcraft.feature.mission.domain.ConversationMission
import java.time.Clock
import javax.inject.Inject
import kotlinx.coroutines.CancellationException

/*
 * Builds Besser evidence from learner messages sent while a
 * conversation mission was active.
 *
 * Evidence comes from existing Room data:
 *
 * - MessageEntity.correction
 * - DetectedMistakeEntity
 * - AssessmentEvidenceEntity.messageId
 * - MissionObjectiveProgress.completedByMessageId
 *
 * This use case does not generate corrections or modify
 * assessment data.
 *
 * The assessment system remains responsible for persisting
 * detected mistakes and Besser corrections.
 */
class BuildMissionBesserEvidenceUseCase
@Inject constructor(
    private val messageDao:
    MessageDao,
    private val mistakeDao:
    AdaptiveLearningMistakeDao,
    private val clock:
    Clock
) {

    suspend operator fun invoke(
        mission:
        ConversationMission
    ): MissionBesserEvidence {
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
            /*
             * Coroutine cancellation must always propagate.
             */
            throw cancellationException
        } catch (throwable: Throwable) {
            /*
             * Besser analytics are supplementary.
             *
             * Failure to read correction evidence must not
             * stop mission completion or prevent the rest of
             * the analytics from being saved.
             */
            Log.e(
                USE_CASE_TAG,
                "Mission Besser evidence could not be built.",
                throwable
            )

            MissionBesserEvidence.EMPTY
        }
    }

    private suspend fun buildEvidence(
        mission:
        ConversationMission
    ): MissionBesserEvidence {
        val missionStartedAt =
            mission.startedAt
                ?: mission.createdAt

        val missionCompletedAt =
            mission.completedAt
                ?: clock.millis()

        /*
         * Query only learner messages sent during the mission.
         *
         * Assistant messages must not affect:
         *
         * - learner message count
         * - correction count
         * - weakness resolution
         * - objective correction attribution
         */
        val learnerMessages =
            messageDao
                .getMessagesForSenderInWindow(
                    conversationId =
                        mission.conversationId,
                    sender =
                        MessageSenderEntity.USER,
                    startedAt =
                        missionStartedAt,
                    completedAt =
                        missionCompletedAt
                )
                .filter { message ->
                    message.content.isNotBlank()
                }
                .sortedBy { message ->
                    message.sequenceNumber
                }

        if (learnerMessages.isEmpty()) {
            return MissionBesserEvidence.EMPTY
        }

        /*
         * Load detected mistakes for every learner message.
         *
         * AdaptiveLearningMistakeDao performs the join:
         *
         * detected_mistakes
         *     ↓ evidenceId
         * assessment_evidence
         *     ↓ messageId
         * messages
         */
        val messageEvidence =
            learnerMessages.map { message ->
                val mistakes =
                    mistakeDao.getMistakesForMessage(
                        messageId =
                            message.id
                    )

                message.toBesserEvidence(
                    mistakes =
                        mistakes
                )
            }

        /*
         * Count corrected learner messages, not individual
         * mistake rows.
         *
         * One learner message may contain multiple mistakes,
         * but it still represents one corrected conversational
         * turn for mission-level grammar analytics.
         */
        val correctedMessages =
            messageEvidence.filter { evidence ->
                evidence.wasCorrected
            }

        val resolvedCorrectionCount =
            correctedMessages.count { correctedMessage ->
                hasResolutionEvidence(
                    correctedMessage =
                        correctedMessage,
                    completeMessageEvidence =
                        messageEvidence
                )
            }

        val persistentWeaknessKeys =
            findPersistentWeaknessKeys(
                messageEvidence =
                    messageEvidence
            )

        val correctionsByObjectiveId =
            attributeCorrectionsToObjectives(
                mission =
                    mission,
                messageEvidence =
                    messageEvidence
            )

        Log.d(
            USE_CASE_TAG,
            buildString {
                append(
                    "Mission Besser evidence built. "
                )

                append(
                    "missionId="
                )

                append(
                    mission.id
                )

                append(
                    ", learnerMessages="
                )

                append(
                    learnerMessages.size
                )

                append(
                    ", correctedMessages="
                )

                append(
                    correctedMessages.size
                )

                append(
                    ", resolvedCorrections="
                )

                append(
                    resolvedCorrectionCount
                )

                append(
                    ", persistentWeaknesses="
                )

                append(
                    persistentWeaknessKeys
                )
            }
        )

        return MissionBesserEvidence(
            learnerMessageCount =
                learnerMessages.size,
            correctedMessageCount =
                correctedMessages.size,
            resolvedCorrectionCount =
                resolvedCorrectionCount,
            persistentWeaknessKeys =
                persistentWeaknessKeys,
            correctionsByObjectiveId =
                correctionsByObjectiveId
        )
    }

    /*
     * Converts one learner message and its detected mistakes
     * into internal Besser evidence.
     *
     * A message is considered corrected when either:
     *
     * - MessageEntity.correction contains a meaningful change
     * - At least one DetectedMistakeEntity belongs to it
     */
    private fun MessageEntity.toBesserEvidence(
        mistakes:
        List<DetectedMistakeEntity>
    ): MessageBesserEvidence {
        return MessageBesserEvidence(
            messageId =
                id,
            sequenceNumber =
                sequenceNumber,
            wasCorrected =
                hasMeaningfulCorrection() ||
                        mistakes.isNotEmpty(),
            weaknessKeys =
                mistakes
                    .map { mistake ->
                        mistake.toWeaknessKey()
                    }
                    .filter(
                        String::isNotBlank
                    )
                    .toSet(),
            allDetectedMistakesResolved =
                mistakes.isNotEmpty() &&
                        mistakes.all { mistake ->
                            mistake.isResolved
                        }
        )
    }

    /*
     * Determines whether a corrected learner message has later
     * resolution evidence within the same mission.
     *
     * A correction is considered resolved when:
     *
     * 1. Every persisted mistake on the message is already
     *    marked isResolved.
     *
     * OR
     *
     * 2. There are at least two later learner messages and
     *    none repeats the same categorized weakness.
     *
     * Important:
     *
     * Not repeating a mistake does not prove permanent
     * mastery. This is mission-level improvement evidence,
     * not a final mastery decision.
     */
    private fun hasResolutionEvidence(
        correctedMessage:
        MessageBesserEvidence,
        completeMessageEvidence:
        List<MessageBesserEvidence>
    ): Boolean {
        if (
            correctedMessage
                .allDetectedMistakesResolved
        ) {
            return true
        }

        val laterMessages =
            completeMessageEvidence.filter { message ->
                message.sequenceNumber >
                        correctedMessage.sequenceNumber
            }

        if (
            laterMessages.size <
            MINIMUM_FOLLOW_UP_MESSAGE_COUNT
        ) {
            return false
        }

        /*
         * A correction may exist directly on MessageEntity
         * without categorized DetectedMistakeEntity records.
         *
         * In that situation, give resolution credit only when
         * the next two learner messages required no correction.
         */
        if (
            correctedMessage
                .weaknessKeys
                .isEmpty()
        ) {
            return laterMessages
                .take(
                    MINIMUM_FOLLOW_UP_MESSAGE_COUNT
                )
                .none { message ->
                    message.wasCorrected
                }
        }

        /*
         * When categorized weaknesses exist, resolution
         * requires that none of those same weakness keys occur
         * again in later mission messages.
         */
        return laterMessages.none { laterMessage ->
            laterMessage
                .weaknessKeys
                .any { laterWeaknessKey ->
                    laterWeaknessKey in
                            correctedMessage.weaknessKeys
                }
        }
    }

    /*
     * Finds weaknesses still appearing near the end of the
     * mission.
     *
     * Only weakness keys appearing in the final third of the
     * learner messages are included.
     *
     * Results are ordered by total mission frequency.
     */
    private fun findPersistentWeaknessKeys(
        messageEvidence:
        List<MessageBesserEvidence>
    ): List<String> {
        if (messageEvidence.isEmpty()) {
            return emptyList()
        }

        val weaknessFrequency =
            messageEvidence
                .flatMap { message ->
                    message.weaknessKeys
                }
                .groupingBy { weaknessKey ->
                    weaknessKey
                }
                .eachCount()

        if (weaknessFrequency.isEmpty()) {
            return emptyList()
        }

        val finalThirdStartIndex =
            calculateFinalThirdStartIndex(
                messageCount =
                    messageEvidence.size
            )

        val weaknessKeysNearMissionEnd =
            messageEvidence
                .drop(
                    finalThirdStartIndex
                )
                .flatMap { message ->
                    message.weaknessKeys
                }
                .toSet()

        return weaknessKeysNearMissionEnd
            .sortedWith(
                compareByDescending<String> { weaknessKey ->
                    weaknessFrequency[
                        weaknessKey
                    ]
                        ?: 0
                }
                    .thenBy { weaknessKey ->
                        weaknessKey
                    }
            )
            .take(
                MAXIMUM_PERSISTENT_WEAKNESS_COUNT
            )
    }

    private fun calculateFinalThirdStartIndex(
        messageCount: Int
    ): Int {
        if (messageCount <= 1) {
            return 0
        }

        return (
                messageCount *
                        FINAL_THIRD_START_NUMERATOR /
                        FINAL_THIRD_START_DENOMINATOR
                )
            .coerceIn(
                minimumValue =
                    0,
                maximumValue =
                    messageCount -
                            1
            )
    }

    /*
     * Associates corrected learner messages with the mission
     * objective active when each message was sent.
     *
     * Objective messages are divided by their persisted
     * completion boundaries.
     *
     * Example:
     *
     * Objective 1 completed by learner message 4:
     *
     * Objective 1 owns learner messages 1 through 4.
     *
     * Objective 2 completed by learner message 7:
     *
     * Objective 2 owns learner messages 5 through 7.
     *
     * The first unfinished objective owns all remaining
     * learner messages.
     */
    private fun attributeCorrectionsToObjectives(
        mission:
        ConversationMission,
        messageEvidence:
        List<MessageBesserEvidence>
    ): Map<String, Int> {
        if (
            mission.objectiveProgress.isEmpty()
        ) {
            return emptyMap()
        }

        val sequenceByMessageId =
            messageEvidence.associate { message ->
                message.messageId to
                        message.sequenceNumber
            }

        val orderedObjectiveProgress =
            mission
                .objectiveProgress
                .sortedBy { progress ->
                    progress
                        .objective
                        .sequenceNumber
                }

        val correctionCounts =
            linkedMapOf<String, Int>()

        var previousObjectiveBoundary =
            Long.MIN_VALUE

        var remainingMessagesAssigned =
            false

        orderedObjectiveProgress.forEach { progress ->
            val objectiveId =
                progress.objective.id

            val completionSequence =
                progress
                    .completedByMessageId
                    ?.let { completionMessageId ->
                        sequenceByMessageId[
                            completionMessageId
                        ]
                    }

            val objectiveBoundary =
                when {
                    /*
                     * Normal completed objective with a known
                     * completion message.
                     */
                    progress.isCompleted &&
                            completionSequence != null -> {

                        completionSequence
                    }

                    /*
                     * The first unfinished objective owns the
                     * messages after the previous boundary.
                     */
                    !progress.isCompleted &&
                            !remainingMessagesAssigned -> {

                        remainingMessagesAssigned =
                            true

                        Long.MAX_VALUE
                    }

                    /*
                     * A completed objective without a known
                     * completion message cannot be safely
                     * attributed.
                     *
                     * Later unfinished objectives also have no
                     * learner-message segment because the first
                     * unfinished objective is still active.
                     */
                    else -> {
                        correctionCounts[
                            objectiveId
                        ] =
                            0

                        return@forEach
                    }
                }

            val correctionCount =
                messageEvidence.count { message ->
                    message.wasCorrected &&
                            message.sequenceNumber >
                            previousObjectiveBoundary &&
                            message.sequenceNumber <=
                            objectiveBoundary
                }

            correctionCounts[
                objectiveId
            ] =
                correctionCount

            previousObjectiveBoundary =
                objectiveBoundary
        }

        /*
         * Guarantee that every mission objective has an entry.
         */
        orderedObjectiveProgress.forEach { progress ->
            correctionCounts.putIfAbsent(
                progress.objective.id,
                0
            )
        }

        return correctionCounts
    }

    /*
     * Checks whether the correction differs meaningfully from
     * the learner's original message.
     *
     * Whitespace, capitalization, and punctuation differences
     * alone do not count as a meaningful Besser correction.
     */
    private fun MessageEntity.hasMeaningfulCorrection():
            Boolean {
        val correctedText =
            correction
                ?.trim()
                ?.takeIf(
                    String::isNotBlank
                )
                ?: return false

        return normalizeText(
            value =
                correctedText
        ) !=
                normalizeText(
                    value =
                        content
                )
    }

    /*
     * Grammar feature is more specific than mistake category,
     * so it is preferred as the stable weakness key.
     *
     * Example:
     *
     * Grammar feature:
     * DATIVE_CASE
     *
     * Category fallback:
     * GRAMMAR
     */
    private fun DetectedMistakeEntity.toWeaknessKey():
            String {
        return grammarFeature?.name
            ?: category.name
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

    /*
     * Internal representation of Besser evidence for one
     * learner message.
     */
    private data class MessageBesserEvidence(
        val messageId: String,
        val sequenceNumber: Long,
        val wasCorrected: Boolean,
        val weaknessKeys: Set<String>,
        val allDetectedMistakesResolved: Boolean
    )

    private companion object {
        const val USE_CASE_TAG =
            "MissionBesserEvidence"

        /*
         * A weakness must not reappear across at least two
         * later learner messages before receiving mission-level
         * resolution credit.
         */
        const val MINIMUM_FOLLOW_UP_MESSAGE_COUNT =
            2

        /*
         * Mission result summaries should remain concise.
         */
        const val MAXIMUM_PERSISTENT_WEAKNESS_COUNT =
            3

        /*
         * Index calculation for the final third:
         *
         * messageCount * 2 / 3
         */
        const val FINAL_THIRD_START_NUMERATOR =
            2

        const val FINAL_THIRD_START_DENOMINATOR =
            3

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