package com.julesmeister.deutschcraft

import android.database.ContentObserver
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.createLifecycleAwareWindowRecomposer
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.core.view.WindowCompat
import com.julesmeister.deutschcraft.app.navigation.DeutschCraftNavHost
import com.julesmeister.deutschcraft.feature.settings.domain.AppTheme
import com.julesmeister.deutschcraft.feature.settings.presentation.AppMotionDurationScale
import com.julesmeister.deutschcraft.feature.settings.presentation.AppSettingsProvider
import com.julesmeister.deutschcraft.ui.theme.DeutschCraftTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val motionDurationScale = AppMotionDurationScale()
    private val animatorScaleObserver = object : ContentObserver(Handler(Looper.getMainLooper())) {
        override fun onChange(selfChange: Boolean) {
            updateSystemAnimationScale()
        }
    }

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

        updateSystemAnimationScale()
        contentResolver.registerContentObserver(
            Settings.Global.getUriFor(Settings.Global.ANIMATOR_DURATION_SCALE),
            false,
            animatorScaleObserver
        )
        val recomposer = window.decorView.createLifecycleAwareWindowRecomposer(
            coroutineContext = motionDurationScale,
            lifecycle = lifecycle
        )

        setContent(parent = recomposer) {
            val settings by
            viewModel
                .settings
                .collectAsStateWithLifecycle()

            SideEffect {
                motionDurationScale.reduceAnimations = settings.reduceAnimations
            }

            AppSettingsProvider(settings) {
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

    private fun updateSystemAnimationScale() {
        motionDurationScale.systemScale = Settings.Global.getFloat(
            contentResolver,
            Settings.Global.ANIMATOR_DURATION_SCALE,
            1f
        )
    }

    override fun onDestroy() {
        contentResolver.unregisterContentObserver(animatorScaleObserver)
        super.onDestroy()
    }
}