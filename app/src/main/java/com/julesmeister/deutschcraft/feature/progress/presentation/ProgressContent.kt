package com.julesmeister.deutschcraft.feature.progress.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.julesmeister.deutschcraft.feature.progress.domain.model.ProgressSummary
import com.julesmeister.deutschcraft.ui.theme.DeutschTextPrimary
import com.julesmeister.deutschcraft.ui.theme.DeutschTextSecondary

@Composable
internal fun ProgressContent(
    progress: ProgressSummary,
    completedMissionCount: Int,
    recentCompletedMissions:
    List<CompletedMissionUiModel>,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier =
            modifier,
        contentPadding =
            PaddingValues(
                start =
                    CONTENT_HORIZONTAL_PADDING,
                top =
                    CONTENT_TOP_PADDING,
                end =
                    CONTENT_HORIZONTAL_PADDING,
                bottom =
                    CONTENT_BOTTOM_PADDING
            ),
        verticalArrangement =
            Arrangement.spacedBy(
                CONTENT_ITEM_SPACING
            )
    ) {
        item(
            key =
                PROGRESS_INTRODUCTION_KEY
        ) {
            ProgressIntroduction()
        }

        item(
            key =
                CURRENT_LEVEL_KEY
        ) {
            CurrentNiveauPanel(
                progress =
                    progress
            )
        }

        item(
            key =
                READINESS_KEY
        ) {
            ReadinessPanel(
                progress =
                    progress
            )
        }

        item(
            key =
                PROGRESS_STATISTICS_KEY
        ) {
            ProgressStatistics(
                progress =
                    progress
            )
        }

        item(
            key =
                COMPLETED_MISSIONS_KEY
        ) {
            CompletedMissionsPanel(
                completedMissionCount =
                    completedMissionCount,
                recentMissions =
                    recentCompletedMissions
            )
        }

        item(
            key =
                SKILL_BREAKDOWN_KEY
        ) {
            SkillBreakdownPanel(
                progress =
                    progress
            )
        }

        item(
            key =
                CONFIDENCE_KEY
        ) {
            ConfidencePanel(
                progress =
                    progress
            )
        }
    }
}

@Composable
private fun ProgressIntroduction() {
    Column {
        Text(
            text =
                "CEFR-Entwicklung",
            style =
                MaterialTheme
                    .typography
                    .headlineSmall,
            fontWeight =
                FontWeight.Bold,
            color =
                DeutschTextPrimary
        )

        Text(
            text =
                "Deine Einschätzung entwickelt sich mit " +
                        "jeder analysierten deutschen Nachricht.",
            modifier =
                Modifier.padding(
                    top =
                        INTRODUCTION_DESCRIPTION_TOP_PADDING
                ),
            style =
                MaterialTheme
                    .typography
                    .bodyMedium,
            color =
                DeutschTextSecondary
        )
    }
}

private const val PROGRESS_INTRODUCTION_KEY =
    "progress_introduction"

private const val CURRENT_LEVEL_KEY =
    "current_level"

private const val READINESS_KEY =
    "readiness"

private const val PROGRESS_STATISTICS_KEY =
    "progress_statistics"

private const val COMPLETED_MISSIONS_KEY =
    "completed_missions"

private const val SKILL_BREAKDOWN_KEY =
    "skill_breakdown"

private const val CONFIDENCE_KEY =
    "confidence"

private val CONTENT_HORIZONTAL_PADDING =
    20.dp

private val CONTENT_TOP_PADDING =
    16.dp

private val CONTENT_BOTTOM_PADDING =
    40.dp

private val CONTENT_ITEM_SPACING =
    16.dp

private val INTRODUCTION_DESCRIPTION_TOP_PADDING =
    6.dp