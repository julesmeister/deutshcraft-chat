package com.julesmeister.deutschcraft.feature.progress.presentation

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Insights
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.julesmeister.deutschcraft.feature.progress.domain.model.ProgressSkillSummary
import com.julesmeister.deutschcraft.feature.progress.domain.model.ProgressSummary
import com.julesmeister.deutschcraft.ui.theme.DeutschLime
import com.julesmeister.deutschcraft.ui.theme.DeutschTextPrimary
import com.julesmeister.deutschcraft.ui.theme.DeutschTextSecondary

@Composable
internal fun SkillBreakdownPanel(
    progress: ProgressSummary,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier =
            modifier.fillMaxWidth(),
        shape =
            RoundedCornerShape(
                PANEL_RADIUS
            ),
        color =
            MaterialTheme
                .colorScheme
                .surfaceContainer,
        tonalElevation =
            0.dp,
        shadowElevation =
            0.dp
    ) {
        Column(
            modifier =
                Modifier.padding(
                    PANEL_PADDING
                )
        ) {
            Row(
                verticalAlignment =
                    Alignment.CenterVertically
            ) {
                Icon(
                    imageVector =
                        Icons.Outlined.Insights,
                    contentDescription =
                        null,
                    tint =
                        DeutschLime
                )

                Text(
                    text =
                        "Fähigkeiten",
                    modifier =
                        Modifier.padding(
                            start =
                                PANEL_TITLE_SPACING
                        ),
                    style =
                        MaterialTheme
                            .typography
                            .titleMedium,
                    fontWeight =
                        FontWeight.SemiBold,
                    color =
                        DeutschTextPrimary
                )
            }

            Spacer(
                modifier =
                    Modifier.height(
                        SKILL_LIST_TOP_SPACING
                    )
            )

            progress
                .skillSummaries
                .forEachIndexed { index, skill ->
                    SkillProgressRow(
                        skill =
                            skill
                    )

                    if (
                        index <
                        progress.skillSummaries.lastIndex
                    ) {
                        Spacer(
                            modifier =
                                Modifier.height(
                                    SKILL_ROW_SPACING
                                )
                        )
                    }
                }
        }
    }
}

@Composable
private fun SkillProgressRow(
    skill: ProgressSkillSummary,
    modifier: Modifier = Modifier
) {
    val safeScore =
        skill.score.coerceIn(
            minimumValue =
                0,
            maximumValue =
                100
        )

    val animatedScore by
    animateFloatAsState(
        targetValue =
            safeScore / 100f,
        animationSpec =
            tween(
                durationMillis =
                    SKILL_PROGRESS_ANIMATION_MILLIS,
                easing =
                    FastOutSlowInEasing
            ),
        label =
            "SkillProgress_${skill.label}"
    )

    Column(
        modifier =
            modifier.fillMaxWidth()
    ) {
        Row(
            modifier =
                Modifier.fillMaxWidth(),
            verticalAlignment =
                Alignment.CenterVertically
        ) {
            Text(
                text =
                    skill.label,
                modifier =
                    Modifier.weight(1f),
                style =
                    MaterialTheme
                        .typography
                        .bodyMedium,
                fontWeight =
                    FontWeight.Medium,
                color =
                    DeutschTextPrimary
            )

            Text(
                text =
                    "$safeScore%",
                style =
                    MaterialTheme
                        .typography
                        .labelLarge,
                fontWeight =
                    FontWeight.Bold,
                color =
                    DeutschTextPrimary
            )
        }

        LinearProgressIndicator(
            progress = {
                animatedScore
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    top =
                        SKILL_PROGRESS_TOP_PADDING
                )
                .height(
                    SKILL_PROGRESS_HEIGHT
                )
                .clip(
                    CircleShape
                ),
            color =
                DeutschLime,
            trackColor =
                MaterialTheme
                    .colorScheme
                    .surfaceContainerHighest
        )
    }
}

@Composable
internal fun ConfidencePanel(
    progress: ProgressSummary,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier =
            modifier.fillMaxWidth(),
        shape =
            RoundedCornerShape(
                PANEL_RADIUS
            ),
        color =
            MaterialTheme
                .colorScheme
                .surfaceContainer,
        tonalElevation =
            0.dp,
        shadowElevation =
            0.dp
    ) {
        Column(
            modifier =
                Modifier.padding(
                    PANEL_PADDING
                )
        ) {
            Text(
                text =
                    "Evidenzqualität",
                style =
                    MaterialTheme
                        .typography
                        .labelLarge,
                color =
                    DeutschTextSecondary
            )

            Text(
                text =
                    progress.confidence.displayName,
                modifier =
                    Modifier.padding(
                        top =
                            CONFIDENCE_NAME_TOP_PADDING
                    ),
                style =
                    MaterialTheme
                        .typography
                        .titleMedium,
                fontWeight =
                    FontWeight.SemiBold,
                color =
                    DeutschTextPrimary
            )

            Text(
                text =
                    progress.confidence.description,
                modifier =
                    Modifier.padding(
                        top =
                            CONFIDENCE_DESCRIPTION_TOP_PADDING
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
}

private val PANEL_RADIUS =
    28.dp

private val PANEL_PADDING =
    20.dp

private val PANEL_TITLE_SPACING =
    10.dp

private val SKILL_LIST_TOP_SPACING =
    20.dp

private val SKILL_ROW_SPACING =
    18.dp

private val SKILL_PROGRESS_TOP_PADDING =
    8.dp

private val SKILL_PROGRESS_HEIGHT =
    8.dp

private val CONFIDENCE_NAME_TOP_PADDING =
    4.dp

private val CONFIDENCE_DESCRIPTION_TOP_PADDING =
    8.dp

private const val SKILL_PROGRESS_ANIMATION_MILLIS =
    850