package ru.fefu.pokeabilityapp.ui.teams

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import ru.fefu.pokeabilityapp.domain.coverage.TeamCoverage
import ru.fefu.pokeabilityapp.domain.coverage.TypeExposure
import ru.fefu.pokeabilityapp.domain.model.PokemonAbilityOption

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeamEditorScreen(
    state: TeamEditorUiState,
    onPickPokemon: (Int) -> Unit,
    onSelectAbility: (Long, PokemonAbilityOption?) -> Unit,
    onClearSlot: (Int) -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.teamName.ifBlank { "Команда" }) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding).fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(items = state.slots, key = { it.position }) { slot ->
                SlotCard(
                    slot = slot,
                    onPick = { onPickPokemon(slot.position) },
                    onSelectAbility = { option ->
                        slot.slotId?.let { onSelectAbility(it, option) }
                    },
                    onClear = { onClearSlot(slot.position) }
                )
            }
            item {
                CoverageSection(coverage = state.coverage, chartReady = state.chartReady)
            }
        }
    }
}

@Composable
private fun SlotCard(
    slot: SlotUi,
    onPick: () -> Unit,
    onSelectAbility: (PokemonAbilityOption?) -> Unit,
    onClear: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            if (slot.pokemonName == null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Слот ${slot.position + 1}: пусто")
                    TextButton(onClick = onPick) { Text("Выбрать") }
                }
                return@Card
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(
                    model = slot.spriteUrl,
                    contentDescription = null,
                    modifier = Modifier.size(56.dp)
                )
                Column(modifier = Modifier.padding(start = 12.dp)) {
                    Text(
                        text = slot.pokemonName.replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = if (slot.types.isEmpty()) {
                            "Типы не загружены"
                        } else {
                            slot.types.joinToString(" / ") { it.apiName }
                        },
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            if (slot.abilityOptions.isNotEmpty()) {
                Text(
                    text = "Способность",
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(top = 8.dp)
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    slot.abilityOptions.forEach { option ->
                        val selected = slot.selectedAbilityId == option.abilityId
                        FilterChip(
                            selected = selected,
                            onClick = { onSelectAbility(if (selected) null else option) },
                            label = { Text(option.name) }
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onPick) { Text("Заменить") }
                IconButton(onClick = onClear) {
                    Icon(Icons.Default.Close, contentDescription = "Очистить слот")
                }
            }
        }
    }
}

@Composable
private fun CoverageSection(coverage: TeamCoverage?, chartReady: Boolean) {
    Column(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
        Text("Покрытие типов", style = MaterialTheme.typography.titleMedium)

        when {
            !chartReady -> Text(
                text = "Таблица типов ещё не загружена",
                style = MaterialTheme.typography.bodyMedium
            )
            coverage == null -> Text(
                text = "Добавьте покемонов, чтобы увидеть расчёт",
                style = MaterialTheme.typography.bodyMedium
            )
            coverage.threats.isEmpty() -> Text(
                text = "Явных слабостей нет",
                style = MaterialTheme.typography.bodyMedium
            )
            else -> Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Уязвимости команды",
                    style = MaterialTheme.typography.bodyMedium
                )
                coverage.threats.forEach { ThreatRow(it) }
            }
        }
    }
}

@Composable
private fun ThreatRow(exposure: TypeExposure) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = exposure.type.apiName,
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            text = "${exposure.weakMembers.size} под ударом, до x${exposure.maxMultiplier.toInt()}",
            style = MaterialTheme.typography.bodySmall
        )
    }
}
