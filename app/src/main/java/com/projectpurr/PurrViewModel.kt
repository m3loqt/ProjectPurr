package com.projectpurr

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.projectpurr.data.PurrPreferences
import com.projectpurr.engine.PurrSessionEngine
import com.projectpurr.engine.PurrUiState
import com.projectpurr.engine.SleepTimerOption
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PurrViewModel(application: Application) : AndroidViewModel(application) {

    private val engine = PurrSessionEngine(application, viewModelScope)
    private val prefs  = PurrPreferences(application)

    val uiState: StateFlow<PurrUiState> = engine.state

    /** null while DataStore loads (~50 ms on first launch), then true/false. */
    val onboardingComplete: StateFlow<Boolean?> = prefs.onboardingComplete
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    fun togglePlay()                          = engine.togglePlay()
    fun setSilentPurr(enabled: Boolean)       = engine.setSilentPurr(enabled)
    fun setChestMode(enabled: Boolean)        = engine.setChestMode(enabled)
    fun setSleepTimer(option: SleepTimerOption) = engine.setSleepTimer(option)

    fun completeOnboarding() {
        viewModelScope.launch { prefs.setOnboardingComplete() }
    }

    override fun onCleared() {
        engine.dispose()
        super.onCleared()
    }
}
