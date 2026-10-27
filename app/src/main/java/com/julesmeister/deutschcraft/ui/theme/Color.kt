package com.julesmeister.deutschcraft.ui.theme

import androidx.compose.ui.graphics.Color

/*
 * Core background colors
 */
val DeutschBlack = Color(0xFF0A0B0B)
val DeutschBlackSoft = Color(0xFF101112)

val DeutschSurface = Color(0xFF1B1D1F)
val DeutschSurfaceHigh = Color(0xFF242629)
val DeutschSurfaceHighest = Color(0xFF2D3033)

/*
 * Brand colors
 */
val DeutschLime = Color(0xFFB7FF83)
val DeutschLimeSoft = Color(0xFFD9FFC2)
val DeutschLimeDark = Color(0xFF78C84F)
val DeutschLimeMuted = Color(0xFF5C7D49)

/*
 * Text colors
 */
val DeutschTextPrimary = Color(0xFFF6F7F4)
val DeutschTextSecondary = Color(0xFFA8AAA5)
val DeutschTextDisabled = Color(0xFF6F726E)
val DeutschTextOnLime = Color(0xFF11140F)

/*
 * Borders and dividers
 */
val DeutschOutline = Color(0xFF34373A)
val DeutschOutlineSoft = Color(0xFF292C2E)

/*
 * Status colors
 */
val DeutschSuccess = Color(0xFF9BEC72)
val DeutschWarning = Color(0xFFFFD166)
val DeutschError = Color(0xFFFF8A80)
val DeutschInfo = Color(0xFF8CC8FF)
/*
 * Chat bubbles
 *
 * Emil (assistant) = sky
 * Learner          = lime
 *
 * Latest message on each side is vibrant.
 * Older messages fade to a deep tint of the same hue.
 */

/*
 * Emil's accent. Same value as DeutschInfo, named for chat use.
 */
val DeutschSky = Color(0xFF8CC8FF)

/*
 * Readable dark navy on DeutschSky.
 */
val DeutschTextOnSky = Color(0xFF0D1620)

/*
 * DeutschSurface with ~18% DeutschSky blended in.
 * Used for Emil's older replies.
 */
val DeutschAssistantBubble = Color(0xFF2F3C47)

/*
 * DeutschSurface with ~18% DeutschLime blended in.
 * Used for the learner's older messages.
 */
val DeutschUserBubble = Color(0xFF374631)