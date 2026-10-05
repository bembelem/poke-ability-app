package ru.fefu.pokeabilityapp.ui.profiles

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import ru.fefu.pokeabilityapp.MainDispatcherRule
import ru.fefu.pokeabilityapp.domain.model.AppSettings
import ru.fefu.pokeabilityapp.domain.model.Profile
import ru.fefu.pokeabilityapp.fake.FakeProfileRepository
import ru.fefu.pokeabilityapp.fake.FakeSettingsRepository

@OptIn(ExperimentalCoroutinesApi::class)
class ProfilesViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val ash = Profile(1, "Ash")
    private val gary = Profile(2, "Gary")

    private fun TestScope.createViewModel(
        activeId: Long,
        vararg profiles: Profile
    ): ProfilesViewModel {
        val settings = FakeSettingsRepository(AppSettings(activeProfileId = activeId))
        val viewModel = ProfilesViewModel(FakeProfileRepository(settings, profiles.toList()), settings)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.profiles.collect { }
        }
        return viewModel
    }

    private fun ProfilesViewModel.activeNames() =
        profiles.value.filter { it.isActive }.map { it.name }

    @Test
    fun `new profile becomes active`() = runTest {
        val viewModel = createViewModel(activeId = 1, ash)
        advanceUntilIdle()

        viewModel.create("Gary")
        advanceUntilIdle()

        assertEquals(listOf("Ash", "Gary"), viewModel.profiles.value.map { it.name })
        assertEquals(listOf("Gary"), viewModel.activeNames())
    }

    @Test
    fun `blank name does not create profile`() = runTest {
        val viewModel = createViewModel(activeId = 1, ash)
        advanceUntilIdle()

        viewModel.create("   ")
        advanceUntilIdle()

        assertEquals(1, viewModel.profiles.value.size)
    }

    @Test
    fun `select makes only chosen profile active`() = runTest {
        val viewModel = createViewModel(activeId = 1, ash, gary)
        advanceUntilIdle()

        viewModel.select(2)
        advanceUntilIdle()

        assertEquals(listOf("Gary"), viewModel.activeNames())
    }

    @Test
    fun `last profile is not deleted`() = runTest {
        val viewModel = createViewModel(activeId = 1, ash)
        advanceUntilIdle()

        viewModel.delete(1)
        advanceUntilIdle()

        assertEquals(listOf("Ash"), viewModel.profiles.value.map { it.name })
    }

    @Test
    fun `deleting active profile activates the remaining one`() = runTest {
        val viewModel = createViewModel(activeId = 2, ash, gary)
        advanceUntilIdle()

        viewModel.delete(2)
        advanceUntilIdle()

        assertEquals(listOf("Ash"), viewModel.profiles.value.map { it.name })
        assertEquals(listOf("Ash"), viewModel.activeNames())
    }
}
