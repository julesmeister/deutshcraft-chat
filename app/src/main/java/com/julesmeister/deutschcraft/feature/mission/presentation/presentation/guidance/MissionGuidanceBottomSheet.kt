package com.julesmeister.deutschcraft.feature.mission.presentation.guidance

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FormatQuote
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.Translate
import androidx.compose.material3.AssistChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(
    ExperimentalMaterial3Api::class
)
@Composable
fun MissionGuidanceBottomSheet(
    state:
    MissionGuidanceUiState,
    onDismiss:
        () -> Unit,
    onRevealNextHint:
        () -> Unit,
    onToggleVocabulary:
        () -> Unit,
    onToggleExamples:
        () -> Unit,
    modifier: Modifier =
        Modifier
) {
    if (
        !state.hasActiveMission ||
        !state.isVisible
    ) {
        return
    }

    val sheetState =
        rememberModalBottomSheetState(
            skipPartiallyExpanded =
                true
        )

    ModalBottomSheet(
        onDismissRequest =
            onDismiss,
        sheetState =
            sheetState,
        modifier =
            modifier,
        containerColor =
            MaterialTheme
                .colorScheme
                .surface
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .verticalScroll(
                        rememberScrollState()
                    )
                    .padding(
                        horizontal =
                            20.dp
                    )
                    .padding(
                        bottom =
                            20.dp
                    )
                    .navigationBarsPadding(),
            verticalArrangement =
                Arrangement.spacedBy(
                    16.dp
                )
        ) {
            Text(
                text =
                    state.missionTitle,
                style =
                    MaterialTheme
                        .typography
                        .titleLarge,
                fontWeight =
                    FontWeight.Bold
            )

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
                            "Mission progress",
                        style =
                            MaterialTheme
                                .typography
                                .labelMedium,
                        color =
                            MaterialTheme
                                .colorScheme
                                .onSurfaceVariant
                    )

                    Text(
                        text =
                            "${state.safeProgressPercentage}%",
                        style =
                            MaterialTheme
                                .typography
                                .labelMedium,
                        color =
                            MaterialTheme
                                .colorScheme
                                .primary,
                        fontWeight =
                            FontWeight.Bold
                    )
                }

                LinearProgressIndicator(
                    progress = {
                        state.safeProgressPercentage /
                                100f
                    },
                    modifier =
                        Modifier.fillMaxWidth()
                )
            }

            GuidanceSection(
                title =
                    "Current objective"
            ) {
                Text(
                    text =
                        state.objectiveTitle,
                    style =
                        MaterialTheme
                            .typography
                            .titleMedium,
                    fontWeight =
                        FontWeight.SemiBold
                )

                if (
                    state.objectiveDescription
                        .isNotBlank()
                ) {
                    Text(
                        text =
                            state.objectiveDescription,
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

            Text(
                text =
                    "Need a little help?",
                style =
                    MaterialTheme
                        .typography
                        .titleSmall,
                fontWeight =
                    FontWeight.SemiBold
            )

            FlowRow(
                horizontalArrangement =
                    Arrangement.spacedBy(
                        8.dp
                    ),
                verticalArrangement =
                    Arrangement.spacedBy(
                        8.dp
                    )
            ) {
                if (
                    state.hasMoreHints ||
                    state.revealedHints
                        .isNotEmpty()
                ) {
                    AssistChip(
                        onClick =
                            onRevealNextHint,
                        enabled =
                            state.hasMoreHints &&
                                    !state.isRevealingHint,
                        leadingIcon = {
                            Icon(
                                imageVector =
                                    Icons
                                        .Outlined
                                        .Lightbulb,
                                contentDescription =
                                    null
                            )
                        },
                        label = {
                            Text(
                                text =
                                    when (
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
                        leadingIcon = {
                            Icon(
                                imageVector =
                                    Icons
                                        .Outlined
                                        .Translate,
                                contentDescription =
                                    null
                            )
                        },
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
                        leadingIcon = {
                            Icon(
                                imageVector =
                                    Icons
                                        .Outlined
                                        .FormatQuote,
                                contentDescription =
                                    null
                            )
                        },
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
                        }
                    )
                }
            }

            AnimatedVisibility(
                visible =
                    state.revealedHints
                        .isNotEmpty()
            ) {
                GuidanceSection(
                    title =
                        "Hints"
                ) {
                    state.revealedHints
                        .forEachIndexed { index, hint ->
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

            AnimatedVisibility(
                visible =
                    state.isVocabularyVisible &&
                            state.targetVocabulary
                                .isNotEmpty()
            ) {
                GuidanceSection(
                    title =
                        "Useful words"
                ) {
                    state.targetVocabulary.forEach { word ->
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

            AnimatedVisibility(
                visible =
                    state.isExamplesVisible &&
                            state.examplePhrases
                                .isNotEmpty()
            ) {
                GuidanceSection(
                    title =
                        "Example phrases"
                ) {
                    state.examplePhrases.forEach { example ->
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

            state.errorMessage
                ?.takeIf(
                    String::isNotBlank
                )
                ?.let { message ->
                    Text(
                        text =
                            message,
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
private fun GuidanceSection(
    title: String,
    content:
    @Composable () -> Unit
) {
    Surface(
        modifier =
            Modifier.fillMaxWidth(),
        shape =
            RoundedCornerShape(
                16.dp
            ),
        color =
            MaterialTheme
                .colorScheme
                .surfaceContainer
    ) {
        Column(
            modifier =
                Modifier.padding(
                    14.dp
                ),
            verticalArrangement =
                Arrangement.spacedBy(
                    8.dp
                )
        ) {
            Text(
                text =
                    title,
                style =
                    MaterialTheme
                        .typography
                        .labelMedium,
                color =
                    MaterialTheme
                        .colorScheme
                        .primary,
                fontWeight =
                    FontWeight.SemiBold
            )

            content()
        }
    }
}