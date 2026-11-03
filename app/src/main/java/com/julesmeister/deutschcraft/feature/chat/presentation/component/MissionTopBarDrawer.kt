package com.julesmeister.deutschcraft.feature.chat.presentation.component

import androidx.compose.animation.core.animate
import androidx.compose.foundation.MutatePriority
import androidx.compose.foundation.MutatorMutex
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.julesmeister.deutschcraft.feature.chat.presentation.theme.ChatDesignTokens
import com.julesmeister.deutschcraft.feature.mission.presentation.guidance.MissionGuidanceUiState
import com.julesmeister.deutschcraft.feature.settings.presentation.LocalUserSettings
import kotlin.math.roundToInt

private typealias DrawerTokens = ChatDesignTokens.MissionDrawer

/** Shared reveal progress (0 = collapsed, 1 = expanded) for the mission pull-down drawer. */
@Stable
internal class MissionDrawerState {
    var progress by mutableFloatStateOf(0f)
        internal set
    internal var panelHeightPx by mutableFloatStateOf(0f)
    internal var fallbackHeightPx = 0f

    internal fun dragBy(deltaPx: Float) {
        val height = panelHeightPx.takeIf { it > 0f } ?: fallbackHeightPx
        if (height <= 0f) return
        progress = (progress + deltaPx / height).coerceIn(0f, 1f)
    }

    private val mutex = MutatorMutex()

    internal suspend fun stopAnimation() = mutex.mutate(MutatePriority.UserInput) {}

    internal suspend fun animateTo(target: Float, reduceMotion: Boolean) = mutex.mutate {
        if (reduceMotion) {
            progress = target
            return@mutate
        }
        animate(
            initialValue = progress,
            targetValue = target,
            animationSpec = spring(
                dampingRatio = DrawerTokens.SpringDampingRatio,
                stiffness = DrawerTokens.SpringStiffness
            )
        ) { value, _ -> progress = value }
    }
}

@Composable
internal fun rememberMissionDrawerState(): MissionDrawerState {
    val state = remember { MissionDrawerState() }
    state.fallbackHeightPx = with(LocalDensity.current) { DrawerTokens.FallbackPanelHeight.toPx() }
    return state
}

/**
 * Vertical drag that pulls the mission drawer open (down) or pushes it closed (up).
 * Apply to the top bar and the drawer handle so either can be dragged.
 */
@Composable
internal fun Modifier.missionDrawerDrag(
    state: MissionDrawerState,
    enabled: Boolean,
    expanded: Boolean,
    onExpand: () -> Unit,
    onCollapse: () -> Unit
): Modifier {
    val reduceMotion = LocalUserSettings.current.reduceAnimations
    val flingVelocity = with(LocalDensity.current) { DrawerTokens.FlingVelocity.toPx() }
    val currentExpanded by rememberUpdatedState(expanded)
    val currentOnExpand by rememberUpdatedState(onExpand)
    val currentOnCollapse by rememberUpdatedState(onCollapse)
    return draggable(
        state = rememberDraggableState { delta -> state.dragBy(delta) },
        orientation = Orientation.Vertical,
        enabled = enabled,
        onDragStarted = { state.stopAnimation() },
        onDragStopped = { velocity ->
            val open = when {
                velocity > flingVelocity -> true
                velocity < -flingVelocity -> false
                currentExpanded -> state.progress > 1f - DrawerTokens.ExpandThreshold
                else -> state.progress > DrawerTokens.ExpandThreshold
            }
            when {
                open && !currentExpanded -> currentOnExpand()
                !open && currentExpanded -> currentOnCollapse()
                else -> state.animateTo(if (open) 1f else 0f, reduceMotion)
            }
        }
    )
}

/**
 * Overlay that slides mission details down from beneath the top bar without moving the
 * conversation, plus a handle tab that hangs below the bar.
 */
@Composable
internal fun MissionTopBarDrawer(
    state: MissionDrawerState,
    guidance: MissionGuidanceUiState,
    expanded: Boolean,
    onExpand: () -> Unit,
    onCollapse: () -> Unit,
    onRevealNextHint: () -> Unit,
    onRetryLanguageSupport: () -> Unit,
    modifier: Modifier = Modifier
) {
    val reduceMotion = LocalUserSettings.current.reduceAnimations
    LaunchedEffect(expanded) { state.animateTo(if (expanded) 1f else 0f, reduceMotion) }
    if (!guidance.hasActiveMission) return

    val progress = state.progress
    val maxPanelHeight = LocalConfiguration.current.screenHeightDp.dp * DrawerTokens.MaxHeightFraction
    val panelColor = MaterialTheme.colorScheme.background
    val shadowColor = Color.Black.copy(
        alpha = if (panelColor.luminance() < ChatDesignTokens.DarkLuminanceThreshold) {
            DrawerTokens.ShadowDarkAlpha
        } else {
            DrawerTokens.ShadowLightAlpha
        }
    )

    Box(modifier = modifier.fillMaxSize()) {
        if (progress > 0f) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.scrim.copy(alpha = DrawerTokens.ScrimMaxAlpha * progress))
                    .pointerInput(Unit) { detectTapGestures { onCollapse() } }
            )
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .drawBehind {
                    drawUnifiedBarOutline(
                        edgeY = state.panelHeightPx * progress,
                        color = panelColor,
                        shadowColor = shadowColor
                    )
                },
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (progress > 0f || expanded) {
                MissionTopBarDetails(
                    state = guidance,
                    onRevealNextHint = onRevealNextHint,
                    onRetryLanguageSupport = onRetryLanguageSupport,
                    modifier = Modifier
                        .clipToBounds()
                        .revealFromTop(progress) { state.panelHeightPx = it.toFloat() }
                        .heightIn(max = maxPanelHeight)
                )
            }
            MissionDrawerHandle(
                state = state,
                expanded = expanded,
                onExpand = onExpand,
                onCollapse = onCollapse
            )
        }
    }
}

/**
 * One continuous outline for the bar's bottom edge (or the open panel's) plus the handle tab,
 * joined by concave fillets, filled with the bar colour and casting a single soft shadow.
 */
private fun DrawScope.drawUnifiedBarOutline(edgeY: Float, color: Color, shadowColor: Color) {
    val tabWidth = DrawerTokens.HandleWidth.toPx()
    val tabBottom = edgeY + DrawerTokens.HandleHeight.toPx()
    val corner = DrawerTokens.HandleCornerRadius.toPx()
    val fillet = DrawerTokens.HandleFilletRadius.toPx()
    val shadowRadius = DrawerTokens.ShadowRadius.toPx()
    val left = (size.width - tabWidth) / 2f
    val right = left + tabWidth
    val top = -shadowRadius * 4f
    val path = Path().apply {
        moveTo(-shadowRadius, top)
        lineTo(-shadowRadius, edgeY)
        lineTo(left - fillet, edgeY)
        quadraticTo(left, edgeY, left, edgeY + fillet)
        lineTo(left, tabBottom - corner)
        quadraticTo(left, tabBottom, left + corner, tabBottom)
        lineTo(right - corner, tabBottom)
        quadraticTo(right, tabBottom, right, tabBottom - corner)
        lineTo(right, edgeY + fillet)
        quadraticTo(right, edgeY, right + fillet, edgeY)
        lineTo(size.width + shadowRadius, edgeY)
        lineTo(size.width + shadowRadius, top)
        close()
    }
    drawIntoCanvas { canvas ->
        val paint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
            this.color = color.toArgb()
            setShadowLayer(shadowRadius, 0f, DrawerTokens.ShadowOffsetY.toPx(), shadowColor.toArgb())
        }
        canvas.nativeCanvas.drawPath(path.asAndroidPath(), paint)
    }
}

@Composable
private fun MissionDrawerHandle(
    state: MissionDrawerState,
    expanded: Boolean,
    onExpand: () -> Unit,
    onCollapse: () -> Unit
) {
    val description = if (expanded) "Collapse mission details" else "Expand mission details"
    Box(
        contentAlignment = Alignment.TopCenter,
        modifier = Modifier
            .size(DrawerTokens.HandleTouchWidth, DrawerTokens.HandleTouchHeight)
            .missionDrawerDrag(state, enabled = true, expanded = expanded, onExpand = onExpand, onCollapse = onCollapse)
            .clickable(role = Role.Button) { if (expanded) onCollapse() else onExpand() }
            .semantics { contentDescription = description }
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(DrawerTokens.HandleWidth, DrawerTokens.HandleHeight)
        ) {
            Icon(
                imageVector = Icons.Outlined.KeyboardArrowDown,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = DrawerTokens.HandleIconAlpha),
                modifier = Modifier
                    .size(DrawerTokens.HandleIconSize)
                    .graphicsLayer { rotationZ = DrawerTokens.HandleRotationDegrees * state.progress }
            )
        }
    }
}

/** Measures content at full height but only lays out [progress] of it, sliding it down from the top. */
private fun Modifier.revealFromTop(progress: Float, onMeasured: (Int) -> Unit): Modifier =
    layout { measurable, constraints ->
        val placeable = measurable.measure(constraints)
        onMeasured(placeable.height)
        val visibleHeight = (placeable.height * progress).roundToInt()
        layout(placeable.width, visibleHeight) {
            placeable.place(0, visibleHeight - placeable.height)
        }
    }
