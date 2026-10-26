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
                            PANEL_ENTER_DURATION_MILLIS,
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
                                    PANEL_ENTER_DURATION_MILLIS,
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
                    PANEL_CORNER_RADIUS
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
                MissionSummaryHeader(
                    completedMissionCount =
                        completedMissionCount
                )

                Spacer(
                    modifier =
                        Modifier.height(
                            HEADER_CONTENT_SPACING
                        )
                )

                if (recentMissions.isEmpty()) {
                    EmptyMissionHistory()
                } else {
                    Text(
                        text =
                            "Zuletzt abgeschlossen",
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
                                MISSION_LIST_TOP_SPACING
                            )
                    )

                    recentMissions
                        .forEachIndexed {
                                index,
                                mission ->

                            CompletedMissionRow(
                                mission =
                                    mission
                            )

                            if (
                                index <
                                recentMissions.lastIndex
                            ) {
                                Spacer(
                                    modifier =
                                        Modifier.height(
                                            MISSION_ROW_SPACING
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
    completedMissionCount: Int
) {
    Row(
        modifier =
            Modifier.fillMaxWidth(),
        verticalAlignment =
            Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(
                    SUMMARY_ICON_SIZE
                )
                .clip(
                    RoundedCornerShape(
                        SUMMARY_ICON_RADIUS
                    )
                )
                .background(
                    color =
                        DeutschLime.copy(
                            alpha =
                                SUMMARY_ICON_BACKGROUND_ALPHA
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
                    SUMMARY_CONTENT_SPACING
                )
        )

        Column(
            modifier =
                Modifier.weight(1f)
        ) {
            Text(
                text =
                    "Gesprächsmissionen",
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
                    "Praktische Situationen, die du " +
                            "gemeistert hast",
                modifier =
                    Modifier.padding(
                        top =
                            SUMMARY_DESCRIPTION_TOP_PADDING
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
                                        COUNT_ANIMATION_DURATION_MILLIS
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
                                                COUNT_ANIMATION_DURATION_MILLIS
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
    mission: CompletedMissionUiModel
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
                    MISSION_PROGRESS_ANIMATION_MILLIS,
                easing =
                    FastOutSlowInEasing
            ),
        label =
            "CompletedMissionProgress"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
            .clip(
                RoundedCornerShape(
                    MISSION_ROW_RADIUS
                )
            )
            .background(
                color =
                    MaterialTheme
                        .colorScheme
                        .surfaceContainerHigh
            )
            .padding(
                MISSION_ROW_PADDING
            ),
        verticalAlignment =
            Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(
                    MISSION_ICON_SIZE
                )
                .clip(
                    CircleShape
                )
                .background(
                    color =
                        DeutschLime.copy(
                            alpha =
                                MISSION_ICON_BACKGROUND_ALPHA
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
                    MISSION_ICON_SPACING
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
                        "Abgeschlossen",
                    modifier =
                        Modifier.size(
                            COMPLETION_CHECK_SIZE
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
                                MISSION_METADATA_TOP_PADDING
                        ),
                horizontalArrangement =
                    Arrangement.SpaceBetween,
                verticalAlignment =
                    Alignment.CenterVertically
            ) {
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
                                mission.completedAt
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
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        top =
                            MISSION_PROGRESS_TOP_PADDING
                    )
                    .height(
                        MISSION_PROGRESS_HEIGHT
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
private fun EmptyMissionHistory() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(
                RoundedCornerShape(
                    MISSION_ROW_RADIUS
                )
            )
            .background(
                color =
                    MaterialTheme
                        .colorScheme
                        .surfaceContainerHigh
            )
            .padding(
                MISSION_ROW_PADDING
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
                    EMPTY_ICON_SPACING
                )
        )

        Text(
            text =
                "Schließe deine erste Gesprächsmission ab.",
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
    timestamp: Long
): String {
    if (timestamp <= 0L) {
        return ""
    }

    return runCatching {
        Instant
            .ofEpochMilli(
                timestamp
            )
            .atZone(
                ZoneId.systemDefault()
            )
            .format(
                COMPLETION_DATE_FORMATTER
            )
    }.getOrDefault(
        ""
    )
}

private val COMPLETION_DATE_FORMATTER =
    DateTimeFormatter.ofPattern(
        "dd. MMM",
        Locale.GERMAN
    )

private val PANEL_CORNER_RADIUS =
    28.dp

private val PANEL_PADDING =
    20.dp

private val SUMMARY_ICON_SIZE =
    48.dp

private val SUMMARY_ICON_RADIUS =
    16.dp

private val SUMMARY_CONTENT_SPACING =
    12.dp

private val SUMMARY_DESCRIPTION_TOP_PADDING =
    2.dp

private val HEADER_CONTENT_SPACING =
    20.dp

private val MISSION_LIST_TOP_SPACING =
    12.dp

private val MISSION_ROW_SPACING =
    10.dp

private val MISSION_ROW_RADIUS =
    20.dp

private val MISSION_ROW_PADDING =
    14.dp

private val MISSION_ICON_SIZE =
    42.dp

private val MISSION_ICON_SPACING =
    12.dp

private val COMPLETION_CHECK_SIZE =
    20.dp

private val MISSION_METADATA_TOP_PADDING =
    3.dp

private val MISSION_PROGRESS_TOP_PADDING =
    8.dp

private val MISSION_PROGRESS_HEIGHT =
    5.dp

private val EMPTY_ICON_SPACING =
    12.dp

private const val SUMMARY_ICON_BACKGROUND_ALPHA =
    0.14f

private const val MISSION_ICON_BACKGROUND_ALPHA =
    0.14f

private const val PANEL_ENTER_DURATION_MILLIS =
    500

private const val COUNT_ANIMATION_DURATION_MILLIS =
    300

private const val MISSION_PROGRESS_ANIMATION_MILLIS =
    850