package ru.fefu.pokeabilityapp.ui.list

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import ru.fefu.pokeabilityapp.FakeAbilityRepository
import ru.fefu.pokeabilityapp.FakeFavouriteRepository
import ru.fefu.pokeabilityapp.MainDispatcherRule
import ru.fefu.pokeabilityapp.domain.model.AbilityFilter
import ru.fefu.pokeabilityapp.domain.model.AbilityItem

class AbilityListViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val overgrow = AbilityItem(id = 1, name = "overgrow")
    private val blaze = AbilityItem(id = 2, name = "blaze")

    private fun createViewModel(
        abilityRepo: FakeAbilityRepository = FakeAbilityRepository(),
        favouriteRepo: FakeFavouriteRepository = FakeFavouriteRepository()
    ) = AbilityListViewModel(abilityRepo, favouriteRepo)

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `loadAbilities success updates items`() = runTest {
        val abilityRepo = FakeAbilityRepository().apply {
            abilities = listOf(overgrow, blaze)
        }
        val viewModel = createViewModel(abilityRepo)
        advanceUntilIdle()

        assertFalse(viewModel.uiState.isLoading)
        assertNull(viewModel.uiState.errorMessage)
        assertEquals(listOf(overgrow, blaze), viewModel.uiState.items)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `loadAbilities failure sets error message`() = runTest {
        val abilityRepo = FakeAbilityRepository().apply {
            failGetAbilities = true
        }
        val viewModel = createViewModel(abilityRepo)
        advanceUntilIdle()

        assertFalse(viewModel.uiState.isLoading)
        assertNotNull(viewModel.uiState.errorMessage)
        assertTrue(viewModel.uiState.items.isEmpty())
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `toggleFavourite adds item to favourites`() = runTest {
        val abilityRepo = FakeAbilityRepository().apply {
            abilities = listOf(overgrow, blaze)
        }
        val viewModel = createViewModel(abilityRepo)
        advanceUntilIdle()

        viewModel.toggleFavourite(overgrow.id)
        advanceUntilIdle()

        assertTrue(overgrow.id in viewModel.uiState.favourites)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `toggleFavourite removes item from favourites`() = runTest {
        val favouriteRepo = FakeFavouriteRepository().apply {
            favourites.add(overgrow)
        }
        val abilityRepo = FakeAbilityRepository().apply {
            abilities = listOf(overgrow, blaze)
        }
        val viewModel = createViewModel(abilityRepo, favouriteRepo)
        advanceUntilIdle()

        viewModel.toggleFavourite(overgrow.id)
        advanceUntilIdle()

        assertFalse(overgrow.id in viewModel.uiState.favourites)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `toggleFavourite does not add duplicate`() = runTest {
        val abilityRepo = FakeAbilityRepository().apply {
            abilities = listOf(overgrow)
        }
        val viewModel = createViewModel(abilityRepo)
        advanceUntilIdle()

        viewModel.toggleFavourite(overgrow.id)
        advanceUntilIdle()
        viewModel.toggleFavourite(overgrow.id)
        advanceUntilIdle()
        viewModel.toggleFavourite(overgrow.id)
        advanceUntilIdle()

        assertEquals(1, viewModel.uiState.favourites.size)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `onFilterChange to favourites shows only favourites`() = runTest {
        val favouriteRepo = FakeFavouriteRepository().apply {
            favourites.add(overgrow)
        }
        val abilityRepo = FakeAbilityRepository().apply {
            abilities = listOf(overgrow, blaze)
        }
        val viewModel = createViewModel(abilityRepo, favouriteRepo)
        advanceUntilIdle()

        viewModel.onFilterChange(AbilityFilter.FAVOURITES)

        assertEquals(listOf(overgrow), viewModel.visibleAbilities)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `retry after error loads abilities successfully`() = runTest {
        val abilityRepo = FakeAbilityRepository().apply {
            failGetAbilities = true
        }
        val viewModel = createViewModel(abilityRepo)
        advanceUntilIdle()

        assertNotNull(viewModel.uiState.errorMessage)

        abilityRepo.failGetAbilities = false
        abilityRepo.abilities = listOf(overgrow)
        viewModel.loadAbilities()
        advanceUntilIdle()

        assertNull(viewModel.uiState.errorMessage)
        assertEquals(listOf(overgrow), viewModel.uiState.items)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `loadMore appends items to existing list`() = runTest {
        val abilityRepo = FakeAbilityRepository().apply {
            abilities = List(20) { AbilityItem(it + 1, "ability-${it + 1}") }
        }
        val viewModel = createViewModel(abilityRepo)
        advanceUntilIdle()

        abilityRepo.abilities = listOf(blaze)
        viewModel.loadMore()
        advanceUntilIdle()

        assertEquals(21, viewModel.uiState.items.size)
    }
}