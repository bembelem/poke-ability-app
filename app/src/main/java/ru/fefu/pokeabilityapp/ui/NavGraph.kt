package ru.fefu.pokeabilityapp.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import ru.fefu.pokeabilityapp.ui.detail.AbilityDetailScreen
import ru.fefu.pokeabilityapp.ui.history.HistoryScreen
import ru.fefu.pokeabilityapp.ui.history.HistoryViewModel
import ru.fefu.pokeabilityapp.ui.list.AbilityListScreen
import ru.fefu.pokeabilityapp.ui.list.AbilityListViewModel
import ru.fefu.pokeabilityapp.ui.settings.SettingsScreen
import ru.fefu.pokeabilityapp.ui.settings.SettingsViewModel

sealed class Screen(val route: String) {
    data object List : Screen("ability_list")
    data object History : Screen("history")
    data object Settings : Screen("settings")
    data object Detail : Screen("ability_detail/{abilityId}") {
        fun createRoute(id: Int) = "ability_detail/$id"
    }
}

private data class BottomItem(val screen: Screen, val label: String, val icon: ImageVector)

private val bottomItems = listOf(
    BottomItem(Screen.List, "Способности", Icons.Default.List),
    BottomItem(Screen.History, "История", Icons.Default.DateRange),
    BottomItem(Screen.Settings, "Настройки", Icons.Default.Settings)
)

@Composable
fun NavGraph(navController: NavHostController) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val showBottomBar = bottomItems.any { it.screen.route == currentRoute }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    bottomItems.forEach { item ->
                        NavigationBarItem(
                            selected = currentRoute == item.screen.route,
                            onClick = {
                                navController.navigate(item.screen.route) {
                                    popUpTo(Screen.List.route) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(item.icon, contentDescription = null) },
                            label = { Text(item.label) }
                        )
                    }
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Screen.List.route,
            modifier = Modifier.padding(padding)
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
                    onQueryChange = { viewModel.onSearchQueryChange(it) },
                    onClearSearch = { viewModel.clearSearch() },
                )
            }
            composable(Screen.History.route) {
                val viewModel: HistoryViewModel = hiltViewModel()
                val entries by viewModel.entries.collectAsStateWithLifecycle()
                HistoryScreen(
                    entries = entries,
                    onEntryClick = { id -> navController.navigate(Screen.Detail.createRoute(id)) },
                    onClear = { viewModel.clear() }
                )
            }
            composable(Screen.Settings.route) {
                val viewModel: SettingsViewModel = hiltViewModel()
                val settings by viewModel.settings.collectAsStateWithLifecycle()
                SettingsScreen(
                    settings = settings,
                    onThemeChange = { viewModel.setThemeMode(it) },
                    onCacheTtlChange = { viewModel.setCacheTtlHours(it) },
                    onHistoryEnabledChange = { viewModel.setHistoryEnabled(it) }
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
}
