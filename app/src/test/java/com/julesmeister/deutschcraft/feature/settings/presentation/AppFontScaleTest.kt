package com.julesmeister.deutschcraft.feature.settings.presentation

import androidx.compose.ui.unit.Density
import com.julesmeister.deutschcraft.feature.settings.domain.FontScaleOption
import org.junit.Assert.assertEquals
import org.junit.Test

class AppFontScaleTest {
    @Test
    fun eachFontPreferencePreservesScreenDensityAndMultipliesSystemScaleOnce() {
        for (systemScale in listOf(0.85f, 1f, 1.5f, 2f)) {
            val systemDensity = Density(density = 3f, fontScale = systemScale)
            for (option in FontScaleOption.entries) {
                val appDensity = systemDensity.withAppFontScale(option.scale)
                assertEquals(3f, appDensity.density, 0f)
                assertEquals(systemScale * option.scale, appDensity.fontScale, 0f)
                assertEquals(systemScale, systemDensity.fontScale, 0f)
            }
        }
    }
}
