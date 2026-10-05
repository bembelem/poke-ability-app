package ru.fefu.pokeabilityapp.work

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.Configuration
import androidx.work.NetworkType
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.testing.SynchronousExecutor
import androidx.work.testing.WorkManagerTestInitHelper
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import ru.fefu.pokeabilityapp.data.repository.FakeSettingsRepository

@RunWith(AndroidJUnit4::class)
class BackgroundWorkTest {

    private lateinit var context: Context
    private lateinit var workManager: WorkManager
    private lateinit var backgroundWork: BackgroundWork

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        val config = Configuration.Builder().setExecutor(SynchronousExecutor()).build()
        WorkManagerTestInitHelper.initializeTestWorkManager(context, config)
        workManager = WorkManager.getInstance(context)
        backgroundWork = BackgroundWork(context, FakeSettingsRepository())
    }

    private fun workInfo(name: String): WorkInfo =
        workManager.getWorkInfosForUniqueWork(name).get().single()

    @Test
    fun enabledRefresh_runsOnAnyNetwork() {
        backgroundWork.scheduleCacheRefresh(enabled = true, wifiOnly = false)

        val info = workInfo(BackgroundWork.CACHE_REFRESH)
        assertEquals(WorkInfo.State.ENQUEUED, info.state)
        assertEquals(NetworkType.CONNECTED, info.constraints.requiredNetworkType)
    }

    @Test
    fun wifiOnlyRefresh_waitsForUnmeteredNetwork() {
        backgroundWork.scheduleCacheRefresh(enabled = true, wifiOnly = true)

        val info = workInfo(BackgroundWork.CACHE_REFRESH)
        assertEquals(NetworkType.UNMETERED, info.constraints.requiredNetworkType)
    }

    @Test
    fun disablingRefresh_cancelsScheduledWork() {
        backgroundWork.scheduleCacheRefresh(enabled = true, wifiOnly = false)

        backgroundWork.scheduleCacheRefresh(enabled = false, wifiOnly = false)

        assertEquals(WorkInfo.State.CANCELLED, workInfo(BackgroundWork.CACHE_REFRESH).state)
    }

    @Test
    fun teamPrefetch_waitsForNetwork() {
        backgroundWork.requestTeamPrefetch()

        val info = workInfo(BackgroundWork.TEAM_PREFETCH)
        assertEquals(WorkInfo.State.ENQUEUED, info.state)
        assertEquals(NetworkType.CONNECTED, info.constraints.requiredNetworkType)
    }
}
