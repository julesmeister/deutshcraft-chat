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
import com.julesmeister.deutschcraft.feature.chat.presentation.MissionObjectiveCompletionEvent
import com.julesmeister.deutschcraft.ui.theme.DeutschLime
import kotlinx.coroutines.delay

@Composable
internal fun AnimatedChatTopBarTitle(
    isAssistantTyping: Boolean,
    isAssessing: Boolean,
    activeMissionTitle: String?,
    currentMissionObjective: String?,
    currentMissionGuidance: String?,
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
            ?.takeIf(
                String::isNotBlank
            )

    val missionObjective =
        currentMissionObjective
            ?.trim()
            ?.takeIf(
                String::isNotBlank
            )

    /*
     * This is learner-facing guidance from the objective's
     * description.
     *
     * The internal completionHint is not displayed.
     */
    val missionGuidance =
        currentMissionGuidance
            ?.trim()
            ?.takeIf(
                String::isNotBlank
            )

    var missionDisplayIndex by
    remember(
        missionTitle,
        missionObjective,
        missionGuidance
    ) {
        mutableIntStateOf(
            NORMAL_DISPLAY_INDEX
        )
    }

    /*
     * When a mission is active, rotate through:
     *
     * 1. Normal Emil status
     * 2. Mission title and progress
     * 3. Current objective and learner guidance
     *
     * Typing, assessment, and completion feedback interrupt
     * this rotation because they have higher visual priority.
     */
    LaunchedEffect(
        missionTitle,
        missionObjective,
        missionGuidance,
        isAssistantTyping,
        isAssessing,
        objectiveCompletionEvent?.eventId
    ) {
        missionDisplayIndex =
            NORMAL_DISPLAY_INDEX

        if (
            missionTitle == null ||
            isAssistantTyping ||
            isAssessing ||
            objectiveCompletionEvent != null
        ) {
            return@LaunchedEffect
        }

        while (true) {
            delay(
                NORMAL_STATUS_DISPLAY_MILLIS
            )

            missionDisplayIndex =
                MISSION_PROGRESS_DISPLAY_INDEX

            delay(
                MISSION_PROGRESS_DISPLAY_MILLIS
            )

            if (missionObjective != null) {
                missionDisplayIndex =
                    MISSION_OBJECTIVE_DISPLAY_INDEX

                delay(
                    MISSION_OBJECTIVE_DISPLAY_MILLIS
                )
            }

            missionDisplayIndex =
                NORMAL_DISPLAY_INDEX
        }
    }

    /*
     * Objective-completion feedback remains visible for a
     * short period before being consumed from ChatUiState.
     */
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

            missionDisplayIndex ==
                    MISSION_PROGRESS_DISPLAY_INDEX -> {

                ChatTopBarStatus.MissionProgress(
                    title =
                        missionTitle,
                    percentage =
                        missionProgressPercentage
                            .coerceIn(
                                minimumValue = 0,
                                maximumValue = 100
                            )
                )
            }

            missionDisplayIndex ==
                    MISSION_OBJECTIVE_DISPLAY_INDEX &&
                    missionObjective != null -> {

                ChatTopBarStatus.MissionObjective(
                    objective =
                        missionObjective,
                    guidance =
                        missionGuidance
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
            .heightIn(
                min =
                    TOP_BAR_CONTENT_MINIMUM_HEIGHT
            )
            .animateContentSize(
                animationSpec =
                    tween(
                        durationMillis =
                            STATUS_SIZE_ANIMATION_MILLIS,
                        easing =
                            FastOutSlowInEasing
                    )
            ),
        transitionSpec = {
            val enterTransition =
                fadeIn(
                    animationSpec =
                        tween(
                            durationMillis =
                                STATUS_ENTER_DURATION_MILLIS,
                            easing =
                                LinearOutSlowInEasing
                        )
                ) +
                        slideInVertically(
                            initialOffsetY = { height ->
                                height / 5
                            },
                            animationSpec =
                                tween(
                                    durationMillis =
                                        STATUS_ENTER_DURATION_MILLIS,
                                    easing =
                                        FastOutSlowInEasing
                                )
                        ) +
                        scaleIn(
                            initialScale =
                                STATUS_ENTER_SCALE
                        )

            val exitTransition =
                fadeOut(
                    animationSpec =
                        tween(
                            durationMillis =
                                STATUS_EXIT_DURATION_MILLIS
                        )
                ) +
                        slideOutVertically(
                            targetOffsetY = { height ->
                                -height / 5
                            },
                            animationSpec =
                                tween(
                                    durationMillis =
                                        STATUS_EXIT_DURATION_MILLIS
                                )
                        ) +
                        scaleOut(
                            targetScale =
                                STATUS_EXIT_SCALE
                        )

            enterTransition togetherWith
                    exitTransition
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
                    icon =
                        "•••",
                    title =
                        "Emil schreibt",
                    subtitle =
                        "Antwort wird vorbereitet"
                )
            }

            ChatTopBarStatus.Assessing -> {
                SimpleStatus(
                    icon =
                        "🧠",
                    title =
                        "Skill Assessment",
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
                MissionGuidanceStatus(
                    objective =
                        displayedStatus.objective,
                    guidance =
                        displayedStatus.guidance
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
        modifier =
            Modifier.fillMaxWidth(),
        horizontalAlignment =
            Alignment.Start
    ) {
        Text(
            text =
                "DeutschCraft",
            modifier =
                Modifier.fillMaxWidth(),
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
            textAlign =
                TextAlign.Start,
            maxLines =
                1,
            overflow =
                TextOverflow.Ellipsis
        )

        Row(
            modifier =
                Modifier.padding(
                    top =
                        NORMAL_SUBTITLE_TOP_PADDING
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
        modifier =
            Modifier.fillMaxWidth(),
        verticalAlignment =
            Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(
                    STATUS_ICON_SIZE
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
                    icon,
                textAlign =
                    TextAlign.Center
            )
        }

        Spacer(
            modifier =
                Modifier.width(
                    STATUS_CONTENT_SPACING
                )
        )

        Column(
            modifier =
                Modifier.weight(1f),
            horizontalAlignment =
                Alignment.Start
        ) {
            Text(
                text =
                    title,
                modifier =
                    Modifier.fillMaxWidth(),
                style =
                    MaterialTheme
                        .typography
                        .titleSmall,
                fontWeight =
                    FontWeight.SemiBold,
                textAlign =
                    TextAlign.Start,
                maxLines =
                    1,
                overflow =
                    TextOverflow.Ellipsis
            )

            Text(
                text =
                    subtitle,
                modifier =
                    Modifier.fillMaxWidth(),
                style =
                    MaterialTheme
                        .typography
                        .labelSmall,
                color =
                    MaterialTheme
                        .colorScheme
                        .onSurfaceVariant,
                textAlign =
                    TextAlign.Start,
                maxLines =
                    1,
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
    val safePercentage =
        percentage.coerceIn(
            minimumValue = 0,
            maximumValue = 100
        )

    Column(
        modifier =
            Modifier.fillMaxWidth(),
        horizontalAlignment =
            Alignment.Start
    ) {
        Text(
            text =
                "🎯 $title",
            modifier =
                Modifier.fillMaxWidth(),
            style =
                MaterialTheme
                    .typography
                    .titleSmall,
            fontWeight =
                FontWeight.SemiBold,
            textAlign =
                TextAlign.Start,
            maxLines =
                1,
            overflow =
                TextOverflow.Ellipsis
        )

        Spacer(
            modifier =
                Modifier.height(
                    MISSION_PROGRESS_VERTICAL_SPACING
                )
        )

        Row(
            modifier =
                Modifier.fillMaxWidth(),
            horizontalArrangement =
                Arrangement.Start,
            verticalAlignment =
                Alignment.CenterVertically
        ) {
            LinearProgressIndicator(
                progress = {
                    safePercentage / 100f
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
                    )
            )

            Spacer(
                modifier =
                    Modifier.width(
                        MISSION_PERCENTAGE_SPACING
                    )
            )

            Text(
                text =
                    "$safePercentage%",
                style =
                    MaterialTheme
                        .typography
                        .labelSmall,
                fontWeight =
                    FontWeight.Bold,
                color =
                    MaterialTheme
                        .colorScheme
                        .primary,
                textAlign =
                    TextAlign.Start
            )
        }
    }
}

@Composable
private fun MissionGuidanceStatus(
    objective: String,
    guidance: String?
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
                    MISSION_GUIDANCE_ICON_SIZE
                )
                .clip(
                    RoundedCornerShape(
                        MISSION_GUIDANCE_ICON_RADIUS
                    )
                )
                .background(
                    color =
                        DeutschLime.copy(
                            alpha =
                                MISSION_GUIDANCE_ICON_ALPHA
                        )
                ),
            contentAlignment =
                Alignment.Center
        ) {
            Text(
                text =
                    "💡",
                textAlign =
                    TextAlign.Center
            )
        }

        Spacer(
            modifier =
                Modifier.width(
                    MISSION_GUIDANCE_SPACING
                )
        )

        Column(
            modifier =
                Modifier.weight(1f),
            horizontalAlignment =
                Alignment.Start
        ) {
            Text(
                text =
                    objective,
                modifier =
                    Modifier.fillMaxWidth(),
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
                textAlign =
                    TextAlign.Start,
                maxLines =
                    1,
                overflow =
                    TextOverflow.Ellipsis
            )

            Text(
                text =
                    guidance
                        ?: DEFAULT_MISSION_GUIDANCE,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(
                            top =
                                MISSION_GUIDANCE_TEXT_SPACING
                        ),
                style =
                    MaterialTheme
                        .typography
                        .labelSmall,
                color =
                    MaterialTheme
                        .colorScheme
                        .onSurfaceVariant,
                textAlign =
                    TextAlign.Start,
                maxLines =
                    1,
                overflow =
                    TextOverflow.Ellipsis
            )
        }
    }
}

private const val NORMAL_DISPLAY_INDEX =
    0

private const val MISSION_PROGRESS_DISPLAY_INDEX =
    1

private const val MISSION_OBJECTIVE_DISPLAY_INDEX =
    2

private const val NORMAL_STATUS_DISPLAY_MILLIS =
    4_500L

private const val MISSION_PROGRESS_DISPLAY_MILLIS =
    4_000L

private const val MISSION_OBJECTIVE_DISPLAY_MILLIS =
    5_000L

private const val OBJECTIVE_COMPLETION_DISPLAY_MILLIS =
    1_650L

private const val DEFAULT_MISSION_GUIDANCE =
    "Continue the conversation to complete this objective."

private val TOP_BAR_CONTENT_MINIMUM_HEIGHT =
    42.dp

private val ONLINE_DOT_SIZE =
    7.dp

private val ONLINE_DOT_SPACING =
    5.dp

private val NORMAL_SUBTITLE_TOP_PADDING =
    1.dp

private val STATUS_ICON_SIZE =
    32.dp

private val STATUS_ICON_CORNER_RADIUS =
    12.dp

private val STATUS_CONTENT_SPACING =
    8.dp

private val MISSION_PROGRESS_VERTICAL_SPACING =
    4.dp

private val MISSION_PROGRESS_WIDTH =
    76.dp

private val MISSION_PROGRESS_HEIGHT =
    4.dp

private val MISSION_PERCENTAGE_SPACING =
    6.dp

private val MISSION_GUIDANCE_ICON_SIZE =
    32.dp

private val MISSION_GUIDANCE_ICON_RADIUS =
    12.dp

private val MISSION_GUIDANCE_SPACING =
    8.dp

private val MISSION_GUIDANCE_TEXT_SPACING =
    1.dp

private const val MISSION_GUIDANCE_ICON_ALPHA =
    0.15f

private const val STATUS_SIZE_ANIMATION_MILLIS =
    380

private const val STATUS_ENTER_DURATION_MILLIS =
    360

private const val STATUS_EXIT_DURATION_MILLIS =
    220

private const val STATUS_ENTER_SCALE =
    0.97f

private const val STATUS_EXIT_SCALE =
    0.98f