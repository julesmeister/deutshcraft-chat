// app/src/main/java/com/julesmeister/deutschcraft/feature/progress/presentation/ProgressContent.kt

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

@Composable
internal fun ProgressContent(
    progress:
    ProgressSummary,

    besserProgress:
    BesserProgressUiModel,

    learningFocuses:
    List<LearningFocusProgressUiModel>,

    reinforcementFocusCount: Int,

    improvingFocusCount: Int,

    masteredFocusCount: Int,

    completedMissionCount: Int,

    recentCompletedMissions:
    List<CompletedMissionUiModel>,

    showEnglish: Boolean,

    modifier:
    Modifier =
        Modifier
) {
    val colors =
        rememberProgressColors()

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
        item(
            key =
                PROGRESS_INTRODUCTION_KEY
        ) {
            ProgressIntroduction(
                showEnglish =
                    showEnglish,

                colors =
                    colors
            )
        }

        item(
            key =
                CEFR_LEVEL_READINESS_BENTO_KEY
        ) {
            CefrLevelReadinessBento(
                progress =
                    progress,

                showEnglish =
                    showEnglish,

                colors =
                    colors
            )
        }

        item(
            key =
                LEARNING_FOCUS_KEY
        ) {
            LearningFocusPanel(
                focuses =
                    learningFocuses,

                reinforcementFocusCount =
                    reinforcementFocusCount,

                improvingFocusCount =
                    improvingFocusCount,

                masteredFocusCount =
                    masteredFocusCount,

                showEnglish =
                    showEnglish,

                colors =
                    colors
            )
        }

        item(
            key =
                BESSER_GROWTH_KEY
        ) {
            BesserGrowthPanel(
                progress =
                    besserProgress,

                showEnglish =
                    showEnglish,

                colors =
                    colors
            )
        }

        item(
            key =
                PROGRESS_STATISTICS_KEY
        ) {
            ProgressStatistics(
                showEnglish =
                    showEnglish,

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
                    recentCompletedMissions,

                showEnglish =
                    showEnglish,

                colors =
                    colors
            )
        }

        item(
            key =
                SKILL_BREAKDOWN_KEY
        ) {
            SkillBreakdownPanel(
                progress =
                    progress,

                colors =
                    colors
            )
        }

        item(
            key =
                CONFIDENCE_KEY
        ) {
            ConfidencePanel(
                progress =
                    progress,

                colors =
                    colors
            )
        }
    }
}

@Composable
private fun ProgressIntroduction(
    showEnglish: Boolean,
    colors: ProgressColors
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
                colors.textPrimary
        )

        Text(
            text =
                if (showEnglish) {
                    "Your assessment develops through analyzed " +
                            "messages, Besser practice, and missions."
                } else {
                    "Deine Einschätzung entwickelt sich durch " +
                            "analysierte Nachrichten, Besser-Übungen " +
                            "und Missionen."
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
                colors.textSecondary
        )
    }
}

private const val PROGRESS_INTRODUCTION_KEY =
    "progress_introduction"

private const val CEFR_LEVEL_READINESS_BENTO_KEY =
    "cefr_level_readiness_bento"

private const val LEARNING_FOCUS_KEY =
    "learning_focus"

private const val BESSER_GROWTH_KEY =
    "besser_growth"

private const val PROGRESS_STATISTICS_KEY =
    "progress_statistics"

private const val COMPLETED_MISSIONS_KEY =
    "completed_missions"

private const val SKILL_BREAKDOWN_KEY =
    "skill_breakdown"

private const val CONFIDENCE_KEY =
    "confidence"