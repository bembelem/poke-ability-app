package ru.fefu.pokeabilityapp.ui.list

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ru.fefu.pokeabilityapp.domain.model.AbilityItem
import ru.fefu.pokeabilityapp.ui.common.ErrorState
import ru.fefu.pokeabilityapp.ui.common.UiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AbilityListScreen(
    onAbilityClick: (Int) -> Unit,
    viewModel: AbilityListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val favourites by viewModel.favourites.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("POKE-ABILITY-MOD_B6_SWIPE_ACTIONS") })
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            when (val state = uiState) {
                is UiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is UiState.Error -> {
                    ErrorState(
                        message = state.message,
                        onRetry = { viewModel.loadAbilities() }
                    )
                }
                is UiState.Content -> {
                    AbilityList(
                        abilities = state.data,
                        favourites = favourites,
                        onAbilityClick = onAbilityClick,
                        onToggleFavourite = { viewModel.toggleFavourite(it) }
                    )
                }
            }
        }
    }
}

@Composable
fun AbilityList(
    abilities: List<AbilityItem>,
    favourites: Set<Int>,
    onAbilityClick: (Int) -> Unit,
    onToggleFavourite: (Int) -> Unit
) {
    LazyColumn {
        items(items = abilities, key = { it.id }) { ability ->
            SwipeableAbilityItem(
                ability = ability,
                isFavourite = ability.id in favourites,
                onClick = { onAbilityClick(ability.id) },
                onToggleFavourite = { onToggleFavourite(ability.id) }
            )
            HorizontalDivider()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeableAbilityItem(
    ability: AbilityItem,
    isFavourite: Boolean,
    onClick: () -> Unit,
    onToggleFavourite: () -> Unit
) {
    val swipeState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.StartToEnd ||
                value == SwipeToDismissBoxValue.EndToStart
            ) {
                onToggleFavourite()
            }
            false
        }
    )

    SwipeToDismissBox(
        state = swipeState,
        backgroundContent = {
            SwipeBackground(
                direction = swipeState.dismissDirection,
                isFavourite = isFavourite
            )
        }
    ) {
        AbilityItem(
            ability = ability,
            isFavourite = isFavourite,
            onClick = onClick
        )
    }
}

@Composable
fun SwipeBackground(
    direction: SwipeToDismissBoxValue,
    isFavourite: Boolean
) {
    val color = if (isFavourite) Color(0xFFFF6B6B) else Color(0xFF4CAF50)
    val icon = if (isFavourite) Icons.Default.Favorite else Icons.Default.FavoriteBorder
    val alignment = when (direction) {
        SwipeToDismissBoxValue.StartToEnd -> Alignment.CenterStart
        SwipeToDismissBoxValue.EndToStart -> Alignment.CenterEnd
        SwipeToDismissBoxValue.Settled -> Alignment.Center
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color)
            .padding(horizontal = 24.dp),
        contentAlignment = alignment
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = Color.White)
    }
}

@Composable
fun AbilityItem(
    ability: AbilityItem,
    isFavourite: Boolean,
    onClick: () -> Unit
) {
    ListItem(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        headlineContent = {
            Text(text = ability.name.replaceFirstChar { it.uppercase() })
        },
        supportingContent = {
            Text(text = "ID: ${ability.id}")
        },
        trailingContent = {
            if (isFavourite) {
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = "Favourite",
                    tint = Color.Red
                )
            }
        }
    )
}