package ru.fefu.pokeabilityapp.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import ru.fefu.pokeabilityapp.domain.model.AppSettings
import ru.fefu.pokeabilityapp.domain.model.ThemeMode
import java.io.File

class SettingsRepositoryImplTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private val scope = CoroutineScope(Dispatchers.Unconfined + SupervisorJob())

    private fun createDataStore(): DataStore<Preferences> {
        val file = File(tempFolder.root, "settings.preferences_pb")
        return PreferenceDataStoreFactory.create(scope = scope, produceFile = { file })
    }

    @Test
    fun `returns defaults when nothing is stored`() = runBlocking {
        val repository = SettingsRepositoryImpl(createDataStore())

        assertEquals(AppSettings(), repository.observeSettings().first())
    }

    @Test
    fun `active profile is persisted`() = runBlocking {
        val repository = SettingsRepositoryImpl(createDataStore())

        repository.setActiveProfile(42L)

        assertEquals(42L, repository.observeSettings().first().activeProfileId)
    }

    @Test
    fun `theme mode survives round trip`() = runBlocking {
        val repository = SettingsRepositoryImpl(createDataStore())

        repository.setThemeMode(ThemeMode.DARK)

        assertEquals(ThemeMode.DARK, repository.observeSettings().first().themeMode)
    }

    @Test
    fun `cache ttl below one hour is clamped`() = runBlocking {
        val repository = SettingsRepositoryImpl(createDataStore())

        repository.setCacheTtlHours(0)

        assertEquals(1, repository.observeSettings().first().cacheTtlHours)
    }

    @Test
    fun `unknown stored theme falls back to system`() = runBlocking {
        val dataStore = createDataStore()
        val repository = SettingsRepositoryImpl(dataStore)
        repository.setActiveProfile(1)
        dataStore.edit { it[stringPreferencesKey("p1_theme_mode")] = "NEON" }

        assertEquals(ThemeMode.SYSTEM, repository.observeSettings().first().themeMode)
    }

    @Test
    fun `each profile keeps its own settings`() = runBlocking {
        val repository = SettingsRepositoryImpl(createDataStore())

        repository.setActiveProfile(1)
        repository.setThemeMode(ThemeMode.DARK)
        repository.setCacheTtlHours(6)

        repository.setActiveProfile(2)
        val second = repository.observeSettings().first()
        assertEquals(ThemeMode.SYSTEM, second.themeMode)
        assertEquals(AppSettings().cacheTtlHours, second.cacheTtlHours)

        repository.setThemeMode(ThemeMode.LIGHT)
        repository.setActiveProfile(1)
        val first = repository.observeSettings().first()
        assertEquals(ThemeMode.DARK, first.themeMode)
        assertEquals(6, first.cacheTtlHours)
    }
}
