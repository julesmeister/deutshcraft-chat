package com.julesmeister.deutschcraft.feature.adaptivelearning.domain.repository

import com.julesmeister.deutschcraft.feature.adaptivelearning.domain.model.AdaptiveLearningFocus
import com.julesmeister.deutschcraft.feature.adaptivelearning.domain.model.LearningEvidenceResult
import com.julesmeister.deutschcraft.feature.adaptivelearning.domain.model.LearningFocusType
import kotlinx.coroutines.flow.Flow

interface AdaptiveLearningRepository {

    /*
     * Observes all known grammar, vocabulary, and
     * communication focuses.
     */
    fun observeAllFocuses():
            Flow<List<AdaptiveLearningFocus>>

    /*
     * Observes focuses that Emil should reinforce through
     * future conversations.
     *
     * Included mastery states:
     *
     * - STRUGGLING
     * - PRACTICING
     * - NEEDS_REVIEW
     */
    fun observeReinforcementFocuses():
            Flow<List<AdaptiveLearningFocus>>

    /*
     * Observes focuses that have accumulated enough
     * successful evidence to be considered mastered.
     */
    fun observeMasteredFocuses():
            Flow<List<AdaptiveLearningFocus>>

    /*
     * Observes focuses belonging to one learning category.
     *
     * Categories:
     *
     * - GRAMMAR
     * - VOCABULARY
     * - COMMUNICATION
     */
    fun observeFocusesByType(
        type: LearningFocusType
    ): Flow<List<AdaptiveLearningFocus>>

    /*
     * Finds a focus using its stable normalized key.
     *
     * Examples:
     *
     * grammar:dative_articles
     * vocabulary:bestellen
     * communication:giving_reasons
     */
    suspend fun getFocusByKey(
        focusKey: String
    ): AdaptiveLearningFocus?

    /*
     * Returns a short prioritized list of focuses that Emil
     * can use when deciding how to steer a conversation.
     */
    suspend fun getTopReinforcementFocuses(
        minimumConfidence: Float =
            DEFAULT_MINIMUM_REINFORCEMENT_CONFIDENCE,
        limit: Int =
            DEFAULT_REINFORCEMENT_LIMIT
    ): List<AdaptiveLearningFocus>

    /*
     * Saves an aggregate adaptive-learning focus without
     * creating a message-level evidence record.
     */
    suspend fun saveFocus(
        focus: AdaptiveLearningFocus
    )

    /*
     * Saves an updated aggregate focus and one message-level
     * evidence record transactionally.
     *
     * The caller is responsible for calculating:
     *
     * - Updated evidence count
     * - Updated struggle count
     * - Updated success count
     * - Updated consecutive success count
     * - Updated pattern confidence
     * - Updated mastery score
     * - Updated mastery state
     */
    suspend fun recordEvidence(
        focus: AdaptiveLearningFocus,
        messageId: String,
        conversationId: String?,
        result: LearningEvidenceResult,
        confidence: Float,
        reason: String,
        learnerText: String,
        correctedText: String? =
            null,
        levelAppropriateText: String? =
            null,
        observedAt: Long
    )

    /*
     * Removes all adaptive-learning focuses and evidence.
     *
     * This can later be used by the Reset Learning Progress
     * setting.
     */
    suspend fun clearAll()

    companion object {
        const val DEFAULT_MINIMUM_REINFORCEMENT_CONFIDENCE =
            0.65f

        const val DEFAULT_REINFORCEMENT_LIMIT =
            3
    }
}