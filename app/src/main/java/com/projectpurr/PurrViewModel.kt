package com.projectpurr

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.projectpurr.data.PurrPreferences
import com.projectpurr.data.RestHistoryRepository
import com.projectpurr.data.db.RestSessionEntity
import com.projectpurr.engine.PurrSessionEngine
import com.projectpurr.engine.PurrUiState
import com.projectpurr.engine.SessionPhase
import com.projectpurr.engine.SleepTimerOption
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PurrViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs   = PurrPreferences(application)
    private val history = RestHistoryRepository(application)

    private var sessionCountAtStart = 0

    private val _pendingBondDelta = MutableStateFlow(0)
    val pendingBondDelta: StateFlow<Int> = _pendingBondDelta.asStateFlow()

    private val engine = PurrSessionEngine(
        application  = application,
        scope        = viewModelScope,
        onSessionCompleted = { startMs, endMs, durMs, silent, chest, timerMins, natural ->
            viewModelScope.launch {
                history.save(
                    RestSessionEntity(
                        startedAtMillis    = startMs,
                        endedAtMillis      = endMs,
                        durationMillis     = durMs,
                        companionId        = "house_cat",
                        companionName      = "House Cat",
                        usedSilentMode     = silent,
                        usedChestMode      = chest,
                        timerOptionMinutes = timerMins,
                        completedNaturally = natural,
                    )
                )
                // Increment count and record epoch only for real saved sessions (engine enforces >=60s)
                prefs.incrementSessionCount()
                prefs.recordLastSession(endMs)
                val newCount = sessionCountAtStart + 1
                val delta = (newCount * 14).coerceAtMost(100) - (sessionCountAtStart * 14).coerceAtMost(100)
                if (delta > 0) _pendingBondDelta.value = delta
            }
        },
    )

    val uiState: StateFlow<PurrUiState> = engine.state

    /** Null while DataStore loads (~50 ms on first launch), then true/false. */
    val onboardingComplete: StateFlow<Boolean?> = prefs.onboardingComplete
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val sessionCount: StateFlow<Int> = prefs.sessionCount
        .stateIn(viewModelScope, SharingStarted.Eagerly, 0)

    val lastSessionEpoch: StateFlow<Long> = prefs.lastSessionEpoch
        .stateIn(viewModelScope, SharingStarted.Eagerly, 0L)

    val recentSessions: StateFlow<List<RestSessionEntity>> = history.recentSessions
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

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

    fun consumeBondDelta() { _pendingBondDelta.value = 0 }

    fun togglePlay() {
        if (uiState.value.phase == SessionPhase.STOPPED) {
            sessionCountAtStart = sessionCount.value
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

    fun clearHistory() {
        viewModelScope.launch {
            history.deleteAll()
            prefs.resetSessionCount()
            prefs.resetLastSession()
        }
    }

    fun completeOnboarding() {
        viewModelScope.launch { prefs.setOnboardingComplete() }
    }

    override fun onCleared() {
        engine.dispose()
        super.onCleared()
    }
}
