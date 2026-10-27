package com.julesmeister.deutschcraft

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.core.view.WindowCompat
import com.julesmeister.deutschcraft.app.navigation.DeutschCraftNavHost
import com.julesmeister.deutschcraft.feature.settings.domain.AppTheme
import com.julesmeister.deutschcraft.ui.theme.DeutschCraftTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel:
            MainViewModel by viewModels()

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(
            savedInstanceState
        )

        WindowCompat.setDecorFitsSystemWindows(
            window,
            false
        )

        setContent {
            val settings by
            viewModel
                .settings
                .collectAsStateWithLifecycle()

            DeutschCraftTheme(
                darkTheme =
                    settings.appTheme ==
                            AppTheme.DARK
            ) {
                DeutschCraftNavHost()
            }
        }
    }
}