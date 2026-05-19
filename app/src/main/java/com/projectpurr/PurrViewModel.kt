package com.projectpurr

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.projectpurr.data.PurrPreferences
import com.projectpurr.engine.PurrSessionEngine
import com.projectpurr.engine.PurrUiState
import com.projectpurr.engine.SessionPhase
import com.projectpurr.engine.SleepTimerOption
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PurrViewModel(application: Application) : AndroidViewModel(application) {

    private val engine = PurrSessionEngine(application, viewModelScope)
    private val prefs  = PurrPreferences(application)

    val uiState: StateFlow<PurrUiState> = engine.state

    /** Null while DataStore loads (~50 ms on first launch), then true/false. */
    val onboardingComplete: StateFlow<Boolean?> = prefs.onboardingComplete
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val sessionCount: StateFlow<Int> = prefs.sessionCount
        .stateIn(viewModelScope, SharingStarted.Eagerly, 0)

    init {
        viewModelScope.launch {
            val savedTimer   = prefs.lastSleepTimer.first()
            val savedChest   = prefs.chestMode.first()
            val savedSilent  = prefs.silentPurr.first()
            val savedSpeaker = prefs.forceSpeaker.first()

            if (savedTimer != SleepTimerOption.OFF) engine.setSleepTimer(savedTimer)
            engine.setChestMode(savedChest)
            engine.setSilentPurr(savedSilent)
            engine.setForceSpeaker(savedSpeaker)
        }
    }

    fun togglePlay() {
        if (uiState.value.phase == SessionPhase.STOPPED) {
            viewModelScope.launch { prefs.incrementSessionCount() }
        }
        engine.togglePlay()
    }

    fun setSilentPurr(enabled: Boolean) {
        engine.setSilentPurr(enabled)
        viewModelScope.launch { prefs.setSilentPurr(enabled) }
    }

    fun setChestMode(enabled: Boolean) {
        engine.setChestMode(enabled)
        viewModelScope.launch { prefs.setChestMode(enabled) }
    }

    fun setForceSpeaker(enabled: Boolean) {
        engine.setForceSpeaker(enabled)
        viewModelScope.launch { prefs.setForceSpeaker(enabled) }
    }

    fun setSleepTimer(option: SleepTimerOption) {
        engine.setSleepTimer(option)
        viewModelScope.launch { prefs.setLastSleepTimer(option) }
    }

    fun previewHaptic() = engine.previewHaptic()

    fun completeOnboarding() {
        viewModelScope.launch { prefs.setOnboardingComplete() }
    }

    override fun onCleared() {
        engine.dispose()
        super.onCleared()
    }
}
