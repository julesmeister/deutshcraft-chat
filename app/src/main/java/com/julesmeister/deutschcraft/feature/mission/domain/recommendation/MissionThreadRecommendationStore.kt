package com.julesmeister.deutschcraft.feature.mission.domain.recommendation

import com.julesmeister.deutschcraft.core.cefr.model.CefrLevel
import kotlinx.coroutines.flow.Flow

/*
 * Persistent source of truth for generated thread-list
 * mission recommendations.
 *
 * Recommendation lifecycle:
 *
 * READY
 *     Generated and waiting to be displayed.
 *
 * DISPLAYED
 *     Currently visible or previously displayed.
 *
 * SKIPPED
 *     Swiped away by the learner. Skipped recommendations
 *     can return to rotation once no READY recommendation
 *     remains (round-robin).
 *
 * STARTED
 *     Converted into a real conversation mission.
 *
 * COMPLETED
 *     The conversation mission created from the
 *     recommendation was completed.
 *
 * EXPIRED
 *     No longer valid because of age or CEFR-level change.
 *
 * INVALID
 *     Persisted recommendation failed validation.
 */
interface MissionThreadRecommendationStore {

    /*
     * Observes the next eligible recommendation.
     *
     * A DISPLAYED recommendation has priority over READY
     * recommendations so the same card is restored after
     * process death.
     */
    fun observeNextEligible(
        cefrLevel: CefrLevel
    ): Flow<MissionThreadRecommendation?>

    /*
     * Returns the first eligible persisted recommendation.
     */
    suspend fun getNextEligible(
        cefrLevel: CefrLevel
    ): MissionThreadRecommendation?

    /*
     * Returns up to limit eligible recommendations in their
     * display order.
     */
    suspend fun getEligibleRecommendations(
        cefrLevel: CefrLevel,
        limit: Int
    ): List<MissionThreadRecommendation>

    /*
     * Counts READY and DISPLAYED recommendations that have
     * not expired.
     */
    suspend fun countEligible(
        cefrLevel: CefrLevel
    ): Int

    /*
     * Returns semantic scenario types that were recently
     * generated, displayed, skipped, started, or completed.
     *
     * The recommendation generator uses these types as
     * exclusions.
     */
    suspend fun getRecentScenarioTypes(
        cefrLevel: CefrLevel
    ): Set<MissionScenarioType>

    /*
     * Returns true when the scenario already exists as a
     * READY, DISPLAYED, or STARTED recommendation.
     */
    suspend fun hasActiveScenario(
        cefrLevel: CefrLevel,
        scenarioType: MissionScenarioType
    ): Boolean

    /*
     * Persists one recommendation and all of its objectives
     * atomically.
     */
    suspend fun save(
        recommendation: MissionThreadRecommendation
    )

    /*
     * Marks a READY recommendation as DISPLAYED.
     */
    suspend fun markDisplayed(
        recommendationId: String
    )

    /*
     * Marks a recommendation as SKIPPED after a left swipe.
     */
    suspend fun markSkipped(
        recommendationId: String
    )

    /*
     * Reactivates the oldest skipped recommendation for the
     * level and returns it as DISPLAYED.
     *
     * This lets swiping wrap around to the beginning of the
     * recommendations already stored in Room instead of
     * showing an empty cell.
     *
     * excludedRecommendationId is avoided first. It is only
     * reused when it is the sole recommendation available.
     *
     * Returns null when no skipped recommendation exists.
     */
    suspend fun reactivateNextSkipped(
        cefrLevel: CefrLevel,
        excludedRecommendationId: String?
    ): MissionThreadRecommendation?

    /*
     * Links the recommendation to the conversation and
     * ConversationMission created from it.
     */
    suspend fun markStarted(
        recommendationId: String,
        conversationId: String,
        conversationMissionId: String
    )

    /*
     * Marks the original recommendation completed when its
     * created ConversationMission is completed.
     */
    suspend fun markCompletedByMissionId(
        conversationMissionId: String
    )

    /*
     * Expires outdated drafts and removes old inactive data.
     */
    suspend fun performMaintenance(
        cefrLevel: CefrLevel
    )
}