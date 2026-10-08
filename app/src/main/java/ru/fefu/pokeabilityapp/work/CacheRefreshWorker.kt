package ru.fefu.pokeabilityapp.work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CancellationException
import ru.fefu.pokeabilityapp.domain.repository.AbilityRepository
import ru.fefu.pokeabilityapp.domain.repository.PokemonRepository

// сеть дёргается только для устаревших данных: решение принимает TTL в репозиториях
@HiltWorker
class CacheRefreshWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val abilityRepository: AbilityRepository,
    private val pokemonRepository: PokemonRepository
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result = try {
        abilityRepository.refreshFirstPage(force = false)
        pokemonRepository.refreshFirstPage(force = false)
        pokemonRepository.refreshTypeChart(force = false)
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
