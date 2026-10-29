package com.julesmeister.deutschcraft.feature.besser.domain.session

data class BesserChallengeSessionState(
    val challengeId: String,
    val messageId: String,
    val capabilityId: String,

    /*
     * Original/current learner sentence being refined.
     */
    val originalText: String,

    /*
     * Progressive assistance generated for this challenge.
     */
    val hints: List<String> =
        emptyList(),

    val revealedHintCount: Int =
        0,

    /*
     * Current editable composer content.
     */
    val refinementText: String =
        "",

    /*
     * Immutable snapshot submitted for evaluation.
     */
    val submittedText: String? =
        null,

    /*
     * Learner-facing feedback or technical error text.
     */
    val feedback: String? =
        null,

    val evaluationScore: Int? =
        null,

    val status: BesserChallengeSessionStatus =
        BesserChallengeSessionStatus.AVAILABLE
) {

    val revealedHints: List<String>
        get() =
            hints.take(
                revealedHintCount.coerceIn(
                    minimumValue =
                        0,
                    maximumValue =
                        hints.size
                )
            )

    val canRevealAnotherHint: Boolean
        get() =
            status.canRequestHint &&
                    revealedHintCount <
                    hints.size

    val isRefinementActive: Boolean
        get() =
            status ==
                    BesserChallengeSessionStatus.REFINING ||
                    status ==
                    BesserChallengeSessionStatus.NEEDS_WORK ||
                    status ==
                    BesserChallengeSessionStatus.EVALUATING

    val isTerminal: Boolean
        get() =
            status ==
                    BesserChallengeSessionStatus.COMPLETED ||
                    status ==
                    BesserChallengeSessionStatus.SKIPPED ||
                    status ==
                    BesserChallengeSessionStatus.CLOSED
}

enum class BesserChallengeSessionStatus {
    AVAILABLE,
    REFINING,
    EVALUATING,
    NEEDS_WORK,
    COMPLETED,
    SKIPPED,
    CLOSED;

    val canRequestHint: Boolean
        get() =
            this ==
                    AVAILABLE ||
                    this ==
                    REFINING ||
                    this ==
                    NEEDS_WORK
}