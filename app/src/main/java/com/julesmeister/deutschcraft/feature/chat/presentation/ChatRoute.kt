package com.julesmeister.deutschcraft.feature.chat.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun ChatRoute(
    onBackClick: () -> Unit,

    viewModel:
    ChatViewModel =
        hiltViewModel()
) {
    val uiState by
    viewModel
        .uiState
        .collectAsStateWithLifecycle()

    val missionGuidanceUiState by
    viewModel
        .missionGuidanceUiState
        .collectAsStateWithLifecycle()

    val missionResultUiState by
    viewModel
        .missionResultUiState
        .collectAsStateWithLifecycle()

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
         * ===================================================
         * BESSER
         * ===================================================
         */

        onBesserImprove =
            viewModel::improveBesserChallenge,

        onBesserHint =
            viewModel::revealNextBesserHint,

        onBesserSkip =
            viewModel::skipBesserChallenge,

        /*
         * ===================================================
         * MISSION GUIDANCE
         * ===================================================
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
         * ===================================================
         * MISSION RESULT
         * ===================================================
         */

        onDismissMissionResult =
            viewModel::dismissMissionResult,

        onContinueAfterMissionResult =
            viewModel::continueAfterMissionResult,

        /*
         * ===================================================
         * MARKED PHRASES
         * ===================================================
         */

        onMarkPhrase =
            viewModel::markPhrase,

        onRemoveMarkedPhrase =
            viewModel::removeMarkedPhrase
    )
}