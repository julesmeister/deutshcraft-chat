package com.julesmeister.deutschcraft.feature.besser.domain.session

data class BesserRefinementDraft(
    /*
     * Challenge currently controlling the composer.
     */
    val challengeId: String,

    /*
     * Message being improved.
     */
    val messageId: String,

    /*
     * Original learner sentence before refinement began.
     *
     * The UI may use this in the refinement header, but the
     * final visible message should be replaced by the improved
     * sentence after successful submission.
     */
    val originalText: String,

    /*
     * Current composer content.
     */
    val draftText: String,

    /*
     * Timestamp at which refinement mode began.
     */
    val startedAt: Long
) {

    val hasChanges: Boolean
        get() =
            draftText
                .trim() !=
                    originalText.trim()

    val canSubmit: Boolean
        get() =
            draftText.isNotBlank() &&
                    hasChanges

    fun updateText(
        value: String
    ): BesserRefinementDraft {
        return copy(
            draftText =
                value
        )
    }
}