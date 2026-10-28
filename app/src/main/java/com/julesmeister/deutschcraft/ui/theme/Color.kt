package com.julesmeister.deutschcraft.ui.theme

import androidx.compose.ui.graphics.Color

/*
 * ===========================================================
 * BRAND
 * ===========================================================
 *
 * DeutschCraft's identity remains stable in both themes.
 */

val DeutschLime =
    Color(0xFFB7FF83)

val DeutschLimeSoft =
    Color(0xFFD9FFC2)

val DeutschLimeDark =
    Color(0xFF78C84F)

val DeutschLimeMuted =
    Color(0xFF5C7D49)

val DeutschSky =
    Color(0xFF8CC8FF)

val DeutschTextOnLime =
    Color(0xFF11140F)

val DeutschTextOnSky =
    Color(0xFF0D1620)


/*
 * ===========================================================
 * EXISTING DARK PALETTE
 * ===========================================================
 *
 * Preserve these names so existing source files that use them
 * continue compiling.
 */

val DeutschBlack =
    Color(0xFF0A0B0B)

val DeutschBlackSoft =
    Color(0xFF101112)

val DeutschSurface =
    Color(0xFF1B1D1F)

val DeutschSurfaceHigh =
    Color(0xFF242629)

val DeutschSurfaceHighest =
    Color(0xFF2D3033)

val DeutschTextPrimary =
    Color(0xFFF6F7F4)

val DeutschTextSecondary =
    Color(0xFFA8AAA5)

val DeutschTextDisabled =
    Color(0xFF6F726E)

val DeutschOutline =
    Color(0xFF34373A)

val DeutschOutlineSoft =
    Color(0xFF292C2E)


/*
 * ===========================================================
 * LIGHT PALETTE
 * ===========================================================
 */

val DeutschLightBackground =
    Color(0xFFF7F7F5)

val DeutschLightBackgroundSoft =
    Color(0xFFF1F1EE)

val DeutschLightSurface =
    Color(0xFFFFFFFF)

val DeutschLightSurfaceHigh =
    Color(0xFFEAEAE7)

val DeutschLightSurfaceHighest =
    Color(0xFFDDDEDA)

val DeutschLightTextPrimary =
    Color(0xFF171817)

val DeutschLightTextSecondary =
    Color(0xFF62655F)

val DeutschLightTextDisabled =
    Color(0xFF969992)

val DeutschLightOutline =
    Color(0xFF7B7E77)

val DeutschLightOutlineSoft =
    Color(0xFFD1D3CD)

/*
 * Slightly deeper lime container so pale surfaces remain
 * visually separated.
 */
val DeutschLightPrimaryContainer =
    Color(0xFFDFFFC8)

val DeutschLightAssistantContainer =
    Color(0xFFE2EEF7)


/*
 * ===========================================================
 * STATUS
 * ===========================================================
 */

val DeutschSuccess =
    Color(0xFF9BEC72)

val DeutschWarning =
    Color(0xFFFFD166)

val DeutschError =
    Color(0xFFFF8A80)

val DeutschInfo =
    Color(0xFF8CC8FF)


/*
 * ===========================================================
 * LEGACY CHAT BUBBLE COLORS
 * ===========================================================
 *
 * Keep these existing values for compatibility.
 *
 * Ideally chat bubble presentation should eventually consume
 * MaterialTheme.colorScheme containers so those bubbles can
 * become completely theme-aware without hardcoded colors.
 */

val DeutschAssistantBubble =
    Color(0xFF2F3C47)

val DeutschUserBubble =
    Color(0xFF374631)

/*
 * Deep ink used for strong accents on light surfaces.
 */
val DeutschInk =
    Color(0xFF16181A)