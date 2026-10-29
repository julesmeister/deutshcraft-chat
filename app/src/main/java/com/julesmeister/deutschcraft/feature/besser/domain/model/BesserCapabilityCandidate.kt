package com.julesmeister.deutschcraft.feature.besser.domain.model

/*
 * One capability shortlisted locally for contextual evaluation.
 *
 * IMPORTANT:
 *
 * localPriorityScore does NOT mean that this capability should
 * automatically become a challenge.
 *
 * Local selection answers:
 *
 *     "Which capabilities are worth showing to AI?"
 *
 * The later Qwen selector answers:
 *
 *     "Which one naturally fits this exact learner message?"
 *
 * Qwen may still return NO_CHALLENGE.
 */
data class BesserCapabilityCandidate(
    val capabilityId: String,
    val name: String,
    val description: String,
    val category: String,
    val cefrLevel: String,

    /*
     * Local relevance/prioritization score.
     */
    val localPriorityScore: Float,

    /*
     * Number of meaningful words shared by the learner message
     * and the capability's catalogue text.
     */
    val lexicalMatchCount: Int,

    /*
     * True when an upstream category/intention hint matches the
     * catalogue category.
     */
    val categoryMatched: Boolean,

    /*
     * Distinguishes reinforcement from forward growth.
     *
     * For B1 -> B2:
     *
     * B1 capability = reinforcement
     * B2 capability = target-level growth
     */
    val isTargetLevelCapability: Boolean,

    /*
     * Original curriculum/catalogue order.
     */
    val sortOrder: Int
)