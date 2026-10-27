package com.julesmeister.deutschcraft.feature.chat.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

/*
 * Navigation-level entry point for the chat feature.
 *
 * ChatRoute collects ViewModel state while ChatScreen stays
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

    /*
     * messageId -> phrases the learner marked on that message.
     */
    val markedPhrases by
    viewModel
        .markedPhrases
        .collectAsStateWithLifecycle()

    ChatScreen(
        uiState =
            uiState,

        missionGuidanceUiState =
            missionGuidanceUiState,

        missionResultUiState =
            missionResultUiState,

        markedPhrases =
            markedPhrases,

        onAction =
            viewModel::onAction,

        onBackClick =
            onBackClick,

        /*
         * Mission guidance.
         */
        onOpenMissionGuidance =
            viewModel::openMissionGuidance,

        onDismissMissionGuidance =
            viewModel::dismissMissionGuidance,

        onRevealNextMissionHint =
            viewModel::revealNextMissionHint,

        onToggleMissionVocabulary =
            viewModel::toggleMissionVocabulary,

        onToggleMissionExamples =
            viewModel::toggleMissionExamples,

        /*
         * Mission result.
         */
        onDismissMissionResult =
            viewModel::dismissMissionResult,

        onContinueAfterMissionResult =
            viewModel::continueAfterMissionResult,

        /*
         * Marked phrases.
         */
        onMarkPhrase =
            viewModel::markPhrase,

        onRemoveMarkedPhrase =
            viewModel::removeMarkedPhrase
    )
}