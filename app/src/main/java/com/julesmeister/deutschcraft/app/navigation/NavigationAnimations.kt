package com.julesmeister.deutschcraft.app.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically

internal fun startupEnterTransition():
        EnterTransition {
    return fadeIn(
        animationSpec =
            tween(
                durationMillis =
                    STARTUP_FADE_DURATION_MILLIS,
                easing =
                    LinearOutSlowInEasing
            )
    )
}

internal fun startupExitTransition():
        ExitTransition {
    return fadeOut(
        animationSpec =
            tween(
                durationMillis =
                    STARTUP_FADE_DURATION_MILLIS,
                easing =
                    FastOutSlowInEasing
            )
    )
}

internal fun forwardEnterTransition():
        EnterTransition {
    return slideInHorizontally(
        initialOffsetX = { fullWidth ->
            fullWidth
        },
        animationSpec =
            tween(
                durationMillis =
                    FORWARD_ENTER_DURATION_MILLIS,
                easing =
                    FastOutSlowInEasing
            )
    ) + fadeIn(
        animationSpec =
            tween(
                durationMillis =
                    FORWARD_FADE_DURATION_MILLIS,
                easing =
                    LinearOutSlowInEasing
            )
    )
}

internal fun forwardExitTransition():
        ExitTransition {
    return slideOutHorizontally(
        targetOffsetX = { fullWidth ->
            -(
                    fullWidth /
                            BACKGROUND_MOVEMENT_DIVISOR
                    )
        },
        animationSpec =
            tween(
                durationMillis =
                    FORWARD_EXIT_DURATION_MILLIS,
                easing =
                    FastOutSlowInEasing
            )
    ) + fadeOut(
        animationSpec =
            tween(
                durationMillis =
                    FORWARD_FADE_DURATION_MILLIS
            )
    )
}

internal fun backEnterTransition():
        EnterTransition {
    return slideInHorizontally(
        initialOffsetX = { fullWidth ->
            -(
                    fullWidth /
                            BACKGROUND_MOVEMENT_DIVISOR
                    )
        },
        animationSpec =
            tween(
                durationMillis =
                    BACK_ENTER_DURATION_MILLIS,
                easing =
                    FastOutSlowInEasing
            )
    ) + fadeIn(
        animationSpec =
            tween(
                durationMillis =
                    BACK_FADE_DURATION_MILLIS,
                easing =
                    LinearOutSlowInEasing
            )
    )
}

internal fun backExitTransition():
        ExitTransition {
    return slideOutHorizontally(
        targetOffsetX = { fullWidth ->
            fullWidth
        },
        animationSpec =
            tween(
                durationMillis =
                    BACK_EXIT_DURATION_MILLIS,
                easing =
                    FastOutSlowInEasing
            )
    ) + fadeOut(
        animationSpec =
            tween(
                durationMillis =
                    BACK_FADE_DURATION_MILLIS
            )
    )
}

internal fun progressEnterTransition():
        EnterTransition {
    return slideInVertically(
        initialOffsetY = { fullHeight ->
            fullHeight
        },
        animationSpec =
            tween(
                durationMillis =
                    PROGRESS_ENTER_DURATION_MILLIS,
                easing =
                    FastOutSlowInEasing
            )
    ) + fadeIn(
        animationSpec =
            tween(
                durationMillis =
                    PROGRESS_FADE_DURATION_MILLIS,
                easing =
                    LinearOutSlowInEasing
            )
    )
}

internal fun progressExitTransition():
        ExitTransition {
    return slideOutVertically(
        targetOffsetY = { fullHeight ->
            fullHeight
        },
        animationSpec =
            tween(
                durationMillis =
                    PROGRESS_EXIT_DURATION_MILLIS,
                easing =
                    FastOutSlowInEasing
            )
    ) + fadeOut(
        animationSpec =
            tween(
                durationMillis =
                    PROGRESS_FADE_DURATION_MILLIS
            )
    )
}

internal fun secondaryScreenEnterTransition():
        EnterTransition {
    return fadeIn(
        animationSpec =
            tween(
                durationMillis =
                    SECONDARY_FADE_DURATION_MILLIS,
                easing =
                    LinearOutSlowInEasing
            )
    )
}

internal fun secondaryScreenExitTransition():
        ExitTransition {
    return fadeOut(
        animationSpec =
            tween(
                durationMillis =
                    SECONDARY_FADE_DURATION_MILLIS,
                easing =
                    FastOutSlowInEasing
            )
    )
}

private const val STARTUP_FADE_DURATION_MILLIS =
    280

private const val FORWARD_ENTER_DURATION_MILLIS =
    360

private const val FORWARD_EXIT_DURATION_MILLIS =
    320

private const val FORWARD_FADE_DURATION_MILLIS =
    240

private const val BACK_ENTER_DURATION_MILLIS =
    320

private const val BACK_EXIT_DURATION_MILLIS =
    300

private const val BACK_FADE_DURATION_MILLIS =
    220

private const val PROGRESS_ENTER_DURATION_MILLIS =
    400

private const val PROGRESS_EXIT_DURATION_MILLIS =
    340

private const val PROGRESS_FADE_DURATION_MILLIS =
    260

private const val SECONDARY_FADE_DURATION_MILLIS =
    260

private const val BACKGROUND_MOVEMENT_DIVISOR =
    4