package ru.fefu.pokeabilityapp.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import ru.fefu.pokeabilityapp.ui.detail.AbilityDetailScreen
import ru.fefu.pokeabilityapp.ui.list.AbilityListScreen
import ru.fefu.pokeabilityapp.ui.list.AbilityListViewModel

sealed class Screen(val route: String) {
    object List : Screen("ability_list")
    object Detail : Screen("ability_detail/{abilityId}") {
        fun createRoute(id: Int) = "ability_detail/$id"
    }
}

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.List.route,
    ) {
        composable(Screen.List.route) {
            val viewModel: AbilityListViewModel = hiltViewModel()
            val state by viewModel.uiState.collectAsStateWithLifecycle()
            AbilityListScreen(
                state = state,
                onAbilityClick = { id -> navController.navigate(Screen.Detail.createRoute(id)) },
                onFilterChange = { viewModel.onFilterChange(it) },
                onToggleFavourite = { viewModel.toggleFavourite(it) },
                onLoadMore = { viewModel.loadMore() },
                onRetry = { viewModel.refresh() },
                onQueryChange = { viewModel.onSearchQueryCghange(it) },
                onClearSearch = { viewModel.clearSearch() },
            )
        }
        composable(
            route = Screen.Detail.route,
            arguments = listOf(navArgument("abilityId") { type = NavType.IntType })
        ) {
            AbilityDetailScreen(onBack = { navController.popBackStack() })
        }
    }
}