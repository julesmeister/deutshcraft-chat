package com.julesmeister.deutschcraft.feature.learninganalytics.domain.repository

import kotlinx.coroutines.flow.Flow

/*
 * Domain abstraction for persistent mission-objective hint
 * usage.
 *
 * The presentation and domain layers use this interface
 * instead of accessing Room directly.
 *
 * Hint levels:
 *
 * 0 = no hint used
 * 1 = gentle hint
 * 2 = contextual / more specific hint
 * 3 = direct wording support
 *
 * Only levels 1 to 3 are persisted. A value of 0 simply
 * means that no hint-usage row exists for the objective.
 */
interface MissionObjectiveHintUsageRepository {

    /*
     * Records one revealed hint.
     *
     * The combination of:
     *
     * - conversationMissionId
     * - objectiveId
     * - hintLevel
     *
     * is unique in Room.
     *
     * Therefore revealing the same hint repeatedly does not
     * count as additional support.
     *
     * Returns:
     *
     * true:
     * A new hint-usage record was inserted.
     *
     * false:
     * This exact hint level had already been recorded.
     */
    suspend fun recordHint(
        conversationMissionId: String,
        objectiveId: String,
        hintLevel: Int,
        hintText: String
    ): Boolean

    /*
     * Returns the number of unique hint levels revealed for
     * one objective.
     *
     * Expected values:
     *
     * 0 = no hints
     * 1 = one hint
     * 2 = two hints
     * 3 = all three hints
     */
    suspend fun getHintsUsed(
        conversationMissionId: String,
        objectiveId: String
    ): Int

    /*
     * Returns the strongest level of support revealed for one
     * objective.
     *
     * Expected values:
     *
     * 0 = independent
     * 1 = gentle support
     * 2 = substantial/contextual support
     * 3 = direct wording support
     */
    suspend fun getHighestHintLevel(
        conversationMissionId: String,
        objectiveId: String
    ): Int

    /*
     * Returns every hint revealed for one objective.
     *
     * The Room implementation returns these in ascending hint
     * level order.
     */
    suspend fun getObjectiveHintUsage(
        conversationMissionId: String,
        objectiveId: String
    ): List<MissionObjectiveHintUsage>

    /*
     * Returns all hint evidence for one complete conversation
     * mission.
     *
     * This is the primary method used later when building:
     *
     * MissionCompletionEvidence
     */
    suspend fun getMissionHintUsage(
        conversationMissionId: String
    ): List<MissionObjectiveHintUsage>

    /*
     * Observes hint evidence while the mission is active.
     *
     * This allows live mission analytics later without
     * requiring manual refreshes.
     */
    fun observeMissionHintUsage(
        conversationMissionId: String
    ): Flow<List<MissionObjectiveHintUsage>>

    /*
     * Total number of unique hint levels revealed during the
     * complete mission.
     *
     * Example:
     *
     * Objective A:
     * hints 1 and 2
     *
     * Objective B:
     * hint 1
     *
     * Result:
     * 3
     */
    suspend fun countMissionHintsUsed(
        conversationMissionId: String
    ): Int

    /*
     * Number of objectives that required at least one hint.
     *
     * Example:
     *
     * 10 objectives
     *
     * 6 completed with no hints
     * 3 completed with hints
     * 1 incomplete
     *
     * Result:
     * 3
     */
    suspend fun countObjectivesUsingHints(
        conversationMissionId: String
    ): Int

    /*
     * Number of objectives where the learner revealed hint
     * level 3.
     *
     * These objectives are considered directly assisted by
     * the mission analytics system.
     */
    suspend fun countDirectlyAssistedObjectives(
        conversationMissionId: String
    ): Int

    /*
     * Deletes all persisted hint evidence for one
     * ConversationMission.
     *
     * Call this only when the corresponding mission is being
     * permanently deleted.
     */
    suspend fun deleteMissionHintUsage(
        conversationMissionId: String
    )
}

/*
 * Domain representation of one coaching hint that was
 * actually revealed to the learner.
 *
 * This intentionally contains the hint text as historical
 * evidence.
 *
 * If generated mission wording changes later, completed
 * missions still retain exactly which hint the learner saw.
 */
data class MissionObjectiveHintUsage(
    val conversationMissionId: String,

    val objectiveId: String,

    /*
     * 1 = gentle
     * 2 = contextual/more specific
     * 3 = direct wording support
     */
    val hintLevel: Int,

    val hintText: String,

    val revealedAt: Long
) {

    /*
     * Convenient semantic properties for analytics and UI.
     */
    val isGentleSupport: Boolean
        get() =
            hintLevel ==
                    GENTLE_HINT_LEVEL

    val isContextualSupport: Boolean
        get() =
            hintLevel ==
                    CONTEXTUAL_HINT_LEVEL

    val isDirectSupport: Boolean
        get() =
            hintLevel >=
                    DIRECT_HINT_LEVEL

    companion object {
        const val GENTLE_HINT_LEVEL =
            1

        const val CONTEXTUAL_HINT_LEVEL =
            2

        const val DIRECT_HINT_LEVEL =
            3
    }
}