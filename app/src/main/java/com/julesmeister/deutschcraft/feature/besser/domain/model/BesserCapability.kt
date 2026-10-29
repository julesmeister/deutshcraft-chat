package com.julesmeister.deutschcraft.feature.besser.domain.model

data class BesserCapability(
    val id: String,
    val stableKey: String,
    val type: BesserCapabilityType,

    val titleEnglish: String,
    val titleGerman: String,

    val descriptionEnglish: String,
    val descriptionGerman: String,

    val minimumCefr: String,
    val targetCefr: String,

    /*
     * Message intents that make this capability relevant.
     *
     * Examples:
     *
     * opinion
     * reason
     * comparison
     * disagreement
     * hypothetical
     * narration
     */
    val applicableIntents: Set<String>,

    /*
     * Optional surface clues used only for local pre-filtering.
     *
     * These do not decide the challenge. Qwen will make the
     * final context-sensitive choice later.
     */
    val triggerTerms: Set<String>,

    val practiceState: BesserPracticeState,

    /*
     * 0f means the learner has no demonstrated mastery.
     * 1f means the capability is strongly demonstrated.
     */
    val masteryScore: Float,

    val successfulUseCount: Int,
    val supportedUseCount: Int,
    val challengeCount: Int,
    val skipCount: Int,

    val lastChallengedAt: Long?,
    val lastPractisedAt: Long?,
    val nextReviewAt: Long?,

    val isEnabled: Boolean,
    val isBuiltIn: Boolean,

    val createdAt: Long,
    val updatedAt: Long
)