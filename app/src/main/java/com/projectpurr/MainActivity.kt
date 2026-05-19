package com.projectpurr

import android.graphics.Color
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.projectpurr.engine.SessionPhase
import com.projectpurr.ui.HomeScreen
import com.projectpurr.ui.OnboardingScreen
import com.projectpurr.ui.SessionScreen
import com.projectpurr.ui.SettingsScreen
import com.projectpurr.ui.theme.ColorBackground
import com.projectpurr.ui.theme.ProjectPurrTheme

private const val ROUTE_ONBOARDING = "onboarding"
private const val ROUTE_HOME       = "home"
private const val ROUTE_SESSION    = "session"
private const val ROUTE_SETTINGS   = "settings"

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val view = LocalView.current
            val vm: PurrViewModel = viewModel()
            val state        by vm.uiState.collectAsState()
            val onboarding   by vm.onboardingComplete.collectAsState()
            val sessionCount by vm.sessionCount.collectAsState()

            SideEffect {
                val sessionActive =
                    state.phase == SessionPhase.PLAYING || state.phase == SessionPhase.FADING
                if (sessionActive) {
                    window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                } else {
                    window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                }

                val chestMode = state.chestMode
                // Bars hide as soon as chest mode is on — brightness only dims when playing.
                val immersiveChest = chestMode

                window.attributes = window.attributes.apply {
                    screenBrightness = if (chestMode && sessionActive) {
                        0.08f
                    } else {
                        WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE
                    }
                }

                // Dim native status + navigation bars when chest mode is on
                if (chestMode) {
                    window.statusBarColor = CHEST_BAR_COLOR
                    window.navigationBarColor = CHEST_BAR_COLOR
                } else {
                    window.statusBarColor = Color.TRANSPARENT
                    window.navigationBarColor = Color.TRANSPARENT
                }

                val controller = WindowCompat.getInsetsController(window, view)
                // Light icons on dark bar
                controller.isAppearanceLightStatusBars = false
                if (immersiveChest) {
                    controller.hide(WindowInsetsCompat.Type.systemBars())
                    controller.systemBarsBehavior =
                        WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                } else {
                    controller.show(WindowInsetsCompat.Type.systemBars())
                }
            }

            ProjectPurrTheme {
                val navController = rememberNavController()

                // Blank while DataStore resolves onboarding (~50 ms first launch).
                if (onboarding == null) {
                    Surface(modifier = Modifier.fillMaxSize(), color = ColorBackground) {}
                } else {
                    val startDestination =
                        if (onboarding == false) ROUTE_ONBOARDING else ROUTE_HOME

                    NavHost(
                        navController    = navController,
                        startDestination = startDestination,
                    ) {
                        composable(ROUTE_ONBOARDING) {
                            OnboardingScreen(
                                onGetStarted = {
                                    vm.completeOnboarding()
                                    navController.navigate(ROUTE_HOME) {
                                        popUpTo(ROUTE_ONBOARDING) { inclusive = true }
                                    }
                                },
                            )
                        }

                        composable(ROUTE_HOME) {
                            HomeScreen(
                                sessionCount         = sessionCount,
                                onSelectHouseCat     = { navController.navigate(ROUTE_SESSION) },
                                onPreviewHaptic      = vm::previewHaptic,
                                onNavigateToSettings = { navController.navigate(ROUTE_SETTINGS) },
                            )
                        }

                        composable(ROUTE_SESSION) {
                            SessionScreen(
                                state                = state,
                                onBack               = {
                                    if (state.isSessionActive) vm.togglePlay()
                                    navController.popBackStack()
                                },
                                onTogglePlay         = vm::togglePlay,
                                onSilentChange       = vm::setSilentPurr,
                                onChestModeChange    = vm::setChestMode,
                                onForceSpeakerChange = vm::setForceSpeaker,
                                onSleepTimerChange   = vm::setSleepTimer,
                            )
                        }

                        composable(ROUTE_SETTINGS) {
                            SettingsScreen(
                                appVersion = "1.0.0",
                                onBack     = { navController.popBackStack() },
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        window.attributes = window.attributes.apply {
            screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE
        }
        window.statusBarColor = Color.TRANSPARENT
        window.navigationBarColor = Color.TRANSPARENT
        super.onDestroy()
    }

    companion object {
        /** Near-black bar tint when chest mode is enabled. */
        private const val CHEST_BAR_COLOR = 0xFF0A0908.toInt()
    }
}
