package com.projectpurr.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.projectpurr.engine.SleepTimerOption
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "purr_prefs")

class PurrPreferences(context: Context) {

    private val store = context.applicationContext.dataStore

    private companion object {
        val KEY_ONBOARDING_COMPLETE = booleanPreferencesKey("onboarding_complete")
        val KEY_LAST_SLEEP_TIMER    = stringPreferencesKey("last_sleep_timer")
        val KEY_CHEST_MODE          = booleanPreferencesKey("chest_mode")
        val KEY_SILENT_PURR         = booleanPreferencesKey("silent_purr")
        val KEY_FORCE_SPEAKER       = booleanPreferencesKey("force_speaker")
        val KEY_SESSION_COUNT       = intPreferencesKey("session_count")
    }

    val onboardingComplete: Flow<Boolean> = store.data.map { prefs ->
        prefs[KEY_ONBOARDING_COMPLETE] ?: false
    }

    val lastSleepTimer: Flow<SleepTimerOption> = store.data.map { prefs ->
        val name = prefs[KEY_LAST_SLEEP_TIMER]
        SleepTimerOption.entries.firstOrNull { it.name == name } ?: SleepTimerOption.OFF
    }

    val chestMode: Flow<Boolean> = store.data.map { prefs ->
        prefs[KEY_CHEST_MODE] ?: true
    }

    val silentPurr: Flow<Boolean> = store.data.map { prefs ->
        prefs[KEY_SILENT_PURR] ?: false
    }

    val forceSpeaker: Flow<Boolean> = store.data.map { prefs ->
        prefs[KEY_FORCE_SPEAKER] ?: false
    }

    val sessionCount: Flow<Int> = store.data.map { prefs ->
        prefs[KEY_SESSION_COUNT] ?: 0
    }

    suspend fun setOnboardingComplete() {
        store.edit { prefs -> prefs[KEY_ONBOARDING_COMPLETE] = true }
    }

    suspend fun setLastSleepTimer(option: SleepTimerOption) {
        store.edit { prefs -> prefs[KEY_LAST_SLEEP_TIMER] = option.name }
    }

    suspend fun setChestMode(enabled: Boolean) {
        store.edit { prefs -> prefs[KEY_CHEST_MODE] = enabled }
    }

    suspend fun setSilentPurr(enabled: Boolean) {
        store.edit { prefs -> prefs[KEY_SILENT_PURR] = enabled }
    }

    suspend fun setForceSpeaker(enabled: Boolean) {
        store.edit { prefs -> prefs[KEY_FORCE_SPEAKER] = enabled }
    }

    suspend fun incrementSessionCount() {
        store.edit { prefs ->
            prefs[KEY_SESSION_COUNT] = (prefs[KEY_SESSION_COUNT] ?: 0) + 1
        }
    }
}
