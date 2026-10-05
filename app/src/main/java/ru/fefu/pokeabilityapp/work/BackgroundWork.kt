package ru.fefu.pokeabilityapp.work

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import ru.fefu.pokeabilityapp.domain.repository.SettingsRepository
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BackgroundWork @Inject constructor(
    @ApplicationContext private val context: Context,
    private val settingsRepository: SettingsRepository
) {

    // WorkManager берёт фабрику воркеров из Application, поэтому получаем его не раньше start()
    private val workManager by lazy { WorkManager.getInstance(context) }
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    fun start() {
        scheduleHistoryCleanup()
        scope.launch {
            settingsRepository.observeSettings()
                .map { it.autoRefreshEnabled to it.refreshOnWifiOnly }
                .distinctUntilChanged()
                .collect { (enabled, wifiOnly) -> scheduleCacheRefresh(enabled, wifiOnly) }
        }
    }

    fun scheduleCacheRefresh(enabled: Boolean, wifiOnly: Boolean) {
        if (!enabled) {
            workManager.cancelUniqueWork(CACHE_REFRESH)
            return
        }
        val network = if (wifiOnly) NetworkType.UNMETERED else NetworkType.CONNECTED
        val request = PeriodicWorkRequestBuilder<CacheRefreshWorker>(
            REFRESH_INTERVAL_HOURS,
            TimeUnit.HOURS
        )
            .setConstraints(Constraints.Builder().setRequiredNetworkType(network).build())
            .build()
        workManager.enqueueUniquePeriodicWork(
            CACHE_REFRESH,
            ExistingPeriodicWorkPolicy.UPDATE,
            request
        )
    }

    fun scheduleHistoryCleanup() {
        val request = PeriodicWorkRequestBuilder<HistoryCleanupWorker>(1, TimeUnit.DAYS).build()
        workManager.enqueueUniquePeriodicWork(
            HISTORY_CLEANUP,
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }

    // ждёт сети, поэтому команда, собранная без интернета, догрузится сама
    fun requestTeamPrefetch() {
        val request = OneTimeWorkRequestBuilder<TeamPrefetchWorker>()
            .setConstraints(
                Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
            )
            .build()
        workManager.enqueueUniqueWork(TEAM_PREFETCH, ExistingWorkPolicy.REPLACE, request)
    }

    companion object {
        const val CACHE_REFRESH = "cache_refresh"
        const val HISTORY_CLEANUP = "history_cleanup"
        const val TEAM_PREFETCH = "team_prefetch"
        private const val REFRESH_INTERVAL_HOURS = 6L
    }
}
