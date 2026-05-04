package com.projectpurr

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.projectpurr.engine.PurrSessionEngine
import com.projectpurr.engine.PurrUiState
import com.projectpurr.engine.SleepTimerOption
import kotlinx.coroutines.flow.StateFlow

class PurrViewModel(application: Application) : AndroidViewModel(application) {

    private val engine = PurrSessionEngine(application, viewModelScope)

    val uiState: StateFlow<PurrUiState> = engine.state

    fun togglePlay() = engine.togglePlay()

    fun setSilentPurr(enabled: Boolean) = engine.setSilentPurr(enabled)

    fun setChestMode(enabled: Boolean) = engine.setChestMode(enabled)

    fun setSleepTimer(option: SleepTimerOption) = engine.setSleepTimer(option)

    override fun onCleared() {
        engine.dispose()
        super.onCleared()
    }
}
