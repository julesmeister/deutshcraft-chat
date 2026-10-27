package com.julesmeister.deutschcraft.feature.mission.presentation.guidance

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FormatQuote
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.Translate
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.Typography
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/*
 * Learner-facing help for the currently active mission
 * objective.
 *
 * No analytics logic lives in this Composable.
 *
 * The Composable only raises actions through callbacks.
 */
@Composable
fun MissionGuidanceCard(
    state:
    MissionGuidanceUiState,
    onRevealNextHint:
        () -> Unit,
    onToggleVocabulary:
        () -> Unit,
    onToggleExamples:
        () -> Unit,
    modifier:
    Modifier =
        Modifier
) {
    if (!state.hasActiveMission) {
        return
    }

    Card(
        modifier =
            modifier
                .fillMaxWidth(),
        shape =
            RoundedCornerShape(
                size =
                    20.dp
            ),
        colors =
            CardDefaults.cardColors(
                containerColor =
                    MaterialTheme
                        .colorScheme
                        .surfaceContainer
            )
    ) {
        Column(
            modifier =
                Modifier.padding(
                    horizontal =
                        16.dp,
                    vertical =
                        14.dp
                ),
            verticalArrangement =
                Arrangement.spacedBy(
                    space =
                        10.dp
                )
        ) {
            MissionGuidanceHeader(
                title =
                    state.missionTitle,
                progress =
                    state.safeProgressPercentage
            )

            MissionObjectiveSection(
                title =
                    state.objectiveTitle,
                description =
                    state.objectiveDescription
            )

            MissionSupportActions(
                state =
                    state,
                onRevealNextHint =
                    onRevealNextHint,
                onToggleVocabulary =
                    onToggleVocabulary,
                onToggleExamples =
                    onToggleExamples
            )

            AnimatedVisibility(
                visible =
                    state.revealedHints
                        .isNotEmpty()
            ) {
                RevealedHintsSection(
                    hints =
                        state.revealedHints
                )
            }

            AnimatedVisibility(
                visible =
                    state.isVocabularyVisible &&
                            state.targetVocabulary
                                .isNotEmpty()
            ) {
                MissionVocabularySection(
                    vocabulary =
                        state.targetVocabulary
                )
            }

            AnimatedVisibility(
                visible =
                    state.isExamplesVisible &&
                            state.examplePhrases
                                .isNotEmpty()
            ) {
                MissionExampleSection(
                    examples =
                        state.examplePhrases
                )
            }

            state.errorMessage
                ?.takeIf(
                    String::isNotBlank
                )
                ?.let { error ->
                    Text(
                        text =
                            error,
                        style =
                            MaterialTheme
                                .typography
                                .bodySmall,
                        color =
                            MaterialTheme
                                .colorScheme
                                .error
                    )
                }
        }
    }
}

@Composable
private fun MissionGuidanceHeader(
    title: String,
    progress: Int
) {
    Column(
        verticalArrangement =
            Arrangement.spacedBy(
                6.dp
            )
    ) {
        Row(
            modifier =
                Modifier.fillMaxWidth(),
            horizontalArrangement =
                Arrangement.SpaceBetween,
            verticalAlignment =
                Alignment.CenterVertically
        ) {
            Text(
                text =
                    title,
                style =
                    MaterialTheme
                        .typography
                        .titleSmall,
                fontWeight =
                    FontWeight.SemiBold,
                modifier =
                    Modifier.weight(
                        weight =
                            1f
                    )
            )

            Spacer(
                modifier =
                    Modifier.width(
                        width =
                            12.dp
                    )
            )

            Text(
                text =
                    "$progress%",
                style =
                    MaterialTheme
                        .typography
                        .labelMedium,
                color =
                    MaterialTheme
                        .colorScheme
                        .primary
            )
        }

        LinearProgressIndicator(
            progress = {
                progress
                    .coerceIn(
                        minimumValue =
                            0,
                        maximumValue =
                            100
                    )
                    .toFloat() /
                        100f
            },
            modifier =
                Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun MissionObjectiveSection(
    title: String,
    description: String
) {
    Column(
        verticalArrangement =
            Arrangement.spacedBy(
                3.dp
            )
    ) {
        Text(
            text =
                "Current objective",
            style =
                MaterialTheme
                    .typography
                    .labelSmall,
            color =
                MaterialTheme
                    .colorScheme
                    .onSurfaceVariant
        )

        Text(
            text =
                title,
            style =
                MaterialTheme
                    .typography
                    .titleMedium,
            fontWeight =
                FontWeight.SemiBold
        )

        if (description.isNotBlank()) {
            Text(
                text =
                    description,
                style =
                    MaterialTheme
                        .typography
                        .bodyMedium,
                color =
                    MaterialTheme
                        .colorScheme
                        .onSurfaceVariant
            )
        }
    }
}

@Composable
private fun MissionSupportActions(
    state:
    MissionGuidanceUiState,
    onRevealNextHint:
        () -> Unit,
    onToggleVocabulary:
        () -> Unit,
    onToggleExamples:
        () -> Unit
) {
    FlowRow(
        horizontalArrangement =
            Arrangement.spacedBy(
                space =
                    8.dp
            ),
        verticalArrangement =
            Arrangement.spacedBy(
                space =
                    8.dp
            )
    ) {
        if (
            state.hasMoreHints ||
            state.revealedHints.isNotEmpty()
        ) {
            AssistChip(
                onClick =
                    onRevealNextHint,
                enabled =
                    state.hasMoreHints &&
                            !state.isRevealingHint,
                label = {
                    if (state.isRevealingHint) {
                        Row(
                            verticalAlignment =
                                Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(
                                modifier =
                                    Modifier
                                        .width(
                                            14.dp
                                        )
                                        .height(
                                            14.dp
                                        ),
                                strokeWidth =
                                    2.dp
                            )

                            Spacer(
                                modifier =
                                    Modifier.width(
                                        6.dp
                                    )
                            )

                            Text(
                                text =
                                    "Hint"
                            )
                        }
                    } else {
                        Text(
                            text =
                                hintButtonText(
                                    state =
                                        state
                                )
                        )
                    }
                },
                leadingIcon = {
                    Icon(
                        imageVector =
                            Icons.Outlined.Lightbulb,
                        contentDescription =
                            null
                    )
                }
            )
        }

        if (
            state.targetVocabulary
                .isNotEmpty()
        ) {
            AssistChip(
                onClick =
                    onToggleVocabulary,
                label = {
                    Text(
                        text =
                            if (
                                state.isVocabularyVisible
                            ) {
                                "Hide words"
                            } else {
                                "Words"
                            }
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector =
                            Icons.Outlined.Translate,
                        contentDescription =
                            null
                    )
                }
            )
        }

        if (
            state.examplePhrases
                .isNotEmpty()
        ) {
            AssistChip(
                onClick =
                    onToggleExamples,
                label = {
                    Text(
                        text =
                            if (
                                state.isExamplesVisible
                            ) {
                                "Hide examples"
                            } else {
                                "Examples"
                            }
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector =
                            Icons.Outlined.FormatQuote,
                        contentDescription =
                            null
                    )
                }
            )
        }
    }
}

@Composable
private fun RevealedHintsSection(
    hints:
    List<String>
) {
    SupportSection(
        title =
            "Hints"
    ) {
        hints.forEachIndexed { index, hint ->
            Text(
                text =
                    "${index + 1}. $hint",
                style =
                    MaterialTheme
                        .typography
                        .bodyMedium
            )
        }
    }
}

@Composable
private fun MissionVocabularySection(
    vocabulary:
    List<String>
) {
    SupportSection(
        title =
            "Useful words"
    ) {
        vocabulary.forEach { word ->
            Text(
                text =
                    "• $word",
                style =
                    MaterialTheme
                        .typography
                        .bodyMedium
            )
        }
    }
}

@Composable
private fun MissionExampleSection(
    examples:
    List<String>
) {
    SupportSection(
        title =
            "Example phrases"
    ) {
        examples.forEach { example ->
            Text(
                text =
                    "“$example”",
                style =
                    MaterialTheme
                        .typography
                        .bodyMedium
            )
        }
    }
}

@Composable
private fun SupportSection(
    title: String,
    content:
    @Composable () -> Unit
) {
    Surface(
        modifier =
            Modifier.fillMaxWidth(),
        shape =
            RoundedCornerShape(
                size =
                    14.dp
            ),
        color =
            MaterialTheme
                .colorScheme
                .surfaceContainerHigh
    ) {
        Column(
            modifier =
                Modifier.padding(
                    all =
                        12.dp
                ),
            verticalArrangement =
                Arrangement.spacedBy(
                    space =
                        6.dp
                )
        ) {
            Text(
                text =
                    title,
                style =
                    MaterialTheme
                        .typography
                        .labelMedium,
                fontWeight =
                    FontWeight.SemiBold,
                color =
                    MaterialTheme
                        .colorScheme
                        .primary
            )

            content()
        }
    }
}

private fun hintButtonText(
    state:
    MissionGuidanceUiState
): String {
    return when (
        state.revealedHintCount
    ) {
        0 -> {
            "Hint"
        }

        1 -> {
            "Another hint"
        }

        2 -> {
            "Final hint"
        }

        else -> {
            "Hints shown"
        }
    }
}