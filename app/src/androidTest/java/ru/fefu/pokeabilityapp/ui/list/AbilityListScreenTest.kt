package ru.fefu.pokeabilityapp.ui.list

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
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
                visibleAbilities = emptyList(),
                onAbilityClick = {},
                onFilterChange = {},
                onToggleFavourite = {},
                onLoadMore = {},
                onRetry = {},
                onSearch = {},
                onClearSearch = {},
            )
        }

        composeRule.onNodeWithText("No favourites yet\nSwipe to add favourite")
            .assertIsDisplayed()
    }
}