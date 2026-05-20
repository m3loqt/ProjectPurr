package com.projectpurr

import android.graphics.Color
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay
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
import com.projectpurr.ui.HistoryScreen
import com.projectpurr.ui.HomeScreen
import com.projectpurr.ui.SplashScreen
import com.projectpurr.ui.OnboardingScreen
import com.projectpurr.ui.ProfileScreen
import com.projectpurr.ui.SessionScreen
import com.projectpurr.ui.SettingsScreen
import com.projectpurr.ui.theme.ColorBackground
import com.projectpurr.ui.theme.ProjectPurrTheme

private const val ROUTE_ONBOARDING = "onboarding"
private const val ROUTE_HOME       = "home"
private const val ROUTE_SESSION    = "session"
private const val ROUTE_PROFILE    = "profile"
private const val ROUTE_HISTORY    = "history"
private const val ROUTE_SETTINGS   = "settings"

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val view = LocalView.current
            val vm: PurrViewModel = viewModel()
            val state             by vm.uiState.collectAsState()
            val onboarding        by vm.onboardingComplete.collectAsState()
            val sessionCount      by vm.sessionCount.collectAsState()
            val lastSessionEpoch  by vm.lastSessionEpoch.collectAsState()
            val recentSessions    by vm.recentSessions.collectAsState()
            val pendingBondDelta  by vm.pendingBondDelta.collectAsState()

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

                var splashDone by remember { mutableStateOf(false) }
                LaunchedEffect(Unit) {
                    delay(2000)
                    splashDone = true
                }

                Crossfade(
                    targetState   = !splashDone || onboarding == null,
                    animationSpec = tween(600),
                    label         = "splashTransition",
                ) { showSplash ->
                    if (showSplash) {
                        SplashScreen()
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
                                sessionCount        = sessionCount,
                                recentSessions      = recentSessions,
                                pendingBondDelta    = pendingBondDelta,
                                onBondDeltaConsumed = vm::consumeBondDelta,
                                onSelectHouseCat    = { navController.navigate(ROUTE_SESSION) },
                                onPreviewHaptic     = vm::previewHaptic,
                                onNavigateToProfile = {
                                    navController.navigate(ROUTE_PROFILE) {
                                        popUpTo(ROUTE_HOME) { inclusive = false }
                                    }
                                },
                                onNavigateToHistory = {
                                    navController.navigate(ROUTE_HISTORY) {
                                        popUpTo(ROUTE_HOME) { inclusive = false }
                                    }
                                },
                            )
                        }

                        composable(ROUTE_HISTORY) {
                            HistoryScreen(
                                sessions            = recentSessions,
                                onNavigateHome      = { navController.popBackStack(ROUTE_HOME, inclusive = false) },
                                onNavigateToSession = {
                                    navController.navigate(ROUTE_SESSION) {
                                        popUpTo(ROUTE_HOME) { inclusive = false }
                                    }
                                },
                                onNavigateToProfile = {
                                    navController.navigate(ROUTE_PROFILE) {
                                        popUpTo(ROUTE_HOME) { inclusive = false }
                                    }
                                },
                            )
                        }

                        composable(ROUTE_PROFILE) {
                            ProfileScreen(
                                lastSessionEpoch      = lastSessionEpoch,
                                lastSessionDurationMs = recentSessions.firstOrNull()?.durationMillis ?: 0L,
                                onNavigateHome        = { navController.popBackStack(ROUTE_HOME, inclusive = false) },
                                onNavigateToSession  = {
                                    navController.navigate(ROUTE_SESSION) {
                                        popUpTo(ROUTE_HOME) { inclusive = false }
                                    }
                                },
                                onNavigateToHistory  = {
                                    navController.navigate(ROUTE_HISTORY) {
                                        popUpTo(ROUTE_HOME) { inclusive = false }
                                    }
                                },
                                onNavigateToSettings = { navController.navigate(ROUTE_SETTINGS) },
                            )
                        }

                        composable(ROUTE_SESSION) {
                            SessionScreen(
                                state                = state,
                                onBack               = {
                                    if (state.isSessionActive) vm.togglePlay()
                                    vm.setChestMode(false)
                                    navController.popBackStack()
                                },
                                onNavigateToProfile  = {
                                    vm.setChestMode(false)
                                    navController.navigate(ROUTE_PROFILE)
                                },
                                onNavigateToHistory  = {
                                    vm.setChestMode(false)
                                    navController.navigate(ROUTE_HISTORY)
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
                                appVersion          = "1.0.0",
                                chestMode           = state.chestMode,
                                silentPurr          = state.silentPurr,
                                sleepTimer          = state.sleepTimer,
                                onChestModeChange   = vm::setChestMode,
                                onSilentPurrChange  = vm::setSilentPurr,
                                onSleepTimerChange  = vm::setSleepTimer,
                                onClearHistory      = vm::clearHistory,
                                onBack              = { navController.popBackStack() },
                            )
                        }
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
