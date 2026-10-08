package ru.fefu.pokeabilityapp.data.repository

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import retrofit2.HttpException
import retrofit2.Response
import ru.fefu.pokeabilityapp.data.local.AppDatabase
import ru.fefu.pokeabilityapp.data.local.dao.AbilityCacheDao
import ru.fefu.pokeabilityapp.data.local.entity.CachedAbilityEntity
import ru.fefu.pokeabilityapp.domain.model.AppSettings
import ru.fefu.pokeabilityapp.fake.FakePokeApi
import ru.fefu.pokeabilityapp.fake.FakeSettingsRepository
import ru.fefu.pokeabilityapp.fake.abilityDto
import ru.fefu.pokeabilityapp.fake.abilityListDto
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class AbilityRepositoryImplTest {

    private lateinit var database: AppDatabase
    private lateinit var dao: AbilityCacheDao
    private lateinit var api: FakePokeApi

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = database.getAbilityCacheDao()
        api = FakePokeApi()
    }

    @After
    fun tearDown() {
        database.close()
    }

    private fun repository(ttlHours: Int = 24) = AbilityRepositoryImpl(
        api = api,
        dao = dao,
        settingsRepository = FakeSettingsRepository(AppSettings(cacheTtlHours = ttlHours))
    )

    private fun httpError(code: Int) =
        HttpException(Response.error<Any>(code, "".toResponseBody(null)))

    @Test
    fun observeAbilities_emitsCachedListWithoutNetwork() = runTest {
        dao.saveListEntries(
            listOf(
                CachedAbilityEntity(id = 1, name = "stench", listOrder = 0, fetchedAt = 100),
                CachedAbilityEntity(id = 2, name = "drizzle", listOrder = 1, fetchedAt = 100)
            )
        )

        val items = repository().observeAbilities().first()

        assertEquals(listOf("stench", "drizzle"), items.map { it.name })
        assertEquals(0, api.listCalls)
    }

    @Test
    fun refreshFirstPage_skipsNetworkWhenCacheIsFresh() = runTest {
        dao.saveListEntries(
            listOf(
                CachedAbilityEntity(
                    id = 1,
                    name = "stench",
                    listOrder = 0,
                    fetchedAt = System.currentTimeMillis()
                )
            )
        )

        repository(ttlHours = 24).refreshFirstPage(force = false)

        assertEquals(0, api.listCalls)
    }

    @Test
    fun refreshFirstPage_goesToNetworkWhenCacheIsStale() = runTest {
        val longAgo = System.currentTimeMillis() - 48L * 60 * 60 * 1000
        dao.saveListEntries(
            listOf(CachedAbilityEntity(id = 1, name = "stench", listOrder = 0, fetchedAt = longAgo))
        )
        api.listResponse = abilityListDto(1 to "stench", 2 to "drizzle")

        repository(ttlHours = 24).refreshFirstPage(force = false)

        assertEquals(1, api.listCalls)
        assertEquals(2, dao.listCount())
    }

    @Test
    fun refreshFirstPage_forceIgnoresTtl() = runTest {
        dao.saveListEntries(
            listOf(
                CachedAbilityEntity(
                    id = 1,
                    name = "stench",
                    listOrder = 0,
                    fetchedAt = System.currentTimeMillis()
                )
            )
        )
        api.listResponse = abilityListDto(1 to "stench")

        repository(ttlHours = 24).refreshFirstPage(force = true)

        assertEquals(1, api.listCalls)
    }

    @Test
    fun refreshFirstPage_failureKeepsCachedData() = runTest {
        dao.saveListEntries(
            listOf(CachedAbilityEntity(id = 1, name = "stench", listOrder = 0, fetchedAt = 100))
        )
        api.failure = IOException("offline")

        runCatching { repository(ttlHours = 0).refreshFirstPage(force = true) }

        assertEquals(1, dao.listCount())
        assertEquals("stench", dao.getById(1)?.name)
    }

    @Test
    fun loadNextPage_appendsWithContinuingOrder() = runTest {
        api.listResponse = abilityListDto(1 to "stench", 2 to "drizzle")
        val repository = repository()
        repository.refreshFirstPage(force = true)

        api.listResponse = abilityListDto(3 to "speed-boost")
        val loaded = repository.loadNextPage()

        assertEquals(1, loaded)
        assertEquals(2, dao.getById(3)?.listOrder)
        assertEquals(
            listOf("stench", "drizzle", "speed-boost"),
            repository.observeAbilities().first().map { it.name }
        )
    }

    @Test
    fun findByName_usesCacheWithoutNetwork() = runTest {
        dao.saveListEntries(
            listOf(CachedAbilityEntity(id = 1, name = "stench", listOrder = 0, fetchedAt = 100))
        )

        val found = repository().findByName("Stench")

        assertEquals(1, found?.id)
        assertEquals(0, api.byNameCalls)
    }

    @Test
    fun findByName_returnsNullWhenAbilityDoesNotExist() = runTest {
        api.failure = httpError(404)

        assertNull(repository().findByName("no-such-ability"))
    }

    @Test(expected = IOException::class)
    fun findByName_rethrowsNetworkFailure() = runTest {
        api.failure = IOException("offline")

        repository().findByName("stench")
        Unit
    }

    @Test
    fun refreshDetail_storesDetailAndKeepsListOrder() = runTest {
        api.listResponse = abilityListDto(1 to "stench")
        val repository = repository()
        repository.refreshFirstPage(force = true)

        api.detailResponse = abilityDto(1, "stench")
        repository.refreshDetail(1, force = true)

        val detail = repository.observeAbilityDetail(1).first()
        assertNotNull(detail)
        assertEquals("short effect of stench", detail?.shortEffect)
        assertEquals(0, dao.getById(1)?.listOrder)
    }

    @Test
    fun refreshDetail_skipsNetworkWhenDetailIsFresh() = runTest {
        api.detailResponse = abilityDto(1, "stench")
        val repository = repository(ttlHours = 24)
        repository.refreshDetail(1, force = true)
        assertEquals(1, api.detailCalls)

        repository.refreshDetail(1, force = false)

        assertEquals(1, api.detailCalls)
    }
}
