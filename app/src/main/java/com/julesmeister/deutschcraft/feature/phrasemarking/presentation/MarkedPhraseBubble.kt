package com.julesmeister.deutschcraft.feature.phrasemarking.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.coerceAtMost
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.julesmeister.deutschcraft.feature.phrasemarking.domain.ExplanationStatus
import com.julesmeister.deutschcraft.feature.phrasemarking.domain.MarkedPhrase
import com.julesmeister.deutschcraft.ui.theme.DeutschLime
import com.julesmeister.deutschcraft.ui.theme.DeutschSky
import com.julesmeister.deutschcraft.ui.theme.DeutschTextOnLime
import com.julesmeister.deutschcraft.ui.theme.DeutschTextOnSky

/*
 * Compact learning note attached flush beneath its source
 * message bubble.
 *
 * Tap bubble:
 *     expand/collapse explanation
 *
 * Tap X:
 *     open removal confirmation
 */
@Composable
fun MarkedPhraseBubble(
    phrase: MarkedPhrase,
    isUserMessage: Boolean,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier,
    isLast: Boolean = true
) {
    val colorScheme =
        MaterialTheme.colorScheme

    var isExpanded by
    rememberSaveable(phrase.id) {
        mutableStateOf(false)
    }

    var showRemoveDialog by
    rememberSaveable(phrase.id) {
        mutableStateOf(false)
    }

    /*
     * Keep DeutschCraft's sender colors:
     *
     * User      -> lime
     * Assistant -> sky
     */
    val accent =
        if (isUserMessage) {
            DeutschLime
        } else {
            DeutschSky
        }

    val onAccent =
        if (isUserMessage) {
            DeutschTextOnLime
        } else {
            DeutschTextOnSky
        }

    val bottomCorner =
        if (isLast) {
            BOTTOM_CORNER_RADIUS
        } else {
            0.dp
        }

    /*
     * Flat top because the marked phrase visually attaches
     * directly underneath its source message.
     */
    val shape =
        RoundedCornerShape(
            topStart = 0.dp,
            topEnd = 0.dp,
            bottomStart = bottomCorner,
            bottomEnd = bottomCorner
        )

    /*
     * Theme-aware base surface.
     */
    val containerColor =
        accent
            .copy(
                alpha =
                    CONTAINER_TINT_ALPHA
            )
            .compositeOver(
                colorScheme.surfaceContainerHigh
            )

    val maxWidth =
        (
                LocalConfiguration
                    .current
                    .screenWidthDp.dp *
                        MAXIMUM_WIDTH_FRACTION
                )
            .coerceAtMost(
                MAXIMUM_WIDTH
            )

    val canExpand =
        phrase.status ==
                ExplanationStatus.READY &&
                (
                        !phrase.explanation.isNullOrBlank() ||
                                !phrase.example.isNullOrBlank()
                        )

    /*
     * =======================================================
     * Removal confirmation
     * =======================================================
     */

    if (showRemoveDialog) {
        RemoveMarkedPhraseDialog(
            phrase =
                phrase.phrase,
            accent =
                accent,
            onAccent =
                onAccent,
            onConfirm = {
                showRemoveDialog =
                    false

                onRemove()
            },
            onDismiss = {
                showRemoveDialog =
                    false
            }
        )
    }

    /*
     * =======================================================
     * Bubble
     * =======================================================
     */

    Row(
        modifier =
            modifier.fillMaxWidth(),
        horizontalArrangement =
            if (isUserMessage) {
                Arrangement.End
            } else {
                Arrangement.Start
            }
    ) {
        Column(
            modifier =
                Modifier
                    .widthIn(
                        max =
                            maxWidth
                    )
                    .clip(
                        shape
                    )
                    .background(
                        containerColor
                    )
                    .clickable(
                        enabled =
                            canExpand,
                        interactionSource =
                            remember {
                                MutableInteractionSource()
                            },
                        indication =
                            null
                    ) {
                        isExpanded =
                            !isExpanded
                    }
                    .animateContentSize(
                        animationSpec =
                            tween(
                                durationMillis =
                                    CONTENT_SIZE_ANIMATION_MILLIS
                            )
                    )
                    .padding(
                        start =
                            CONTENT_START_PADDING,
                        end =
                            CONTENT_END_PADDING,
                        top =
                            CONTENT_TOP_PADDING,
                        bottom =
                            CONTENT_BOTTOM_PADDING
                    )
        ) {

            /*
             * ===================================================
             * Compact row
             * ===================================================
             */

            Row(
                verticalAlignment =
                    Alignment.CenterVertically
            ) {
                Text(
                    text =
                        buildAnnotatedString {
                            append(
                                phrase.type.icon
                            )

                            append(" ")

                            withStyle(
                                SpanStyle(
                                    color =
                                        accent,
                                    fontWeight =
                                        FontWeight.SemiBold
                                )
                            ) {
                                append(
                                    phrase.phrase
                                )
                            }

                            val secondary =
                                when (
                                    phrase.status
                                ) {
                                    ExplanationStatus.PENDING ->
                                        "…"

                                    ExplanationStatus.FAILED ->
                                        "gespeichert"

                                    ExplanationStatus.READY ->
                                        phrase
                                            .meaning
                                            .orEmpty()
                                }

                            if (
                                secondary.isNotBlank()
                            ) {
                                withStyle(
                                    SpanStyle(
                                        color =
                                            colorScheme
                                                .onSurfaceVariant
                                    )
                                ) {
                                    append(
                                        "  ·  "
                                    )

                                    append(
                                        secondary
                                    )
                                }
                            }
                        },
                    modifier =
                        Modifier.weight(
                            weight = 1f,
                            fill = false
                        ),
                    style =
                        MaterialTheme
                            .typography
                            .bodySmall
                            .copy(
                                fontSize =
                                    COMPACT_FONT_SIZE,
                                lineHeight =
                                    COMPACT_LINE_HEIGHT
                            ),
                    color =
                        colorScheme.onSurface,
                    maxLines =
                        if (isExpanded) {
                            3
                        } else {
                            1
                        },
                    overflow =
                        TextOverflow.Ellipsis
                )

                Spacer(
                    modifier =
                        Modifier.width(
                            REMOVE_BUTTON_SPACING
                        )
                )

                /*
                 * X no longer removes immediately.
                 */
                Box(
                    modifier =
                        Modifier
                            .size(
                                REMOVE_BUTTON_SIZE
                            )
                            .clip(
                                CircleShape
                            )
                            .clickable {
                                showRemoveDialog =
                                    true
                            },
                    contentAlignment =
                        Alignment.Center
                ) {
                    Icon(
                        imageVector =
                            Icons.Outlined.Close,
                        contentDescription =
                            "Markierung entfernen",
                        modifier =
                            Modifier.size(
                                REMOVE_ICON_SIZE
                            ),
                        tint =
                            colorScheme
                                .onSurfaceVariant
                                .copy(
                                    alpha =
                                        REMOVE_ICON_ALPHA
                                )
                    )
                }
            }

            /*
             * ===================================================
             * Expanded explanation
             * ===================================================
             */

            AnimatedVisibility(
                visible =
                    isExpanded &&
                            canExpand,
                enter =
                    fadeIn(
                        animationSpec =
                            tween(
                                durationMillis =
                                    DETAILS_FADE_IN_MILLIS
                            )
                    ) +
                            expandVertically(
                                animationSpec =
                                    tween(
                                        durationMillis =
                                            DETAILS_EXPAND_MILLIS
                                    )
                            ),
                exit =
                    fadeOut(
                        animationSpec =
                            tween(
                                durationMillis =
                                    DETAILS_FADE_OUT_MILLIS
                            )
                    ) +
                            shrinkVertically(
                                animationSpec =
                                    tween(
                                        durationMillis =
                                            DETAILS_SHRINK_MILLIS
                                    )
                            )
            ) {
                ExpandedDetails(
                    explanation =
                        phrase.explanation,
                    example =
                        phrase.example,
                    accent =
                        accent
                )
            }
        }
    }
}

/*
 * ===========================================================
 * CUSTOM REMOVE DIALOG
 * ===========================================================
 */

@Composable
private fun RemoveMarkedPhraseDialog(
    phrase: String,
    accent: Color,
    onAccent: Color,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val colorScheme =
        MaterialTheme.colorScheme

    var animationStarted by
    remember {
        mutableStateOf(false)
    }

    /*
     * Start one frame after composition so the initial scale
     * is actually visible.
     */
    LaunchedEffect(Unit) {
        animationStarted =
            true
    }

    val dialogScale by
    animateFloatAsState(
        targetValue =
            if (animationStarted) {
                DIALOG_NORMAL_SCALE
            } else {
                DIALOG_INITIAL_SCALE
            },
        animationSpec =
            spring(
                dampingRatio =
                    Spring.DampingRatioMediumBouncy,
                stiffness =
                    Spring.StiffnessMediumLow
            ),
        label =
            "MarkedPhraseDialogScale"
    )

    val dialogAlpha by
    animateFloatAsState(
        targetValue =
            if (animationStarted) {
                DIALOG_VISIBLE_ALPHA
            } else {
                DIALOG_HIDDEN_ALPHA
            },
        animationSpec =
            tween(
                durationMillis =
                    DIALOG_FADE_MILLIS
            ),
        label =
            "MarkedPhraseDialogAlpha"
    )

    Dialog(
        onDismissRequest =
            onDismiss,
        properties =
            DialogProperties(
                usePlatformDefaultWidth =
                    false
            )
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal =
                            DIALOG_OUTER_HORIZONTAL_PADDING
                    )
                    .graphicsLayer {
                        scaleX =
                            dialogScale

                        scaleY =
                            dialogScale

                        alpha =
                            dialogAlpha
                    }
                    .clip(
                        DIALOG_SHAPE
                    )
                    .background(
                        colorScheme.surfaceContainerHigh
                    )
                    .padding(
                        DIALOG_CONTENT_PADDING
                    ),
            horizontalAlignment =
                Alignment.CenterHorizontally
        ) {

            /*
             * ===================================================
             * Delete icon
             * ===================================================
             */

            Box(
                modifier =
                    Modifier
                        .size(
                            DIALOG_ICON_CONTAINER_SIZE
                        )
                        .clip(
                            CircleShape
                        )
                        .background(
                            colorScheme
                                .error
                                .copy(
                                    alpha =
                                        DIALOG_ERROR_CONTAINER_ALPHA
                                )
                        ),
                contentAlignment =
                    Alignment.Center
            ) {
                Icon(
                    imageVector =
                        Icons.Outlined.DeleteOutline,
                    contentDescription =
                        null,
                    modifier =
                        Modifier.size(
                            DIALOG_ICON_SIZE
                        ),
                    tint =
                        colorScheme.error
                )
            }

            Spacer(
                modifier =
                    Modifier.height(
                        DIALOG_HEADER_SPACING
                    )
            )

            /*
             * ===================================================
             * Heading
             * ===================================================
             */

            Text(
                text =
                    "Markierung entfernen?",
                style =
                    MaterialTheme
                        .typography
                        .titleLarge,
                fontWeight =
                    FontWeight.Bold,
                color =
                    colorScheme.onSurface,
                textAlign =
                    TextAlign.Center
            )

            Spacer(
                modifier =
                    Modifier.height(
                        DIALOG_PHRASE_TOP_SPACING
                    )
            )

            /*
             * ===================================================
             * Phrase chip
             * ===================================================
             *
             * Uses the same sender accent as the marked phrase:
             *
             * User      -> lime
             * Assistant -> sky
             */

            Text(
                text =
                    phrase,
                modifier =
                    Modifier
                        .widthIn(
                            max =
                                DIALOG_PHRASE_MAX_WIDTH
                        )
                        .clip(
                            CircleShape
                        )
                        .background(
                            accent
                        )
                        .padding(
                            horizontal =
                                DIALOG_PHRASE_HORIZONTAL_PADDING,
                            vertical =
                                DIALOG_PHRASE_VERTICAL_PADDING
                        ),
                style =
                    MaterialTheme
                        .typography
                        .labelLarge,
                fontWeight =
                    FontWeight.SemiBold,
                color =
                    onAccent,
                maxLines =
                    DIALOG_PHRASE_MAX_LINES,
                overflow =
                    TextOverflow.Ellipsis,
                textAlign =
                    TextAlign.Center
            )

            Spacer(
                modifier =
                    Modifier.height(
                        DIALOG_DESCRIPTION_TOP_SPACING
                    )
            )

            Text(
                text =
                    "Die Erklärung und das Beispiel werden " +
                            "aus deinen Markierungen gelöscht.",
                style =
                    MaterialTheme
                        .typography
                        .bodyMedium,
                color =
                    colorScheme.onSurfaceVariant,
                textAlign =
                    TextAlign.Center
            )

            Spacer(
                modifier =
                    Modifier.height(
                        DIALOG_ACTIONS_TOP_SPACING
                    )
            )

            /*
             * ===================================================
             * Remove
             * ===================================================
             */

            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(
                            DIALOG_ACTION_HEIGHT
                        )
                        .clip(
                            CircleShape
                        )
                        .background(
                            colorScheme.error
                        )
                        .clickable(
                            onClick =
                                onConfirm
                        ),
                contentAlignment =
                    Alignment.Center
            ) {
                Text(
                    text =
                        "Entfernen",
                    style =
                        MaterialTheme
                            .typography
                            .labelLarge,
                    fontWeight =
                        FontWeight.Bold,
                    color =
                        colorScheme.onError
                )
            }

            Spacer(
                modifier =
                    Modifier.height(
                        DIALOG_ACTION_SPACING
                    )
            )

            /*
             * ===================================================
             * Cancel
             * ===================================================
             */

            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(
                            DIALOG_ACTION_HEIGHT
                        )
                        .clip(
                            CircleShape
                        )
                        .background(
                            colorScheme.surfaceContainerHighest
                        )
                        .clickable(
                            onClick =
                                onDismiss
                        ),
                contentAlignment =
                    Alignment.Center
            ) {
                Text(
                    text =
                        "Abbrechen",
                    style =
                        MaterialTheme
                            .typography
                            .labelLarge,
                    fontWeight =
                        FontWeight.SemiBold,
                    color =
                        colorScheme.onSurface
                )
            }
        }
    }
}

/*
 * ===========================================================
 * EXPANDED DETAILS
 * ===========================================================
 */

@Composable
private fun ExpandedDetails(
    explanation: String?,
    example: String?,
    accent: Color
) {
    val colorScheme =
        MaterialTheme.colorScheme

    Column(
        modifier =
            Modifier.padding(
                top =
                    DETAILS_TOP_PADDING,
                end =
                    DETAILS_END_PADDING
            ),
        verticalArrangement =
            Arrangement.spacedBy(
                DETAILS_SPACING
            )
    ) {
        explanation
            ?.takeIf(
                String::isNotBlank
            )
            ?.let { text ->
                Text(
                    text =
                        text,
                    style =
                        MaterialTheme
                            .typography
                            .bodySmall
                            .copy(
                                fontSize =
                                    DETAILS_FONT_SIZE,
                                lineHeight =
                                    DETAILS_LINE_HEIGHT
                            ),
                    color =
                        colorScheme
                            .onSurfaceVariant
                )
            }

        example
            ?.takeIf(
                String::isNotBlank
            )
            ?.let { text ->
                Row(
                    modifier =
                        Modifier.height(
                            IntrinsicSize.Min
                        )
                ) {
                    Box(
                        modifier =
                            Modifier
                                .width(
                                    EXAMPLE_ACCENT_WIDTH
                                )
                                .fillMaxHeight()
                                .clip(
                                    RoundedCornerShape(
                                        EXAMPLE_ACCENT_RADIUS_PERCENT
                                    )
                                )
                                .background(
                                    accent.copy(
                                        alpha =
                                            EXAMPLE_ACCENT_ALPHA
                                    )
                                )
                    )

                    Spacer(
                        modifier =
                            Modifier.width(
                                EXAMPLE_TEXT_SPACING
                            )
                    )

                    Text(
                        text =
                            "„$text“",
                        style =
                            MaterialTheme
                                .typography
                                .bodySmall
                                .copy(
                                    fontSize =
                                        DETAILS_FONT_SIZE,
                                    lineHeight =
                                        DETAILS_LINE_HEIGHT
                                ),
                        fontStyle =
                            FontStyle.Italic,
                        color =
                            colorScheme
                                .onSurface
                                .copy(
                                    alpha =
                                        EXAMPLE_TEXT_ALPHA
                                )
                    )
                }
            }
    }
}

/*
 * ===========================================================
 * BUBBLE
 * ===========================================================
 */

private val BOTTOM_CORNER_RADIUS =
    14.dp

private const val MAXIMUM_WIDTH_FRACTION =
    0.72f

private val MAXIMUM_WIDTH =
    380.dp

private const val CONTAINER_TINT_ALPHA =
    0.06f

private val CONTENT_START_PADDING =
    12.dp

private val CONTENT_END_PADDING =
    4.dp

private val CONTENT_TOP_PADDING =
    6.dp

private val CONTENT_BOTTOM_PADDING =
    7.dp

private const val CONTENT_SIZE_ANIMATION_MILLIS =
    220

private val COMPACT_FONT_SIZE =
    13.sp

private val COMPACT_LINE_HEIGHT =
    17.sp

private val REMOVE_BUTTON_SPACING =
    4.dp

private val REMOVE_BUTTON_SIZE =
    20.dp

private val REMOVE_ICON_SIZE =
    12.dp

private const val REMOVE_ICON_ALPHA =
    0.70f

/*
 * ===========================================================
 * DETAILS
 * ===========================================================
 */

private val DETAILS_TOP_PADDING =
    4.dp

private val DETAILS_END_PADDING =
    8.dp

private val DETAILS_SPACING =
    4.dp

private val DETAILS_FONT_SIZE =
    12.sp

private val DETAILS_LINE_HEIGHT =
    16.sp

private val EXAMPLE_ACCENT_WIDTH =
    2.dp

private const val EXAMPLE_ACCENT_RADIUS_PERCENT =
    50

private const val EXAMPLE_ACCENT_ALPHA =
    0.50f

private val EXAMPLE_TEXT_SPACING =
    8.dp

private const val EXAMPLE_TEXT_ALPHA =
    0.82f

private const val DETAILS_FADE_IN_MILLIS =
    180

private const val DETAILS_EXPAND_MILLIS =
    220

private const val DETAILS_FADE_OUT_MILLIS =
    120

private const val DETAILS_SHRINK_MILLIS =
    180

/*
 * ===========================================================
 * REMOVE DIALOG
 * ===========================================================
 */

private val DIALOG_SHAPE =
    RoundedCornerShape(
        32.dp
    )

private val DIALOG_OUTER_HORIZONTAL_PADDING =
    28.dp

private val DIALOG_CONTENT_PADDING =
    24.dp

private val DIALOG_ICON_CONTAINER_SIZE =
    64.dp

private val DIALOG_ICON_SIZE =
    30.dp

private val DIALOG_HEADER_SPACING =
    20.dp

private val DIALOG_PHRASE_TOP_SPACING =
    12.dp

private val DIALOG_PHRASE_MAX_WIDTH =
    260.dp

private val DIALOG_PHRASE_HORIZONTAL_PADDING =
    16.dp

private val DIALOG_PHRASE_VERTICAL_PADDING =
    7.dp

private const val DIALOG_PHRASE_MAX_LINES =
    1

private val DIALOG_DESCRIPTION_TOP_SPACING =
    12.dp

private val DIALOG_ACTIONS_TOP_SPACING =
    24.dp

private val DIALOG_ACTION_HEIGHT =
    50.dp

private val DIALOG_ACTION_SPACING =
    8.dp

private const val DIALOG_ERROR_CONTAINER_ALPHA =
    0.14f

private const val DIALOG_INITIAL_SCALE =
    0.88f

private const val DIALOG_NORMAL_SCALE =
    1f

private const val DIALOG_HIDDEN_ALPHA =
    0f

private const val DIALOG_VISIBLE_ALPHA =
    1f

private const val DIALOG_FADE_MILLIS =
    160