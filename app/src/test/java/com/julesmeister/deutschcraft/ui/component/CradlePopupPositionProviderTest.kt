package com.julesmeister.deutschcraft.ui.component

import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import org.junit.Assert.assertEquals
import org.junit.Test

class CradlePopupPositionProviderTest {
    @Test
    fun threadsMenuKeepsRightGutterAndCradleAlignedWithInsetTrigger() {
        val provider = CradlePopupPositionProvider(20, 20, 8)
        val position = provider.calculatePosition(
            IntRect(352, 24, 392, 64),
            IntSize(400, 800),
            LayoutDirection.Ltr,
            IntSize(280, 260)
        )
        assertEquals(372, position.x + 280 - 20)
        assertEquals(8, 400 - (position.x + 280))
        assertEquals(44, position.y + 20)
    }

    @Test
    fun cradleCenterOverlaysEitherTriggerSizeAtDifferentDensities() {
        for (density in listOf(1, 2, 3)) {
            for (triggerSize in listOf(40, 48)) {
                for (direction in LayoutDirection.entries) {
                    val radius = 30 * density
                    val provider = CradlePopupPositionProvider(radius, radius, 8 * density)
                    val centerX = 360 * density
                    val centerY = 52 * density
                    val halfTrigger = triggerSize * density / 2
                    val popup = IntSize(304 * density, 300 * density)
                    val position = provider.calculatePosition(
                        anchorBounds = IntRect(
                            centerX - halfTrigger, centerY - halfTrigger,
                            centerX + halfTrigger, centerY + halfTrigger
                        ),
                        windowSize = IntSize(400 * density, 800 * density),
                        layoutDirection = direction,
                        popupContentSize = popup
                    )

                    assertEquals(centerX, position.x + popup.width - radius)
                    assertEquals(centerY, position.y + radius)
                }
            }
        }
    }

    @Test
    fun horizontalPositionStaysWithinWindowMargins() {
        val provider = CradlePopupPositionProvider(30, 30, 8)
        val window = IntSize(400, 800)
        val popup = IntSize(304, 300)
        assertEquals(
            IntOffset(8, 10),
            provider.calculatePosition(IntRect(0, 20, 40, 60), window, LayoutDirection.Ltr, popup)
        )
        assertEquals(
            IntOffset(88, 10),
            provider.calculatePosition(IntRect(400, 20, 440, 60), window, LayoutDirection.Ltr, popup)
        )
    }
}
