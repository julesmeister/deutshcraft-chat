package com.julesmeister.deutschcraft.feature.progress.presentation

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoGraph
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.ChatBubbleOutline
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
import com.julesmeister.deutschcraft.feature.progress.domain.model.ProgressSummary
import com.julesmeister.deutschcraft.ui.theme.DeutschBlack
import com.julesmeister.deutschcraft.ui.theme.DeutschLime
import com.julesmeister.deutschcraft.ui.theme.DeutschTextPrimary
import com.julesmeister.deutschcraft.ui.theme.DeutschTextSecondary

@Composable
internal fun CurrentNiveauPanel(
    progress: ProgressSummary,
    modifier: Modifier = Modifier
) {
    val safeOverallScore =
        progress.overallScore.coerceIn(
            minimumValue =
                0,
            maximumValue =
                100
        )

    val animatedOverallScore by
    animateFloatAsState(
        targetValue =
            safeOverallScore / 100f,
        animationSpec =
            tween(
                durationMillis =
                    HERO_PROGRESS_ANIMATION_MILLIS,
                easing =
                    FastOutSlowInEasing
            ),
        label =
            "CurrentLevelOverallScore"
    )

    Surface(
        modifier =
            modifier.fillMaxWidth(),
        shape =
            RoundedCornerShape(
                HERO_PANEL_RADIUS
            ),
        color =
            DeutschLime,
        contentColor =
            DeutschBlack,
        tonalElevation =
            0.dp,
        shadowElevation =
            0.dp
    ) {
        Row(
            modifier =
                Modifier.padding(
                    HERO_PANEL_PADDING
                ),
            verticalAlignment =
                Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(
                        LEVEL_BADGE_SIZE
                    )
                    .clip(
                        CircleShape
                    )
                    .background(
                        color =
                            DeutschBlack.copy(
                                alpha =
                                    LEVEL_BADGE_ALPHA
                            )
                    ),
                contentAlignment =
                    Alignment.Center
            ) {
                Text(
                    text =
                        progress.currentLevel.name,
                    style =
                        MaterialTheme
                            .typography
                            .headlineMedium,
                    fontWeight =
                        FontWeight.Bold,
                    color =
                        DeutschLime
                )
            }

            Spacer(
                modifier =
                    Modifier.width(
                        HERO_CONTENT_SPACING
                    )
            )

            Column(
                modifier =
                    Modifier.weight(1f)
            ) {
                Text(
                    text =
                        "Aktuelles Niveau",
                    style =
                        MaterialTheme
                            .typography
                            .labelLarge,
                    fontWeight =
                        FontWeight.Medium,
                    color =
                        DeutschBlack.copy(
                            alpha =
                                HERO_SECONDARY_TEXT_ALPHA
                        )
                )

                Text(
                    text =
                        progress.currentLevel.name,
                    modifier =
                        Modifier.padding(
                            top =
                                LEVEL_NAME_TOP_PADDING
                        ),
                    style =
                        MaterialTheme
                            .typography
                            .headlineSmall,
                    fontWeight =
                        FontWeight.Bold,
                    color =
                        DeutschBlack
                )

                Text(
                    text =
                        "$safeOverallScore% Gesamtbewertung",
                    modifier =
                        Modifier.padding(
                            top =
                                SCORE_TEXT_TOP_PADDING
                        ),
                    style =
                        MaterialTheme
                            .typography
                            .bodyMedium,
                    fontWeight =
                        FontWeight.SemiBold,
                    color =
                        DeutschBlack.copy(
                            alpha =
                                HERO_SCORE_TEXT_ALPHA
                        )
                )

                LinearProgressIndicator(
                    progress = {
                        animatedOverallScore
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            top =
                                HERO_PROGRESS_TOP_PADDING
                        )
                        .height(
                            HERO_PROGRESS_HEIGHT
                        )
                        .clip(
                            CircleShape
                        ),
                    color =
                        DeutschBlack,
                    trackColor =
                        DeutschBlack.copy(
                            alpha =
                                HERO_PROGRESS_TRACK_ALPHA
                        )
                )
            }
        }
    }
}

@Composable
internal fun ReadinessPanel(
    progress: ProgressSummary,
    modifier: Modifier = Modifier
) {
    val destinationLabel =
        if (progress.isHighestLevel) {
            "C2 festigen"
        } else {
            "Bereitschaft für ${progress.nextLevelName}"
        }

    val safeReadiness =
        progress.normalizedReadiness.coerceIn(
            minimumValue =
                0f,
            maximumValue =
                1f
        )

    val animatedReadiness by
    animateFloatAsState(
        targetValue =
            safeReadiness,
        animationSpec =
            tween(
                durationMillis =
                    READINESS_ANIMATION_MILLIS,
                easing =
                    FastOutSlowInEasing
            ),
        label =
            "ReadinessProgress"
    )

    Surface(
        modifier =
            modifier.fillMaxWidth(),
        shape =
            RoundedCornerShape(
                SUPPORTING_PANEL_RADIUS
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
                    SUPPORTING_PANEL_PADDING
                )
        ) {
            Row(
                modifier =
                    Modifier.fillMaxWidth(),
                verticalAlignment =
                    Alignment.CenterVertically
            ) {
                Icon(
                    imageVector =
                        Icons.Outlined.AutoGraph,
                    contentDescription =
                        null,
                    tint =
                        DeutschLime
                )

                Text(
                    text =
                        destinationLabel,
                    modifier = Modifier
                        .padding(
                            start =
                                READINESS_TITLE_SPACING
                        )
                        .weight(1f),
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
                        "${
                            progress
                                .readinessPercentage
                                .coerceIn(
                                    minimumValue = 0,
                                    maximumValue = 100
                                )
                        }%",
                    style =
                        MaterialTheme
                            .typography
                            .titleMedium,
                    fontWeight =
                        FontWeight.Bold,
                    color =
                        DeutschLime
                )
            }

            LinearProgressIndicator(
                progress = {
                    animatedReadiness
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        top =
                            READINESS_PROGRESS_TOP_PADDING
                    )
                    .height(
                        READINESS_PROGRESS_HEIGHT
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

            Text(
                text =
                    if (progress.isHighestLevel) {
                        "Du hast das höchste CEFR-Niveau " +
                                "erreicht. Deine Bewertung zeigt " +
                                "jetzt die Festigung deiner " +
                                "C2-Fähigkeiten."
                    } else {
                        "Diese Bewertung zeigt, wie nah dein " +
                                "aktuelles Sprachprofil am nächsten " +
                                "Niveau liegt."
                    },
                modifier =
                    Modifier.padding(
                        top =
                            READINESS_DESCRIPTION_TOP_PADDING
                    ),
                style =
                    MaterialTheme
                        .typography
                        .bodySmall,
                color =
                    DeutschTextSecondary
            )
        }
    }
}

@Composable
internal fun ProgressStatistics(
    progress: ProgressSummary,
    modifier: Modifier = Modifier
) {
    Row(
        modifier =
            modifier.fillMaxWidth(),
        horizontalArrangement =
            Arrangement.spacedBy(
                STATISTIC_ITEM_SPACING
            )
    ) {
        ProgressStatisticItem(
            value =
                progress
                    .assessedMessageCount
                    .toString(),
            label =
                "Bewertete\nNachrichten",
            icon = {
                Icon(
                    imageVector =
                        Icons.Outlined.ChatBubbleOutline,
                    contentDescription =
                        null,
                    tint =
                        DeutschLime
                )
            },
            modifier =
                Modifier.weight(1f)
        )

        ProgressStatisticItem(
            value =
                progress
                    .activeDayCount
                    .toString(),
            label =
                "Aktive\nTage",
            icon = {
                Icon(
                    imageVector =
                        Icons.Outlined.CalendarMonth,
                    contentDescription =
                        null,
                    tint =
                        DeutschLime
                )
            },
            modifier =
                Modifier.weight(1f)
        )
    }
}

@Composable
private fun ProgressStatisticItem(
    value: String,
    label: String,
    icon: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier =
            modifier,
        shape =
            RoundedCornerShape(
                STATISTIC_PANEL_RADIUS
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
                    STATISTIC_PANEL_PADDING
                )
        ) {
            icon()

            Text(
                text =
                    value,
                modifier =
                    Modifier.padding(
                        top =
                            STATISTIC_VALUE_TOP_PADDING
                    ),
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
                    label,
                modifier =
                    Modifier.padding(
                        top =
                            STATISTIC_LABEL_TOP_PADDING
                    ),
                style =
                    MaterialTheme
                        .typography
                        .bodySmall,
                color =
                    DeutschTextSecondary
            )
        }
    }
}

private val HERO_PANEL_RADIUS =
    28.dp

private val HERO_PANEL_PADDING =
    20.dp

private val LEVEL_BADGE_SIZE =
    80.dp

private val HERO_CONTENT_SPACING =
    18.dp

private val LEVEL_NAME_TOP_PADDING =
    2.dp

private val SCORE_TEXT_TOP_PADDING =
    6.dp

private val HERO_PROGRESS_TOP_PADDING =
    12.dp

private val HERO_PROGRESS_HEIGHT =
    6.dp

private val SUPPORTING_PANEL_RADIUS =
    28.dp

private val SUPPORTING_PANEL_PADDING =
    20.dp

private val READINESS_TITLE_SPACING =
    10.dp

private val READINESS_PROGRESS_TOP_PADDING =
    18.dp

private val READINESS_PROGRESS_HEIGHT =
    10.dp

private val READINESS_DESCRIPTION_TOP_PADDING =
    12.dp

private val STATISTIC_ITEM_SPACING =
    12.dp

private val STATISTIC_PANEL_RADIUS =
    24.dp

private val STATISTIC_PANEL_PADDING =
    18.dp

private val STATISTIC_VALUE_TOP_PADDING =
    14.dp

private val STATISTIC_LABEL_TOP_PADDING =
    4.dp

private const val LEVEL_BADGE_ALPHA =
    0.92f

private const val HERO_SECONDARY_TEXT_ALPHA =
    0.72f

private const val HERO_SCORE_TEXT_ALPHA =
    0.78f

private const val HERO_PROGRESS_TRACK_ALPHA =
    0.18f

private const val HERO_PROGRESS_ANIMATION_MILLIS =
    900

private const val READINESS_ANIMATION_MILLIS =
    850