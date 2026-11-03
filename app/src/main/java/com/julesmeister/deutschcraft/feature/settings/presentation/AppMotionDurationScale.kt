package com.julesmeister.deutschcraft.feature.settings.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.MotionDurationScale

class AppMotionDurationScale : MotionDurationScale {
    var reduceAnimations by mutableStateOf(false)
    var systemScale by mutableFloatStateOf(1f)

    override val scaleFactor: Float
        get() = if (reduceAnimations) 0f else systemScale
}
