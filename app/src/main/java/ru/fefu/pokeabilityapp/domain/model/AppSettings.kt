package ru.fefu.pokeabilityapp.domain.model

enum class ThemeMode { SYSTEM, LIGHT, DARK }

data class AppSettings(
    val activeProfileId: Long = NO_PROFILE,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val cacheTtlHours: Int = 24,
    val autoRefreshEnabled: Boolean = true,
    val refreshOnWifiOnly: Boolean = false,
    val prefetchTeamsForOffline: Boolean = true,
    val historyEnabled: Boolean = true,
    val historyRetentionDays: Int = 30,
    val threatThreshold: Int = 3,
) {
    companion object {
        const val NO_PROFILE = 0L
    }
}
