// app/src/main/java/com/julesmeister/deutschcraft/feature/chat/presentation/ChatRoute.kt

package com.julesmeister.deutschcraft.feature.chat.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.julesmeister.deutschcraft.feature.chat.presentation.progress.ChatLevelProgressViewModel
import com.julesmeister.deutschcraft.feature.chat.presentation.progress.LocalChatLevelProgress
import com.julesmeister.deutschcraft.feature.chat.presentation.theme.LocalChatFontScale
import com.julesmeister.deutschcraft.feature.settings.domain.UserSettings

@Composable
fun ChatRoute(
    onBackClick:
        () -> Unit,

    viewModel:
    ChatViewModel =
        hiltViewModel(),

    levelProgressViewModel:
    ChatLevelProgressViewModel =
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

    val levelProgressState by
    levelProgressViewModel
        .uiState
        .collectAsStateWithLifecycle()

    val settings by
    viewModel
        .settings
        .collectAsStateWithLifecycle(
            initialValue =
                UserSettings()
        )

    CompositionLocalProvider(
        LocalChatFontScale provides
                settings.fontScale,

        LocalChatLevelProgress provides
                levelProgressState.withAnimationPreference(
                    reduceAnimations =
                        settings.reduceAnimations
                )
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

            onBesserImprove =
                viewModel::improveBesserChallenge,

            onBesserHint =
                viewModel::revealNextBesserHint,

            onBesserSkip =
                viewModel::skipBesserChallenge,

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

            onDismissMissionResult =
                viewModel::dismissMissionResult,

            onContinueAfterMissionResult =
                viewModel::continueAfterMissionResult,

            onMarkPhrase =
                viewModel::markPhrase,

            onRemoveMarkedPhrase =
                viewModel::removeMarkedPhrase
        )
    }
}