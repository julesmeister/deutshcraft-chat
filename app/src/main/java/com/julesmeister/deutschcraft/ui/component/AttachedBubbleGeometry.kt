package com.julesmeister.deutschcraft.ui.component

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/*
 * Geometry for a vertical stack of bubbles glued to a source
 * message (source -> Verbessern challenge -> marked phrases...).
 *
 * All bubbles in a stack share the same aligned "inner" edge
 * (right for user messages, left for assistant messages), so
 * inner corners at junctions are always flat.
 *
 * The opposite "outer" edge is ragged because every bubble has
 * its own width. At each junction, whichever bubble sticks out
 * further gets a rounded outer corner; the other one stays flat.
 * Equal widths produce a flush, seamless edge.
 *
 * The rounded radius is capped by how far the bubble sticks out
 * so small width differences produce a smooth step instead of a
 * notch.
 */
object AttachedBubbleGeometry {

    val CornerRadius: Dp = 14.dp

    private const val WIDTH_TOLERANCE_PX = 2

    /*
     * Radius of the outer corner of a bubble at its junction with
     * a neighbour. Returns 0.dp while widths are unmeasured, when
     * the bubbles are flush, or when the neighbour is wider.
     */
    fun exposedOuterCorner(
        selfWidthPx: Int,
        neighbourWidthPx: Int,
        maxRadius: Dp,
        density: Density
    ): Dp {
        if (
            selfWidthPx <= 0 ||
            neighbourWidthPx <= 0
        ) {
            return 0.dp
        }

        val overhangPx =
            selfWidthPx - neighbourWidthPx

        if (overhangPx <= WIDTH_TOLERANCE_PX) {
            return 0.dp
        }

        val overhang =
            with(density) {
                overhangPx.toDp()
            }

        return minOf(
            overhang,
            maxRadius
        )
    }

    /*
     * Shape for an attached bubble. [belowWidthPx] is null when
     * this bubble is the last one in the stack.
     */
    fun attachmentShape(
        isUserMessage: Boolean,
        aboveWidthPx: Int,
        selfWidthPx: Int,
        belowWidthPx: Int?,
        radius: Dp,
        density: Density
    ): RoundedCornerShape {
        val topOuter =
            exposedOuterCorner(
                selfWidthPx =
                    selfWidthPx,
                neighbourWidthPx =
                    aboveWidthPx,
                maxRadius =
                    radius,
                density =
                    density
            )

        val bottomOuter =
            if (belowWidthPx == null) {
                radius
            } else {
                exposedOuterCorner(
                    selfWidthPx =
                        selfWidthPx,
                    neighbourWidthPx =
                        belowWidthPx,
                    maxRadius =
                        radius,
                    density =
                        density
                )
            }

        val bottomInner =
            if (belowWidthPx == null) {
                radius
            } else {
                0.dp
            }

        return if (isUserMessage) {
            RoundedCornerShape(
                topStart =
                    topOuter,
                topEnd =
                    0.dp,
                bottomStart =
                    bottomOuter,
                bottomEnd =
                    bottomInner
            )
        } else {
            RoundedCornerShape(
                topStart =
                    0.dp,
                topEnd =
                    topOuter,
                bottomStart =
                    bottomInner,
                bottomEnd =
                    bottomOuter
            )
        }
    }
}
