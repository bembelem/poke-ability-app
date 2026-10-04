package ru.fefu.pokeabilityapp.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ru.fefu.pokeabilityapp.domain.model.AppSettings
import ru.fefu.pokeabilityapp.domain.model.ThemeMode
import ru.fefu.pokeabilityapp.domain.repository.SettingsRepository
import javax.inject.Inject

class SettingsRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : SettingsRepository {

    private object Keys {
        val activeProfileId = longPreferencesKey("active_profile_id")
        val themeMode = stringPreferencesKey("theme_mode")
        val cacheTtlHours = intPreferencesKey("cache_ttl_hours")
        val autoRefreshEnabled = booleanPreferencesKey("auto_refresh_enabled")
        val refreshOnWifiOnly = booleanPreferencesKey("refresh_on_wifi_only")
        val prefetchTeamsForOffline = booleanPreferencesKey("prefetch_teams_for_offline")
        val historyEnabled = booleanPreferencesKey("history_enabled")
        val historyRetentionDays = intPreferencesKey("history_retention_days")
    }

    override fun observeSettings(): Flow<AppSettings> = dataStore.data.map { prefs ->
        val defaults = AppSettings()
        AppSettings(
            activeProfileId = prefs[Keys.activeProfileId] ?: defaults.activeProfileId,
            themeMode = readThemeMode(prefs[Keys.themeMode]),
            cacheTtlHours = prefs[Keys.cacheTtlHours] ?: defaults.cacheTtlHours,
            autoRefreshEnabled = prefs[Keys.autoRefreshEnabled] ?: defaults.autoRefreshEnabled,
            refreshOnWifiOnly = prefs[Keys.refreshOnWifiOnly] ?: defaults.refreshOnWifiOnly,
            prefetchTeamsForOffline = prefs[Keys.prefetchTeamsForOffline]
                ?: defaults.prefetchTeamsForOffline,
            historyEnabled = prefs[Keys.historyEnabled] ?: defaults.historyEnabled,
            historyRetentionDays = prefs[Keys.historyRetentionDays]
                ?: defaults.historyRetentionDays,
        )
    }

    override suspend fun setActiveProfile(id: Long) = update {
        it[Keys.activeProfileId] = id
    }

    override suspend fun setThemeMode(mode: ThemeMode) = update {
        it[Keys.themeMode] = mode.name
    }

    override suspend fun setCacheTtlHours(hours: Int) = update {
        it[Keys.cacheTtlHours] = hours.coerceAtLeast(1)
    }

    override suspend fun setAutoRefreshEnabled(enabled: Boolean) = update {
        it[Keys.autoRefreshEnabled] = enabled
    }

    override suspend fun setRefreshOnWifiOnly(enabled: Boolean) = update {
        it[Keys.refreshOnWifiOnly] = enabled
    }

    override suspend fun setPrefetchTeamsForOffline(enabled: Boolean) = update {
        it[Keys.prefetchTeamsForOffline] = enabled
    }

    override suspend fun setHistoryEnabled(enabled: Boolean) = update {
        it[Keys.historyEnabled] = enabled
    }

    override suspend fun setHistoryRetentionDays(days: Int) = update {
        it[Keys.historyRetentionDays] = days.coerceAtLeast(1)
    }

    private suspend fun update(block: (MutablePreferences) -> Unit) {
        dataStore.edit(block)
    }

    private fun readThemeMode(stored: String?): ThemeMode =
        ThemeMode.entries.firstOrNull { it.name == stored } ?: ThemeMode.SYSTEM
}
