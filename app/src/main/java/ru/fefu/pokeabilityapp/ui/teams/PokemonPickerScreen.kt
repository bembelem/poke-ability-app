package ru.fefu.pokeabilityapp.ui.teams

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import ru.fefu.pokeabilityapp.domain.model.PokemonItem
import ru.fefu.pokeabilityapp.ui.common.ErrorState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PokemonPickerScreen(
    state: PokemonPickerUiState,
    onQueryChange: (String) -> Unit,
    onPick: (PokemonItem) -> Unit,
    onLoadMore: () -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Выбор покемона") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            when {
                state.isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                state.errorMessage != null -> {
                    ErrorState(message = state.errorMessage, onRetry = onLoadMore)
                }
                else -> {
                    Column {
                        OutlinedTextField(
                            value = state.query,
                            onValueChange = onQueryChange,
                            modifier = Modifier.fillMaxWidth().padding(8.dp),
                            singleLine = true,
                            placeholder = { Text("Поиск по имени") }
                        )
                        if (state.items.isEmpty()) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Ничего не найдено",
                                    textAlign = TextAlign.Center
                                )
                            }
                        } else {
                            LazyColumn {
                                items(items = state.items, key = { it.id }) { item ->
                                    PokemonRow(item = item, onClick = { onPick(item) })
                                    HorizontalDivider()
                                }
                                if (state.query.isBlank()) {
                                    item {
                                        LoadMoreRow(
                                            isLoading = state.isLoadingMore,
                                            onLoadMore = onLoadMore
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PokemonRow(item: PokemonItem, onClick: () -> Unit) {
    ListItem(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        leadingContent = {
            AsyncImage(
                model = item.spriteUrl,
                contentDescription = null,
                modifier = Modifier.size(48.dp)
            )
        },
        headlineContent = { Text(item.name.replaceFirstChar { it.uppercase() }) },
        supportingContent = { Text("ID: ${item.id}") }
    )
}

@Composable
private fun LoadMoreRow(isLoading: Boolean, onLoadMore: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        if (isLoading) {
            CircularProgressIndicator()
        } else {
            LaunchedEffect(Unit) { onLoadMore() }
        }
    }
}
