package com.julesmeister.deutschcraft.feature.engagement.presentation

data class EngagementSettingsState(
    val remindersEnabled: Boolean = false,
    val shouldRequestPermission: Boolean = false
)