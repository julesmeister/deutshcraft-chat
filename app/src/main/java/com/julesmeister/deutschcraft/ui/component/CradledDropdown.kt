package com.julesmeister.deutschcraft.ui.component

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
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.semantics.Role
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

internal object CradledDropdownDefaults {
    val Radius = 30.dp
    val Width = 304.dp
    val AnchorSize = 48.dp
    val Padding = 12.dp
    val ContentSpacing = 8.dp
    val WindowMargin = 8.dp
}

@Composable
internal fun CradledDropdown(
    expanded: Boolean,
    onDismiss: () -> Unit,
    anchorContent: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    anchorSize: Dp = CradledDropdownDefaults.AnchorSize,
    cradleRadius: Dp = CradledDropdownDefaults.Radius,
    anchorDescription: String,
    width: Dp = CradledDropdownDefaults.Width,
    windowMargin: Dp = CradledDropdownDefaults.WindowMargin,
    bodyTopOffset: Dp = 44.dp,
    curvedConnector: Boolean = true,
    content: @Composable ColumnScope.() -> Unit
) {
    val transition = remember { MutableTransitionState(false) }
    LaunchedEffect(expanded) {
        transition.targetState = expanded
    }
    if (!transition.currentState && !transition.targetState && transition.isIdle) return

    val density = LocalDensity.current
    val availableWidth = (LocalConfiguration.current.screenWidthDp.dp - windowMargin * 2)
        .coerceAtLeast(1.dp)
    val popupWidth = width.coerceAtMost(availableWidth)
    val positionProvider = remember(density, windowMargin, cradleRadius) {
        CradlePopupPositionProvider(
            cradleCenterFromRightPx = with(density) { cradleRadius.roundToPx() },
            cradleCenterFromTopPx = with(density) { cradleRadius.roundToPx() },
            windowMarginPx = with(density) { windowMargin.roundToPx() }
        )
    }
    val pivot = TransformOrigin(
        1f - cradleRadius.value / popupWidth.value,
        0.04f
    )
    val shape = remember(bodyTopOffset, cradleRadius, curvedConnector) {
        CradleDropdownShape(bodyTopOffset, cradleRadius, curvedConnector)
    }
    val colors = MaterialTheme.colorScheme

    Popup(
        popupPositionProvider = positionProvider,
        onDismissRequest = onDismiss,
        properties = PopupProperties(
            focusable = true,
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            clippingEnabled = false
        )
    ) {
        AnimatedVisibility(
            visibleState = transition,
            enter = fadeIn(tween(280, easing = FastOutSlowInEasing)) +
                slideInVertically(
                    initialOffsetY = { with(density) { (-6).dp.roundToPx() } },
                    animationSpec = tween(320, easing = FastOutSlowInEasing)
                ) +
                scaleIn(
                    initialScale = 0.94f,
                    transformOrigin = pivot,
                    animationSpec = spring(dampingRatio = 0.95f, stiffness = Spring.StiffnessLow)
                ),
            exit = fadeOut(tween(180)) +
                slideOutVertically(
                    targetOffsetY = { with(density) { (-4).dp.roundToPx() } },
                    animationSpec = tween(180, easing = FastOutSlowInEasing)
                ) +
                scaleOut(
                    targetScale = 0.97f,
                    transformOrigin = pivot,
                    animationSpec = tween(160, easing = FastOutSlowInEasing)
                )
        ) {
            Box(
                modifier = modifier
                    .requiredWidth(popupWidth)
                    .padding(bottom = 16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(
                            elevation = 14.dp,
                            shape = shape,
                            clip = false,
                            ambientColor = Color.Black.copy(alpha = 0.18f),
                            spotColor = Color.Black.copy(alpha = 0.24f)
                        )
                        .clip(shape)
                        .background(colors.surfaceContainerHigh)
                        .border(1.dp, colors.outlineVariant.copy(alpha = 0.55f), shape)
                        .padding(
                            start = CradledDropdownDefaults.Padding,
                            end = CradledDropdownDefaults.Padding,
                            bottom = CradledDropdownDefaults.Padding,
                            top = maxOf(cradleRadius * 2 + 4.dp, bodyTopOffset + 16.dp)
                        ),
                    verticalArrangement = Arrangement.spacedBy(CradledDropdownDefaults.ContentSpacing),
                    content = content
                )
                // This copy overlays the real trigger, making it look embedded in the popup.
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(
                            x = -(cradleRadius - anchorSize / 2),
                            y = cradleRadius - anchorSize / 2
                        )
                        .size(anchorSize)
                        .clip(CircleShape)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            role = Role.Button,
                            onClickLabel = anchorDescription,
                            onClick = onDismiss
                        ),
                    contentAlignment = Alignment.Center,
                    content = { anchorContent() }
                )
            }
        }
    }
}

internal class CradleDropdownShape(
    private val bodyTopOffset: Dp = 44.dp,
    private val cradleRadius: Dp = CradledDropdownDefaults.Radius,
    private val curvedConnector: Boolean = true
) : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        val radius = with(density) { cradleRadius.toPx() }
        val bodyTop = with(density) { bodyTopOffset.toPx() }
        val fillet = with(density) { 14.dp.toPx() }
        val corner = with(density) { 26.dp.toPx() }
        val width = size.width
        val height = size.height
        val centerX = width - radius
        val centerY = radius
        val dy = (bodyTop - centerY).coerceIn(0f, radius * 0.95f)
        val intersectionX = centerX - sqrt(radius * radius - dy * dy)
        val intersectionAngle = 180.0 - Math.toDegrees(asin((dy / radius).toDouble()))
        val arcStartAngle = intersectionAngle + 14.0
        val radians = Math.toRadians(arcStartAngle)
        val arcStart = Offset(
            centerX + radius * cos(radians).toFloat(),
            centerY + radius * sin(radians).toFloat()
        )
        return Outline.Generic(
            Path().apply {
                moveTo(0f, bodyTop + corner)
                arcTo(Rect(0f, bodyTop, corner * 2, bodyTop + corner * 2), 180f, 90f, false)
                if (curvedConnector) {
                    lineTo(intersectionX - fillet, bodyTop)
                    quadraticTo(intersectionX, bodyTop, arcStart.x, arcStart.y)
                    arcTo(
                        Rect(centerX - radius, centerY - radius, centerX + radius, centerY + radius),
                        arcStartAngle.toFloat(), (360.0 - arcStartAngle).toFloat(), false
                    )
                } else {
                    lineTo(centerX - radius, bodyTop)
                    lineTo(centerX - radius, centerY)
                    arcTo(
                        Rect(centerX - radius, centerY - radius, centerX + radius, centerY + radius),
                        180f, 180f, false
                    )
                }
                lineTo(width, height - corner)
                arcTo(Rect(width - corner * 2, height - corner * 2, width, height), 0f, 90f, false)
                lineTo(corner, height)
                arcTo(Rect(0f, height - corner * 2, corner * 2, height), 90f, 90f, false)
                close()
            }
        )
    }
}

internal class CradlePopupPositionProvider(
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
        val centerX = anchorBounds.left + anchorBounds.width / 2
        val centerY = anchorBounds.top + anchorBounds.height / 2
        val preferredX = centerX - (popupContentSize.width - cradleCenterFromRightPx)
        val maximumX = (windowSize.width - popupContentSize.width - windowMarginPx)
            .coerceAtLeast(windowMarginPx)
        return IntOffset(
            preferredX.coerceIn(windowMarginPx, maximumX),
            centerY - cradleCenterFromTopPx
        )
    }
}
