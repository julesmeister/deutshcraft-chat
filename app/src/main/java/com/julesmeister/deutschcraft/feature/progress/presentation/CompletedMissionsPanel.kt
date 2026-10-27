package com.julesmeister.deutschcraft.feature.progress.presentation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
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
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.julesmeister.deutschcraft.ui.theme.DeutschLime
import com.julesmeister.deutschcraft.ui.theme.DeutschTextPrimary
import com.julesmeister.deutschcraft.ui.theme.DeutschTextSecondary
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
internal fun CompletedMissionsPanel(
    completedMissionCount: Int,
    recentMissions: List<CompletedMissionUiModel>,
    showEnglish: Boolean,
    modifier: Modifier = Modifier
) {
    var contentVisible by
    remember {
        mutableStateOf(false)
    }

    LaunchedEffect(Unit) {
        contentVisible =
            true
    }

    AnimatedVisibility(
        visible =
            contentVisible,
        modifier =
            modifier,
        enter =
            fadeIn(
                animationSpec =
                    tween(
                        durationMillis =
                            CompletedMissionsPanelEnterDurationMillis,
                        easing =
                            FastOutSlowInEasing
                    )
            ) +
                    slideInVertically(
                        initialOffsetY = { height ->
                            height / 5
                        },
                        animationSpec =
                            tween(
                                durationMillis =
                                    CompletedMissionsPanelEnterDurationMillis,
                                easing =
                                    FastOutSlowInEasing
                            )
                    ),
        exit =
            fadeOut() +
                    slideOutVertically()
    ) {
        Surface(
            modifier =
                Modifier.fillMaxWidth(),
            shape =
                RoundedCornerShape(
                    CompletedMissionsPanelCornerRadius
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
                        CompletedMissionsPanelPadding
                    )
            ) {
                MissionSummaryHeader(
                    completedMissionCount =
                        completedMissionCount,
                    showEnglish =
                        showEnglish
                )

                Spacer(
                    modifier =
                        Modifier.height(
                            CompletedMissionsHeaderContentSpacing
                        )
                )

                if (recentMissions.isEmpty()) {
                    EmptyMissionHistory(
                        showEnglish =
                            showEnglish
                    )
                } else {
                    Text(
                        text =
                            if (showEnglish) {
                                "Recently completed"
                            } else {
                                "Zuletzt abgeschlossen"
                            },
                        style =
                            MaterialTheme
                                .typography
                                .labelLarge,
                        fontWeight =
                            FontWeight.SemiBold,
                        color =
                            DeutschTextSecondary
                    )

                    Spacer(
                        modifier =
                            Modifier.height(
                                CompletedMissionsListTopSpacing
                            )
                    )

                    recentMissions.forEachIndexed { index, mission ->
                        CompletedMissionRow(
                            mission =
                                mission,
                            showEnglish =
                                showEnglish
                        )

                        if (
                            index <
                            recentMissions.lastIndex
                        ) {
                            Spacer(
                                modifier =
                                    Modifier.height(
                                        CompletedMissionRowSpacing
                                    )
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MissionSummaryHeader(
    completedMissionCount: Int,
    showEnglish: Boolean
) {
    Row(
        modifier =
            Modifier.fillMaxWidth(),
        verticalAlignment =
            Alignment.CenterVertically
    ) {
        Box(
            modifier =
                Modifier
                    .size(
                        CompletedMissionsSummaryIconSize
                    )
                    .clip(
                        RoundedCornerShape(
                            CompletedMissionsSummaryIconRadius
                        )
                    )
                    .background(
                        color =
                            DeutschLime.copy(
                                alpha =
                                    CompletedMissionsSummaryIconBackgroundAlpha
                            )
                    ),
            contentAlignment =
                Alignment.Center
        ) {
            Icon(
                imageVector =
                    Icons.Outlined.EmojiEvents,
                contentDescription =
                    null,
                tint =
                    DeutschLime
            )
        }

        Spacer(
            modifier =
                Modifier.width(
                    CompletedMissionsSummaryContentSpacing
                )
        )

        Column(
            modifier =
                Modifier.weight(1f)
        ) {
            Text(
                text =
                    if (showEnglish) {
                        "Conversation missions"
                    } else {
                        "Gesprächsmissionen"
                    },
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
                    if (showEnglish) {
                        "Real-world situations you've mastered"
                    } else {
                        "Praktische Situationen, die du gemeistert hast"
                    },
                modifier =
                    Modifier.padding(
                        top =
                            CompletedMissionsSummaryDescriptionTopPadding
                    ),
                style =
                    MaterialTheme
                        .typography
                        .bodySmall,
                color =
                    DeutschTextSecondary
            )
        }

        AnimatedContent(
            targetState =
                completedMissionCount,
            transitionSpec = {
                (
                        fadeIn(
                            animationSpec =
                                tween(
                                    durationMillis =
                                        CompletedMissionCountAnimationDurationMillis
                                )
                        ) +
                                slideInVertically(
                                    initialOffsetY = { height ->
                                        height
                                    }
                                )
                        ) togetherWith
                        (
                                fadeOut(
                                    animationSpec =
                                        tween(
                                            durationMillis =
                                                CompletedMissionCountAnimationDurationMillis
                                        )
                                ) +
                                        slideOutVertically(
                                            targetOffsetY = { height ->
                                                -height
                                            }
                                        )
                                )
            },
            label =
                "CompletedMissionCount"
        ) { count ->
            Text(
                text =
                    count.toString(),
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
    }
}

@Composable
private fun CompletedMissionRow(
    mission: CompletedMissionUiModel,
    showEnglish: Boolean
) {
    val completionFraction =
        if (mission.totalObjectiveCount <= 0) {
            1f
        } else {
            (
                    mission.completedObjectiveCount.toFloat() /
                            mission.totalObjectiveCount.toFloat()
                    ).coerceIn(
                    minimumValue =
                        0f,
                    maximumValue =
                        1f
                )
        }

    val animatedCompletion by
    animateFloatAsState(
        targetValue =
            completionFraction,
        animationSpec =
            tween(
                durationMillis =
                    CompletedMissionProgressAnimationMillis,
                easing =
                    FastOutSlowInEasing
            ),
        label =
            "CompletedMissionProgress"
    )

    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .animateContentSize()
                .clip(
                    RoundedCornerShape(
                        CompletedMissionRowRadius
                    )
                )
                .background(
                    color =
                        MaterialTheme
                            .colorScheme
                            .surfaceContainerHigh
                )
                .padding(
                    CompletedMissionRowPadding
                ),
        verticalAlignment =
            Alignment.CenterVertically
    ) {
        Box(
            modifier =
                Modifier
                    .size(
                        CompletedMissionIconSize
                    )
                    .clip(
                        CircleShape
                    )
                    .background(
                        color =
                            DeutschLime.copy(
                                alpha =
                                    CompletedMissionIconBackgroundAlpha
                            )
                    ),
            contentAlignment =
                Alignment.Center
        ) {
            Text(
                text =
                    mission.safeIcon,
                style =
                    MaterialTheme
                        .typography
                        .titleMedium
            )
        }

        Spacer(
            modifier =
                Modifier.width(
                    CompletedMissionIconSpacing
                )
        )

        Column(
            modifier =
                Modifier.weight(1f)
        ) {
            Row(
                modifier =
                    Modifier.fillMaxWidth(),
                verticalAlignment =
                    Alignment.CenterVertically
            ) {
                /*
                 * Mission titles remain stored/generated
                 * content and are not translated here.
                 */
                Text(
                    text =
                        mission.title,
                    modifier =
                        Modifier.weight(1f),
                    style =
                        MaterialTheme
                            .typography
                            .bodyLarge,
                    fontWeight =
                        FontWeight.SemiBold,
                    color =
                        DeutschTextPrimary,
                    maxLines =
                        1,
                    overflow =
                        TextOverflow.Ellipsis
                )

                Icon(
                    imageVector =
                        Icons.Outlined.CheckCircle,
                    contentDescription =
                        if (showEnglish) {
                            "Completed"
                        } else {
                            "Abgeschlossen"
                        },
                    modifier =
                        Modifier.size(
                            CompletedMissionCheckSize
                        ),
                    tint =
                        DeutschLime
                )
            }

            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(
                            top =
                                CompletedMissionMetadataTopPadding
                        ),
                horizontalArrangement =
                    Arrangement.SpaceBetween,
                verticalAlignment =
                    Alignment.CenterVertically
            ) {
                /*
                 * Dynamic model content stays unchanged.
                 */
                Text(
                    text =
                        mission.completionLabel,
                    style =
                        MaterialTheme
                            .typography
                            .labelSmall,
                    color =
                        DeutschTextSecondary
                )

                Text(
                    text =
                        formatCompletionDate(
                            timestamp =
                                mission.completedAt,
                            showEnglish =
                                showEnglish
                        ),
                    style =
                        MaterialTheme
                            .typography
                            .labelSmall,
                    color =
                        DeutschTextSecondary
                )
            }

            LinearProgressIndicator(
                progress = {
                    animatedCompletion
                },
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(
                            top =
                                CompletedMissionProgressTopPadding
                        )
                        .height(
                            CompletedMissionProgressHeight
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
}

@Composable
private fun EmptyMissionHistory(
    showEnglish: Boolean
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(
                    RoundedCornerShape(
                        CompletedMissionRowRadius
                    )
                )
                .background(
                    color =
                        MaterialTheme
                            .colorScheme
                            .surfaceContainerHigh
                )
                .padding(
                    CompletedMissionRowPadding
                ),
        verticalAlignment =
            Alignment.CenterVertically
    ) {
        Icon(
            imageVector =
                Icons.Outlined.EmojiEvents,
            contentDescription =
                null,
            tint =
                DeutschTextSecondary
        )

        Spacer(
            modifier =
                Modifier.width(
                    CompletedMissionEmptyIconSpacing
                )
        )

        Text(
            text =
                if (showEnglish) {
                    "Complete your first conversation mission."
                } else {
                    "Schließe deine erste Gesprächsmission ab."
                },
            style =
                MaterialTheme
                    .typography
                    .bodyMedium,
            color =
                DeutschTextSecondary
        )
    }
}

private fun formatCompletionDate(
    timestamp: Long,
    showEnglish: Boolean
): String {
    if (timestamp <= 0L) {
        return ""
    }

    val formatter =
        DateTimeFormatter.ofPattern(
            if (showEnglish) {
                "MMM d"
            } else {
                "dd. MMM"
            },
            if (showEnglish) {
                Locale.ENGLISH
            } else {
                Locale.GERMAN
            }
        )

    return runCatching {
        Instant
            .ofEpochMilli(
                timestamp
            )
            .atZone(
                ZoneId.systemDefault()
            )
            .format(
                formatter
            )
    }.getOrDefault("")
}