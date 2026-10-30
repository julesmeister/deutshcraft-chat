// app/src/main/java/com/julesmeister/deutschcraft/feature/progress/presentation/ProgressScreen.kt

package com.julesmeister.deutschcraft.feature.progress.presentation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Insights
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.julesmeister.deutschcraft.feature.settings.domain.ProgressLanguage
import com.julesmeister.deutschcraft.ui.theme.DeutschLime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgressScreen(
    uiState:
    ProgressUiState,

    progressLanguage:
    ProgressLanguage,

    onProgressLanguageChanged:
        (ProgressLanguage) -> Unit,

    onBackClick:
        () -> Unit,

    modifier:
    Modifier =
        Modifier
) {
    val showEnglish =
        progressLanguage ==
                ProgressLanguage.ENGLISH

    Scaffold(
        modifier =
            modifier.fillMaxSize(),

        containerColor =
            MaterialTheme
                .colorScheme
                .background,

        topBar = {
            ProgressTopBar(
                onBackClick =
                    onBackClick,

                showEnglish =
                    showEnglish,

                onLanguageToggle = {
                    val newLanguage =
                        if (showEnglish) {
                            ProgressLanguage.GERMAN
                        } else {
                            ProgressLanguage.ENGLISH
                        }

                    onProgressLanguageChanged(
                        newLanguage
                    )
                }
            )
        }
    ) { innerPadding ->
        when {
            uiState.isLoading -> {
                ProgressLoadingContent(
                    showEnglish =
                        showEnglish,

                    modifier =
                        Modifier
                            .fillMaxSize()
                            .padding(
                                innerPadding
                            )
                )
            }

            uiState.progress !=
                    null -> {
                ProgressContent(
                    progress =
                        uiState.progress,

                    besserProgress =
                        uiState.besserProgress
                            ?: BesserProgressUiModel(),

                    learningFocuses =
                        uiState.prioritizedLearningFocuses,

                    reinforcementFocusCount =
                        uiState.reinforcementFocusCount,

                    improvingFocusCount =
                        uiState.improvingFocusCount,

                    masteredFocusCount =
                        uiState.masteredFocusCount,

                    completedMissionCount =
                        uiState.completedMissionCount,

                    recentCompletedMissions =
                        uiState.recentCompletedMissions,

                    showEnglish =
                        showEnglish,

                    modifier =
                        Modifier
                            .fillMaxSize()
                            .padding(
                                innerPadding
                            )
                )
            }

            else -> {
                ProgressEmptyContent(
                    message =
                        uiState.errorMessage
                            ?: if (showEnglish) {
                                ProgressDefaultEmptyMessageEnglish
                            } else {
                                ProgressDefaultEmptyMessageGerman
                            },

                    showEnglish =
                        showEnglish,

                    modifier =
                        Modifier
                            .fillMaxSize()
                            .padding(
                                innerPadding
                            )
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProgressTopBar(
    onBackClick:
        () -> Unit,

    showEnglish: Boolean,

    onLanguageToggle:
        () -> Unit
) {
    TopAppBar(
        title = {
            AnimatedContent(
                targetState =
                    showEnglish,

                transitionSpec = {
                    fadeIn(
                        animationSpec =
                            tween(
                                durationMillis =
                                    ProgressLanguageAnimationDurationMillis
                            )
                    ) togetherWith
                            fadeOut(
                                animationSpec =
                                    tween(
                                        durationMillis =
                                            ProgressLanguageAnimationDurationMillis
                                    )
                            )
                },

                label =
                    "ProgressTitleLanguage"
            ) { english ->
                Text(
                    text =
                        if (english) {
                            "Your progress"
                        } else {
                            "Dein Fortschritt"
                        },

                    modifier =
                        Modifier.padding(
                            start =
                                ProgressTopBarTitleStartPadding
                        ),

                    color =
                        MaterialTheme
                            .colorScheme
                            .onBackground,

                    fontWeight =
                        FontWeight.SemiBold
                )
            }
        },

        navigationIcon = {
            IconButton(
                onClick =
                    onBackClick,

                modifier =
                    Modifier
                        .padding(
                            start =
                                ProgressTopBarEdgePadding
                        )
                        .size(
                            ProgressTopBarButtonSize
                        ),

                shape =
                    CircleShape,

                colors =
                    IconButtonDefaults.iconButtonColors(
                        containerColor =
                            MaterialTheme
                                .colorScheme
                                .surfaceContainer,

                        contentColor =
                            MaterialTheme
                                .colorScheme
                                .onSurface
                    )
            ) {
                Icon(
                    imageVector =
                        Icons
                            .AutoMirrored
                            .Outlined
                            .ArrowBack,

                    contentDescription =
                        if (showEnglish) {
                            "Back"
                        } else {
                            "Zurück"
                        }
                )
            }
        },

        actions = {
            ProgressLanguageButton(
                showEnglish =
                    showEnglish,

                onClick =
                    onLanguageToggle
            )
        },

        colors =
            TopAppBarDefaults.topAppBarColors(
                containerColor =
                    MaterialTheme
                        .colorScheme
                        .background,

                scrolledContainerColor =
                    MaterialTheme
                        .colorScheme
                        .background,

                titleContentColor =
                    MaterialTheme
                        .colorScheme
                        .onBackground,

                navigationIconContentColor =
                    MaterialTheme
                        .colorScheme
                        .onBackground,

                actionIconContentColor =
                    MaterialTheme
                        .colorScheme
                        .onBackground
            )
    )
}

@Composable
private fun ProgressLanguageButton(
    showEnglish: Boolean,
    onClick: () -> Unit
) {
    IconButton(
        onClick =
            onClick,

        modifier =
            Modifier
                .padding(
                    end =
                        ProgressTopBarEdgePadding
                )
                .size(
                    ProgressTopBarButtonSize
                ),

        shape =
            CircleShape,

        colors =
            IconButtonDefaults.iconButtonColors(
                containerColor =
                    DeutschLime.copy(
                        alpha =
                            ProgressLanguageButtonAlpha
                    ),

                contentColor =
                    MaterialTheme
                        .colorScheme
                        .primary
            )
    ) {
        AnimatedContent(
            targetState =
                showEnglish,

            transitionSpec = {
                fadeIn(
                    animationSpec =
                        tween(
                            durationMillis =
                                ProgressLanguageAnimationDurationMillis
                        )
                ) togetherWith
                        fadeOut(
                            animationSpec =
                                tween(
                                    durationMillis =
                                        ProgressLanguageAnimationDurationMillis
                                )
                        )
            },

            label =
                "ProgressLanguageButton"
        ) { english ->
            Text(
                text =
                    if (english) {
                        "DE"
                    } else {
                        "EN"
                    },

                style =
                    MaterialTheme
                        .typography
                        .labelMedium,

                fontWeight =
                    FontWeight.Bold
            )
        }
    }
}

@Composable
private fun ProgressLoadingContent(
    showEnglish: Boolean,

    modifier:
    Modifier =
        Modifier
) {
    Column(
        modifier =
            modifier,

        horizontalAlignment =
            Alignment.CenterHorizontally,

        verticalArrangement =
            Arrangement.Center
    ) {
        CircularProgressIndicator(
            color =
                MaterialTheme
                    .colorScheme
                    .primary
        )

        Text(
            text =
                if (showEnglish) {
                    "Loading progress …"
                } else {
                    "Fortschritt wird geladen …"
                },

            modifier =
                Modifier.padding(
                    top =
                        ProgressLoadingTextTopPadding
                ),

            color =
                MaterialTheme
                    .colorScheme
                    .onSurfaceVariant,

            style =
                MaterialTheme
                    .typography
                    .bodyMedium
        )
    }
}

@Composable
private fun ProgressEmptyContent(
    message: String,

    showEnglish: Boolean,

    modifier:
    Modifier =
        Modifier
) {
    Column(
        modifier =
            modifier.padding(
                ProgressEmptyContentPadding
            ),

        horizontalAlignment =
            Alignment.CenterHorizontally,

        verticalArrangement =
            Arrangement.Center
    ) {
        Box(
            modifier =
                Modifier
                    .size(
                        ProgressEmptyIconContainerSize
                    )
                    .clip(
                        CircleShape
                    )
                    .background(
                        MaterialTheme
                            .colorScheme
                            .surfaceContainer
                    ),

            contentAlignment =
                Alignment.Center
        ) {
            Icon(
                imageVector =
                    Icons
                        .Outlined
                        .Insights,

                contentDescription =
                    null,

                tint =
                    MaterialTheme
                        .colorScheme
                        .onSurfaceVariant
            )
        }

        Text(
            text =
                if (showEnglish) {
                    "No CEFR data yet"
                } else {
                    "Noch keine CEFR-Daten"
                },

            modifier =
                Modifier.padding(
                    top =
                        ProgressEmptyTitleTopPadding
                ),

            style =
                MaterialTheme
                    .typography
                    .titleMedium,

            fontWeight =
                FontWeight.SemiBold,

            color =
                MaterialTheme
                    .colorScheme
                    .onBackground,

            textAlign =
                TextAlign.Center
        )

        Text(
            text =
                message,

            modifier =
                Modifier.padding(
                    top =
                        ProgressEmptyMessageTopPadding
                ),

            style =
                MaterialTheme
                    .typography
                    .bodyMedium,

            color =
                MaterialTheme
                    .colorScheme
                    .onSurfaceVariant,

            textAlign =
                TextAlign.Center
        )
    }
}