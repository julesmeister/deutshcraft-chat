package com.julesmeister.deutschcraft.feature.chat.presentation.component

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import com.julesmeister.deutschcraft.feature.chat.presentation.theme.ChatDesignTokens

internal object ChatBottomBarTransitions {

    val SuggestionEnter: EnterTransition =
        fadeIn(
            animationSpec =
                tween(
                    durationMillis =
                        ChatDesignTokens
                            .OfferEnterDurationMillis
                )
        ) +
                scaleIn(
                    initialScale =
                        ChatDesignTokens
                            .OfferEnterScale,
                    animationSpec =
                        tween(
                            durationMillis =
                                ChatDesignTokens
                                    .OfferEnterDurationMillis,
                            easing =
                                FastOutSlowInEasing
                        )
                )

    val SuggestionExit: ExitTransition =
        fadeOut(
            animationSpec =
                tween(
                    durationMillis =
                        ChatDesignTokens
                            .OfferExitDurationMillis
                )
        ) +
                scaleOut(
                    targetScale =
                        ChatDesignTokens
                            .OfferExitScale,
                    animationSpec =
                        tween(
                            durationMillis =
                                ChatDesignTokens
                                    .OfferExitDurationMillis,
                            easing =
                                FastOutSlowInEasing
                        )
                )

    val RefinementEnter: EnterTransition =
        fadeIn(
            animationSpec =
                tween(
                    durationMillis =
                        ChatDesignTokens
                            .RefinementAnimationMillis
                )
        ) +
                slideInVertically(
                    initialOffsetY = { fullHeight ->
                        fullHeight /
                                ChatDesignTokens
                                    .RefinementSlideOffsetDivisor
                    },
                    animationSpec =
                        tween(
                            durationMillis =
                                ChatDesignTokens
                                    .RefinementAnimationMillis,
                            easing =
                                FastOutSlowInEasing
                        )
                )

    val RefinementExit: ExitTransition =
        fadeOut(
            animationSpec =
                tween(
                    durationMillis =
                        ChatDesignTokens
                            .RefinementAnimationMillis
                )
        ) +
                slideOutVertically(
                    targetOffsetY = { fullHeight ->
                        fullHeight /
                                ChatDesignTokens
                                    .RefinementSlideOffsetDivisor
                    },
                    animationSpec =
                        tween(
                            durationMillis =
                                ChatDesignTokens
                                    .RefinementAnimationMillis,
                            easing =
                                FastOutSlowInEasing
                        )
                )
}