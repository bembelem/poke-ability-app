package ru.fefu.pokeabilityapp.ui.abilities

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import ru.fefu.pokeabilityapp.domain.model.AbilityFilter
import ru.fefu.pokeabilityapp.domain.model.AbilityItem

class AbilityListScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val overgrow = AbilityItem(id = 1, name = "overgrow")
    private val blaze = AbilityItem(id = 2, name = "blaze")

    @Test
    fun abilityName_isShown_whenListIsNotEmpty() {
        composeRule.setContent {
            AbilityList(
                abilities = listOf(overgrow, blaze),
                favourites = emptySet(),
                onAbilityClick = {},
                onToggleFavourite = {},
                onLoadMore = {},
                isLoadingMore = false,
                loadMoreError = null,
                canLoadMore = false
            )
        }

        composeRule.onNodeWithText("Overgrow").assertIsDisplayed()
        composeRule.onNodeWithText("Blaze").assertIsDisplayed()
    }

    @Test
    fun emptyFavourites_showsHintText() {
        composeRule.setContent {
            AbilityListScreen(
                state = AbilityListUiState(filter = AbilityFilter.FAVOURITES),
                onAbilityClick = {},
                onFilterChange = {},
                onToggleFavourite = {},
                onLoadMore = {},
                onRetry = {},
                onQueryChange = {},
                onClearSearch = {},
                onHistoryClick = {},
            )
        }

        composeRule.onNodeWithText("No favourites yet\nSwipe to add favourite")
            .assertIsDisplayed()
    }

    @Test
    fun abilityItem_click_callsOnAbilityClick() {
        var clickedId: Int? = null

        composeRule.setContent {
            AbilityList(
                abilities = listOf(overgrow, blaze),
                favourites = emptySet(),
                onAbilityClick = { clickedId = it },
                onToggleFavourite = {},
                onLoadMore = {},
                isLoadingMore = false,
                loadMoreError = null,
                canLoadMore = false
            )
        }

        composeRule.onNodeWithText("Overgrow").performClick()

        assertEquals(overgrow.id, clickedId)
    }

    @Test
    fun loadMoreError_showsRetry_andRetryCallsOnLoadMore() {
        var retried = false

        composeRule.setContent {
            AbilityList(
                abilities = listOf(overgrow),
                favourites = emptySet(),
                onAbilityClick = {},
                onToggleFavourite = {},
                onLoadMore = { retried = true },
                isLoadingMore = false,
                loadMoreError = "Network error",
                canLoadMore = true
            )
        }

        composeRule.onNodeWithText("Retry").assertIsDisplayed()
        composeRule.onNodeWithText("Retry").performClick()

        assertTrue(retried)
    }

    @Test
    fun searchError_showsErrorState() {
        composeRule.setContent {
            AbilityListScreen(
                state = AbilityListUiState(
                    searchQuery = "overgrow",
                    errorMessage = "Network error",
                    hasSearched = true,
                ),
                onAbilityClick = {},
                onFilterChange = {},
                onToggleFavourite = {},
                onLoadMore = {},
                onRetry = {},
                onQueryChange = {},
                onClearSearch = {},
                onHistoryClick = {},
            )
        }

        composeRule.onNodeWithText("Ошибка: Network error").assertIsDisplayed()
    }

    @Test
    fun emptySearchResult_showsQueryInMessage() {
        composeRule.setContent {
            AbilityListScreen(
                state = AbilityListUiState(
                    searchQuery = "zzz",
                    hasSearched = true,
                ),
                onAbilityClick = {},
                onFilterChange = {},
                onToggleFavourite = {},
                onLoadMore = {},
                onRetry = {},
                onQueryChange = {},
                onClearSearch = {},
                onHistoryClick = {},
            )
        }

        composeRule.onNodeWithText("Nothing found for \"zzz\"").assertIsDisplayed()
    }
}
