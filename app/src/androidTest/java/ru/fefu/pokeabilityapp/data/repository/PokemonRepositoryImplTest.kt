package ru.fefu.pokeabilityapp.data.repository

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import ru.fefu.pokeabilityapp.data.local.AppDatabase
import ru.fefu.pokeabilityapp.domain.model.AppSettings
import ru.fefu.pokeabilityapp.domain.model.PokeType

@RunWith(AndroidJUnit4::class)
class PokemonRepositoryImplTest {

    private lateinit var database: AppDatabase
    private lateinit var api: FakePokeApi
    private lateinit var repository: PokemonRepositoryImpl

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        api = FakePokeApi()
        repository = PokemonRepositoryImpl(
            api = api,
            dao = database.getPokemonCacheDao(),
            settingsRepository = FakeSettingsRepository(AppSettings(cacheTtlHours = 24))
        )
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun refreshFirstPage_cachesList() = runTest {
        api.pokemonListResponse = pokemonListDto(1 to "bulbasaur", 4 to "charmander")

        repository.refreshFirstPage(force = true)

        val items = repository.observePokemon().first()
        assertEquals(listOf("bulbasaur", "charmander"), items.map { it.name })
        assertEquals(listOf(1, 4), items.map { it.id })
    }

    @Test
    fun refreshFirstPage_skipsNetworkWhenCacheIsFresh() = runTest {
        api.pokemonListResponse = pokemonListDto(1 to "bulbasaur")
        repository.refreshFirstPage(force = true)
        assertEquals(1, api.pokemonListCalls)

        repository.refreshFirstPage(force = false)

        assertEquals(1, api.pokemonListCalls)
    }

    @Test
    fun ensureDetail_storesTypesAndAbilities() = runTest {
        api.pokemonResponse = pokemonDto(
            id = 6,
            name = "charizard",
            types = listOf("fire", "flying"),
            abilities = listOf(66 to "blaze", 94 to "solar-power")
        )

        repository.ensureDetail(6)

        val detail = repository.observeDetails(listOf(6)).first().single()
        assertEquals(listOf(PokeType.FIRE, PokeType.FLYING), detail.types)
        assertEquals(listOf("blaze", "solar-power"), detail.abilities.map { it.name })
        assertEquals(listOf(false, true), detail.abilities.map { it.isHidden })
    }

    @Test
    fun ensureDetail_skipsNetworkWhenDetailIsFresh() = runTest {
        api.pokemonResponse = pokemonDto(6, "charizard", listOf("fire"))
        repository.ensureDetail(6)
        assertEquals(1, api.pokemonCalls)

        repository.ensureDetail(6)

        assertEquals(1, api.pokemonCalls)
    }

    @Test
    fun typeChart_keepsAttackerToDefenderDirection() = runTest {
        // земля бьёт по электричеству вдвое, по летающему не бьёт вовсе
        api.typeResponses = mapOf(
            4 to typeDto(
                id = 4,
                name = "electric",
                doubleDamageFrom = listOf("ground")
            ),
            10 to typeDto(
                id = 10,
                name = "flying",
                noDamageFrom = listOf("ground")
            )
        )

        repository.refreshTypeChart(force = true)

        val chart = repository.observeTypeChart().first()
        assertEquals(2.0, chart.multiplier(PokeType.GROUND, PokeType.ELECTRIC), 0.0001)
        assertEquals(0.0, chart.multiplier(PokeType.GROUND, PokeType.FLYING), 0.0001)
        assertEquals(1.0, chart.multiplier(PokeType.ELECTRIC, PokeType.GROUND), 0.0001)
    }

    @Test
    fun refreshTypeChart_skipsNetworkWhenFresh() = runTest {
        api.typeResponses = mapOf(
            4 to typeDto(id = 4, name = "electric", doubleDamageFrom = listOf("ground"))
        )
        repository.refreshTypeChart(force = true)
        val calls = api.typeCalls

        repository.refreshTypeChart(force = false)

        assertEquals(calls, api.typeCalls)
    }
}
