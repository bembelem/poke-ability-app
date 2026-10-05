package ru.fefu.pokeabilityapp.work

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.ListenableWorker
import androidx.work.ListenableWorker.Result
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import androidx.work.testing.TestListenableWorkerBuilder
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import ru.fefu.pokeabilityapp.FakeAbilityRepository
import ru.fefu.pokeabilityapp.FakePokemonRepository
import ru.fefu.pokeabilityapp.FakeTeamRepository
import ru.fefu.pokeabilityapp.domain.model.Team
import ru.fefu.pokeabilityapp.domain.model.TeamSlot
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class WorkersTest {

    private val context = ApplicationProvider.getApplicationContext<Context>()

    private fun slot(id: Long, position: Int, pokemonId: Int) = TeamSlot(
        id = id,
        position = position,
        pokemonId = pokemonId,
        pokemonName = "pokemon-$pokemonId",
        abilityId = null,
        abilityName = null,
        nickname = null,
        note = null
    )

    private fun prefetchWorker(
        teams: FakeTeamRepository,
        pokemon: FakePokemonRepository,
        attempt: Int = 0
    ): TeamPrefetchWorker =
        TestListenableWorkerBuilder<TeamPrefetchWorker>(context)
            .setRunAttemptCount(attempt)
            .setWorkerFactory(object : WorkerFactory() {
                override fun createWorker(
                    appContext: Context,
                    workerClassName: String,
                    workerParameters: WorkerParameters
                ): ListenableWorker = TeamPrefetchWorker(appContext, workerParameters, teams, pokemon)
            })
            .build()

    private fun teamOf(vararg pokemonIds: Int) = FakeTeamRepository(
        Team(
            id = 1,
            name = "main",
            note = "",
            slots = pokemonIds.mapIndexed { index, id -> slot(index.toLong(), index, id) }
        )
    )

    @Test
    fun prefetch_loadsEveryPokemonOfTheTeamOnce() = runBlocking {
        val pokemon = FakePokemonRepository()

        val result = prefetchWorker(teamOf(6, 7, 6), pokemon).doWork()

        assertEquals(Result.success(), result)
        assertEquals(listOf(6, 7), pokemon.ensureDetailCalls)
        assertEquals(1, pokemon.refreshTypeChartCalls)
    }

    @Test
    fun prefetch_retriesWhenNetworkFails() = runBlocking {
        val pokemon = FakePokemonRepository().apply { failure = IOException("offline") }

        val result = prefetchWorker(teamOf(6), pokemon).doWork()

        assertEquals(Result.retry(), result)
    }

    @Test
    fun prefetch_givesUpAfterSeveralAttempts() = runBlocking {
        val pokemon = FakePokemonRepository().apply { failure = IOException("offline") }

        val result = prefetchWorker(teamOf(6), pokemon, attempt = 3).doWork()

        assertEquals(Result.failure(), result)
    }

    @Test
    fun cacheRefresh_letsTtlDecideWhatToUpdate() = runBlocking {
        val abilities = FakeAbilityRepository()
        val pokemon = FakePokemonRepository()
        val worker = TestListenableWorkerBuilder<CacheRefreshWorker>(context)
            .setWorkerFactory(object : WorkerFactory() {
                override fun createWorker(
                    appContext: Context,
                    workerClassName: String,
                    workerParameters: WorkerParameters
                ): ListenableWorker =
                    CacheRefreshWorker(appContext, workerParameters, abilities, pokemon)
            })
            .build()

        val result = worker.doWork()

        assertEquals(Result.success(), result)
        assertEquals(false, abilities.lastRefreshForce)
        assertEquals(1, pokemon.refreshFirstPageCalls)
        assertEquals(1, pokemon.refreshTypeChartCalls)
    }
}
