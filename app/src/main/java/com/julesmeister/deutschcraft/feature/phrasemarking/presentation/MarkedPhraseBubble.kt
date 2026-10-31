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
import androidx.compose.ui.graphics.luminance
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
    rememberSaveable(
        phrase.id
    ) {
        mutableStateOf(
            false
        )
    }

    var showRemoveDialog by
    rememberSaveable(
        phrase.id
    ) {
        mutableStateOf(
            false
        )
    }

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

    val isLightTheme =
        colorScheme.background.luminance() >=
                LIGHT_THEME_LUMINANCE_THRESHOLD

    val readableAccent =
        when {
            !isLightTheme ->
                accent

            isUserMessage ->
                colorScheme.primary

            else ->
                LIGHT_MODE_ASSISTANT_READABLE_ACCENT
        }

    val bottomCorner =
        if (isLast) {
            BOTTOM_CORNER_RADIUS
        } else {
            0.dp
        }

    val shape =
        RoundedCornerShape(
            topStart =
                0.dp,

            topEnd =
                0.dp,

            bottomStart =
                bottomCorner,

            bottomEnd =
                bottomCorner
        )

    val containerTintAlpha =
        if (isLightTheme) {
            LIGHT_CONTAINER_TINT_ALPHA
        } else {
            DARK_CONTAINER_TINT_ALPHA
        }

    val containerColor =
        accent
            .copy(
                alpha =
                    containerTintAlpha
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

    if (showRemoveDialog) {
        RemoveMarkedPhraseDialog(
            phrase =
                phrase.phrase,

            accent =
                accent,

            onAccent =
                onAccent,

            isUserMessage =
                isUserMessage,

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

                            append(
                                " "
                            )

                            withStyle(
                                SpanStyle(
                                    color =
                                        readableAccent,

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
                            weight =
                                1f,

                            fill =
                                false
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
                        readableAccent
                )
            }
        }
    }
}

@Composable
private fun RemoveMarkedPhraseDialog(
    phrase: String,
    accent: Color,
    onAccent: Color,
    isUserMessage: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val colorScheme =
        MaterialTheme.colorScheme

    val isLightTheme =
        colorScheme.background.luminance() >=
                LIGHT_THEME_LUMINANCE_THRESHOLD

    val readableAccent =
        when {
            !isLightTheme ->
                accent

            isUserMessage ->
                colorScheme.primary

            else ->
                LIGHT_MODE_ASSISTANT_READABLE_ACCENT
        }

    val phraseChipColor =
        when {
            !isLightTheme ->
                accent

            isUserMessage ->
                colorScheme.primary

            else ->
                LIGHT_MODE_ASSISTANT_READABLE_ACCENT
        }

    val phraseChipContentColor =
        when {
            !isLightTheme ->
                onAccent

            isUserMessage ->
                colorScheme.onPrimary

            else ->
                Color.White
        }

    val dialogContainerColor =
        if (isLightTheme) {
            colorScheme.surfaceContainerLow
        } else {
            colorScheme.surfaceContainerHigh
        }

    val deleteIconBackground =
        colorScheme.error.copy(
            alpha =
                if (isLightTheme) {
                    LIGHT_DELETE_ICON_BACKGROUND_ALPHA
                } else {
                    DARK_DELETE_ICON_BACKGROUND_ALPHA
                }
        )

    val cancelContainerColor =
        if (isLightTheme) {
            colorScheme.surfaceContainerHigh
        } else {
            colorScheme.surfaceContainerHighest
        }

    var animationStarted by
    remember {
        mutableStateOf(
            false
        )
    }

    LaunchedEffect(
        Unit
    ) {
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
                        dialogContainerColor
                    )
                    .padding(
                        horizontal =
                            DIALOG_HORIZONTAL_PADDING,

                        vertical =
                            DIALOG_VERTICAL_PADDING
                    )
        ) {
            /*
             * Compact flat header.
             *
             * The delete icon no longer sits inside a large
             * ornamental circle. The icon receives only a small,
             * flat error-tinted container.
             */
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
                                DIALOG_ICON_CONTAINER_SIZE
                            )
                            .clip(
                                DIALOG_ICON_SHAPE
                            )
                            .background(
                                deleteIconBackground
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
                        Modifier.width(
                            DIALOG_HEADER_HORIZONTAL_SPACING
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
                            "Markierung entfernen?",

                        style =
                            MaterialTheme
                                .typography
                                .titleMedium,

                        fontWeight =
                            FontWeight.Bold,

                        color =
                            colorScheme.onSurface,

                        maxLines =
                            1,

                        overflow =
                            TextOverflow.Ellipsis
                    )

                    Spacer(
                        modifier =
                            Modifier.height(
                                DIALOG_TITLE_DESCRIPTION_SPACING
                            )
                    )

                    Text(
                        text =
                            "Diese Lernnotiz wird gelöscht.",

                        style =
                            MaterialTheme
                                .typography
                                .bodySmall,

                        color =
                            colorScheme.onSurfaceVariant,

                        maxLines =
                            1,

                        overflow =
                            TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(
                modifier =
                    Modifier.height(
                        DIALOG_PHRASE_TOP_SPACING
                    )
            )

            /*
             * Flat phrase section.
             *
             * No outline, no surrounding card border and no
             * nested decorative frame.
             */
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .clip(
                            DIALOG_PHRASE_ROW_SHAPE
                        )
                        .background(
                            readableAccent
                                .copy(
                                    alpha =
                                        if (isLightTheme) {
                                            LIGHT_PHRASE_ROW_ALPHA
                                        } else {
                                            DARK_PHRASE_ROW_ALPHA
                                        }
                                )
                                .compositeOver(
                                    dialogContainerColor
                                )
                        )
                        .padding(
                            horizontal =
                                DIALOG_PHRASE_ROW_HORIZONTAL_PADDING,

                            vertical =
                                DIALOG_PHRASE_ROW_VERTICAL_PADDING
                        ),

                verticalAlignment =
                    Alignment.CenterVertically
            ) {
                Box(
                    modifier =
                        Modifier
                            .size(
                                DIALOG_PHRASE_ACCENT_DOT_SIZE
                            )
                            .clip(
                                CircleShape
                            )
                            .background(
                                readableAccent
                            )
                )

                Spacer(
                    modifier =
                        Modifier.width(
                            DIALOG_PHRASE_ACCENT_SPACING
                        )
                )

                Text(
                    text =
                        phrase,

                    modifier =
                        Modifier.weight(
                            1f
                        ),

                    style =
                        MaterialTheme
                            .typography
                            .bodyMedium,

                    fontWeight =
                        FontWeight.SemiBold,

                    color =
                        readableAccent,

                    maxLines =
                        DIALOG_PHRASE_MAX_LINES,

                    overflow =
                        TextOverflow.Ellipsis
                )
            }

            Spacer(
                modifier =
                    Modifier.height(
                        DIALOG_DESCRIPTION_TOP_SPACING
                    )
            )

            Text(
                text =
                    "Bedeutung, Erklärung und Beispiel werden " +
                            "aus deinen Markierungen entfernt.",

                style =
                    MaterialTheme
                        .typography
                        .bodySmall,

                color =
                    colorScheme.onSurfaceVariant,

                lineHeight =
                    DIALOG_DESCRIPTION_LINE_HEIGHT
            )

            Spacer(
                modifier =
                    Modifier.height(
                        DIALOG_ACTIONS_TOP_SPACING
                    )
            )

            /*
             * Flat side-by-side actions.
             *
             * Cancel remains a neutral filled surface.
             * Delete uses the semantic error color.
             *
             * Neither button has a border.
             */
            Row(
                modifier =
                    Modifier.fillMaxWidth(),

                horizontalArrangement =
                    Arrangement.spacedBy(
                        DIALOG_ACTION_SPACING
                    )
            ) {
                Box(
                    modifier =
                        Modifier
                            .weight(
                                1f
                            )
                            .height(
                                DIALOG_ACTION_HEIGHT
                            )
                            .clip(
                                DIALOG_ACTION_SHAPE
                            )
                            .background(
                                cancelContainerColor
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

                Box(
                    modifier =
                        Modifier
                            .weight(
                                1f
                            )
                            .height(
                                DIALOG_ACTION_HEIGHT
                            )
                            .clip(
                                DIALOG_ACTION_SHAPE
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
                    Row(
                        verticalAlignment =
                            Alignment.CenterVertically,

                        horizontalArrangement =
                            Arrangement.spacedBy(
                                DIALOG_DELETE_CONTENT_SPACING
                            )
                    ) {
                        Icon(
                            imageVector =
                                Icons.Outlined.DeleteOutline,

                            contentDescription =
                                null,

                            modifier =
                                Modifier.size(
                                    DIALOG_DELETE_ACTION_ICON_SIZE
                                ),

                            tint =
                                colorScheme.onError
                        )

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
                }
            }
        }
    }
}

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
                        colorScheme.onSurfaceVariant
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

private val LIGHT_MODE_ASSISTANT_READABLE_ACCENT =
    Color(
        0xFF176B91
    )

private const val LIGHT_THEME_LUMINANCE_THRESHOLD =
    0.5f

private val BOTTOM_CORNER_RADIUS =
    14.dp

private const val MAXIMUM_WIDTH_FRACTION =
    0.72f

private val MAXIMUM_WIDTH =
    380.dp

private const val DARK_CONTAINER_TINT_ALPHA =
    0.06f

private const val LIGHT_CONTAINER_TINT_ALPHA =
    0.09f

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
    0.65f

private val EXAMPLE_TEXT_SPACING =
    8.dp

private const val EXAMPLE_TEXT_ALPHA =
    0.90f

private const val DETAILS_FADE_IN_MILLIS =
    180

private const val DETAILS_EXPAND_MILLIS =
    220

private const val DETAILS_FADE_OUT_MILLIS =
    120

private const val DETAILS_SHRINK_MILLIS =
    180

/*
 * ============================================================
 * COMPACT FLAT REMOVE DIALOG
 * ============================================================
 */

private val DIALOG_SHAPE =
    RoundedCornerShape(
        24.dp
    )

private val DIALOG_OUTER_HORIZONTAL_PADDING =
    36.dp

private val DIALOG_HORIZONTAL_PADDING =
    18.dp

private val DIALOG_VERTICAL_PADDING =
    18.dp

private val DIALOG_ICON_CONTAINER_SIZE =
    38.dp

private val DIALOG_ICON_SIZE =
    20.dp

private val DIALOG_ICON_SHAPE =
    RoundedCornerShape(
        12.dp
    )

private val DIALOG_HEADER_HORIZONTAL_SPACING =
    12.dp

private val DIALOG_TITLE_DESCRIPTION_SPACING =
    2.dp

private const val LIGHT_DELETE_ICON_BACKGROUND_ALPHA =
    0.09f

private const val DARK_DELETE_ICON_BACKGROUND_ALPHA =
    0.15f

private val DIALOG_PHRASE_TOP_SPACING =
    16.dp

private val DIALOG_PHRASE_ROW_SHAPE =
    RoundedCornerShape(
        14.dp
    )

private val DIALOG_PHRASE_ROW_HORIZONTAL_PADDING =
    12.dp

private val DIALOG_PHRASE_ROW_VERTICAL_PADDING =
    10.dp

private const val LIGHT_PHRASE_ROW_ALPHA =
    0.08f

private const val DARK_PHRASE_ROW_ALPHA =
    0.10f

private val DIALOG_PHRASE_ACCENT_DOT_SIZE =
    7.dp

private val DIALOG_PHRASE_ACCENT_SPACING =
    9.dp

private const val DIALOG_PHRASE_MAX_LINES =
    2

private val DIALOG_DESCRIPTION_TOP_SPACING =
    10.dp

private val DIALOG_DESCRIPTION_LINE_HEIGHT =
    18.sp

private val DIALOG_ACTIONS_TOP_SPACING =
    16.dp

private val DIALOG_ACTION_HEIGHT =
    44.dp

private val DIALOG_ACTION_SPACING =
    8.dp

private val DIALOG_ACTION_SHAPE =
    RoundedCornerShape(
        14.dp
    )

private val DIALOG_DELETE_CONTENT_SPACING =
    6.dp

private val DIALOG_DELETE_ACTION_ICON_SIZE =
    16.dp

private const val DIALOG_INITIAL_SCALE =
    0.94f

private const val DIALOG_NORMAL_SCALE =
    1f

private const val DIALOG_HIDDEN_ALPHA =
    0f

private const val DIALOG_VISIBLE_ALPHA =
    1f

private const val DIALOG_FADE_MILLIS =
    140