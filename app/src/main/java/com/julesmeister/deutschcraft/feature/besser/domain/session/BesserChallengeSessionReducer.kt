package com.julesmeister.deutschcraft.feature.besser.domain.session

/*
 * Pure state transition logic for one contextual Besser
 * Challenge.
 *
 * This file deliberately performs no Compose, Room, Qwen,
 * repository, or coroutine work.
 */
object BesserChallengeSessionReducer {

    fun reduce(
        state: BesserChallengeSessionState,
        action: BesserChallengeSessionAction
    ): BesserChallengeSessionState {
        return when (action) {
            BesserChallengeSessionAction.ImproveRequested -> {
                reduceImproveRequested(
                    state =
                        state
                )
            }

            BesserChallengeSessionAction.HintRequested -> {
                reduceHintRequested(
                    state =
                        state
                )
            }

            BesserChallengeSessionAction.SkipRequested -> {
                reduceSkipRequested(
                    state =
                        state
                )
            }

            BesserChallengeSessionAction.RefinementCancelled -> {
                reduceRefinementCancelled(
                    state =
                        state
                )
            }

            is BesserChallengeSessionAction.RefinementSubmitted -> {
                reduceRefinementSubmitted(
                    state =
                        state,
                    submittedText =
                        action.text
                )
            }

            BesserChallengeSessionAction.EvaluationStarted -> {
                reduceEvaluationStarted(
                    state =
                        state
                )
            }

            is BesserChallengeSessionAction.RefinementSucceeded -> {
                reduceRefinementSucceeded(
                    state =
                        state,
                    finalText =
                        action.finalText,
                    score =
                        action.score
                )
            }

            is BesserChallengeSessionAction.RefinementNeedsWork -> {
                reduceRefinementNeedsWork(
                    state =
                        state,
                    feedback =
                        action.feedback,
                    score =
                        action.score
                )
            }

            is BesserChallengeSessionAction.EvaluationFailed -> {
                reduceEvaluationFailed(
                    state =
                        state,
                    message =
                        action.message
                )
            }

            BesserChallengeSessionAction.FeedbackDismissed -> {
                state.copy(
                    feedback =
                        null
                )
            }

            BesserChallengeSessionAction.SessionClosed -> {
                reduceSessionClosed(
                    state =
                        state
                )
            }
        }
    }

    private fun reduceImproveRequested(
        state: BesserChallengeSessionState
    ): BesserChallengeSessionState {
        if (
            state.status !=
            BesserChallengeSessionStatus.AVAILABLE
        ) {
            return state
        }

        return state.copy(
            status =
                BesserChallengeSessionStatus.REFINING,
            refinementText =
                state.submittedText
                    ?.trim()
                    ?.takeIf(
                        String::isNotBlank
                    )
                    ?: state.originalText,
            submittedText =
                null,
            feedback =
                null,
            evaluationScore =
                null
        )
    }

    private fun reduceHintRequested(
        state: BesserChallengeSessionState
    ): BesserChallengeSessionState {
        if (!state.status.canRevealHint()) {
            return state
        }

        if (
            state.revealedHintCount >=
            state.hints.size
        ) {
            return state
        }

        return state.copy(
            revealedHintCount =
                (
                        state.revealedHintCount +
                                1
                        )
                    .coerceAtMost(
                        state.hints.size
                    ),
            feedback =
                null
        )
    }

    private fun reduceSkipRequested(
        state: BesserChallengeSessionState
    ): BesserChallengeSessionState {
        if (state.status.isTerminal()) {
            return state
        }

        return state.copy(
            status =
                BesserChallengeSessionStatus.SKIPPED,
            refinementText =
                "",
            submittedText =
                null,
            feedback =
                null,
            evaluationScore =
                null
        )
    }

    private fun reduceRefinementCancelled(
        state: BesserChallengeSessionState
    ): BesserChallengeSessionState {
        if (
            state.status !=
            BesserChallengeSessionStatus.REFINING &&
            state.status !=
            BesserChallengeSessionStatus.NEEDS_WORK
        ) {
            return state
        }

        return state.copy(
            status =
                BesserChallengeSessionStatus.AVAILABLE,
            refinementText =
                "",
            submittedText =
                null,
            feedback =
                null,
            evaluationScore =
                null
        )
    }

    private fun reduceRefinementSubmitted(
        state: BesserChallengeSessionState,
        submittedText: String
    ): BesserChallengeSessionState {
        if (
            state.status !=
            BesserChallengeSessionStatus.REFINING &&
            state.status !=
            BesserChallengeSessionStatus.NEEDS_WORK
        ) {
            return state
        }

        val cleanText =
            submittedText.trim()

        if (cleanText.isBlank()) {
            return state.copy(
                feedback =
                    EMPTY_REFINEMENT_FEEDBACK
            )
        }

        return state.copy(
            refinementText =
                cleanText,
            submittedText =
                cleanText,
            feedback =
                null,
            evaluationScore =
                null
        )
    }

    private fun reduceEvaluationStarted(
        state: BesserChallengeSessionState
    ): BesserChallengeSessionState {
        if (
            state.status !=
            BesserChallengeSessionStatus.REFINING &&
            state.status !=
            BesserChallengeSessionStatus.NEEDS_WORK
        ) {
            return state
        }

        val submittedText =
            state.submittedText
                ?.trim()
                ?.takeIf(
                    String::isNotBlank
                )
                ?: return state

        return state.copy(
            status =
                BesserChallengeSessionStatus.EVALUATING,
            submittedText =
                submittedText,
            feedback =
                null,
            evaluationScore =
                null
        )
    }

    private fun reduceRefinementSucceeded(
        state: BesserChallengeSessionState,
        finalText: String,
        score: Int?
    ): BesserChallengeSessionState {
        if (
            state.status !=
            BesserChallengeSessionStatus.EVALUATING
        ) {
            return state
        }

        val resolvedText =
            finalText
                .trim()
                .takeIf(
                    String::isNotBlank
                )
                ?: state.submittedText
                    ?.trim()
                    ?.takeIf(
                        String::isNotBlank
                    )
                ?: return state

        return state.copy(
            status =
                BesserChallengeSessionStatus.COMPLETED,
            refinementText =
                resolvedText,
            submittedText =
                resolvedText,
            feedback =
                null,
            evaluationScore =
                normalizeScore(
                    score =
                        score
                )
        )
    }

    private fun reduceRefinementNeedsWork(
        state: BesserChallengeSessionState,
        feedback: String?,
        score: Int?
    ): BesserChallengeSessionState {
        if (
            state.status !=
            BesserChallengeSessionStatus.EVALUATING
        ) {
            return state
        }

        val submittedText =
            state.submittedText
                ?.trim()
                .orEmpty()

        return state.copy(
            status =
                BesserChallengeSessionStatus.NEEDS_WORK,
            refinementText =
                submittedText,
            feedback =
                feedback
                    ?.trim()
                    ?.takeIf(
                        String::isNotBlank
                    )
                    ?: DEFAULT_NEEDS_WORK_FEEDBACK,
            evaluationScore =
                normalizeScore(
                    score =
                        score
                )
        )
    }

    private fun reduceEvaluationFailed(
        state: BesserChallengeSessionState,
        message: String?
    ): BesserChallengeSessionState {
        if (
            state.status !=
            BesserChallengeSessionStatus.EVALUATING
        ) {
            return state
        }

        return state.copy(
            status =
                BesserChallengeSessionStatus.REFINING,
            refinementText =
                state.submittedText
                    ?.trim()
                    ?.takeIf(
                        String::isNotBlank
                    )
                    ?: state.refinementText,
            feedback =
                message
                    ?.trim()
                    ?.takeIf(
                        String::isNotBlank
                    )
                    ?: DEFAULT_EVALUATION_ERROR,
            evaluationScore =
                null
        )
    }

    private fun reduceSessionClosed(
        state: BesserChallengeSessionState
    ): BesserChallengeSessionState {
        if (
            state.status ==
            BesserChallengeSessionStatus.CLOSED
        ) {
            return state
        }

        return state.copy(
            status =
                BesserChallengeSessionStatus.CLOSED,
            refinementText =
                "",
            submittedText =
                null,
            feedback =
                null,
            evaluationScore =
                null
        )
    }

    private fun normalizeScore(
        score: Int?
    ): Int? {
        return score
            ?.coerceIn(
                minimumValue =
                    MINIMUM_EVALUATION_SCORE,
                maximumValue =
                    MAXIMUM_EVALUATION_SCORE
            )
    }
}

private fun BesserChallengeSessionStatus.canRevealHint():
        Boolean {
    return this ==
            BesserChallengeSessionStatus.AVAILABLE ||
            this ==
            BesserChallengeSessionStatus.REFINING ||
            this ==
            BesserChallengeSessionStatus.NEEDS_WORK
}

private fun BesserChallengeSessionStatus.isTerminal():
        Boolean {
    return this ==
            BesserChallengeSessionStatus.COMPLETED ||
            this ==
            BesserChallengeSessionStatus.SKIPPED ||
            this ==
            BesserChallengeSessionStatus.CLOSED
}

private const val MINIMUM_EVALUATION_SCORE =
    0

private const val MAXIMUM_EVALUATION_SCORE =
    100

private const val EMPTY_REFINEMENT_FEEDBACK =
    "Write your improved version before submitting."

private const val DEFAULT_NEEDS_WORK_FEEDBACK =
    "Almost there. Refine your sentence and try again."

private const val DEFAULT_EVALUATION_ERROR =
    "The refinement could not be evaluated. Your draft has been kept."