package ru.fefu.pokeabilityapp.work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.first
import ru.fefu.pokeabilityapp.domain.repository.PokemonRepository
import ru.fefu.pokeabilityapp.domain.repository.TeamRepository

@HiltWorker
class TeamPrefetchWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val teamRepository: TeamRepository,
    private val pokemonRepository: PokemonRepository
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result = try {
        pokemonRepository.refreshTypeChart(force = false)
        teamRepository.observeTeams().first()
            .flatMap { team -> team.slots.map { it.pokemonId } }
            .distinct()
            .forEach { pokemonRepository.ensureDetail(it) }
        Result.success()
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        if (runAttemptCount < MAX_ATTEMPTS) Result.retry() else Result.failure()
    }

    private companion object {
        const val MAX_ATTEMPTS = 3
    }
}
