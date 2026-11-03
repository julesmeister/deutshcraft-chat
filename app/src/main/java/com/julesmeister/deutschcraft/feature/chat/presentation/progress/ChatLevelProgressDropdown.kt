// app/src/main/java/com/julesmeister/deutschcraft/feature/chat/presentation/progress/ChatLevelProgressDropdown.kt

package com.julesmeister.deutschcraft.feature.chat.presentation.progress

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

@Composable
internal fun ChatLevelProgressDropdown(
    state: ChatLevelProgressUiState,
    expanded: Boolean,
    onDismiss: () -> Unit,
    anchorContent: @Composable () -> Unit
) {
    val transitionState =
        remember {
            MutableTransitionState(false)
        }

    transitionState.targetState =
        expanded

    if (
        !transitionState.currentState &&
        !transitionState.targetState &&
        transitionState.isIdle
    ) {
        return
    }

    val density =
        LocalDensity.current

    val positionProvider =
        remember(density) {
            CradlePopupPositionProvider(
                cradleCenterFromRightPx =
                    with(density) { CRADLE_RADIUS.roundToPx() },
                cradleCenterFromTopPx =
                    with(density) { CRADLE_RADIUS.roundToPx() },
                windowMarginPx =
                    with(density) { WINDOW_MARGIN.roundToPx() }
            )
        }

    val pivot =
        TransformOrigin(
            pivotFractionX =
                1f - CRADLE_RADIUS.value / DROPDOWN_WIDTH.value,
            pivotFractionY = 0.04f
        )

    Popup(
        popupPositionProvider = positionProvider,
        onDismissRequest = onDismiss,
        properties =
            PopupProperties(
                focusable = true,
                dismissOnBackPress = true,
                dismissOnClickOutside = true,
                clippingEnabled = false
            )
    ) {
        AnimatedVisibility(
            visibleState = transitionState,
            enter =
                fadeIn(
                    animationSpec =
                        tween(
                            durationMillis = DROPDOWN_ENTER_MILLIS,
                            easing = FastOutSlowInEasing
                        )
                ) +
                        scaleIn(
                            initialScale = DROPDOWN_INITIAL_SCALE,
                            transformOrigin = pivot,
                            animationSpec =
                                spring(
                                    dampingRatio = 0.82f,
                                    stiffness = Spring.StiffnessMediumLow
                                )
                        ),
            exit =
                fadeOut(
                    animationSpec =
                        tween(durationMillis = DROPDOWN_EXIT_MILLIS)
                ) +
                        scaleOut(
                            targetScale = DROPDOWN_EXIT_SCALE,
                            transformOrigin = pivot,
                            animationSpec =
                                tween(
                                    durationMillis = DROPDOWN_EXIT_MILLIS,
                                    easing = FastOutSlowInEasing
                                )
                        )
        ) {
            CradledProgressBento(
                state = state,
                onAnchorClick = onDismiss,
                anchorContent = anchorContent
            )
        }
    }
}

@Composable
private fun CradledProgressBento(
    state: ChatLevelProgressUiState,
    onAnchorClick: () -> Unit,
    anchorContent: @Composable () -> Unit
) {
    val colors =
        MaterialTheme.colorScheme

    val accent =
        readinessColor(state.safeReadinessPercentage)

    val need =
        state.immediateNeed

    val shape =
        remember {
            CradleDropdownShape(
                cradleRadius = CRADLE_RADIUS,
                bodyTopOffset = BODY_TOP_OFFSET,
                filletLength = FILLET_LENGTH,
                cornerRadius = BODY_CORNER_RADIUS
            )
        }

    Box(
        modifier =
            Modifier
                .requiredWidth(DROPDOWN_WIDTH)
                .padding(bottom = SHADOW_SPACE)
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .shadow(
                        elevation = DROPDOWN_ELEVATION,
                        shape = shape,
                        clip = false,
                        ambientColor = Color.Black.copy(alpha = 0.18f),
                        spotColor = Color.Black.copy(alpha = 0.24f)
                    )
                    .clip(shape)
                    .background(colors.surfaceContainerHigh)
                    .border(
                        width = DROPDOWN_BORDER_WIDTH,
                        color =
                            colors.outlineVariant.copy(
                                alpha = DROPDOWN_BORDER_ALPHA
                            ),
                        shape = shape
                    )
                    .padding(
                        start = DROPDOWN_PADDING,
                        end = DROPDOWN_PADDING,
                        bottom = DROPDOWN_PADDING,
                        top = CRADLE_RADIUS * 2 + CONTENT_GAP_BELOW_CRADLE
                    ),
            verticalArrangement =
                Arrangement.spacedBy(BENTO_GAP)
        ) {
            ProgressHeaderBento(
                state = state,
                accent = accent
            )

            if (need == null) {
                EmptyNeedBento(accent = accent)
            } else {
                PrimaryNeedBento(
                    need = need,
                    accent = accent
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(BENTO_GAP)
                ) {
                    TeachingTargetBento(
                        label = "SAY IT WITH",
                        values = need.grammarTargets,
                        accent = accent,
                        modifier = Modifier.weight(1f)
                    )

                    TeachingTargetBento(
                        label = "USE MORE",
                        values = need.vocabularyTargets,
                        accent = accent,
                        modifier = Modifier.weight(1f)
                    )
                }

                ExampleBento(
                    phrase = need.examplePhrase,
                    accent = accent
                )
            }
        }

        /*
         * Live copy of the indicator, centered inside the cradle.
         * It sits exactly over the real button, so the button
         * looks embedded in the dropdown.
         */
        Box(
            modifier =
                Modifier
                    .align(Alignment.TopEnd)
                    .offset(
                        x = -(CRADLE_RADIUS - INDICATOR_TOUCH_SIZE / 2),
                        y = CRADLE_RADIUS - INDICATOR_TOUCH_SIZE / 2
                    )
                    .size(INDICATOR_TOUCH_SIZE)
                    .clip(CircleShape)
                    .clickable(
                        interactionSource =
                            remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onAnchorClick
                    ),
            contentAlignment = Alignment.Center
        ) {
            anchorContent()
        }
    }
}

@Composable
private fun ProgressHeaderBento(
    state: ChatLevelProgressUiState,
    accent: Color
) {
    val colors =
        MaterialTheme.colorScheme

    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(HEADER_SHAPE)
                .background(accent.copy(alpha = HEADER_BACKGROUND_ALPHA))
                .padding(HEADER_PADDING),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = state.progressTitle,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = colors.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(5.dp))

            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(READINESS_TRACK_HEIGHT)
                        .clip(CircleShape)
                        .background(
                            colors.surface.copy(alpha = READINESS_TRACK_ALPHA)
                        )
            ) {
                Box(
                    modifier =
                        Modifier
                            .fillMaxWidth(
                                fraction = state.safeReadinessPercentage / 100f
                            )
                            .height(READINESS_TRACK_HEIGHT)
                            .clip(CircleShape)
                            .background(accent)
                )
            }
        }

        Spacer(modifier = Modifier.width(HEADER_CONTENT_SPACING))

        Text(
            text =
                if (state.isHighestLevel) {
                    "C2"
                } else {
                    "${state.safeReadinessPercentage}%"
                },
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.ExtraBold,
            color = accent
        )
    }
}

@Composable
private fun PrimaryNeedBento(
    need: ChatImmediateLevelNeedUiModel,
    accent: Color
) {
    val colors =
        MaterialTheme.colorScheme

    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(PRIMARY_NEED_SHAPE)
                .background(colors.surfaceContainer)
                .border(
                    width = PRIMARY_NEED_BORDER_WIDTH,
                    color = accent.copy(alpha = PRIMARY_NEED_BORDER_ALPHA),
                    shape = PRIMARY_NEED_SHAPE
                )
                .padding(PRIMARY_NEED_PADDING),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "NEXT BEST STEP",
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = accent
            )

            Text(
                text = need.statusLabel,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = colors.onSurfaceVariant
            )
        }

        Text(
            text = need.title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = colors.onSurface
        )

        Text(
            text = need.explanation,
            style = MaterialTheme.typography.bodySmall,
            color = colors.onSurfaceVariant
        )

        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .clip(WHY_NOW_SHAPE)
                    .background(accent.copy(alpha = WHY_NOW_BACKGROUND_ALPHA))
                    .padding(WHY_NOW_PADDING)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(
                    text = "WHY NOW",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = accent
                )

                Text(
                    text = need.whyNow,
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onSurface
                )
            }
        }
    }
}

@Composable
private fun TeachingTargetBento(
    label: String,
    values: List<String>,
    accent: Color,
    modifier: Modifier = Modifier
) {
    val colors =
        MaterialTheme.colorScheme

    Column(
        modifier =
            modifier
                .height(TEACHING_BENTO_HEIGHT)
                .clip(SMALL_BENTO_SHAPE)
                .background(colors.surfaceContainer)
                .padding(SMALL_BENTO_PADDING),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = accent
        )

        values
            .take(MAXIMUM_TARGET_COUNT)
            .forEach { value ->
                Row(
                    verticalAlignment = Alignment.Top
                ) {
                    Box(
                        modifier =
                            Modifier
                                .padding(top = TARGET_DOT_TOP_PADDING)
                                .size(TARGET_DOT_SIZE)
                                .clip(CircleShape)
                                .background(accent)
                    )

                    Spacer(modifier = Modifier.width(TARGET_DOT_SPACING))

                    Text(
                        text = value,
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
    }
}

@Composable
private fun ExampleBento(
    phrase: String,
    accent: Color
) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(EXAMPLE_SHAPE)
                .background(accent.copy(alpha = EXAMPLE_BACKGROUND_ALPHA))
                .padding(EXAMPLE_PADDING),
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Text(
            text = "TRY SAYING",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = accent
        )

        Text(
            text = "„$phrase“",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            fontStyle = FontStyle.Italic,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun EmptyNeedBento(
    accent: Color
) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(PRIMARY_NEED_SHAPE)
                .background(MaterialTheme.colorScheme.surfaceContainer)
                .padding(PRIMARY_NEED_PADDING),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = "BUILDING YOUR NEXT STEP",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = accent
        )

        Text(
            text =
                "Keep chatting naturally. DeutschCraft is collecting " +
                        "enough evidence to identify one specific " +
                        "next-level requirement.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/*
 * Body with a circular cradle growing out of its top-right
 * corner:
 *
 *                         ╭────╮
 *                        │  ◉   │   ← cradle wraps the button
 *   ╭────────────────────╯       │
 *   │                            │  ← soft fillet, no seam
 *   │            body            │
 *   ╰────────────────────────────╯
 */
private class CradleDropdownShape(
    private val cradleRadius: Dp,
    private val bodyTopOffset: Dp,
    private val filletLength: Dp,
    private val cornerRadius: Dp
) : Shape {

    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val r = with(density) { cradleRadius.toPx() }
        val bodyTop = with(density) { bodyTopOffset.toPx() }
        val fillet = with(density) { filletLength.toPx() }
        val corner = with(density) { cornerRadius.toPx() }

        val w = size.width
        val h = size.height

        val cx = w - r
        val cy = r

        // Where the cradle circle crosses the body's top edge.
        val dy = (bodyTop - cy).coerceIn(0f, r * 0.95f)
        val dx = sqrt(r * r - dy * dy)
        val intersectionX = cx - dx

        // Angle (Compose: 0° = right, 90° = down) of that crossing.
        val intersectionAngle =
            180.0 - Math.toDegrees(asin((dy / r).toDouble()))

        // Start the circle arc slightly above the crossing so the
        // fillet curve blends into it.
        val arcStartAngle =
            intersectionAngle + FILLET_ARC_DEGREES

        val arcStartRadians =
            Math.toRadians(arcStartAngle)

        val arcStart =
            Offset(
                x = cx + r * cos(arcStartRadians).toFloat(),
                y = cy + r * sin(arcStartRadians).toFloat()
            )

        val path =
            Path().apply {
                moveTo(0f, bodyTop + corner)

                arcTo(
                    rect = Rect(0f, bodyTop, corner * 2f, bodyTop + corner * 2f),
                    startAngleDegrees = 180f,
                    sweepAngleDegrees = 90f,
                    forceMoveTo = false
                )

                lineTo(intersectionX - fillet, bodyTop)

                // Soft concave join into the cradle.
                quadraticTo(
                    x1 = intersectionX,
                    y1 = bodyTop,
                    x2 = arcStart.x,
                    y2 = arcStart.y
                )

                // Over the top of the cradle down to its right edge.
                arcTo(
                    rect = Rect(cx - r, cy - r, cx + r, cy + r),
                    startAngleDegrees = arcStartAngle.toFloat(),
                    sweepAngleDegrees = (360.0 - arcStartAngle).toFloat(),
                    forceMoveTo = false
                )

                lineTo(w, h - corner)

                arcTo(
                    rect = Rect(w - corner * 2f, h - corner * 2f, w, h),
                    startAngleDegrees = 0f,
                    sweepAngleDegrees = 90f,
                    forceMoveTo = false
                )

                lineTo(corner, h)

                arcTo(
                    rect = Rect(0f, h - corner * 2f, corner * 2f, h),
                    startAngleDegrees = 90f,
                    sweepAngleDegrees = 90f,
                    forceMoveTo = false
                )

                close()
            }

        return Outline.Generic(path)
    }

    private companion object {
        const val FILLET_ARC_DEGREES = 14.0
    }
}

/*
 * Places the popup so the cradle's center lands exactly on the
 * indicator's center.
 */
private class CradlePopupPositionProvider(
    private val cradleCenterFromRightPx: Int,
    private val cradleCenterFromTopPx: Int,
    private val windowMarginPx: Int
) : PopupPositionProvider {

    override fun calculatePosition(
        anchorBounds: IntRect,
        windowSize: IntSize,
        layoutDirection: LayoutDirection,
        popupContentSize: IntSize
    ): IntOffset {
        val anchorCenterX =
            anchorBounds.left + anchorBounds.width / 2

        val anchorCenterY =
            anchorBounds.top + anchorBounds.height / 2

        val preferredX =
            anchorCenterX -
                    (popupContentSize.width - cradleCenterFromRightPx)

        val maximumX =
            (windowSize.width - popupContentSize.width)
                .coerceAtLeast(windowMarginPx)

        return IntOffset(
            x = preferredX.coerceIn(windowMarginPx, maximumX),
            y = anchorCenterY - cradleCenterFromTopPx
        )
    }
}

private fun readinessColor(
    readiness: Int
): Color {
    val progress =
        readiness.coerceIn(0, 100) / 100f

    return if (progress <= COLOR_MIDPOINT) {
        lerp(
            start = PROGRESS_ORANGE,
            stop = PROGRESS_AMBER,
            fraction = progress / COLOR_MIDPOINT
        )
    } else {
        lerp(
            start = PROGRESS_AMBER,
            stop = PROGRESS_GREEN,
            fraction = (progress - COLOR_MIDPOINT) / COLOR_MIDPOINT
        )
    }
}

/*
 * Cradle geometry.
 *
 * The indicator touch area is 48.dp (radius 24.dp). A 30.dp
 * cradle radius leaves a 6.dp collar of dropdown surface around
 * the button.
 */
private val CRADLE_RADIUS = 30.dp
private val BODY_TOP_OFFSET = 44.dp
private val FILLET_LENGTH = 14.dp
private val CONTENT_GAP_BELOW_CRADLE = 4.dp
private val WINDOW_MARGIN = 8.dp

private val DROPDOWN_WIDTH = 304.dp
private val DROPDOWN_PADDING = 12.dp
private val DROPDOWN_BORDER_WIDTH = 1.dp
private val DROPDOWN_ELEVATION = 14.dp
private val SHADOW_SPACE = 16.dp
private val BODY_CORNER_RADIUS = 26.dp

private val BENTO_GAP = 8.dp

private val HEADER_SHAPE = RoundedCornerShape(18.dp)
private val HEADER_PADDING = 12.dp
private val HEADER_CONTENT_SPACING = 10.dp
private val READINESS_TRACK_HEIGHT = 6.dp

private val PRIMARY_NEED_SHAPE = RoundedCornerShape(20.dp)
private val PRIMARY_NEED_BORDER_WIDTH = 0.75.dp
private val PRIMARY_NEED_PADDING = 14.dp

private val WHY_NOW_SHAPE = RoundedCornerShape(13.dp)
private val WHY_NOW_PADDING = 10.dp

private val SMALL_BENTO_SHAPE = RoundedCornerShape(17.dp)
private val SMALL_BENTO_PADDING = 11.dp
private val TEACHING_BENTO_HEIGHT = 126.dp

private val TARGET_DOT_SIZE = 5.dp
private val TARGET_DOT_TOP_PADDING = 5.dp
private val TARGET_DOT_SPACING = 7.dp

private val EXAMPLE_SHAPE = RoundedCornerShape(18.dp)
private val EXAMPLE_PADDING = 13.dp

private val PROGRESS_ORANGE = Color(0xFFE87516)
private val PROGRESS_AMBER = Color(0xFFE6B522)
private val PROGRESS_GREEN = Color(0xFF2E9B57)

private const val DROPDOWN_ENTER_MILLIS = 240
private const val DROPDOWN_EXIT_MILLIS = 160
private const val DROPDOWN_INITIAL_SCALE = 0.86f
private const val DROPDOWN_EXIT_SCALE = 0.92f
private const val DROPDOWN_BORDER_ALPHA = 0.55f

private const val HEADER_BACKGROUND_ALPHA = 0.13f
private const val READINESS_TRACK_ALPHA = 0.72f
private const val PRIMARY_NEED_BORDER_ALPHA = 0.30f
private const val WHY_NOW_BACKGROUND_ALPHA = 0.10f
private const val EXAMPLE_BACKGROUND_ALPHA = 0.13f

private const val MAXIMUM_TARGET_COUNT = 4
private const val COLOR_MIDPOINT = 0.5f