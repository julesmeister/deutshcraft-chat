package com.julesmeister.deutschcraft.feature.settings.presentation

import org.junit.Assert.assertEquals
import org.junit.Test

class AppMotionDurationScaleTest {
    @Test
    fun reducedMotionDisablesAnimationsAndRestoresCurrentSystemScale() {
        val scale = AppMotionDurationScale()
        assertEquals(1f, scale.scaleFactor, 0f)
        scale.systemScale = 1.5f
        assertEquals(1.5f, scale.scaleFactor, 0f)
        scale.reduceAnimations = true
        assertEquals(0f, scale.scaleFactor, 0f)
        scale.systemScale = 0.5f
        assertEquals(0f, scale.scaleFactor, 0f)
        scale.reduceAnimations = false
        assertEquals(0.5f, scale.scaleFactor, 0f)
    }

    @Test
    fun disablingAppPreferenceDoesNotOverrideSystemDisabledAnimations() {
        val scale = AppMotionDurationScale()
        scale.systemScale = 0f
        scale.reduceAnimations = true
        scale.reduceAnimations = false
        assertEquals(0f, scale.scaleFactor, 0f)
    }
}
