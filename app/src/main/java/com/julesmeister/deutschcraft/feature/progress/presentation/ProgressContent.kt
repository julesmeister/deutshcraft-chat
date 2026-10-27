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
import com.julesmeister.deutschcraft.feature.progress.domain.model.ProgressSummary
import com.julesmeister.deutschcraft.ui.theme.DeutschTextPrimary
import com.julesmeister.deutschcraft.ui.theme.DeutschTextSecondary

@Composable
internal fun ProgressContent(
    progress: ProgressSummary,
    completedMissionCount: Int,
    recentCompletedMissions: List<CompletedMissionUiModel>,

    /*
     * Single language source for the entire Progress screen.
     *
     * false = German
     * true  = English
     */
    showEnglish: Boolean,

    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier =
            modifier,
        contentPadding =
            PaddingValues(
                start =
                    ProgressContentHorizontalPadding,
                top =
                    ProgressContentTopPadding,
                end =
                    ProgressContentHorizontalPadding,
                bottom =
                    ProgressContentBottomPadding
            ),
        verticalArrangement =
            Arrangement.spacedBy(
                ProgressContentItemSpacing
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
            ProgressIntroduction(
                showEnglish =
                    showEnglish
            )
        }

        /*
         * ===================================================
         * CEFR level + readiness Bento
         * ===================================================
         */
        item(
            key =
                CEFR_LEVEL_READINESS_BENTO_KEY
        ) {
            CefrLevelReadinessBento(
                progress =
                    progress,
                showEnglish =
                    showEnglish
            )
        }

        /*
         * ===================================================
         * Progress statistics
         * ===================================================
         *
         * This panel can receive showEnglish next when its
         * labels are made bilingual.
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
                    recentCompletedMissions,
                showEnglish =
                    showEnglish
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
private fun ProgressIntroduction(
    showEnglish: Boolean
) {
    Column {
        Text(
            text =
                if (showEnglish) {
                    "CEFR development"
                } else {
                    "CEFR-Entwicklung"
                },
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
                if (showEnglish) {
                    "Your assessment develops with every " +
                            "analyzed German message."
                } else {
                    "Deine Einschätzung entwickelt sich mit " +
                            "jeder analysierten deutschen Nachricht."
                },
            modifier =
                Modifier.padding(
                    top =
                        ProgressIntroductionDescriptionTopPadding
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

/*
 * Keep LazyColumn keys local.
 *
 * These aren't shared UI dimensions or styling values, so
 * they don't belong in ProgressUiConstants.kt.
 */
private const val PROGRESS_INTRODUCTION_KEY =
    "progress_introduction"

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