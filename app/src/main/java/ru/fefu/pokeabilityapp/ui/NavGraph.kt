package ru.fefu.pokeabilityapp.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import ru.fefu.pokeabilityapp.ui.detail.AbilityDetailScreen
import ru.fefu.pokeabilityapp.ui.list.AbilityListScreen

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
        startDestination = Screen.List.route
    ) {
        composable(Screen.List.route) {
            AbilityListScreen(
                onAbilityClick = { id ->
                    navController.navigate(Screen.Detail.createRoute(id))
                }
            )
        }
        composable(
            route = Screen.Detail.route,
            arguments = listOf(navArgument("abilityId") { type = NavType.IntType })
        ) {
            AbilityDetailScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}