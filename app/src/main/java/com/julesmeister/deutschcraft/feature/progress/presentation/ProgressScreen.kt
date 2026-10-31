// app/src/main/java/com/julesmeister/deutschcraft/feature/progress/presentation/ProgressScreen.kt

package com.julesmeister.deutschcraft.feature.progress.presentation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.julesmeister.deutschcraft.feature.settings.domain.ProgressLanguage
import com.julesmeister.deutschcraft.ui.theme.DeutschLime
import com.julesmeister.deutschcraft.ui.theme.DeutschTextOnLime

@OptIn(
    ExperimentalMaterial3Api::class
)
@Composable
fun ProgressScreen(
    uiState: ProgressUiState,
    progressLanguage: ProgressLanguage,
    onProgressLanguageChanged: (ProgressLanguage) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
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

            uiState.progress != null -> {
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

@OptIn(
    ExperimentalMaterial3Api::class
)
@Composable
private fun ProgressTopBar(
    onBackClick: () -> Unit,
    showEnglish: Boolean,
    onLanguageToggle: () -> Unit
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
            ProgressBackButton(
                onClick =
                    onBackClick,

                contentDescription =
                    if (showEnglish) {
                        "Back"
                    } else {
                        "Zurück"
                    },

                modifier =
                    Modifier.padding(
                        start =
                            ProgressTopBarEdgePadding
                    )
            )
        },

        actions = {
            ProgressLanguageToggle(
                showEnglish =
                    showEnglish,

                onClick =
                    onLanguageToggle,

                modifier =
                    Modifier.padding(
                        end =
                            ProgressTopBarEdgePadding
                    )
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
private fun ProgressBackButton(
    onClick: () -> Unit,
    contentDescription: String,
    modifier: Modifier = Modifier
) {
    val colors =
        MaterialTheme.colorScheme

    val isLightTheme =
        colors.background.luminance() >
                LIGHT_THEME_LUMINANCE_THRESHOLD

    IconButton(
        onClick =
            onClick,

        modifier =
            modifier.size(
                ProgressTopBarButtonSize
            ),

        shape =
            CircleShape,

        colors =
            IconButtonDefaults.iconButtonColors(
                containerColor =
                    if (isLightTheme) {
                        colors.surfaceContainerHigh
                    } else {
                        colors.surfaceContainer
                    },

                contentColor =
                    colors.onSurface
            )
    ) {
        Icon(
            imageVector =
                Icons
                    .AutoMirrored
                    .Outlined
                    .ArrowBack,

            contentDescription =
                contentDescription,

            modifier =
                Modifier.size(
                    PROGRESS_BACK_ICON_SIZE
                )
        )
    }
}

@Composable
private fun ProgressLanguageToggle(
    showEnglish: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors =
        MaterialTheme.colorScheme

    val isLightTheme =
        colors.background.luminance() >
                LIGHT_THEME_LUMINANCE_THRESHOLD

    val containerColor =
        if (isLightTheme) {
            LIGHT_LANGUAGE_TOGGLE_BACKGROUND
        } else {
            colors.surfaceContainerHigh
        }

    val inactiveTextColor =
        if (isLightTheme) {
            LIGHT_LANGUAGE_TOGGLE_INACTIVE_TEXT
        } else {
            colors.onSurfaceVariant
        }

    val toggleScale by
    animateFloatAsState(
        targetValue =
            1f,

        animationSpec =
            tween(
                durationMillis =
                    LANGUAGE_TOGGLE_SCALE_ANIMATION_MILLIS,

                easing =
                    FastOutSlowInEasing
            ),

        label =
            "ProgressLanguageToggleScale"
    )

    Row(
        modifier =
            modifier
                .graphicsLayer {
                    scaleX =
                        toggleScale

                    scaleY =
                        toggleScale
                }
                .width(
                    LANGUAGE_TOGGLE_WIDTH
                )
                .height(
                    LANGUAGE_TOGGLE_HEIGHT
                )
                .clip(
                    LANGUAGE_TOGGLE_SHAPE
                )
                .background(
                    containerColor
                )
                .clickable(
                    interactionSource =
                        remember {
                            MutableInteractionSource()
                        },

                    indication =
                        null,

                    role =
                        Role.Button,

                    onClick =
                        onClick
                )
                .semantics {
                    role =
                        Role.Button

                    contentDescription =
                        if (showEnglish) {
                            "Switch progress language to German"
                        } else {
                            "Fortschrittssprache auf Englisch umstellen"
                        }
                }
                .padding(
                    LANGUAGE_TOGGLE_INNER_PADDING
                ),

        verticalAlignment =
            Alignment.CenterVertically,

        horizontalArrangement =
            Arrangement.spacedBy(
                LANGUAGE_OPTION_SPACING
            )
    ) {
        ProgressLanguageOption(
            label =
                "EN",

            selected =
                showEnglish,

            selectedContainerColor =
                DeutschLime,

            selectedContentColor =
                DeutschTextOnLime,

            inactiveContentColor =
                inactiveTextColor,

            modifier =
                Modifier.weight(
                    1f
                )
        )

        ProgressLanguageOption(
            label =
                "DE",

            selected =
                !showEnglish,

            selectedContainerColor =
                DeutschLime,

            selectedContentColor =
                DeutschTextOnLime,

            inactiveContentColor =
                inactiveTextColor,

            modifier =
                Modifier.weight(
                    1f
                )
        )
    }
}

@Composable
private fun ProgressLanguageOption(
    label: String,
    selected: Boolean,
    selectedContainerColor: Color,
    selectedContentColor: Color,
    inactiveContentColor: Color,
    modifier: Modifier = Modifier
) {
    val selectedProgress by
    animateFloatAsState(
        targetValue =
            if (selected) {
                1f
            } else {
                0f
            },

        animationSpec =
            tween(
                durationMillis =
                    ProgressLanguageAnimationDurationMillis,

                easing =
                    FastOutSlowInEasing
            ),

        label =
            "ProgressLanguageOption_$label"
    )

    Box(
        modifier =
            modifier
                .height(
                    LANGUAGE_OPTION_HEIGHT
                )
                .clip(
                    LANGUAGE_OPTION_SHAPE
                )
                .background(
                    selectedContainerColor.copy(
                        alpha =
                            selectedProgress
                    )
                ),

        contentAlignment =
            Alignment.Center
    ) {
        Text(
            text =
                label,

            style =
                MaterialTheme
                    .typography
                    .labelMedium,

            fontWeight =
                if (selected) {
                    FontWeight.ExtraBold
                } else {
                    FontWeight.SemiBold
                },

            color =
                if (selected) {
                    selectedContentColor
                } else {
                    inactiveContentColor
                }
        )
    }
}

@Composable
private fun ProgressLoadingContent(
    showEnglish: Boolean,
    modifier: Modifier = Modifier
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
    modifier: Modifier = Modifier
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

private val PROGRESS_BACK_ICON_SIZE =
    22.dp

private val LANGUAGE_TOGGLE_WIDTH =
    90.dp

private val LANGUAGE_TOGGLE_HEIGHT =
    40.dp

private val LANGUAGE_TOGGLE_INNER_PADDING =
    4.dp

private val LANGUAGE_OPTION_HEIGHT =
    32.dp

private val LANGUAGE_OPTION_SPACING =
    2.dp

private val LANGUAGE_TOGGLE_SHAPE =
    RoundedCornerShape(
        percent =
            50
    )

private val LANGUAGE_OPTION_SHAPE =
    RoundedCornerShape(
        percent =
            50
    )

private val LIGHT_LANGUAGE_TOGGLE_BACKGROUND =
    Color(
        0xFFE2E4DE
    )

private val LIGHT_LANGUAGE_TOGGLE_INACTIVE_TEXT =
    Color(
        0xFF4E5249
    )

private const val LIGHT_THEME_LUMINANCE_THRESHOLD =
    0.5f

private const val LANGUAGE_TOGGLE_SCALE_ANIMATION_MILLIS =
    130