package com.julesmeister.deutschcraft.feature.settings.domain

data class UserSettings(
    val appTheme: AppTheme =
        AppTheme.DARK,

    val progressLanguage: ProgressLanguage =
        ProgressLanguage.GERMAN,

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

    val isDarkTheme: Boolean
        get() =
            appTheme == AppTheme.DARK

    val showProgressInEnglish: Boolean
        get() =
            progressLanguage ==
                    ProgressLanguage.ENGLISH
}