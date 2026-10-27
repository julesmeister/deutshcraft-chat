package com.julesmeister.deutschcraft.feature.chat.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

/*
 * Navigation-level entry point for the chat feature.
 *
 * ChatRoute owns ViewModel collection while ChatScreen remains
 * a stateless presentation component.
 */
@Composable
fun ChatRoute(
    onBackClick:
        () -> Unit,
    viewModel:
    ChatViewModel =
        hiltViewModel()
) {
    /*
     * Main conversation state.
     */
    val uiState by
    viewModel
        .uiState
        .collectAsStateWithLifecycle()

    /*
     * Active-mission learner support.
     */
    val missionGuidanceUiState by
    viewModel
        .missionGuidanceUiState
        .collectAsStateWithLifecycle()

    /*
     * Completed-mission analytics.
     */
    val missionResultUiState by
    viewModel
        .missionResultUiState
        .collectAsStateWithLifecycle()

    ChatScreen(
        uiState =
            uiState,

        missionGuidanceUiState =
            missionGuidanceUiState,

        missionResultUiState =
            missionResultUiState,

        onAction =
            viewModel::onAction,

        onBackClick =
            onBackClick,

        /*
         * Mission top-bar tap.
         */
        onOpenMissionGuidance =
            viewModel::openMissionGuidance,

        /*
         * Mission Guidance sheet.
         */
        onDismissMissionGuidance =
            viewModel::dismissMissionGuidance,

        onRevealNextMissionHint =
            viewModel::revealNextMissionHint,

        onToggleMissionVocabulary =
            viewModel::toggleMissionVocabulary,

        onToggleMissionExamples =
            viewModel::toggleMissionExamples,

        /*
         * Mission Result sheet.
         */
        onDismissMissionResult =
            viewModel::dismissMissionResult,

        onContinueAfterMissionResult =
            viewModel::continueAfterMissionResult
    )
}