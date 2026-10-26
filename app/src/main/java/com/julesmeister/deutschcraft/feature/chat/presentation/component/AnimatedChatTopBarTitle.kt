package com.julesmeister.deutschcraft.feature.chat.presentation.component

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

@Composable
internal fun AnimatedChatTopBarTitle(
    isAssistantTyping: Boolean,
    isAssessing: Boolean,
    activeMissionTitle: String?,
    currentMissionObjective: String?,
    missionProgressPercentage: Int,
    modifier: Modifier = Modifier
) {
    val missionTitle =
        activeMissionTitle
            ?.trim()
            ?.takeIf { title ->
                title.isNotBlank()
            }

    val missionObjective =
        currentMissionObjective
            ?.trim()
            ?.takeIf { objective ->
                objective.isNotBlank()
            }

    val hasActiveMission =
        missionTitle != null

    var missionDisplayIndex by remember(
        missionTitle,
        missionObjective,
        missionProgressPercentage
    ) {
        mutableIntStateOf(0)
    }

    LaunchedEffect(
        hasActiveMission,
        missionTitle,
        missionObjective,
        isAssistantTyping,
        isAssessing
    ) {
        missionDisplayIndex = 0

        if (
            !hasActiveMission ||
            isAssistantTyping ||
            isAssessing
        ) {
            return@LaunchedEffect
        }

        while (true) {
            delay(
                NORMAL_STATUS_DURATION_MILLIS
            )

            missionDisplayIndex = 1

            delay(
                MISSION_PROGRESS_DURATION_MILLIS
            )

            if (missionObjective != null) {
                missionDisplayIndex = 2

                delay(
                    MISSION_OBJECTIVE_DURATION_MILLIS
                )
            }

            missionDisplayIndex = 0
        }
    }

    val displayedStatus =
        when {
            isAssistantTyping -> {
                ChatTopBarStatus.AssistantTyping
            }

            isAssessing -> {
                ChatTopBarStatus.Assessing
            }

            !hasActiveMission -> {
                ChatTopBarStatus.Normal
            }

            missionDisplayIndex == 1 -> {
                ChatTopBarStatus.MissionProgress(
                    title =
                        missionTitle.orEmpty(),
                    percentage =
                        missionProgressPercentage
                            .coerceIn(
                                minimumValue = 0,
                                maximumValue = 100
                            )
                )
            }

            missionDisplayIndex == 2 &&
                    missionObjective != null -> {
                ChatTopBarStatus.MissionObjective(
                    objective =
                        missionObjective
                )
            }

            else -> {
                ChatTopBarStatus.Normal
            }
        }

    AnimatedContent(
        targetState =
            displayedStatus,
        modifier = modifier
            .fillMaxWidth()
            .heightIn(
                min = MINIMUM_TITLE_HEIGHT
            )
            .animateContentSize(
                animationSpec =
                    tween(
                        durationMillis =
                            SIZE_ANIMATION_DURATION_MILLIS,
                        easing =
                            FastOutSlowInEasing
                    )
            ),
        transitionSpec = {
            (
                    fadeIn(
                        animationSpec =
                            tween(
                                durationMillis =
                                    ENTER_ANIMATION_DURATION_MILLIS,
                                easing =
                                    LinearOutSlowInEasing
                            )
                    ) +
                            slideInVertically(
                                animationSpec =
                                    tween(
                                        durationMillis =
                                            ENTER_ANIMATION_DURATION_MILLIS,
                                        easing =
                                            FastOutSlowInEasing
                                    ),
                                initialOffsetY = { height ->
                                    height /
                                            VERTICAL_MOVEMENT_DIVISOR
                                }
                            ) +
                            scaleIn(
                                initialScale =
                                    ENTER_SCALE,
                                animationSpec =
                                    tween(
                                        durationMillis =
                                            ENTER_ANIMATION_DURATION_MILLIS
                                    )
                            )
                    ) togetherWith
                    (
                            fadeOut(
                                animationSpec =
                                    tween(
                                        durationMillis =
                                            EXIT_ANIMATION_DURATION_MILLIS
                                    )
                            ) +
                                    slideOutVertically(
                                        animationSpec =
                                            tween(
                                                durationMillis =
                                                    EXIT_ANIMATION_DURATION_MILLIS,
                                                easing =
                                                    FastOutSlowInEasing
                                            ),
                                        targetOffsetY = { height ->
                                            -height /
                                                    VERTICAL_MOVEMENT_DIVISOR
                                        }
                                    ) +
                                    scaleOut(
                                        targetScale =
                                            EXIT_SCALE,
                                        animationSpec =
                                            tween(
                                                durationMillis =
                                                    EXIT_ANIMATION_DURATION_MILLIS
                                            )
                                    )
                            )
        },
        contentAlignment =
            Alignment.Center,
        label =
            "ChatTopBarStatusTransition"
    ) { status ->
        when (status) {
            ChatTopBarStatus.Normal -> {
                NormalChatStatus()
            }

            ChatTopBarStatus.AssistantTyping -> {
                AssistantTypingStatus()
            }

            ChatTopBarStatus.Assessing -> {
                AssessmentStatus()
            }

            is ChatTopBarStatus.MissionProgress -> {
                MissionProgressStatus(
                    title =
                        status.title,
                    percentage =
                        status.percentage
                )
            }

            is ChatTopBarStatus.MissionObjective -> {
                MissionObjectiveStatus(
                    objective =
                        status.objective
                )
            }
        }
    }
}

@Composable
private fun NormalChatStatus() {
    Column(
        horizontalAlignment =
            Alignment.CenterHorizontally,
        verticalArrangement =
            Arrangement.Center
    ) {
        Text(
            text =
                "DeutschCraft",
            style =
                MaterialTheme
                    .typography
                    .titleMedium,
            fontWeight =
                FontWeight.SemiBold,
            color =
                MaterialTheme
                    .colorScheme
                    .onBackground,
            maxLines =
                1,
            overflow =
                TextOverflow.Ellipsis
        )

        Text(
            text =
                "Emil ist online",
            modifier =
                Modifier.padding(
                    top = 1.dp
                ),
            style =
                MaterialTheme
                    .typography
                    .labelSmall,
            color =
                MaterialTheme
                    .colorScheme
                    .onSurfaceVariant,
            maxLines =
                1
        )
    }
}

@Composable
private fun AssistantTypingStatus() {
    Row(
        verticalAlignment =
            Alignment.CenterVertically,
        horizontalArrangement =
            Arrangement.Center
    ) {
        TypingStatusDots()

        Spacer(
            modifier =
                Modifier.width(8.dp)
        )

        Column(
            horizontalAlignment =
                Alignment.Start
        ) {
            Text(
                text =
                    "Emil schreibt",
                style =
                    MaterialTheme
                        .typography
                        .titleSmall,
                fontWeight =
                    FontWeight.SemiBold,
                color =
                    MaterialTheme
                        .colorScheme
                        .onBackground,
                maxLines =
                    1
            )

            Text(
                text =
                    "Antwort wird vorbereitet",
                style =
                    MaterialTheme
                        .typography
                        .labelSmall,
                color =
                    MaterialTheme
                        .colorScheme
                        .onSurfaceVariant,
                maxLines =
                    1
            )
        }
    }
}

@Composable
private fun AssessmentStatus() {
    Row(
        verticalAlignment =
            Alignment.CenterVertically,
        horizontalArrangement =
            Arrangement.Center
    ) {
        StatusIconContainer(
            emoji = "🧠"
        )

        Spacer(
            modifier =
                Modifier.width(8.dp)
        )

        Column(
            horizontalAlignment =
                Alignment.Start
        ) {
            Text(
                text =
                    "Skill Assessment",
                style =
                    MaterialTheme
                        .typography
                        .titleSmall,
                fontWeight =
                    FontWeight.SemiBold,
                color =
                    MaterialTheme
                        .colorScheme
                        .onBackground,
                maxLines =
                    1
            )

            Text(
                text =
                    "Fortschritt wird aktualisiert",
                style =
                    MaterialTheme
                        .typography
                        .labelSmall,
                color =
                    MaterialTheme
                        .colorScheme
                        .onSurfaceVariant,
                maxLines =
                    1
            )
        }
    }
}

@Composable
private fun MissionProgressStatus(
    title: String,
    percentage: Int
) {
    Column(
        modifier =
            Modifier.fillMaxWidth(),
        horizontalAlignment =
            Alignment.CenterHorizontally,
        verticalArrangement =
            Arrangement.Center
    ) {
        Row(
            verticalAlignment =
                Alignment.CenterVertically,
            horizontalArrangement =
                Arrangement.Center
        ) {
            Text(
                text = "🎯",
                style =
                    MaterialTheme
                        .typography
                        .titleSmall
            )

            Spacer(
                modifier =
                    Modifier.width(5.dp)
            )

            Text(
                text =
                    title,
                style =
                    MaterialTheme
                        .typography
                        .titleSmall,
                fontWeight =
                    FontWeight.SemiBold,
                color =
                    MaterialTheme
                        .colorScheme
                        .onBackground,
                maxLines =
                    1,
                overflow =
                    TextOverflow.Ellipsis
            )
        }

        Spacer(
            modifier =
                Modifier.height(4.dp)
        )

        Row(
            verticalAlignment =
                Alignment.CenterVertically,
            horizontalArrangement =
                Arrangement.Center
        ) {
            LinearProgressIndicator(
                progress = {
                    percentage /
                            MAXIMUM_PERCENTAGE
                },
                modifier = Modifier
                    .width(
                        MISSION_PROGRESS_WIDTH
                    )
                    .height(
                        MISSION_PROGRESS_HEIGHT
                    )
                    .clip(
                        CircleShape
                    ),
                color =
                    MaterialTheme
                        .colorScheme
                        .primary,
                trackColor =
                    MaterialTheme
                        .colorScheme
                        .surfaceContainerHighest
            )

            Spacer(
                modifier =
                    Modifier.width(6.dp)
            )

            Text(
                text =
                    "$percentage%",
                style =
                    MaterialTheme
                        .typography
                        .labelSmall,
                fontWeight =
                    FontWeight.Bold,
                color =
                    MaterialTheme
                        .colorScheme
                        .primary
            )
        }
    }
}

@Composable
private fun MissionObjectiveStatus(
    objective: String
) {
    Row(
        modifier =
            Modifier.fillMaxWidth(),
        verticalAlignment =
            Alignment.CenterVertically,
        horizontalArrangement =
            Arrangement.Center
    ) {
        StatusIconContainer(
            emoji = "🎯"
        )

        Spacer(
            modifier =
                Modifier.width(8.dp)
        )

        Column(
            modifier =
                Modifier.heightIn(
                    min =
                        OBJECTIVE_CONTENT_MINIMUM_HEIGHT
                ),
            horizontalAlignment =
                Alignment.Start,
            verticalArrangement =
                Arrangement.Center
        ) {
            Text(
                text =
                    "Aktuelles Ziel",
                style =
                    MaterialTheme
                        .typography
                        .labelSmall,
                fontWeight =
                    FontWeight.Medium,
                color =
                    MaterialTheme
                        .colorScheme
                        .primary,
                maxLines =
                    1
            )

            Text(
                text =
                    objective,
                style =
                    MaterialTheme
                        .typography
                        .titleSmall,
                fontWeight =
                    FontWeight.SemiBold,
                color =
                    MaterialTheme
                        .colorScheme
                        .onBackground,
                maxLines =
                    1,
                overflow =
                    TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun StatusIconContainer(
    emoji: String
) {
    Box(
        modifier = Modifier
            .size(
                STATUS_ICON_CONTAINER_SIZE
            )
            .clip(
                RoundedCornerShape(
                    STATUS_ICON_CORNER_RADIUS
                )
            )
            .background(
                color =
                    MaterialTheme
                        .colorScheme
                        .primaryContainer
            ),
        contentAlignment =
            Alignment.Center
    ) {
        Text(
            text =
                emoji,
            style =
                MaterialTheme
                    .typography
                    .titleSmall,
            textAlign =
                TextAlign.Center
        )
    }
}

@Composable
private fun TypingStatusDots() {
    Row(
        modifier = Modifier
            .clip(
                RoundedCornerShape(
                    TYPING_DOTS_CORNER_RADIUS
                )
            )
            .background(
                color =
                    MaterialTheme
                        .colorScheme
                        .surfaceContainerHigh
            )
            .padding(
                horizontal = 9.dp,
                vertical = 8.dp
            ),
        horizontalArrangement =
            Arrangement.spacedBy(
                3.dp
            ),
        verticalAlignment =
            Alignment.CenterVertically
    ) {
        repeat(
            times = TYPING_DOT_COUNT
        ) {
            Box(
                modifier = Modifier
                    .size(
                        TYPING_DOT_SIZE
                    )
                    .clip(
                        CircleShape
                    )
                    .background(
                        color =
                            MaterialTheme
                                .colorScheme
                                .primary
                    )
            )
        }
    }
}

private val MINIMUM_TITLE_HEIGHT =
    42.dp

private val MISSION_PROGRESS_WIDTH =
    76.dp

private val MISSION_PROGRESS_HEIGHT =
    4.dp

private val STATUS_ICON_CONTAINER_SIZE =
    32.dp

private val STATUS_ICON_CORNER_RADIUS =
    12.dp

private val OBJECTIVE_CONTENT_MINIMUM_HEIGHT =
    34.dp

private val TYPING_DOTS_CORNER_RADIUS =
    14.dp

private val TYPING_DOT_SIZE =
    4.dp

private const val TYPING_DOT_COUNT =
    3

private const val MAXIMUM_PERCENTAGE =
    100f

private const val NORMAL_STATUS_DURATION_MILLIS =
    4_500L

private const val MISSION_PROGRESS_DURATION_MILLIS =
    4_000L

private const val MISSION_OBJECTIVE_DURATION_MILLIS =
    4_000L

private const val ENTER_ANIMATION_DURATION_MILLIS =
    420

private const val EXIT_ANIMATION_DURATION_MILLIS =
    260

private const val SIZE_ANIMATION_DURATION_MILLIS =
    420

private const val VERTICAL_MOVEMENT_DIVISOR =
    5

private const val ENTER_SCALE =
    0.97f

private const val EXIT_SCALE =
    0.98f