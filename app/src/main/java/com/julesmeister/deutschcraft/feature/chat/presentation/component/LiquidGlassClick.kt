package com.julesmeister.deutschcraft.feature.chat.presentation.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier

@Stable
internal fun Modifier.liquidGlassClick(
    enabled: Boolean,
    interactionSource: MutableInteractionSource,
    onClick: () -> Unit
): Modifier {
    return clickable(
        enabled =
            enabled,
        interactionSource =
            interactionSource,
        indication =
            null,
        onClick =
            onClick
    )
}