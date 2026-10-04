package ru.fefu.pokeabilityapp.domain.repository

import kotlinx.coroutines.flow.Flow
import ru.fefu.pokeabilityapp.domain.model.AppSettings
import ru.fefu.pokeabilityapp.domain.model.ThemeMode

interface SettingsRepository {
    fun observeSettings(): Flow<AppSettings>
    suspend fun setActiveProfile(id: Long)
    suspend fun setThemeMode(mode: ThemeMode)
    suspend fun setCacheTtlHours(hours: Int)
    suspend fun setAutoRefreshEnabled(enabled: Boolean)
    suspend fun setRefreshOnWifiOnly(enabled: Boolean)
    suspend fun setPrefetchTeamsForOffline(enabled: Boolean)
    suspend fun setHistoryEnabled(enabled: Boolean)
    suspend fun setHistoryRetentionDays(days: Int)
}
