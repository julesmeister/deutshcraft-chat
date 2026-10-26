package com.julesmeister.deutschcraft.feature.chat.presentation

import com.julesmeister.deutschcraft.feature.mission.domain.ConversationMission
import com.julesmeister.deutschcraft.feature.mission.domain.MissionObjectiveProgress
import javax.inject.Inject

class ChatMissionTracker @Inject constructor() {

    /*
     * null means the first Room mission emission has not
     * been received yet.
     *
     * The first emission establishes the baseline so old
     * completions do not animate when reopening a chat.
     */
    private var knownCompletedObjectiveIds:
            Set<String>? =
        null

    private var observedMissionId: String? =
        null

    fun createUpdate(
        mission: ConversationMission?
    ): ChatMissionUpdate {
        if (mission == null) {
            reset()

            return ChatMissionUpdate(
                mission =
                    null,
                newlyCompletedObjective =
                    null
            )
        }

        ensureMissionBaselineMatches(
            missionId =
                mission.id
        )

        val completedProgress =
            mission.objectiveProgress
                .filter { progress ->
                    progress.isCompleted
                }

        val currentCompletedIds =
            completedProgress
                .map { progress ->
                    progress.objective.id
                }
                .toSet()

        val previousCompletedIds =
            knownCompletedObjectiveIds

        if (previousCompletedIds == null) {
            knownCompletedObjectiveIds =
                currentCompletedIds

            return ChatMissionUpdate(
                mission =
                    mission,
                newlyCompletedObjective =
                    null
            )
        }

        val newlyCompletedIds =
            currentCompletedIds -
                    previousCompletedIds

        val newestCompletion =
            completedProgress
                .asSequence()
                .filter { progress ->
                    progress.objective.id in
                            newlyCompletedIds
                }
                .maxByOrNull { progress ->
                    progress.completedAt
                        ?: Long.MIN_VALUE
                }

        knownCompletedObjectiveIds =
            currentCompletedIds

        return ChatMissionUpdate(
            mission =
                mission,
            newlyCompletedObjective =
                newestCompletion
        )
    }

    fun reset() {
        knownCompletedObjectiveIds =
            null

        observedMissionId =
            null
    }

    private fun ensureMissionBaselineMatches(
        missionId: String
    ) {
        if (observedMissionId == missionId) {
            return
        }

        observedMissionId =
            missionId

        knownCompletedObjectiveIds =
            null
    }
}

data class ChatMissionUpdate(
    val mission: ConversationMission?,
    val newlyCompletedObjective:
    MissionObjectiveProgress?
)