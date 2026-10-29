package com.julesmeister.deutschcraft.feature.chat.presentation.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.LineHeightStyle
import com.julesmeister.deutschcraft.feature.chat.presentation.theme.ChatDesignTokens
import com.julesmeister.deutschcraft.feature.chat.presentation.theme.ChatGlassColors
import com.julesmeister.deutschcraft.feature.chat.presentation.theme.LocalChatFontScale
import com.julesmeister.deutschcraft.feature.chat.presentation.theme.scaledForChat
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.effects.vibrancy

/*
 * Removes excess font padding above and below visible letters
 * while preserving a centered line box.
 */
private val CenteredLineHeightStyle =
    LineHeightStyle(
        alignment =
            LineHeightStyle.Alignment.Center,
        trim =
            LineHeightStyle.Trim.Both
    )

@Composable
internal fun ChatGlassInputField(
    backdrop: Backdrop,
    value: TextFieldValue,
    enabled: Boolean,
    isSending: Boolean,
    isRefinementMode: Boolean,
    glassColors: ChatGlassColors,
    onValueChange: (TextFieldValue) -> Unit,
    onSendClick: () -> Unit,
    onInputFocused: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colorScheme =
        MaterialTheme.colorScheme

    /*
     * Persisted DeutschCraft chat font preference.
     *
     * SMALL       = 0.90
     * NORMAL      = 1.00
     * LARGE       = 1.15
     * EXTRA_LARGE = 1.30
     */
    val fontScale =
        LocalChatFontScale.current

    val interactionSource =
        remember {
            MutableInteractionSource()
        }

    val isFocused by
    interactionSource
        .collectIsFocusedAsState()

    /*
     * The actual number of lines rendered by Compose.
     *
     * This detects:
     *
     * - natural word wrapping
     * - explicit newline characters
     * - changes caused by the available field width
     * - changes caused by the selected font scale
     */
    var visualLineCount by
    remember {
        mutableIntStateOf(
            1
        )
    }

    val isMultiline =
        visualLineCount > 1

    /*
     * =======================================================
     * ADAPTIVE SHAPE
     * =======================================================
     */

    val normalCornerRadius by
    animateDpAsState(
        targetValue =
            if (isMultiline) {
                ChatDesignTokens
                    .ComposerMultilineCornerRadius
            } else {
                ChatDesignTokens
                    .ComposerSingleLineCornerRadius
            },
        animationSpec =
            tween(
                durationMillis =
                    ChatDesignTokens
                        .ComposerShapeAnimationMillis,
                easing =
                    FastOutSlowInEasing
            ),
        label =
            "ComposerNormalCornerRadius"
    )

    val bottomEndCornerRadius by
    animateDpAsState(
        targetValue =
            if (isMultiline) {
                ChatDesignTokens
                    .ComposerMultilineBottomEndCornerRadius
            } else {
                ChatDesignTokens
                    .ComposerSingleLineCornerRadius
            },
        animationSpec =
            tween(
                durationMillis =
                    ChatDesignTokens
                        .ComposerShapeAnimationMillis,
                easing =
                    FastOutSlowInEasing
            ),
        label =
            "ComposerBottomEndCornerRadius"
    )

    /*
     * Animate the outer breathing room when the composer moves
     * between one and multiple rendered lines.
     */
    val composerVerticalPadding by
    animateDpAsState(
        targetValue =
            if (isMultiline) {
                ChatDesignTokens
                    .ComposerMultilineVerticalPadding
            } else {
                ChatDesignTokens
                    .ComposerVerticalPadding
            },
        animationSpec =
            tween(
                durationMillis =
                    ChatDesignTokens
                        .ComposerAnimationMillis,
                easing =
                    FastOutSlowInEasing
            ),
        label =
            "ComposerVerticalPadding"
    )

    val composerShape =
        remember(
            normalCornerRadius,
            bottomEndCornerRadius
        ) {
            RoundedCornerShape(
                topStart =
                    normalCornerRadius,
                topEnd =
                    normalCornerRadius,
                bottomStart =
                    normalCornerRadius,
                bottomEnd =
                    bottomEndCornerRadius
            )
        }

    /*
     * =======================================================
     * ANIMATED EDGE REFLECTION
     * =======================================================
     *
     * The reflection travels diagonally across the outline.
     * It reverses direction at each end instead of suddenly
     * jumping back to its starting position.
     */

    val reflectionTransition =
        rememberInfiniteTransition(
            label =
                "GlassReflectionTransition"
        )

    val reflectionPosition by
    reflectionTransition.animateFloat(
        initialValue =
            ChatDesignTokens
                .GlassReflectionStartFraction,
        targetValue =
            ChatDesignTokens
                .GlassReflectionEndFraction,
        animationSpec =
            infiniteRepeatable(
                animation =
                    tween(
                        durationMillis =
                            ChatDesignTokens
                                .GlassReflectionDurationMillis,
                        easing =
                            LinearEasing
                    ),
                repeatMode =
                    RepeatMode.Reverse
            ),
        label =
            "GlassReflectionPosition"
    )

    /*
     * Focus makes the reflection slightly stronger without
     * changing its color or creating a neon effect.
     */
    val reflectionStrength by
    animateFloatAsState(
        targetValue =
            if (isFocused) {
                ChatDesignTokens
                    .GlassReflectionFocusedStrength
            } else {
                ChatDesignTokens
                    .GlassReflectionRestingStrength
            },
        animationSpec =
            tween(
                durationMillis =
                    ChatDesignTokens
                        .ComposerAnimationMillis,
                easing =
                    FastOutSlowInEasing
            ),
        label =
            "GlassReflectionStrength"
    )

    val reflectionBaseColor =
        Color.White.copy(
            alpha =
                if (glassColors.isDark) {
                    ChatDesignTokens
                        .GlassReflectionDarkAlpha
                } else {
                    ChatDesignTokens
                        .GlassReflectionLightAlpha
                }
        )

    /*
     * =======================================================
     * INPUT STATE
     * =======================================================
     */

    val isInputEnabled =
        enabled &&
                !isSending

    val canSend =
        isInputEnabled &&
                value.text.isNotBlank()

    /*
     * Scale both font size and line height.
     *
     * Scaling only the font size would make Large and Extra
     * Large composer text vertically cramped.
     */
    val baseInputTextStyle =
        MaterialTheme
            .typography
            .bodyLarge

    val scaledInputFontSize =
        baseInputTextStyle
            .fontSize
            .scaledForChat(
                scale =
                    fontScale
            )

    val scaledInputLineHeight =
        baseInputTextStyle
            .lineHeight
            .scaledForChat(
                scale =
                    fontScale
            )

    val inputTextStyle =
        baseInputTextStyle
            .copy(
                fontSize =
                    scaledInputFontSize,
                lineHeight =
                    scaledInputLineHeight
            )
            .merge(
                TextStyle(
                    lineHeightStyle =
                        CenteredLineHeightStyle
                )
            )

    /*
     * =======================================================
     * GLASS APPEARANCE
     * =======================================================
     */

    val surfaceTint =
        if (glassColors.isDark) {
            Color.Black.copy(
                alpha =
                    ChatDesignTokens
                        .LiquidDarkSurfaceAlpha
            )
        } else {
            Color.White.copy(
                alpha =
                    ChatDesignTokens
                        .LiquidLightSurfaceAlpha
            )
        }

    val stateBorderColor by
    animateColorAsState(
        targetValue =
            when {
                isRefinementMode -> {
                    glassColors.borderRefinement
                }

                isFocused -> {
                    glassColors.borderFocused
                }

                else -> {
                    glassColors.borderResting
                }
            },
        animationSpec =
            tween(
                durationMillis =
                    ChatDesignTokens
                        .ComposerAnimationMillis,
                easing =
                    FastOutSlowInEasing
            ),
        label =
            "ComposerBorderColor"
    )

    /*
     * =======================================================
     * GLASS CONTAINER
     * =======================================================
     */

    Row(
        modifier =
            modifier
                .defaultMinSize(
                    minHeight =
                        ChatDesignTokens
                            .ComposerMinHeight
                )

                /*
                 * Clip before Backdrop so blur, tint, and lens
                 * refraction cannot leak outside the shape.
                 */
                .clip(
                    composerShape
                )
                .drawBackdrop(
                    backdrop =
                        backdrop,
                    shape = {
                        composerShape
                    },
                    effects = {
                        vibrancy()

                        blur(
                            ChatDesignTokens
                                .LiquidBlurRadius
                                .toPx()
                        )

                        lens(
                            ChatDesignTokens
                                .LiquidLensHeight
                                .toPx(),
                            ChatDesignTokens
                                .LiquidLensAmount
                                .toPx()
                        )
                    },
                    onDrawSurface = {
                        drawOutline(
                            outline =
                                composerShape.createOutline(
                                    size =
                                        size,
                                    layoutDirection =
                                        layoutDirection,
                                    density =
                                        this
                                ),
                            color =
                                surfaceTint
                        )
                    }
                )

                /*
                 * Static structural border.
                 *
                 * This keeps the glass readable even while the
                 * moving reflection is away from a particular
                 * edge.
                 */
                .border(
                    width =
                        ChatDesignTokens
                            .GlassBorderWidth,
                    brush =
                        glassColors.borderBrush(
                            stateColor =
                                stateBorderColor
                        ),
                    shape =
                        composerShape
                )

                /*
                 * Moving specular reflection.
                 *
                 * The animated gradient is painted only over
                 * the outline. It does not cover the text or
                 * change the glass surface tint.
                 */
                .drawWithCache {
                    val outline =
                        composerShape.createOutline(
                            size =
                                size,
                            layoutDirection =
                                layoutDirection,
                            density =
                                this
                        )

                    val bandWidth =
                        size.width *
                                ChatDesignTokens
                                    .GlassReflectionBandWidthFraction

                    val reflectionCenterX =
                        size.width *
                                reflectionPosition

                    val startX =
                        reflectionCenterX -
                                bandWidth

                    val endX =
                        reflectionCenterX +
                                bandWidth

                    /*
                     * A diagonal gradient makes the highlight
                     * touch different portions of the top,
                     * side, and bottom edges as it travels.
                     */
                    val reflectionBrush =
                        Brush.linearGradient(
                            colorStops =
                                arrayOf(
                                    0f to
                                            Color.Transparent,

                                    ChatDesignTokens
                                        .GlassReflectionSoftStop to
                                            reflectionBaseColor.copy(
                                                alpha =
                                                    reflectionBaseColor.alpha *
                                                            reflectionStrength *
                                                            ChatDesignTokens
                                                                .GlassReflectionSoftAlphaMultiplier
                                            ),

                                    ChatDesignTokens
                                        .GlassReflectionCenterStop to
                                            reflectionBaseColor.copy(
                                                alpha =
                                                    reflectionBaseColor.alpha *
                                                            reflectionStrength
                                            ),

                                    ChatDesignTokens
                                        .GlassReflectionEndSoftStop to
                                            reflectionBaseColor.copy(
                                                alpha =
                                                    reflectionBaseColor.alpha *
                                                            reflectionStrength *
                                                            ChatDesignTokens
                                                                .GlassReflectionSoftAlphaMultiplier
                                            ),

                                    1f to
                                            Color.Transparent
                                ),
                            start =
                                Offset(
                                    x =
                                        startX,
                                    y =
                                        size.height
                                ),
                            end =
                                Offset(
                                    x =
                                        endX,
                                    y =
                                        0f
                                )
                        )

                    val reflectionStrokeWidth =
                        ChatDesignTokens
                            .GlassReflectionStrokeWidth
                            .toPx()

                    onDrawWithContent {
                        drawContent()

                        drawOutline(
                            outline =
                                outline,
                            brush =
                                reflectionBrush,
                            style =
                                Stroke(
                                    width =
                                        reflectionStrokeWidth
                                )
                        )
                    }
                }
                .padding(
                    start =
                        ChatDesignTokens
                            .ComposerStartPadding,
                    top =
                        composerVerticalPadding,
                    end =
                        ChatDesignTokens
                            .ComposerEndPadding,
                    bottom =
                        composerVerticalPadding
                ),
        verticalAlignment =
            Alignment.Bottom
    ) {
        /*
         * ===================================================
         * TEXT AREA
         * ===================================================
         */

        Box(
            modifier =
                Modifier
                    .weight(
                        weight =
                            1f
                    )
                    .then(
                        if (isMultiline) {
                            Modifier
                        } else {
                            Modifier.heightIn(
                                min =
                                    ChatDesignTokens
                                        .ComposerInnerMinHeight
                            )
                        }
                    ),
            contentAlignment =
                if (isMultiline) {
                    Alignment.TopStart
                } else {
                    Alignment.CenterStart
                }
        ) {
            BasicTextField(
                value =
                    value,
                onValueChange = { updatedValue ->
                    if (isInputEnabled) {
                        onValueChange(
                            updatedValue
                        )
                    }
                },
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .onFocusChanged { focusState ->
                            if (focusState.isFocused) {
                                onInputFocused()
                            }
                        }
                        .padding(
                            horizontal =
                                ChatDesignTokens
                                    .ComposerTextHorizontalPadding,
                            vertical =
                                ChatDesignTokens
                                    .ComposerTextVerticalPadding
                        ),
                enabled =
                    isInputEnabled,
                textStyle =
                    inputTextStyle.merge(
                        TextStyle(
                            color =
                                colorScheme.onSurface
                        )
                    ),
                cursorBrush =
                    SolidColor(
                        colorScheme.primary
                    ),
                interactionSource =
                    interactionSource,
                onTextLayout = { result: TextLayoutResult ->
                    val renderedLineCount =
                        result
                            .lineCount
                            .coerceIn(
                                minimumValue =
                                    1,
                                maximumValue =
                                    ChatDesignTokens
                                        .ComposerMaxLines
                            )

                    if (
                        visualLineCount !=
                        renderedLineCount
                    ) {
                        visualLineCount =
                            renderedLineCount
                    }
                },
                keyboardOptions =
                    KeyboardOptions(
                        capitalization =
                            KeyboardCapitalization.Sentences,
                        autoCorrectEnabled =
                            false,
                        imeAction =
                            ImeAction.Send
                    ),
                keyboardActions =
                    KeyboardActions(
                        onSend = {
                            if (canSend) {
                                onSendClick()
                            }
                        }
                    ),
                minLines =
                    1,
                maxLines =
                    ChatDesignTokens
                        .ComposerMaxLines,
                decorationBox = { innerTextField ->
                    Box(
                        modifier =
                            Modifier.fillMaxWidth(),
                        contentAlignment =
                            if (isMultiline) {
                                Alignment.TopStart
                            } else {
                                Alignment.CenterStart
                            }
                    ) {
                        if (value.text.isEmpty()) {
                            Text(
                                text =
                                    if (isRefinementMode) {
                                        REFINEMENT_PLACEHOLDER
                                    } else {
                                        DEFAULT_PLACEHOLDER
                                    },
                                style =
                                    inputTextStyle,
                                color =
                                    glassColors.placeholder
                            )
                        }

                        innerTextField()
                    }
                }
            )
        }

        Spacer(
            modifier =
                Modifier.width(
                    ChatDesignTokens
                        .ComposerTextSendSpacing
                )
        )

        /*
         * ===================================================
         * SEND BUTTON
         * ===================================================
         */

        Box(
            modifier =
                Modifier.heightIn(
                    min =
                        ChatDesignTokens
                            .ComposerInnerMinHeight
                ),
            contentAlignment =
                Alignment.Center
        ) {
            ChatSendButton(
                enabled =
                    canSend,
                isSending =
                    isSending,
                onClick = {
                    if (canSend) {
                        onSendClick()
                    }
                }
            )
        }
    }
}

private const val DEFAULT_PLACEHOLDER =
    "Nachricht"

private const val REFINEMENT_PLACEHOLDER =
    "Verbessere deine Antwort"