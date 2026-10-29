package com.julesmeister.deutschcraft.feature.besser.domain.session

sealed interface BesserChallengeSessionAction {

    data object ImproveRequested :
        BesserChallengeSessionAction

    data object HintRequested :
        BesserChallengeSessionAction

    data object SkipRequested :
        BesserChallengeSessionAction

    data object RefinementCancelled :
        BesserChallengeSessionAction

    data class RefinementSubmitted(
        val text: String
    ) : BesserChallengeSessionAction

    data object EvaluationStarted :
        BesserChallengeSessionAction

    data class RefinementSucceeded(
        val finalText: String,
        val score: Int? = null
    ) : BesserChallengeSessionAction

    data class RefinementNeedsWork(
        val feedback: String?,
        val score: Int? = null
    ) : BesserChallengeSessionAction

    data class EvaluationFailed(
        val message: String?
    ) : BesserChallengeSessionAction

    data object FeedbackDismissed :
        BesserChallengeSessionAction

    data object SessionClosed :
        BesserChallengeSessionAction
}