package com.julesmeister.deutschcraft.feature.progress.presentation

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import com.julesmeister.deutschcraft.ui.theme.DeutschLime

@Immutable
internal data class ProgressColors(
    val isDark: Boolean,

    val textPrimary: Color,
    val textSecondary: Color,

    val accent: Color,
    val accentContainer: Color,
    val progressTrack: Color,

    val currentLevelBackground: Color,
    val currentLevelTextPrimary: Color,
    val currentLevelTextSecondary: Color,

    val learningAccent: Color,
    val learningAccentContainer: Color,
    val learningPanelBackground: Color,
    val learningItemBackground: Color,
    val learningSummaryBackground: Color,
    val learningProgressTrack: Color,
    val learningTextPrimary: Color,
    val learningTextSecondary: Color,

    val growthAccent: Color,
    val growthAccentContainer: Color,
    val growthPanelBackground: Color,
    val growthItemBackground: Color,
    val growthProgressTrack: Color,
    val growthTextPrimary: Color,
    val growthTextSecondary: Color,

    val missionAccent: Color,
    val missionAccentContainer: Color,
    val missionPanelBackground: Color,
    val missionItemBackground: Color,
    val missionProgressTrack: Color,
    val missionTextPrimary: Color,
    val missionTextSecondary: Color,

    val skillAccent: Color,
    val skillAccentContainer: Color,
    val skillPanelBackground: Color,
    val skillProgressTrack: Color,
    val skillTextPrimary: Color,
    val skillTextSecondary: Color,

    val confidenceAccent: Color,
    val confidencePanelBackground: Color,
    val confidenceTextPrimary: Color,
    val confidenceTextSecondary: Color
)

@Composable
internal fun rememberProgressColors():
        ProgressColors {
    val scheme =
        MaterialTheme.colorScheme

    return remember(
        scheme
    ) {
        val isDark =
            scheme.background.luminance() <
                    DARK_LUMINANCE_THRESHOLD

        if (isDark) {
            val accent =
                DeutschLime

            val accentContainer =
                DeutschLime.copy(
                    alpha =
                        DARK_ACCENT_CONTAINER_ALPHA
                )

            ProgressColors(
                isDark =
                    true,

                textPrimary =
                    scheme.onSurface,

                textSecondary =
                    scheme.onSurfaceVariant,

                accent =
                    accent,

                accentContainer =
                    accentContainer,

                progressTrack =
                    scheme.surfaceContainerHighest,

                currentLevelBackground =
                    CefrCurrentLevelBackground,

                currentLevelTextPrimary =
                    CefrCurrentLevelTextPrimary,

                currentLevelTextSecondary =
                    CefrCurrentLevelTextSecondary,

                learningAccent =
                    accent,

                learningAccentContainer =
                    accentContainer,

                learningPanelBackground =
                    scheme.surfaceContainer,

                learningItemBackground =
                    scheme.surfaceContainerHigh,

                learningSummaryBackground =
                    scheme.surfaceContainerHighest,

                learningProgressTrack =
                    scheme.surfaceContainerHighest,

                learningTextPrimary =
                    scheme.onSurface,

                learningTextSecondary =
                    scheme.onSurfaceVariant,

                growthAccent =
                    accent,

                growthAccentContainer =
                    accentContainer,

                growthPanelBackground =
                    scheme.surfaceContainer,

                growthItemBackground =
                    scheme.surfaceContainerHigh,

                growthProgressTrack =
                    scheme.surfaceContainerHighest,

                growthTextPrimary =
                    scheme.onSurface,

                growthTextSecondary =
                    scheme.onSurfaceVariant,

                missionAccent =
                    accent,

                missionAccentContainer =
                    accentContainer,

                missionPanelBackground =
                    scheme.surfaceContainer,

                missionItemBackground =
                    scheme.surfaceContainerHigh,

                missionProgressTrack =
                    scheme.surfaceContainerHighest,

                missionTextPrimary =
                    scheme.onSurface,

                missionTextSecondary =
                    scheme.onSurfaceVariant,

                skillAccent =
                    accent,

                skillAccentContainer =
                    accentContainer,

                skillPanelBackground =
                    scheme.surfaceContainer,

                skillProgressTrack =
                    scheme.surfaceContainerHighest,

                skillTextPrimary =
                    scheme.onSurface,

                skillTextSecondary =
                    scheme.onSurfaceVariant,

                confidenceAccent =
                    accent,

                confidencePanelBackground =
                    scheme.surfaceContainer,

                confidenceTextPrimary =
                    scheme.onSurface,

                confidenceTextSecondary =
                    scheme.onSurfaceVariant
            )
        } else {
            ProgressColors(
                isDark =
                    false,

                textPrimary =
                    LIGHT_TEXT_PRIMARY,

                textSecondary =
                    LIGHT_TEXT_SECONDARY,

                accent =
                    scheme.primary,

                accentContainer =
                    scheme.primaryContainer,

                progressTrack =
                    LIGHT_PROGRESS_TRACK,

                currentLevelBackground =
                    scheme.inverseSurface,

                currentLevelTextPrimary =
                    scheme.inverseOnSurface,

                currentLevelTextSecondary =
                    scheme.inverseOnSurface.copy(
                        alpha =
                            INVERSE_SECONDARY_ALPHA
                    ),

                /*
                 * Learning Focus:
                 * dark outer panel, layered dark inner surfaces,
                 * lime accents and explicit light foregrounds.
                 */
                learningAccent =
                    DeutschLime,

                learningAccentContainer =
                    DARK_LIME_CONTAINER,

                learningPanelBackground =
                    DARK_PANEL,

                learningItemBackground =
                    DARK_ITEM,

                learningSummaryBackground =
                    DARK_ITEM_HIGH,

                learningProgressTrack =
                    DARK_PROGRESS_TRACK,

                learningTextPrimary =
                    LIGHT_ON_DARK_PRIMARY,

                learningTextSecondary =
                    LIGHT_ON_DARK_SECONDARY,

                /*
                 * Communication Growth:
                 * full lime treatment matching the CEFR bento.
                 */
                growthAccent =
                    ON_LIME_PRIMARY,

                growthAccentContainer =
                    ON_LIME_SOFT_CONTAINER,

                growthPanelBackground =
                    DeutschLime,

                growthItemBackground =
                    LIME_INNER_SURFACE,

                growthProgressTrack =
                    ON_LIME_TRACK,

                growthTextPrimary =
                    ON_LIME_PRIMARY,

                growthTextSecondary =
                    ON_LIME_SECONDARY,

                /*
                 * Conversation Missions:
                 * dark panel as requested.
                 */
                missionAccent =
                    DeutschLime,

                missionAccentContainer =
                    DARK_LIME_CONTAINER,

                missionPanelBackground =
                    DARK_PANEL,

                missionItemBackground =
                    DARK_ITEM,

                missionProgressTrack =
                    DARK_PROGRESS_TRACK,

                missionTextPrimary =
                    LIGHT_ON_DARK_PRIMARY,

                missionTextSecondary =
                    LIGHT_ON_DARK_SECONDARY,

                /*
                 * Skills:
                 * dark panel.
                 */
                skillAccent =
                    DeutschLime,

                skillAccentContainer =
                    DARK_LIME_CONTAINER,

                skillPanelBackground =
                    DARK_PANEL,

                skillProgressTrack =
                    DARK_PROGRESS_TRACK,

                skillTextPrimary =
                    LIGHT_ON_DARK_PRIMARY,

                skillTextSecondary =
                    LIGHT_ON_DARK_SECONDARY,

                /*
                 * Evidence Quality:
                 * light panel with explicit dark foregrounds.
                 */
                confidenceAccent =
                    scheme.primary,

                confidencePanelBackground =
                    LIGHT_PANEL,

                confidenceTextPrimary =
                    LIGHT_TEXT_PRIMARY,

                confidenceTextSecondary =
                    LIGHT_TEXT_SECONDARY
            )
        }
    }
}

private const val DARK_LUMINANCE_THRESHOLD =
    0.5f

private const val DARK_ACCENT_CONTAINER_ALPHA =
    0.14f

private const val INVERSE_SECONDARY_ALPHA =
    0.72f

private val DARK_PANEL =
    Color(
        0xFF191B18
    )

private val DARK_ITEM =
    Color(
        0xFF232620
    )

private val DARK_ITEM_HIGH =
    Color(
        0xFF2B2F27
    )

private val DARK_PROGRESS_TRACK =
    Color(
        0xFF3A3F34
    )

private val DARK_LIME_CONTAINER =
    Color(
        0xFF30372B
    )

private val LIGHT_ON_DARK_PRIMARY =
    Color(
        0xFFF4F5F1
    )

private val LIGHT_ON_DARK_SECONDARY =
    Color(
        0xFFBEC2B8
    )

private val ON_LIME_PRIMARY =
    CefrOnLimePrimary

private val ON_LIME_SECONDARY =
    CefrOnLimeSecondary

private val ON_LIME_TRACK =
    CefrOnLimeTrack

private val LIME_INNER_SURFACE =
    Color(
        0xFFB5E679
    )

private val ON_LIME_SOFT_CONTAINER =
    Color(
        0x29111111
    )

private val LIGHT_PANEL =
    Color(
        0xFFF4F4F2
    )

private val LIGHT_PROGRESS_TRACK =
    Color(
        0xFFD8DAD4
    )

private val LIGHT_TEXT_PRIMARY =
    Color(
        0xFF151613
    )

private val LIGHT_TEXT_SECONDARY =
    Color(
        0xFF60645B
    )