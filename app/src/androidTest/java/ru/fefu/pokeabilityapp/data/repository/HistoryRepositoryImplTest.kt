package ru.fefu.pokeabilityapp.data.repository

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import ru.fefu.pokeabilityapp.data.local.AppDatabase
import ru.fefu.pokeabilityapp.data.local.ProfileEntity
import ru.fefu.pokeabilityapp.domain.model.AppSettings

@RunWith(AndroidJUnit4::class)
class HistoryRepositoryImplTest {

    private lateinit var database: AppDatabase

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun tearDown() {
        database.close()
    }

    private fun repository(settings: FakeSettingsRepository): HistoryRepositoryImpl {
        val profiles = ProfileRepositoryImpl(database.getProfileDao(), settings)
        return HistoryRepositoryImpl(database.getHistoryDao(), settings, profiles)
    }

    @Test
    fun record_storesEntry() = runTest {
        val repository = repository(FakeSettingsRepository())

        repository.record(1, "stench")

        val entries = repository.observeRecent().first()
        assertEquals(listOf("stench"), entries.map { it.abilityName })
    }

    @Test
    fun record_keepsOneEntryPerAbility() = runTest {
        val repository = repository(FakeSettingsRepository())

        repository.record(1, "stench")
        val firstTime = repository.observeRecent().first().single().viewedAt
        Thread.sleep(5)
        repository.record(1, "stench")

        val entries = repository.observeRecent().first()
        assertEquals(1, entries.size)
        assertTrue(entries.single().viewedAt >= firstTime)
    }

    @Test
    fun record_ordersNewestFirst() = runTest {
        val repository = repository(FakeSettingsRepository())

        repository.record(1, "stench")
        Thread.sleep(5)
        repository.record(2, "drizzle")

        val entries = repository.observeRecent().first()
        assertEquals(listOf("drizzle", "stench"), entries.map { it.abilityName })
    }

    @Test
    fun record_isSkippedWhenHistoryIsDisabled() = runTest {
        val settings = FakeSettingsRepository(AppSettings(historyEnabled = false))
        val repository = repository(settings)

        repository.record(1, "stench")

        assertTrue(repository.observeRecent().first().isEmpty())
    }

    @Test
    fun clear_removesEverything() = runTest {
        val repository = repository(FakeSettingsRepository())
        repository.record(1, "stench")
        repository.record(2, "drizzle")

        repository.clear()

        assertTrue(repository.observeRecent().first().isEmpty())
    }

    @Test
    fun history_isSeparatedByProfile() = runTest {
        val settings = FakeSettingsRepository()
        val repository = repository(settings)
        repository.record(1, "stench")

        val second = database.getProfileDao()
            .insert(ProfileEntity(name = "Gary", createdAt = 2))
        settings.setActiveProfile(second)

        assertTrue(repository.observeRecent().first().isEmpty())

        repository.record(2, "drizzle")
        assertEquals(listOf("drizzle"), repository.observeRecent().first().map { it.abilityName })
    }
}
