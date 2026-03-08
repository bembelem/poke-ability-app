package ru.fefu.pokeabilityapp.ui.detail

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ru.fefu.pokeabilityapp.domain.model.AbilityDetail
import ru.fefu.pokeabilityapp.ui.common.ErrorState
import ru.fefu.pokeabilityapp.ui.common.UiState


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AbilityDetailScreen(
    onBack: () -> Unit,
    viewModel: AbilityDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ability Detail") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
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
                        onRetry = { viewModel.loadDetail() }
                    )
                }
                is UiState.Content -> {
                    AbilityDetailContent(detail = state.data)
                }
            }
        }
    }
}

@Composable
fun AbilityDetailContent(detail: AbilityDetail) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = detail.name.replaceFirstChar { it.uppercase() },
                style = MaterialTheme.typography.headlineMedium
            )
        }
        item {
            DetailCard(title = "Generation") {
                Text(text = detail.generation)
            }
        }
        item {
            DetailCard(title = "Short Effect") {
                Text(text = detail.shortEffect)
            }
        }
        item {
            DetailCard(title = "Full Effect") {
                Text(text = detail.fullEffect)
            }
        }
        item {
            DetailCard(title = "Flavor Text") {
                Text(text = detail.flavorText)
            }
        }
        item {
            DetailCard(title = "Pokémon with this ability (${detail.pokemonList.size})") {
                detail.pokemonList.forEach { name ->
                    Text(
                        text = "• ${name.replaceFirstChar { it.uppercase() }}",
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun DetailCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            content()
        }
    }
}