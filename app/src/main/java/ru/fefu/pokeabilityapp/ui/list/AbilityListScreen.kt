package ru.fefu.pokeabilityapp.ui.list

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ru.fefu.pokeabilityapp.domain.model.AbilityFilter
import ru.fefu.pokeabilityapp.domain.model.AbilityItem
import ru.fefu.pokeabilityapp.ui.common.ErrorState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AbilityListScreen(
    state: AbilityListUiState,
    visibleAbilities: List<AbilityItem>,
    onAbilityClick: (Int) -> Unit,
    onFilterChange: (AbilityFilter) -> Unit,
    onToggleFavourite: (Int) -> Unit,
    onLoadMore: () -> Unit,
    onRetry: () -> Unit,
    onSearch: (String) -> Unit,
    onClearSearch: () -> Unit,
) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("Poke Abilities") }) }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            when {
                state.isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                state.errorMessage != null -> {
                    ErrorState(message = state.errorMessage, onRetry = onRetry)
                }
                else -> {
                    Column {
                        SearchBar(
                            query = state.searchQuery,
                            onSearch = onSearch,
                            onClear = onClearSearch,
                        )
                        FilterRow(
                            filter = state.filter,
                            onFilterChange = onFilterChange
                        )
                        if (visibleAbilities.isEmpty()) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (state.filter == AbilityFilter.FAVOURITES)
                                        "No favourites yet\nSwipe to add favourite"
                                    else "Nothing found",
                                    textAlign = TextAlign.Center
                                )
                            }
                        } else {
                            AbilityList(
                                abilities = visibleAbilities,
                                favourites = state.favourites,
                                onAbilityClick = onAbilityClick,
                                onToggleFavourite = onToggleFavourite,
                                onLoadMore = onLoadMore,
                                isLoadingMore = state.isLoadingMore,
                                canLoadMore = state.canLoadMore && state.filter == AbilityFilter.ALL
                            )
                        }
                    }
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
    onToggleFavourite: (Int) -> Unit,
    onLoadMore: () -> Unit,
    isLoadingMore: Boolean,
    canLoadMore: Boolean
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
        if (canLoadMore) {
            item {
                if (isLoadingMore) {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                } else {
                    LaunchedEffect(Unit) { onLoadMore() }
                }
            }
        }
    }
}


@Composable
fun SearchBar(
    query: String,
    onSearch: (String) -> Unit,
    onClear: () -> Unit,
) {
    var text by remember { mutableStateOf(query) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            modifier = Modifier.weight(1f),
            placeholder = { Text("Search ability...") },
            singleLine = true,
            trailingIcon = {
                if (text.isNotEmpty()) {
                    IconButton(onClick = {
                        text = ""
                        onClear()
                    }) {
                        Icon(Icons.Default.Close, contentDescription = "Clear")
                    }
                }
            }
        )
        Spacer(modifier = Modifier.width(8.dp))
        Button(onClick = { onSearch(text) }) {
            Text("Search")
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
        AbilityListItem(
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
fun AbilityListItem(
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
