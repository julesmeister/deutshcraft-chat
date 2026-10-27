package com.julesmeister.deutschcraft.feature.learninganalytics.presentation.result

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/*
 * Displays finalized mission analytics after the final
 * required objective is completed.
 *
 * This component does not calculate analytics.
 *
 * All values have already been:
 *
 * - calculated
 * - persisted
 * - mapped into MissionResultUiState
 */
@OptIn(
    ExperimentalMaterial3Api::class
)
@Composable
fun MissionResultBottomSheet(
    state:
    MissionResultUiState,
    onDismiss:
        () -> Unit,
    onContinue:
        () -> Unit,
    modifier:
    Modifier =
        Modifier
) {
    if (!state.hasMissionResult) {
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
                .surface,
        contentColor =
            MaterialTheme
                .colorScheme
                .onSurface
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
                            16.dp
                    )
                    .navigationBarsPadding(),
            horizontalAlignment =
                Alignment.CenterHorizontally
        ) {
            MissionResultHeader(
                state =
                    state
            )

            Spacer(
                modifier =
                    Modifier.height(
                        20.dp
                    )
            )

            OverallScoreCard(
                state =
                    state
            )

            Spacer(
                modifier =
                    Modifier.height(
                        18.dp
                    )
            )

            ScoreSection(
                state =
                    state
            )

            Spacer(
                modifier =
                    Modifier.height(
                        18.dp
                    )
            )

            EvidenceSummarySection(
                state =
                    state
            )

            if (
                state.strongObjectiveTitles
                    .isNotEmpty()
            ) {
                Spacer(
                    modifier =
                        Modifier.height(
                            18.dp
                        )
                )

                ObjectiveResultSection(
                    title =
                        "Strong",
                    objectiveTitles =
                        state.strongObjectiveTitles,
                    marker =
                        "✓"
                )
            }

            if (
                state.improvementObjectiveTitles
                    .isNotEmpty()
            ) {
                Spacer(
                    modifier =
                        Modifier.height(
                            14.dp
                        )
                )

                ObjectiveResultSection(
                    title =
                        "Practice next",
                    objectiveTitles =
                        state
                            .improvementObjectiveTitles,
                    marker =
                        "△"
                )
            }

            Spacer(
                modifier =
                    Modifier.height(
                        24.dp
                    )
            )

            Button(
                onClick =
                    onContinue,
                modifier =
                    Modifier.fillMaxWidth(),
                shape =
                    RoundedCornerShape(
                        size =
                            16.dp
                    )
            ) {
                Text(
                    text =
                        "Continue",
                    modifier =
                        Modifier.padding(
                            vertical =
                                4.dp
                        )
                )
            }
        }
    }
}

@Composable
private fun MissionResultHeader(
    state:
    MissionResultUiState
) {
    Column(
        horizontalAlignment =
            Alignment.CenterHorizontally
    ) {
        Text(
            text =
                "Mission complete",
            style =
                MaterialTheme
                    .typography
                    .headlineSmall,
            fontWeight =
                FontWeight.Bold,
            textAlign =
                TextAlign.Center
        )

        Spacer(
            modifier =
                Modifier.height(
                    6.dp
                )
        )

        Text(
            text =
                state.missionTitle,
            style =
                MaterialTheme
                    .typography
                    .titleMedium,
            fontWeight =
                FontWeight.SemiBold,
            textAlign =
                TextAlign.Center
        )

        if (
            state.cefrLevelLabel.isNotBlank()
        ) {
            Spacer(
                modifier =
                    Modifier.height(
                        4.dp
                    )
            )

            Surface(
                shape =
                    RoundedCornerShape(
                        size =
                            50.dp
                    ),
                color =
                    MaterialTheme
                        .colorScheme
                        .primaryContainer
            ) {
                Text(
                    text =
                        state.cefrLevelLabel,
                    modifier =
                        Modifier.padding(
                            horizontal =
                                12.dp,
                            vertical =
                                5.dp
                        ),
                    style =
                        MaterialTheme
                            .typography
                            .labelMedium,
                    color =
                        MaterialTheme
                            .colorScheme
                            .onPrimaryContainer
                )
            }
        }
    }
}

@Composable
private fun OverallScoreCard(
    state:
    MissionResultUiState
) {
    Card(
        modifier =
            Modifier.fillMaxWidth(),
        shape =
            RoundedCornerShape(
                size =
                    22.dp
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
                Modifier
                    .fillMaxWidth()
                    .padding(
                        vertical =
                            22.dp,
                        horizontal =
                            18.dp
                    ),
            horizontalAlignment =
                Alignment.CenterHorizontally
        ) {
            Text(
                text =
                    "${state.safeOverallScore}%",
                style =
                    MaterialTheme
                        .typography
                        .displaySmall,
                fontWeight =
                    FontWeight.Bold,
                color =
                    MaterialTheme
                        .colorScheme
                        .primary
            )

            Spacer(
                modifier =
                    Modifier.height(
                        2.dp
                    )
            )

            Text(
                text =
                    "Overall",
                style =
                    MaterialTheme
                        .typography
                        .labelLarge,
                color =
                    MaterialTheme
                        .colorScheme
                        .onSurfaceVariant
            )

            Spacer(
                modifier =
                    Modifier.height(
                        8.dp
                    )
            )

            Text(
                text =
                    state.performanceMessage,
                style =
                    MaterialTheme
                        .typography
                        .bodyMedium,
                textAlign =
                    TextAlign.Center
            )

            Spacer(
                modifier =
                    Modifier.height(
                        14.dp
                    )
            )

            LinearProgressIndicator(
                progress = {
                    state.safeOverallScore
                        .toFloat() /
                            100f
                },
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(
                            8.dp
                        )
                        .clip(
                            RoundedCornerShape(
                                size =
                                    50.dp
                            )
                        )
            )
        }
    }
}

@Composable
private fun ScoreSection(
    state:
    MissionResultUiState
) {
    ResultSectionCard(
        title =
            "Performance"
    ) {
        MissionScoreRow(
            label =
                "Conversation",
            score =
                state.conversationScore
        )

        MissionScoreRow(
            label =
                "Independence",
            score =
                state.independenceScore
        )

        MissionScoreRow(
            label =
                "Grammar",
            score =
                state.grammarScore
        )

        MissionScoreRow(
            label =
                "Vocabulary",
            score =
                state.vocabularyScore
        )

        MissionScoreRow(
            label =
                "Interaction",
            score =
                state.interactionScore
        )

        MissionScoreRow(
            label =
                "Comprehension",
            score =
                state.comprehensionScore
        )
    }
}

@Composable
private fun MissionScoreRow(
    label: String,
    score: Int
) {
    val safeScore =
        score.coerceIn(
            minimumValue =
                0,
            maximumValue =
                100
        )

    Column(
        verticalArrangement =
            Arrangement.spacedBy(
                space =
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
                    label,
                style =
                    MaterialTheme
                        .typography
                        .bodyMedium
            )

            Text(
                text =
                    "$safeScore%",
                style =
                    MaterialTheme
                        .typography
                        .labelLarge,
                fontWeight =
                    FontWeight.SemiBold
            )
        }

        LinearProgressIndicator(
            progress = {
                safeScore.toFloat() /
                        100f
            },
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(
                        6.dp
                    )
                    .clip(
                        RoundedCornerShape(
                            size =
                                50.dp
                        )
                    )
        )
    }
}

@Composable
private fun EvidenceSummarySection(
    state:
    MissionResultUiState
) {
    ResultSectionCard(
        title =
            "Mission evidence"
    ) {
        EvidenceLine(
            label =
                "Objectives",
            value =
                state.objectiveProgressLabel
        )

        EvidenceLine(
            label =
                "Hints",
            value =
                state.supportSummary
        )

        EvidenceLine(
            label =
                "Independent objectives",
            value =
                state
                    .independentlyCompletedObjectiveCount
                    .toString()
        )

        EvidenceLine(
            label =
                "Supported objectives",
            value =
                state
                    .supportedObjectiveCount
                    .toString()
        )

        EvidenceLine(
            label =
                "Directly assisted",
            value =
                state
                    .directlyAssistedObjectiveCount
                    .toString()
        )

        EvidenceLine(
            label =
                "Besser corrections",
            value =
                state
                    .besserCorrectionCount
                    .toString()
        )

        if (
            state.besserCorrectionCount >
            0
        ) {
            EvidenceLine(
                label =
                    "Besser improvement",
                value =
                    state.resolvedCorrectionLabel
            )
        }

        EvidenceLine(
            label =
                "Target vocabulary",
            value =
                state.vocabularyProgressLabel
        )
    }
}

@Composable
private fun EvidenceLine(
    label: String,
    value: String
) {
    Row(
        modifier =
            Modifier.fillMaxWidth(),
        horizontalArrangement =
            Arrangement.SpaceBetween,
        verticalAlignment =
            Alignment.Top
    ) {
        Text(
            text =
                label,
            modifier =
                Modifier.weight(
                    weight =
                        1f
                ),
            style =
                MaterialTheme
                    .typography
                    .bodyMedium,
            color =
                MaterialTheme
                    .colorScheme
                    .onSurfaceVariant
        )

        Spacer(
            modifier =
                Modifier.width(
                    width =
                        16.dp
                )
        )

        Text(
            text =
                value,
            style =
                MaterialTheme
                    .typography
                    .bodyMedium,
            fontWeight =
                FontWeight.Medium,
            textAlign =
                TextAlign.End
        )
    }
}

@Composable
private fun ObjectiveResultSection(
    title: String,
    objectiveTitles:
    List<String>,
    marker: String
) {
    ResultSectionCard(
        title =
            title
    ) {
        objectiveTitles.forEach { objectiveTitle ->
            Row(
                modifier =
                    Modifier.fillMaxWidth(),
                verticalAlignment =
                    Alignment.Top
            ) {
                Text(
                    text =
                        marker,
                    style =
                        MaterialTheme
                            .typography
                            .bodyMedium,
                    fontWeight =
                        FontWeight.Bold,
                    color =
                        MaterialTheme
                            .colorScheme
                            .primary
                )

                Spacer(
                    modifier =
                        Modifier.width(
                            width =
                                10.dp
                        )
                )

                Text(
                    text =
                        objectiveTitle,
                    modifier =
                        Modifier.weight(
                            weight =
                                1f
                        ),
                    style =
                        MaterialTheme
                            .typography
                            .bodyMedium
                )
            }
        }
    }
}

@Composable
private fun ResultSectionCard(
    title: String,
    content:
    @Composable () -> Unit
) {
    Card(
        modifier =
            Modifier.fillMaxWidth(),
        shape =
            RoundedCornerShape(
                size =
                    18.dp
            ),
        colors =
            CardDefaults.cardColors(
                containerColor =
                    MaterialTheme
                        .colorScheme
                        .surfaceContainerLow
            )
    ) {
        Column(
            modifier =
                Modifier.padding(
                    all =
                        16.dp
                ),
            verticalArrangement =
                Arrangement.spacedBy(
                    space =
                        12.dp
                )
        ) {
            Text(
                text =
                    title,
                style =
                    MaterialTheme
                        .typography
                        .titleSmall,
                fontWeight =
                    FontWeight.SemiBold
            )

            HorizontalDivider(
                color =
                    MaterialTheme
                        .colorScheme
                        .outlineVariant
            )

            content()
        }
    }
}