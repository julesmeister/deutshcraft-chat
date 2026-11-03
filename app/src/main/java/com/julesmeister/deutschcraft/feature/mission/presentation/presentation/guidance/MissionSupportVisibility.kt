package com.julesmeister.deutschcraft.feature.mission.presentation.guidance

import com.julesmeister.deutschcraft.feature.settings.domain.UserSettings

data class MissionSupportVisibility(
    val hints: Boolean,
    val vocabulary: Boolean,
    val examples: Boolean
) {
    val hasSupport: Boolean
        get() = hints || vocabulary || examples
}

fun MissionGuidanceUiState.supportVisibility(settings: UserSettings): MissionSupportVisibility =
    MissionSupportVisibility(
        hints = settings.showMissionHints && (hasMoreHints || revealedHints.isNotEmpty()),
        vocabulary = settings.showTargetVocabulary && targetVocabulary.isNotEmpty(),
        examples = settings.showExamplePhrases && examplePhrases.isNotEmpty()
    )
