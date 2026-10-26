package com.julesmeister.deutschcraft

import DeutschCraftNavHost
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.view.WindowCompat
import com.julesmeister.deutschcraft.app.DeutschCraftSettingsProvider
import com.julesmeister.deutschcraft.ui.theme.DeutschCraftTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(
            window,
            false
        )

        setContent {
            DeutschCraftTheme {
                DeutschCraftSettingsProvider {
                    DeutschCraftNavHost()
                }
            }
        }
    }
}
