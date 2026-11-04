package com.julesmeister.deutschcraft.ui.component

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Test

class AttachedBubbleGeometryTest {

    private val density = Density(1f)
    private val radius = 14.dp
    private val size = Size(1000f, 1000f)

    private data class Corners(
        val topStart: Float,
        val topEnd: Float,
        val bottomEnd: Float,
        val bottomStart: Float
    )

    private fun RoundedCornerShape.corners() =
        Corners(
            topStart.toPx(size, density),
            topEnd.toPx(size, density),
            bottomEnd.toPx(size, density),
            bottomStart.toPx(size, density)
        )

    private fun shape(
        isUser: Boolean,
        above: Int,
        self: Int,
        below: Int?
    ) =
        AttachedBubbleGeometry.attachmentShape(
            isUserMessage = isUser,
            aboveWidthPx = above,
            selfWidthPx = self,
            belowWidthPx = below,
            radius = radius,
            density = density
        ).corners()

    @Test
    fun `user middle bubble wider than both neighbours rounds both outer corners`() {
        assertEquals(
            Corners(topStart = 14f, topEnd = 0f, bottomEnd = 0f, bottomStart = 14f),
            shape(isUser = true, above = 200, self = 300, below = 150)
        )
    }

    @Test
    fun `user middle bubble narrower than neighbour below keeps bottom outer flat`() {
        assertEquals(
            Corners(topStart = 0f, topEnd = 0f, bottomEnd = 0f, bottomStart = 0f),
            shape(isUser = true, above = 300, self = 200, below = 250)
        )
    }

    @Test
    fun `last bubble rounds both bottom corners`() {
        assertEquals(
            Corners(topStart = 0f, topEnd = 0f, bottomEnd = 14f, bottomStart = 14f),
            shape(isUser = true, above = 300, self = 200, below = null)
        )
    }

    @Test
    fun `assistant bubble mirrors outer corners to the end side`() {
        assertEquals(
            Corners(topStart = 0f, topEnd = 14f, bottomEnd = 14f, bottomStart = 0f),
            shape(isUser = false, above = 100, self = 300, below = 100)
        )
    }

    @Test
    fun `flush or unmeasured widths stay flat`() {
        assertEquals(
            Corners(0f, 0f, 0f, 0f),
            shape(isUser = true, above = 201, self = 200, below = 0)
        )
    }

    @Test
    fun `small overhang caps radius to the overhang`() {
        assertEquals(
            5.dp,
            AttachedBubbleGeometry.exposedOuterCorner(
                selfWidthPx = 205,
                neighbourWidthPx = 200,
                maxRadius = radius,
                density = density
            )
        )
    }
}
