// app/src/main/java/com/julesmeister/deutschcraft/feature/progress/presentation/ProgressColors.kt

package com.julesmeister.deutschcraft.feature.progress.presentation

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import com.julesmeister.deutschcraft.ui.theme.DeutschInfo
import com.julesmeister.deutschcraft.ui.theme.DeutschLime
import com.julesmeister.deutschcraft.ui.theme.DeutschLimeDark
import com.julesmeister.deutschcraft.ui.theme.DeutschSurfaceHigh
import com.julesmeister.deutschcraft.ui.theme.DeutschSurfaceHighest
import com.julesmeister.deutschcraft.ui.theme.DeutschTextPrimary
import com.julesmeister.deutschcraft.ui.theme.DeutschTextSecondary

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

    /*
     * Learning focus.
     *
     * Blue deliberately separates adaptive observation from
     * the lime Besser / progression identity.
     */
    val learningAccent: Color,

    val learningAccentContainer: Color,

    val learningPanelBackground: Color,

    val learningItemBackground: Color,

    val learningSummaryBackground: Color,

    val learningProgressTrack: Color,

    /*
     * Communication growth.
     *
     * Besser keeps the DeutschCraft lime identity, but uses a
     * warmer / deeper panel treatment instead of nesting the
     * same generic Material surfaces.
     */
    val growthAccent: Color,

    val growthAccentContainer: Color,

    val growthPanelBackground: Color,

    val growthItemBackground: Color,

    val growthProgressTrack: Color,

    /*
     * Mission history stays deliberately neutral.
     *
     * Lime is reserved for successful completion instead of
     * tinting every surface inside the card.
     */
    val missionAccent: Color,

    val missionAccentContainer: Color,

    val missionPanelBackground: Color,

    val missionItemBackground: Color,

    val missionProgressTrack: Color
)

@Composable
internal fun rememberProgressColors():
        ProgressColors {

    val colorScheme =
        MaterialTheme.colorScheme

    return remember(
        colorScheme
    ) {
        val isDark =
            colorScheme
                .background
                .luminance() <
                    ProgressDarkLuminanceThreshold

        if (isDark) {
            ProgressColors(
                isDark =
                    true,

                textPrimary =
                    DeutschTextPrimary,

                textSecondary =
                    DeutschTextSecondary,

                accent =
                    DeutschLime,

                accentContainer =
                    DeutschLime.copy(
                        alpha =
                            ProgressDarkAccentContainerAlpha
                    ),

                progressTrack =
                    colorScheme.surfaceContainerHighest,

                currentLevelBackground =
                    CefrCurrentLevelBackground,

                currentLevelTextPrimary =
                    CefrCurrentLevelTextPrimary,

                currentLevelTextSecondary =
                    CefrCurrentLevelTextSecondary,

                learningAccent =
                    DeutschInfo,

                learningAccentContainer =
                    DeutschInfo.copy(
                        alpha =
                            DarkLearningAccentContainerAlpha
                    ),

                learningPanelBackground =
                    DarkLearningPanelBackground,

                learningItemBackground =
                    DarkLearningItemBackground,

                learningSummaryBackground =
                    DarkLearningSummaryBackground,

                learningProgressTrack =
                    DarkLearningProgressTrack,

                growthAccent =
                    DeutschLime,

                growthAccentContainer =
                    DeutschLime.copy(
                        alpha =
                            DarkGrowthAccentContainerAlpha
                    ),

                growthPanelBackground =
                    DarkGrowthPanelBackground,

                growthItemBackground =
                    DarkGrowthItemBackground,

                growthProgressTrack =
                    DarkGrowthProgressTrack,

                missionAccent =
                    DeutschLime,

                missionAccentContainer =
                    colorScheme.surfaceContainerHighest,

                missionPanelBackground =
                    DarkMissionPanelBackground,

                missionItemBackground =
                    DarkMissionItemBackground,

                missionProgressTrack =
                    DarkMissionProgressTrack
            )
        } else {
            ProgressColors(
                isDark =
                    false,

                textPrimary =
                    colorScheme.onSurface,

                textSecondary =
                    colorScheme.onSurfaceVariant,

                accent =
                    ProgressLightAccent,

                accentContainer =
                    DeutschLime.copy(
                        alpha =
                            ProgressLightAccentContainerAlpha
                    ),

                progressTrack =
                    colorScheme.surfaceContainerHighest,

                currentLevelBackground =
                    colorScheme.inverseSurface,

                currentLevelTextPrimary =
                    colorScheme.inverseOnSurface,

                currentLevelTextSecondary =
                    colorScheme.inverseOnSurface.copy(
                        alpha =
                            ProgressLightInverseSecondaryAlpha
                    ),

                learningAccent =
                    LightLearningAccent,

                learningAccentContainer =
                    LightLearningAccentContainer,

                learningPanelBackground =
                    LightLearningPanelBackground,

                learningItemBackground =
                    LightLearningItemBackground,

                learningSummaryBackground =
                    LightLearningSummaryBackground,

                learningProgressTrack =
                    LightLearningProgressTrack,

                growthAccent =
                    DeutschLimeDark,

                growthAccentContainer =
                    LightGrowthAccentContainer,

                growthPanelBackground =
                    LightGrowthPanelBackground,

                growthItemBackground =
                    LightGrowthItemBackground,

                growthProgressTrack =
                    LightGrowthProgressTrack,

                missionAccent =
                    DeutschLimeDark,

                missionAccentContainer =
                    LightMissionAccentContainer,

                missionPanelBackground =
                    LightMissionPanelBackground,

                missionItemBackground =
                    LightMissionItemBackground,

                missionProgressTrack =
                    LightMissionProgressTrack
            )
        }
    }
}

private const val ProgressDarkLuminanceThreshold =
    0.5f

private const val ProgressDarkAccentContainerAlpha =
    0.14f

private val ProgressLightAccent =
    Color(
        0xFF5A7A00
    )

private const val ProgressLightAccentContainerAlpha =
    0.35f

private const val ProgressLightInverseSecondaryAlpha =
    0.72f

private const val DarkLearningAccentContainerAlpha =
    0.15f

private val DarkLearningPanelBackground =
    Color(
        0xFF141C23
    )

private val DarkLearningItemBackground =
    Color(
        0xFF1B2731
    )

private val DarkLearningSummaryBackground =
    Color(
        0xFF18222B
    )

private val DarkLearningProgressTrack =
    Color(
        0xFF2A3945
    )

private val LightLearningAccent =
    Color(
        0xFF25679D
    )

private val LightLearningAccentContainer =
    Color(
        0xFFDCEEFF
    )

private val LightLearningPanelBackground =
    Color(
        0xFFF1F7FC
    )

private val LightLearningItemBackground =
    Color(
        0xFFFFFFFF
    )

private val LightLearningSummaryBackground =
    Color(
        0xFFE5F2FC
    )

private val LightLearningProgressTrack =
    Color(
        0xFFD3E3EF
    )

private const val DarkGrowthAccentContainerAlpha =
    0.11f

private val DarkGrowthPanelBackground =
    Color(
        0xFF171B15
    )

private val DarkGrowthItemBackground =
    Color(
        0xFF20271D
    )

private val DarkGrowthProgressTrack =
    Color(
        0xFF35412F
    )

private val LightGrowthAccentContainer =
    Color(
        0xFFE5F7D9
    )

private val LightGrowthPanelBackground =
    Color(
        0xFFF4F8F0
    )

private val LightGrowthItemBackground =
    Color(
        0xFFFFFFFF
    )

private val LightGrowthProgressTrack =
    Color(
        0xFFD9E5D2
    )

private val DarkMissionPanelBackground =
    DeutschSurfaceHigh

private val DarkMissionItemBackground =
    Color(
        0xFF181A1C
    )

private val DarkMissionProgressTrack =
    DeutschSurfaceHighest

private val LightMissionAccentContainer =
    Color(
        0xFFEEF1EB
    )

private val LightMissionPanelBackground =
    Color(
        0xFFFFFFFF
    )

private val LightMissionItemBackground =
    Color(
        0xFFF1F2EF
    )

private val LightMissionProgressTrack =
    Color(
        0xFFDADDD6
    )