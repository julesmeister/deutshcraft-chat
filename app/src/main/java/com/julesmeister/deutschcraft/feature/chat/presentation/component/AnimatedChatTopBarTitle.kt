package com.julesmeister.deutschcraft.feature.chat.presentation.component

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.julesmeister.deutschcraft.feature.chat.presentation.MissionObjectiveCompletionEvent
import kotlinx.coroutines.delay
import com.julesmeister.deutschcraft.ui.theme.DeutschLime
@Composable
internal fun AnimatedChatTopBarTitle(
    isAssistantTyping: Boolean,
    isAssessing: Boolean,
    activeMissionTitle: String?,
    currentMissionObjective: String?,
    missionProgressPercentage: Int,
    objectiveCompletionEvent:
    MissionObjectiveCompletionEvent?,
    onObjectiveCompletionShown:
        (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val missionTitle =
        activeMissionTitle
            ?.trim()
            ?.takeIf(String::isNotBlank)

    val missionObjective =
        currentMissionObjective
            ?.trim()
            ?.takeIf(String::isNotBlank)

    var missionDisplayIndex by remember(
        missionTitle,
        missionObjective,
        missionProgressPercentage
    ) {
        mutableIntStateOf(0)
    }

    LaunchedEffect(
        missionTitle,
        missionObjective,
        isAssistantTyping,
        isAssessing,
        objectiveCompletionEvent?.eventId
    ) {
        missionDisplayIndex = 0

        if (
            missionTitle == null ||
            isAssistantTyping ||
            isAssessing ||
            objectiveCompletionEvent != null
        ) {
            return@LaunchedEffect
        }

        while (true) {
            delay(4_500L)
            missionDisplayIndex = 1

            delay(4_000L)

            if (missionObjective != null) {
                missionDisplayIndex = 2
                delay(4_000L)
            }

            missionDisplayIndex = 0
        }
    }

    LaunchedEffect(
        objectiveCompletionEvent?.eventId
    ) {
        val event =
            objectiveCompletionEvent
                ?: return@LaunchedEffect

        delay(
            OBJECTIVE_COMPLETION_DISPLAY_MILLIS
        )

        onObjectiveCompletionShown(
            event.eventId
        )
    }

    val status =
        when {
            objectiveCompletionEvent != null -> {
                ChatTopBarStatus.ObjectiveCompleted(
                    eventId =
                        objectiveCompletionEvent.eventId,
                    objectiveTitle =
                        objectiveCompletionEvent
                            .objectiveTitle
                )
            }

            isAssistantTyping -> {
                ChatTopBarStatus.AssistantTyping
            }

            isAssessing -> {
                ChatTopBarStatus.Assessing
            }

            missionTitle == null -> {
                ChatTopBarStatus.Normal
            }

            missionDisplayIndex == 1 -> {
                ChatTopBarStatus.MissionProgress(
                    title =
                        missionTitle,
                    percentage =
                        missionProgressPercentage
                            .coerceIn(0, 100)
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
            status,
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 42.dp)
            .animateContentSize(
                animationSpec =
                    tween(
                        durationMillis = 380,
                        easing =
                            FastOutSlowInEasing
                    )
            ),
        transitionSpec = {
            (
                    fadeIn(
                        tween(
                            durationMillis = 360,
                            easing =
                                LinearOutSlowInEasing
                        )
                    ) +
                            slideInVertically(
                                initialOffsetY = {
                                    it / 5
                                },
                                animationSpec =
                                    tween(
                                        durationMillis = 360,
                                        easing =
                                            FastOutSlowInEasing
                                    )
                            ) +
                            scaleIn(
                                initialScale = 0.97f
                            )
                    ) togetherWith
                    (
                            fadeOut(
                                tween(220)
                            ) +
                                    slideOutVertically(
                                        targetOffsetY = {
                                            -it / 5
                                        },
                                        animationSpec =
                                            tween(220)
                                    ) +
                                    scaleOut(
                                        targetScale = 0.98f
                                    )
                            )
        },
        contentAlignment =
            Alignment.CenterStart,
        label =
            "ChatTopBarStatus"
    ) { displayedStatus ->
        when (displayedStatus) {
            ChatTopBarStatus.Normal -> {
                NormalStatus()
            }

            ChatTopBarStatus.AssistantTyping -> {
                SimpleStatus(
                    icon = "•••",
                    title = "Emil schreibt",
                    subtitle =
                        "Antwort wird vorbereitet"
                )
            }

            ChatTopBarStatus.Assessing -> {
                SimpleStatus(
                    icon = "🧠",
                    title = "Skill Assessment",
                    subtitle =
                        "Fortschritt wird aktualisiert"
                )
            }

            is ChatTopBarStatus.MissionProgress -> {
                MissionProgressStatus(
                    title =
                        displayedStatus.title,
                    percentage =
                        displayedStatus.percentage
                )
            }

            is ChatTopBarStatus.MissionObjective -> {
                SimpleStatus(
                    icon = "🎯",
                    title = "Aktuelles Ziel",
                    subtitle =
                        displayedStatus.objective
                )
            }

            is ChatTopBarStatus.ObjectiveCompleted -> {
                MissionObjectiveCompletedStatus(
                    eventId =
                        displayedStatus.eventId,
                    objectiveTitle =
                        displayedStatus.objectiveTitle
                )
            }
        }
    }
}

@Composable
private fun NormalStatus() {
    Column(
        horizontalAlignment =
            Alignment.Start
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

        Row(
            modifier =
                Modifier.padding(
                    top = 1.dp
                ),
            verticalAlignment =
                Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(
                        ONLINE_DOT_SIZE
                    )
                    .clip(
                        CircleShape
                    )
                    .background(
                        color =
                            DeutschLime
                    )
            )

            Spacer(
                modifier =
                    Modifier.width(
                        ONLINE_DOT_SPACING
                    )
            )

            Text(
                text =
                    "Emil ist online",
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
private fun SimpleStatus(
    icon: String,
    title: String,
    subtitle: String
) {
    Row(
        verticalAlignment =
            Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(
                    RoundedCornerShape(12.dp)
                )
                .background(
                    MaterialTheme
                        .colorScheme
                        .primaryContainer
                ),
            contentAlignment =
                Alignment.Center
        ) {
            Text(
                text = icon,
                textAlign =
                    TextAlign.Center
            )
        }

        Spacer(
            modifier =
                Modifier.width(8.dp)
        )

        Column {
            Text(
                text = title,
                style =
                    MaterialTheme
                        .typography
                        .titleSmall,
                fontWeight =
                    FontWeight.SemiBold,
                maxLines = 1
            )

            Text(
                text = subtitle,
                style =
                    MaterialTheme
                        .typography
                        .labelSmall,
                color =
                    MaterialTheme
                        .colorScheme
                        .onSurfaceVariant,
                maxLines = 1,
                overflow =
                    TextOverflow.Ellipsis
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
        horizontalAlignment =
            Alignment.CenterHorizontally
    ) {
        Text(
            text = "🎯 $title",
            style =
                MaterialTheme
                    .typography
                    .titleSmall,
            fontWeight =
                FontWeight.SemiBold,
            maxLines = 1,
            overflow =
                TextOverflow.Ellipsis
        )

        Spacer(
            modifier =
                Modifier.height(4.dp)
        )

        Row(
            verticalAlignment =
                Alignment.CenterVertically
        ) {
            LinearProgressIndicator(
                progress = {
                    percentage / 100f
                },
                modifier = Modifier
                    .width(76.dp)
                    .height(4.dp)
                    .clip(CircleShape)
            )

            Spacer(
                modifier =
                    Modifier.width(6.dp)
            )

            Text(
                text = "$percentage%",
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

private const val OBJECTIVE_COMPLETION_DISPLAY_MILLIS =
    1_650L

private val ONLINE_DOT_SIZE =
    7.dp

private val ONLINE_DOT_SPACING =
    5.dp