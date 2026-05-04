package com.projectpurr.engine

enum class SessionPhase {
    STOPPED,
    PLAYING,
    FADING,
}

enum class SleepTimerOption(val label: String) {
    OFF("Off"),
    M10("10 min"),
    M20("20 min"),
    M30("30 min"),
    ;

    fun durationMillis(): Long? = when (this) {
        OFF -> null
        M10 -> 10L * 60L * 1000L
        M20 -> 20L * 60L * 1000L
        M30 -> 30L * 60L * 1000L
    }
}

data class PurrUiState(
    val phase: SessionPhase = SessionPhase.STOPPED,
    val silentPurr: Boolean = false,
    val chestMode: Boolean = true,
    val sleepTimer: SleepTimerOption = SleepTimerOption.OFF,
) {
    val isSessionActive: Boolean
        get() = phase != SessionPhase.STOPPED
}
