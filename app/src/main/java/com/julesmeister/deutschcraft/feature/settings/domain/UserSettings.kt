package com.julesmeister.deutschcraft.feature.settings.domain

data class UserSettings(
    val fontScaleOption: FontScaleOption =
        FontScaleOption.NORMAL,

    val reduceAnimations: Boolean =
        false,

    val showMissionHints: Boolean =
        true,

    val showExamplePhrases: Boolean =
        true,

    val showTargetVocabulary: Boolean =
        true
) {
    val fontScale: Float
        get() =
            fontScaleOption.scale
}
