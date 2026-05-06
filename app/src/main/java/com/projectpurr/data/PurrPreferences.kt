package com.projectpurr.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.projectpurr.engine.SleepTimerOption
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "purr_prefs")

class PurrPreferences(context: Context) {

    private val store = context.applicationContext.dataStore

    // ── Keys ──────────────────────────────────────────────────────────────────
    private companion object {
        val KEY_ONBOARDING_COMPLETE = booleanPreferencesKey("onboarding_complete")
        val KEY_LAST_SLEEP_TIMER    = stringPreferencesKey("last_sleep_timer")
    }

    // ── Reads ─────────────────────────────────────────────────────────────────

    /** Emits null until DataStore is first read, then true/false. */
    val onboardingComplete: Flow<Boolean> = store.data.map { prefs ->
        prefs[KEY_ONBOARDING_COMPLETE] ?: false
    }

    val lastSleepTimer: Flow<SleepTimerOption> = store.data.map { prefs ->
        val name = prefs[KEY_LAST_SLEEP_TIMER]
        SleepTimerOption.entries.firstOrNull { it.name == name } ?: SleepTimerOption.OFF
    }

    // ── Writes ────────────────────────────────────────────────────────────────

    suspend fun setOnboardingComplete() {
        store.edit { prefs -> prefs[KEY_ONBOARDING_COMPLETE] = true }
    }

    suspend fun setLastSleepTimer(option: SleepTimerOption) {
        store.edit { prefs -> prefs[KEY_LAST_SLEEP_TIMER] = option.name }
    }
}
