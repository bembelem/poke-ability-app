package ru.fefu.pokeabilityapp.ui.list

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
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

@OptIn(ExperimentalCoroutinesApi::class)
class AbilityListViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val overgrow = AbilityItem(id = 1, name = "overgrow")
    private val blaze = AbilityItem(id = 2, name = "blaze")

    // uiState собран через WhileSubscribed, без подписчика value не обновляется
    private fun TestScope.createViewModel(
        abilityRepo: FakeAbilityRepository = FakeAbilityRepository(),
        favouriteRepo: FakeFavouriteRepository = FakeFavouriteRepository()
    ): AbilityListViewModel {
        val viewModel = AbilityListViewModel(abilityRepo, favouriteRepo)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect { }
        }
        return viewModel
    }

    @Test
    fun `first page success updates items`() = runTest {
        val abilityRepo = FakeAbilityRepository().apply {
            abilities = listOf(overgrow, blaze)
        }
        val viewModel = createViewModel(abilityRepo)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNull(state.errorMessage)
        assertEquals(listOf(overgrow, blaze), state.items)
    }

    @Test
    fun `first page failure sets error message`() = runTest {
        val abilityRepo = FakeAbilityRepository().apply {
            failGetAbilities = true
        }
        val viewModel = createViewModel(abilityRepo)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNotNull(state.errorMessage)
        assertTrue(state.items.isEmpty())
    }

    @Test
    fun `toggleFavourite adds item to favourites`() = runTest {
        val abilityRepo = FakeAbilityRepository().apply {
            abilities = listOf(overgrow, blaze)
        }
        val viewModel = createViewModel(abilityRepo)
        advanceUntilIdle()

        viewModel.toggleFavourite(overgrow.id)
        advanceUntilIdle()

        assertTrue(overgrow.id in viewModel.uiState.value.favourites)
    }

    @Test
    fun `toggleFavourite removes item from favourites`() = runTest {
        val favouriteRepo = FakeFavouriteRepository().apply {
            seed(overgrow)
        }
        val abilityRepo = FakeAbilityRepository().apply {
            abilities = listOf(overgrow, blaze)
        }
        val viewModel = createViewModel(abilityRepo, favouriteRepo)
        advanceUntilIdle()

        viewModel.toggleFavourite(overgrow.id)
        advanceUntilIdle()

        assertFalse(overgrow.id in viewModel.uiState.value.favourites)
    }

    @Test
    fun `toggleFavourite three times leaves one favourite`() = runTest {
        val abilityRepo = FakeAbilityRepository().apply {
            abilities = listOf(overgrow)
        }
        val viewModel = createViewModel(abilityRepo)
        advanceUntilIdle()

        repeat(3) {
            viewModel.toggleFavourite(overgrow.id)
            advanceUntilIdle()
        }

        assertEquals(1, viewModel.uiState.value.favourites.size)
    }

    @Test
    fun `favourites filter shows only favourites`() = runTest {
        val favouriteRepo = FakeFavouriteRepository().apply {
            seed(overgrow)
        }
        val abilityRepo = FakeAbilityRepository().apply {
            abilities = listOf(overgrow, blaze)
        }
        val viewModel = createViewModel(abilityRepo, favouriteRepo)
        advanceUntilIdle()

        viewModel.onFilterChange(AbilityFilter.FAVOURITES)
        advanceUntilIdle()

        assertEquals(listOf(overgrow), viewModel.uiState.value.items)
    }

    @Test
    fun `refresh after error loads first page again`() = runTest {
        val abilityRepo = FakeAbilityRepository().apply {
            failGetAbilities = true
        }
        val viewModel = createViewModel(abilityRepo)
        advanceUntilIdle()

        assertNotNull(viewModel.uiState.value.errorMessage)

        abilityRepo.failGetAbilities = false
        abilityRepo.abilities = listOf(overgrow)
        viewModel.refresh()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertNull(state.errorMessage)
        assertEquals(listOf(overgrow), state.items)
    }

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

        assertEquals(21, viewModel.uiState.value.items.size)
    }

    @Test
    fun `loadMore failure sets loadMoreError and keeps items`() = runTest {
        val abilityRepo = FakeAbilityRepository().apply {
            abilities = List(20) { AbilityItem(it + 1, "ability-${it + 1}") }
        }
        val viewModel = createViewModel(abilityRepo)
        advanceUntilIdle()

        abilityRepo.failGetAbilities = true
        viewModel.loadMore()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("Network error", state.loadMoreError)
        assertEquals(20, state.items.size)
        assertFalse(state.isLoadingMore)
    }

    @Test
    fun `search error clears previous results`() = runTest {
        val abilityRepo = FakeAbilityRepository().apply {
            abilities = listOf(overgrow, blaze)
            searchResult = overgrow
        }
        val viewModel = createViewModel(abilityRepo)
        advanceUntilIdle()

        viewModel.onSearchQueryChange("overgrow")
        advanceUntilIdle()
        assertEquals(listOf(overgrow), viewModel.uiState.value.items)

        abilityRepo.failSearch = true
        viewModel.onSearchQueryChange("blaze")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertNotNull(state.errorMessage)
        assertTrue(state.items.isEmpty())
    }
}
