package com.julesmeister.deutschcraft.feature.chat.presentation.component

internal sealed interface ChatTopBarStatus {

    data object Normal :
        ChatTopBarStatus

    data object AssistantTyping :
        ChatTopBarStatus

    data object Assessing :
        ChatTopBarStatus

    data class MissionProgress(
        val title: String,
        val percentage: Int
    ) : ChatTopBarStatus

    /*
     * Represents the learner's current mission objective.
     *
     * objective:
     * A short statement describing what should be completed.
     *
     * guidance:
     * A learner-facing explanation of what to say or discuss
     * next. This must not contain the internal AI completion
     * criteria.
     */
    data class MissionObjective(
        val objective: String,
        val guidance: String?
    ) : ChatTopBarStatus

    data class ObjectiveCompleted(
        val eventId: String,
        val objectiveTitle: String
    ) : ChatTopBarStatus
}