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

    // общий только активный профиль, остальные ключи у каждого профиля свои
    private object Keys {
        val activeProfileId = longPreferencesKey("active_profile_id")

        fun themeMode(profileId: Long) = stringPreferencesKey("p${profileId}_theme_mode")
        fun cacheTtlHours(profileId: Long) = intPreferencesKey("p${profileId}_cache_ttl_hours")
        fun autoRefreshEnabled(profileId: Long) =
            booleanPreferencesKey("p${profileId}_auto_refresh_enabled")
        fun refreshOnWifiOnly(profileId: Long) =
            booleanPreferencesKey("p${profileId}_refresh_on_wifi_only")
        fun prefetchTeamsForOffline(profileId: Long) =
            booleanPreferencesKey("p${profileId}_prefetch_teams_for_offline")
        fun historyEnabled(profileId: Long) = booleanPreferencesKey("p${profileId}_history_enabled")
        fun historyRetentionDays(profileId: Long) =
            intPreferencesKey("p${profileId}_history_retention_days")
    }

    override fun observeSettings(): Flow<AppSettings> = dataStore.data.map { prefs ->
        val defaults = AppSettings()
        val profileId = activeProfile(prefs)
        AppSettings(
            activeProfileId = profileId,
            themeMode = readThemeMode(prefs[Keys.themeMode(profileId)]),
            cacheTtlHours = prefs[Keys.cacheTtlHours(profileId)] ?: defaults.cacheTtlHours,
            autoRefreshEnabled = prefs[Keys.autoRefreshEnabled(profileId)]
                ?: defaults.autoRefreshEnabled,
            refreshOnWifiOnly = prefs[Keys.refreshOnWifiOnly(profileId)]
                ?: defaults.refreshOnWifiOnly,
            prefetchTeamsForOffline = prefs[Keys.prefetchTeamsForOffline(profileId)]
                ?: defaults.prefetchTeamsForOffline,
            historyEnabled = prefs[Keys.historyEnabled(profileId)] ?: defaults.historyEnabled,
            historyRetentionDays = prefs[Keys.historyRetentionDays(profileId)]
                ?: defaults.historyRetentionDays,
        )
    }

    override suspend fun setActiveProfile(id: Long) {
        dataStore.edit { it[Keys.activeProfileId] = id }
    }

    override suspend fun setThemeMode(mode: ThemeMode) = update { prefs, profileId ->
        prefs[Keys.themeMode(profileId)] = mode.name
    }

    override suspend fun setCacheTtlHours(hours: Int) = update { prefs, profileId ->
        prefs[Keys.cacheTtlHours(profileId)] = hours.coerceAtLeast(1)
    }

    override suspend fun setAutoRefreshEnabled(enabled: Boolean) = update { prefs, profileId ->
        prefs[Keys.autoRefreshEnabled(profileId)] = enabled
    }

    override suspend fun setRefreshOnWifiOnly(enabled: Boolean) = update { prefs, profileId ->
        prefs[Keys.refreshOnWifiOnly(profileId)] = enabled
    }

    override suspend fun setPrefetchTeamsForOffline(enabled: Boolean) = update { prefs, profileId ->
        prefs[Keys.prefetchTeamsForOffline(profileId)] = enabled
    }

    override suspend fun setHistoryEnabled(enabled: Boolean) = update { prefs, profileId ->
        prefs[Keys.historyEnabled(profileId)] = enabled
    }

    override suspend fun setHistoryRetentionDays(days: Int) = update { prefs, profileId ->
        prefs[Keys.historyRetentionDays(profileId)] = days.coerceAtLeast(1)
    }

    private suspend fun update(block: (MutablePreferences, Long) -> Unit) {
        dataStore.edit { prefs -> block(prefs, activeProfile(prefs)) }
    }

    private fun activeProfile(prefs: Preferences): Long =
        prefs[Keys.activeProfileId] ?: AppSettings.NO_PROFILE

    private fun readThemeMode(stored: String?): ThemeMode =
        ThemeMode.entries.firstOrNull { it.name == stored } ?: ThemeMode.SYSTEM
}
