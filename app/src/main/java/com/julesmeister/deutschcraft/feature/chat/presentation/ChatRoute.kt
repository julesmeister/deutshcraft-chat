package com.julesmeister.deutschcraft.feature.chat.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.julesmeister.deutschcraft.feature.chat.presentation.theme.LocalChatFontScale
import com.julesmeister.deutschcraft.feature.settings.domain.UserSettings

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

    /*
     * SettingsRepository.settings is a Flow rather than a
     * StateFlow.
     *
     * UserSettings() supplies the normal 1.00 font scale until
     * DataStore emits the persisted preference.
     */
    val settings by
    viewModel
        .settings
        .collectAsStateWithLifecycle(
            initialValue =
                UserSettings()
        )

    /*
     * Provide the persisted font scale once around the entire
     * chat presentation tree.
     *
     * All nested chat components can read LocalChatFontScale
     * without receiving another function parameter.
     */
    CompositionLocalProvider(
        LocalChatFontScale provides
                settings.fontScale
    ) {
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
             * ===============================================
             * BESSER
             * ===============================================
             */

            onBesserImprove =
                viewModel::improveBesserChallenge,

            onBesserHint =
                viewModel::revealNextBesserHint,

            onBesserSkip =
                viewModel::skipBesserChallenge,

            /*
             * ===============================================
             * MISSION GUIDANCE
             * ===============================================
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
             * ===============================================
             * MISSION RESULT
             * ===============================================
             */

            onDismissMissionResult =
                viewModel::dismissMissionResult,

            onContinueAfterMissionResult =
                viewModel::continueAfterMissionResult,

            /*
             * ===============================================
             * MARKED PHRASES
             * ===============================================
             */

            onMarkPhrase =
                viewModel::markPhrase,

            onRemoveMarkedPhrase =
                viewModel::removeMarkedPhrase
        )
    }
}