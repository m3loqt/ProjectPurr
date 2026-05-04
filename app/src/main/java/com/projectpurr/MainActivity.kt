package com.projectpurr

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.projectpurr.engine.SessionPhase
import com.projectpurr.ui.PurrScreen
import com.projectpurr.ui.theme.ProjectPurrTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val view = LocalView.current
            val vm: PurrViewModel = viewModel()
            val state by vm.uiState.collectAsState()

            SideEffect {
                val lowVisual =
                    state.chestMode &&
                        (state.phase == SessionPhase.PLAYING || state.phase == SessionPhase.FADING)
                window.attributes = window.attributes.apply {
                    screenBrightness = if (lowVisual) {
                        0.08f
                    } else {
                        WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE
                    }
                }

                val controller = WindowCompat.getInsetsController(window, view)
                controller.isAppearanceLightStatusBars = false
                if (lowVisual) {
                    controller.hide(WindowInsetsCompat.Type.systemBars())
                    controller.systemBarsBehavior =
                        WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                } else {
                    controller.show(WindowInsetsCompat.Type.systemBars())
                }
            }

            ProjectPurrTheme {
                PurrScreen(
                    state = state,
                    onTogglePlay = vm::togglePlay,
                    onSilentChange = vm::setSilentPurr,
                    onChestModeChange = vm::setChestMode,
                    onSleepTimerChange = vm::setSleepTimer,
                )
            }
        }
    }

    override fun onDestroy() {
        window.attributes = window.attributes.apply {
            screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE
        }
        super.onDestroy()
    }
}
