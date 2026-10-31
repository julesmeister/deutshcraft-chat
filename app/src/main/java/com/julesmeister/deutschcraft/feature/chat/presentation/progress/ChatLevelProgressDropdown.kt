// app/src/main/java/com/julesmeister/deutschcraft/feature/chat/presentation/progress/ChatLevelProgressDropdown.kt

package com.julesmeister.deutschcraft.feature.chat.presentation.progress

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandIn
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties

@Composable
internal fun ChatLevelProgressDropdown(
    state:
    ChatLevelProgressUiState,

    expanded:
    Boolean,

    onDismiss:
        () -> Unit
) {
    if (!expanded) {
        return
    }

    Popup(
        popupPositionProvider =
            ConnectedProgressPopupPositionProvider,

        onDismissRequest =
            onDismiss,

        properties =
            PopupProperties(
                focusable =
                    true,

                dismissOnBackPress =
                    true,

                dismissOnClickOutside =
                    true,

                clippingEnabled =
                    false
            )
    ) {
        AnimatedVisibility(
            visible =
                expanded,

            enter =
                fadeIn(
                    animationSpec =
                        tween(
                            durationMillis =
                                DROPDOWN_ENTER_MILLIS
                        )
                ) +
                        scaleIn(
                            initialScale =
                                DROPDOWN_INITIAL_SCALE,

                            transformOrigin =
                                TransformOrigin(
                                    pivotFractionX =
                                        0.92f,

                                    pivotFractionY =
                                        0f
                                ),

                            animationSpec =
                                spring(
                                    dampingRatio =
                                        Spring
                                            .DampingRatioNoBouncy,

                                    stiffness =
                                        Spring
                                            .StiffnessMediumLow
                                )
                        ) +
                        expandIn(
                            expandFrom =
                                Alignment.TopEnd,

                            animationSpec =
                                tween(
                                    durationMillis =
                                        DROPDOWN_ENTER_MILLIS,

                                    easing =
                                        FastOutSlowInEasing
                                )
                        ),

            exit =
                fadeOut(
                    animationSpec =
                        tween(
                            durationMillis =
                                DROPDOWN_EXIT_MILLIS
                        )
                ) +
                        scaleOut(
                            targetScale =
                                DROPDOWN_EXIT_SCALE,

                            transformOrigin =
                                TransformOrigin(
                                    pivotFractionX =
                                        0.92f,

                                    pivotFractionY =
                                        0f
                                )
                        ) +
                        shrinkOut(
                            shrinkTowards =
                                Alignment.TopEnd
                        )
        ) {
            ConnectedProgressBento(
                state =
                    state
            )
        }
    }
}

@Composable
private fun ConnectedProgressBento(
    state:
    ChatLevelProgressUiState
) {
    val colors =
        MaterialTheme.colorScheme

    val accent =
        readinessColor(
            readiness =
                state.safeReadinessPercentage
        )

    val need =
        state.immediateNeed

    Column(
        modifier =
            Modifier
                .requiredWidth(
                    DROPDOWN_WIDTH
                )
    ) {
        Row(
            modifier =
                Modifier.fillMaxWidth(),

            horizontalArrangement =
                Arrangement.End
        ) {
            Box(
                modifier =
                    Modifier
                        .padding(
                            end =
                                CONNECTOR_END_PADDING
                        )
                        .width(
                            CONNECTOR_WIDTH
                        )
                        .height(
                            CONNECTOR_HEIGHT
                        )
                        .background(
                            color =
                                colors.surfaceContainerHigh,

                            shape =
                                RoundedCornerShape(
                                    topStart =
                                        CONNECTOR_TOP_RADIUS,

                                    topEnd =
                                        CONNECTOR_TOP_RADIUS
                                )
                        )
                        .border(
                            width =
                                DROPDOWN_BORDER_WIDTH,

                            color =
                                colors.outlineVariant.copy(
                                    alpha =
                                        DROPDOWN_BORDER_ALPHA
                                ),

                            shape =
                                RoundedCornerShape(
                                    topStart =
                                        CONNECTOR_TOP_RADIUS,

                                    topEnd =
                                        CONNECTOR_TOP_RADIUS
                                )
                        )
            )
        }

        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .clip(
                        DROPDOWN_SHAPE
                    )
                    .background(
                        colors.surfaceContainerHigh
                    )
                    .border(
                        width =
                            DROPDOWN_BORDER_WIDTH,

                        color =
                            colors.outlineVariant.copy(
                                alpha =
                                    DROPDOWN_BORDER_ALPHA
                            ),

                        shape =
                            DROPDOWN_SHAPE
                    )
                    .padding(
                        DROPDOWN_PADDING
                    ),

            verticalArrangement =
                Arrangement.spacedBy(
                    BENTO_GAP
                )
        ) {
            ProgressHeaderBento(
                state =
                    state,

                accent =
                    accent
            )

            if (need == null) {
                EmptyNeedBento(
                    accent =
                        accent
                )
            } else {
                PrimaryNeedBento(
                    need =
                        need,

                    accent =
                        accent
                )

                Row(
                    modifier =
                        Modifier.fillMaxWidth(),

                    horizontalArrangement =
                        Arrangement.spacedBy(
                            BENTO_GAP
                        )
                ) {
                    TeachingTargetBento(
                        label =
                            "SAY IT WITH",

                        values =
                            need.grammarTargets,

                        accent =
                            accent,

                        modifier =
                            Modifier.weight(
                                1f
                            )
                    )

                    TeachingTargetBento(
                        label =
                            "USE MORE",

                        values =
                            need.vocabularyTargets,

                        accent =
                            accent,

                        modifier =
                            Modifier.weight(
                                1f
                            )
                    )
                }

                ExampleBento(
                    phrase =
                        need.examplePhrase,

                    accent =
                        accent
                )
            }
        }
    }
}

@Composable
private fun ProgressHeaderBento(
    state:
    ChatLevelProgressUiState,

    accent:
    Color
) {
    val colors =
        MaterialTheme.colorScheme

    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(
                    HEADER_SHAPE
                )
                .background(
                    accent.copy(
                        alpha =
                            HEADER_BACKGROUND_ALPHA
                    )
                )
                .padding(
                    HEADER_PADDING
                ),

        verticalAlignment =
            Alignment.CenterVertically
    ) {
        Box(
            modifier =
                Modifier
                    .size(
                        HEADER_LEVEL_SIZE
                    )
                    .clip(
                        CircleShape
                    )
                    .background(
                        accent
                    ),

            contentAlignment =
                Alignment.Center
        ) {
            Text(
                text =
                    state.levelLabel,

                style =
                    MaterialTheme
                        .typography
                        .labelLarge,

                fontWeight =
                    FontWeight.ExtraBold,

                color =
                    Color.White
            )
        }

        Spacer(
            modifier =
                Modifier.width(
                    HEADER_CONTENT_SPACING
                )
        )

        Column(
            modifier =
                Modifier.weight(
                    1f
                )
        ) {
            Text(
                text =
                    state.progressTitle,

                style =
                    MaterialTheme
                        .typography
                        .titleSmall,

                fontWeight =
                    FontWeight.Bold,

                color =
                    colors.onSurface,

                maxLines =
                    1,

                overflow =
                    TextOverflow.Ellipsis
            )

            Spacer(
                modifier =
                    Modifier.height(
                        5.dp
                    )
            )

            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(
                            READINESS_TRACK_HEIGHT
                        )
                        .clip(
                            CircleShape
                        )
                        .background(
                            colors.surface.copy(
                                alpha =
                                    READINESS_TRACK_ALPHA
                            )
                        )
            ) {
                Box(
                    modifier =
                        Modifier
                            .fillMaxWidth(
                                fraction =
                                    state
                                        .safeReadinessPercentage /
                                            100f
                            )
                            .height(
                                READINESS_TRACK_HEIGHT
                            )
                            .clip(
                                CircleShape
                            )
                            .background(
                                accent
                            )
                )
            }
        }

        Spacer(
            modifier =
                Modifier.width(
                    HEADER_CONTENT_SPACING
                )
        )

        Text(
            text =
                if (state.isHighestLevel) {
                    "C2"
                } else {
                    "${state.safeReadinessPercentage}%"
                },

            style =
                MaterialTheme
                    .typography
                    .titleMedium,

            fontWeight =
                FontWeight.ExtraBold,

            color =
                accent
        )
    }
}

@Composable
private fun PrimaryNeedBento(
    need:
    ChatImmediateLevelNeedUiModel,

    accent:
    Color
) {
    val colors =
        MaterialTheme.colorScheme

    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(
                    PRIMARY_NEED_SHAPE
                )
                .background(
                    colors.surfaceContainer
                )
                .border(
                    width =
                        PRIMARY_NEED_BORDER_WIDTH,

                    color =
                        accent.copy(
                            alpha =
                                PRIMARY_NEED_BORDER_ALPHA
                        ),

                    shape =
                        PRIMARY_NEED_SHAPE
                )
                .padding(
                    PRIMARY_NEED_PADDING
                ),

        verticalArrangement =
            Arrangement.spacedBy(
                8.dp
            )
    ) {
        Row(
            modifier =
                Modifier.fillMaxWidth(),

            verticalAlignment =
                Alignment.CenterVertically
        ) {
            Text(
                text =
                    "NEXT BEST STEP",

                modifier =
                    Modifier.weight(
                        1f
                    ),

                style =
                    MaterialTheme
                        .typography
                        .labelSmall,

                fontWeight =
                    FontWeight.Bold,

                color =
                    accent
            )

            Text(
                text =
                    need.statusLabel,

                style =
                    MaterialTheme
                        .typography
                        .labelSmall,

                fontWeight =
                    FontWeight.SemiBold,

                color =
                    colors.onSurfaceVariant
            )
        }

        Text(
            text =
                need.title,

            style =
                MaterialTheme
                    .typography
                    .titleMedium,

            fontWeight =
                FontWeight.Bold,

            color =
                colors.onSurface
        )

        Text(
            text =
                need.explanation,

            style =
                MaterialTheme
                    .typography
                    .bodySmall,

            color =
                colors.onSurfaceVariant
        )

        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .clip(
                        WHY_NOW_SHAPE
                    )
                    .background(
                        accent.copy(
                            alpha =
                                WHY_NOW_BACKGROUND_ALPHA
                        )
                    )
                    .padding(
                        WHY_NOW_PADDING
                    )
        ) {
            Column(
                verticalArrangement =
                    Arrangement.spacedBy(
                        3.dp
                    )
            ) {
                Text(
                    text =
                        "WHY NOW",

                    style =
                        MaterialTheme
                            .typography
                            .labelSmall,

                    fontWeight =
                        FontWeight.Bold,

                    color =
                        accent
                )

                Text(
                    text =
                        need.whyNow,

                    style =
                        MaterialTheme
                            .typography
                            .bodySmall,

                    color =
                        colors.onSurface
                )
            }
        }
    }
}

@Composable
private fun TeachingTargetBento(
    label:
    String,

    values:
    List<String>,

    accent:
    Color,

    modifier:
    Modifier =
        Modifier
) {
    val colors =
        MaterialTheme.colorScheme

    Column(
        modifier =
            modifier
                .height(
                    TEACHING_BENTO_HEIGHT
                )
                .clip(
                    SMALL_BENTO_SHAPE
                )
                .background(
                    colors.surfaceContainer
                )
                .padding(
                    SMALL_BENTO_PADDING
                ),

        verticalArrangement =
            Arrangement.spacedBy(
                6.dp
            )
    ) {
        Text(
            text =
                label,

            style =
                MaterialTheme
                    .typography
                    .labelSmall,

            fontWeight =
                FontWeight.Bold,

            color =
                accent
        )

        values
            .take(
                MAXIMUM_TARGET_COUNT
            )
            .forEach { value ->
                Row(
                    verticalAlignment =
                        Alignment.Top
                ) {
                    Box(
                        modifier =
                            Modifier
                                .padding(
                                    top =
                                        TARGET_DOT_TOP_PADDING
                                )
                                .size(
                                    TARGET_DOT_SIZE
                                )
                                .clip(
                                    CircleShape
                                )
                                .background(
                                    accent
                                )
                    )

                    Spacer(
                        modifier =
                            Modifier.width(
                                TARGET_DOT_SPACING
                            )
                    )

                    Text(
                        text =
                            value,

                        style =
                            MaterialTheme
                                .typography
                                .labelSmall,

                        color =
                            colors.onSurface,

                        maxLines =
                            2,

                        overflow =
                            TextOverflow.Ellipsis
                    )
                }
            }
    }
}

@Composable
private fun ExampleBento(
    phrase:
    String,

    accent:
    Color
) {
    val colors =
        MaterialTheme.colorScheme

    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(
                    EXAMPLE_SHAPE
                )
                .background(
                    accent.copy(
                        alpha =
                            EXAMPLE_BACKGROUND_ALPHA
                    )
                )
                .padding(
                    EXAMPLE_PADDING
                ),

        verticalArrangement =
            Arrangement.spacedBy(
                5.dp
            )
    ) {
        Text(
            text =
                "TRY SAYING",

            style =
                MaterialTheme
                    .typography
                    .labelSmall,

            fontWeight =
                FontWeight.Bold,

            color =
                accent
        )

        Text(
            text =
                "„$phrase“",

            style =
                MaterialTheme
                    .typography
                    .bodyMedium,

            fontWeight =
                FontWeight.Medium,

            fontStyle =
                FontStyle.Italic,

            color =
                colors.onSurface
        )
    }
}

@Composable
private fun EmptyNeedBento(
    accent:
    Color
) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(
                    PRIMARY_NEED_SHAPE
                )
                .background(
                    MaterialTheme
                        .colorScheme
                        .surfaceContainer
                )
                .padding(
                    PRIMARY_NEED_PADDING
                ),

        verticalArrangement =
            Arrangement.spacedBy(
                6.dp
            )
    ) {
        Text(
            text =
                "BUILDING YOUR NEXT STEP",

            style =
                MaterialTheme
                    .typography
                    .labelSmall,

            fontWeight =
                FontWeight.Bold,

            color =
                accent
        )

        Text(
            text =
                "Keep chatting naturally. DeutschCraft is collecting enough evidence to identify one specific next-level requirement.",

            style =
                MaterialTheme
                    .typography
                    .bodySmall,

            color =
                MaterialTheme
                    .colorScheme
                    .onSurfaceVariant
        )
    }
}

private object ConnectedProgressPopupPositionProvider :
    PopupPositionProvider {

    override fun calculatePosition(
        anchorBounds:
        IntRect,

        windowSize:
        IntSize,

        layoutDirection:
        LayoutDirection,

        popupContentSize:
        IntSize
    ): IntOffset {
        val preferredX =
            anchorBounds.right -
                    popupContentSize.width

        val minimumX =
            POPUP_WINDOW_MARGIN_PX

        val maximumX =
            (
                    windowSize.width -
                            popupContentSize.width -
                            POPUP_WINDOW_MARGIN_PX
                    )
                .coerceAtLeast(
                    minimumValue =
                        minimumX
                )

        val x =
            preferredX.coerceIn(
                minimumValue =
                    minimumX,

                maximumValue =
                    maximumX
            )

        val preferredY =
            anchorBounds.bottom -
                    POPUP_CONNECTION_OVERLAP_PX

        val maximumY =
            (
                    windowSize.height -
                            popupContentSize.height -
                            POPUP_WINDOW_MARGIN_PX
                    )
                .coerceAtLeast(
                    minimumValue =
                        POPUP_WINDOW_MARGIN_PX
                )

        val y =
            preferredY.coerceIn(
                minimumValue =
                    POPUP_WINDOW_MARGIN_PX,

                maximumValue =
                    maximumY
            )

        return IntOffset(
            x =
                x,

            y =
                y
        )
    }
}

private fun readinessColor(
    readiness:
    Int
): Color {
    val progress =
        readiness
            .coerceIn(
                minimumValue = 0,
                maximumValue = 100
            ) /
                100f

    return if (
        progress <=
        COLOR_MIDPOINT
    ) {
        lerp(
            start =
                PROGRESS_ORANGE,

            stop =
                PROGRESS_AMBER,

            fraction =
                progress /
                        COLOR_MIDPOINT
        )
    } else {
        lerp(
            start =
                PROGRESS_AMBER,

            stop =
                PROGRESS_GREEN,

            fraction =
                (
                        progress -
                                COLOR_MIDPOINT
                        ) /
                        COLOR_MIDPOINT
        )
    }
}

private val DROPDOWN_WIDTH =
    304.dp

private val DROPDOWN_PADDING =
    12.dp

private val DROPDOWN_SHAPE =
    RoundedCornerShape(
        topStart =
            26.dp,

        topEnd =
            8.dp,

        bottomStart =
            26.dp,

        bottomEnd =
            26.dp
    )

private val DROPDOWN_BORDER_WIDTH =
    1.dp

private val CONNECTOR_WIDTH =
    34.dp

private val CONNECTOR_HEIGHT =
    12.dp

private val CONNECTOR_END_PADDING =
    7.dp

private val CONNECTOR_TOP_RADIUS =
    12.dp

private val BENTO_GAP =
    8.dp

private val HEADER_SHAPE =
    RoundedCornerShape(
        18.dp
    )

private val HEADER_PADDING =
    12.dp

private val HEADER_LEVEL_SIZE =
    38.dp

private val HEADER_CONTENT_SPACING =
    10.dp

private val READINESS_TRACK_HEIGHT =
    6.dp

private val PRIMARY_NEED_SHAPE =
    RoundedCornerShape(
        20.dp
    )

private val PRIMARY_NEED_BORDER_WIDTH =
    0.75.dp

private val PRIMARY_NEED_PADDING =
    14.dp

private val WHY_NOW_SHAPE =
    RoundedCornerShape(
        13.dp
    )

private val WHY_NOW_PADDING =
    10.dp

private val SMALL_BENTO_SHAPE =
    RoundedCornerShape(
        17.dp
    )

private val SMALL_BENTO_PADDING =
    11.dp

private val TEACHING_BENTO_HEIGHT =
    126.dp

private val TARGET_DOT_SIZE =
    5.dp

private val TARGET_DOT_TOP_PADDING =
    5.dp

private val TARGET_DOT_SPACING =
    7.dp

private val EXAMPLE_SHAPE =
    RoundedCornerShape(
        18.dp
    )

private val EXAMPLE_PADDING =
    13.dp

private val PROGRESS_ORANGE =
    Color(
        0xFFE87516
    )

private val PROGRESS_AMBER =
    Color(
        0xFFE6B522
    )

private val PROGRESS_GREEN =
    Color(
        0xFF2E9B57
    )

private const val DROPDOWN_ENTER_MILLIS =
    280

private const val DROPDOWN_EXIT_MILLIS =
    170

private const val DROPDOWN_INITIAL_SCALE =
    0.92f

private const val DROPDOWN_EXIT_SCALE =
    0.96f

private const val DROPDOWN_BORDER_ALPHA =
    0.72f

private const val HEADER_BACKGROUND_ALPHA =
    0.13f

private const val READINESS_TRACK_ALPHA =
    0.72f

private const val PRIMARY_NEED_BORDER_ALPHA =
    0.30f

private const val WHY_NOW_BACKGROUND_ALPHA =
    0.10f

private const val EXAMPLE_BACKGROUND_ALPHA =
    0.13f

private const val MAXIMUM_TARGET_COUNT =
    4

private const val COLOR_MIDPOINT =
    0.5f

private const val POPUP_WINDOW_MARGIN_PX =
    12

private const val POPUP_CONNECTION_OVERLAP_PX =
    5