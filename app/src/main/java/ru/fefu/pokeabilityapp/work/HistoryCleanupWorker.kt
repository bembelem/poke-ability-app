package ru.fefu.pokeabilityapp.work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import ru.fefu.pokeabilityapp.domain.repository.HistoryRepository

@HiltWorker
class HistoryCleanupWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val historyRepository: HistoryRepository
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        historyRepository.deleteExpired(System.currentTimeMillis())
        return Result.success()
    }
}
