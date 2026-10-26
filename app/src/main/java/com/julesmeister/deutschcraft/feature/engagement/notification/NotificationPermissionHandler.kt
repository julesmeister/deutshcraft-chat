package com.julesmeister.deutschcraft.feature.engagement.notification

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect

@Composable
fun NotificationPermissionHandler(
    shouldRequestPermission: Boolean,
    onPermissionResult: (Boolean) -> Unit
) {
    val permissionLauncher =
        rememberLauncherForActivityResult(
            contract =
                ActivityResultContracts
                    .RequestPermission(),
            onResult =
                onPermissionResult
        )

    LaunchedEffect(
        shouldRequestPermission
    ) {
        if (!shouldRequestPermission) {
            return@LaunchedEffect
        }

        if (
            Build.VERSION.SDK_INT >=
            Build.VERSION_CODES.TIRAMISU
        ) {
            permissionLauncher.launch(
                Manifest.permission
                    .POST_NOTIFICATIONS
            )
        } else {
            onPermissionResult(true)
        }
    }
}