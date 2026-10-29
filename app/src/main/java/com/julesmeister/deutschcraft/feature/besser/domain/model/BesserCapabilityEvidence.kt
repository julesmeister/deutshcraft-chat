package com.julesmeister.deutschcraft.feature.besser.domain.model

data class BesserCapabilityEvidence(
    val id: String,
    val capabilityId: String,
    val messageId: String,
    val challengeId: String?,

    val assistanceLevel: Int,

    val wasChallengeAccepted: Boolean,
    val wasSuccessfullyDemonstrated: Boolean,
    val wasSkipped: Boolean,

    val scoreBefore: Int?,
    val scoreAfter: Int?,

    /*
     * Keep the learning history lightweight.
     *
     * We do not need to duplicate the full original and refined
     * messages because the owning ChatMessage already contains
     * the final visible conversation text.
     */
    val evidenceSummary: String?,

    val createdAt: Long
)