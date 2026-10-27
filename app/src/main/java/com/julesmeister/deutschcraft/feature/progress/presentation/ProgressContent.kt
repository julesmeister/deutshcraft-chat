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
    progress:
    ProgressSummary,
    completedMissionCount:
    Int,
    recentCompletedMissions:
    List<CompletedMissionUiModel>,
    modifier:
    Modifier =
        Modifier
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
        /*
         * ===================================================
         * Introduction
         * ===================================================
         */
        item(
            key =
                PROGRESS_INTRODUCTION_KEY
        ) {
            ProgressIntroduction()
        }

        /*
         * ===================================================
         * CEFR level + readiness Bento
         * ===================================================
         *
         * Replaces the old separate:
         *
         * CurrentNiveauPanel
         *
         * and:
         *
         * ReadinessPanel
         *
         * with one asymmetric dashboard section.
         *
         * Visual structure:
         *
         * ┌─────────────┬─────────────────────┐
         * │ Aktuelles   │ Bereitschaft        │
         * │ Niveau      │                     │
         * │             │        43%          │
         * │     B1      │       für B2        │
         * └─────────────┘                     │
         * ┌───────────────────────────────────┤
         * │                                   │
         * │ ███████████░░░░░░░░░░░░   43%   │
         * │ Auf dem Weg zu B2                 │
         * └───────────────────────────────────┘
         *
         * The readiness area owns the dominant lime visual
         * treatment, while the current-level tile remains the
         * smaller neutral anchor.
         */
        item(
            key =
                CEFR_LEVEL_READINESS_BENTO_KEY
        ) {
            CefrLevelReadinessBento(
                progress =
                    progress
            )
        }

        /*
         * ===================================================
         * Progress statistics
         * ===================================================
         */
        item(
            key =
                PROGRESS_STATISTICS_KEY
        ) {
            ProgressStatistics(
                progress =
                    progress
            )
        }

        /*
         * ===================================================
         * Completed missions
         * ===================================================
         */
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

        /*
         * ===================================================
         * Skill breakdown
         * ===================================================
         */
        item(
            key =
                SKILL_BREAKDOWN_KEY
        ) {
            SkillBreakdownPanel(
                progress =
                    progress
            )
        }

        /*
         * ===================================================
         * Confidence
         * ===================================================
         */
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

/*
 * Current level and readiness now belong to one LazyColumn
 * item instead of two unrelated rows.
 */
private const val CEFR_LEVEL_READINESS_BENTO_KEY =
    "cefr_level_readiness_bento"

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