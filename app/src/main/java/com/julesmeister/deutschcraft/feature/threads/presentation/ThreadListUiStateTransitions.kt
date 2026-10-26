package com.julesmeister.deutschcraft.feature.threads.presentation

import com.julesmeister.deutschcraft.feature.mission.domain.recommendation.MissionThreadRecommendation
import com.julesmeister.deutschcraft.feature.threads.domain.ConversationThread

internal fun ThreadListUiState.withThreadLists(
    sections:
    List<com.julesmeister.deutschcraft
    .feature.threads.domain.ThreadSection>,
    archivedThreads:
    List<ConversationThread>
): ThreadListUiState {
    return copy(
        sections =
            sections,
        archivedThreads =
            archivedThreads,
        isLoading =
            false
    )
}

internal fun ThreadListUiState.threadLoadingFailed(
    message: String
): ThreadListUiState {
    return copy(
        isLoading =
            false,
        errorMessage =
            message
    )
}

internal fun ThreadListUiState.threadCreationStarted():
        ThreadListUiState {
    return copy(
        isCreatingThread =
            true,
        errorMessage =
            null
    )
}

internal fun ThreadListUiState.threadCreationFinished():
        ThreadListUiState {
    return copy(
        isCreatingThread =
            false
    )
}

internal fun ThreadListUiState.recommendationLoadingStarted():
        ThreadListUiState {
    return copy(
        isLoadingMissionRecommendation =
            true
    )
}

internal fun ThreadListUiState.recommendationLoadingFinished(
    recommendation:
    MissionThreadRecommendation?
): ThreadListUiState {
    return copy(
        missionRecommendation =
            recommendation,
        isLoadingMissionRecommendation =
            false
    )
}

internal fun ThreadListUiState.recommendationLoadingFailed():
        ThreadListUiState {
    return copy(
        isLoadingMissionRecommendation =
            false
    )
}

internal fun ThreadListUiState.recommendationRestored(
    recommendation:
    MissionThreadRecommendation?
): ThreadListUiState {
    return copy(
        missionRecommendation =
            recommendation
    )
}

internal fun ThreadListUiState.recommendedMissionStarted():
        ThreadListUiState {
    return copy(
        isStartingRecommendedMission =
            true,
        errorMessage =
            null
    )
}

internal fun ThreadListUiState.recommendedMissionSucceeded():
        ThreadListUiState {
    return copy(
        missionRecommendation =
            null,
        isStartingRecommendedMission =
            false
    )
}

internal fun ThreadListUiState.recommendedMissionFailed(
    message: String
): ThreadListUiState {
    return copy(
        isStartingRecommendedMission =
            false,
        errorMessage =
            message
    )
}

internal fun ThreadListUiState.withError(
    message: String
): ThreadListUiState {
    return copy(
        errorMessage =
            message
    )
}

internal fun ThreadListUiState.withoutError():
        ThreadListUiState {
    return copy(
        errorMessage =
            null
    )
}