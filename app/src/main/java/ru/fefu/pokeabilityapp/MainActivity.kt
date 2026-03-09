package ru.fefu.pokeabilityapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import ru.fefu.pokeabilityapp.ui.NavGraph
import ru.fefu.pokeabilityapp.ui.theme.PokeAbilityAppTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PokeAbilityAppTheme {
                val navController = rememberNavController()
                NavGraph(navController = navController)
            }
        }
    }
}
