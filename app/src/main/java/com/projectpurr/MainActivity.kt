package com.projectpurr

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
import com.projectpurr.ui.theme.ColorBackground
import com.projectpurr.ui.theme.ProjectPurrTheme

private const val ROUTE_ONBOARDING = "onboarding"
private const val ROUTE_HOME       = "home"
private const val ROUTE_SESSION    = "session"

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val view = LocalView.current
            val vm: PurrViewModel = viewModel()
            val state by vm.uiState.collectAsState()
            val onboarding by vm.onboardingComplete.collectAsState()

            SideEffect {
                val sessionActive =
                    state.phase == SessionPhase.PLAYING || state.phase == SessionPhase.FADING
                if (sessionActive) {
                    window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                } else {
                    window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                }

                val lowVisual = state.chestMode && sessionActive
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
                val navController = rememberNavController()

                // Blank slate while DataStore resolves onboarding state (~50 ms first launch).
                if (onboarding == null) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = ColorBackground,
                    ) {}
                } else {
                    val startDestination =
                        if (onboarding == false) ROUTE_ONBOARDING else ROUTE_HOME

                    NavHost(
                        navController = navController,
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
                                onSelectHouseCat = {
                                    navController.navigate(ROUTE_SESSION)
                                },
                            )
                        }

                        composable(ROUTE_SESSION) {
                            SessionScreen(
                                state = state,
                                onBack = {
                                    if (state.isSessionActive) vm.togglePlay()
                                    navController.popBackStack()
                                },
                                onTogglePlay = vm::togglePlay,
                                onSilentChange = vm::setSilentPurr,
                                onChestModeChange = vm::setChestMode,
                                onSleepTimerChange = vm::setSleepTimer,
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
        super.onDestroy()
    }
}
