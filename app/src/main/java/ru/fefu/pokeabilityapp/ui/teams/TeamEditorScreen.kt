package ru.fefu.pokeabilityapp.ui.teams

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.AlertDialog
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import ru.fefu.pokeabilityapp.domain.coverage.THREAT_THRESHOLD
import ru.fefu.pokeabilityapp.domain.coverage.TeamCoverage
import ru.fefu.pokeabilityapp.domain.coverage.TypeExposure
import ru.fefu.pokeabilityapp.domain.model.PokeType
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
                CoverageSection(
                    coverage = state.coverage,
                    chartReady = state.chartReady,
                    slots = state.slots
                )
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
private fun CoverageSection(coverage: TeamCoverage?, chartReady: Boolean, slots: List<SlotUi>) {
    var showHelp by remember { mutableStateOf(false) }

    if (showHelp) {
        AlertDialog(
            onDismissRequest = { showHelp = false },
            title = { Text("Как читать таблицу") },
            text = {
                Text(
                    "Строка: тип атаки. Столбец: покемон из команды.\n\n" +
                        "В ячейке множитель урона, который покемон получит от атаки этого типа:\n" +
                        "2× и 4×: урон увеличен\n" +
                        "½ и ¼: урон уменьшен\n" +
                        "0: иммунитет, от типа или способности\n" +
                        "пусто: обычный урон\n\n" +
                        "«Слаб»: сколько покемонов получают увеличенный урон. " +
                        "Подсвечено, если таких $THREAT_THRESHOLD и больше: это главные угрозы.\n" +
                        "«Сопр»: сколько покемонов урон уменьшают."
                )
            },
            confirmButton = {
                TextButton(onClick = { showHelp = false }) { Text("Понятно") }
            }
        )
    }

    Column(
        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
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
            else -> {
                Text("Главные угрозы", style = MaterialTheme.typography.titleSmall)
                if (coverage.threats.isEmpty()) {
                    Text("Явных слабостей нет", style = MaterialTheme.typography.bodyMedium)
                } else {
                    coverage.threats.forEach { ThreatRow(it) }
                }

                Row(
                    modifier = Modifier.padding(top = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Таблица уязвимостей", style = MaterialTheme.typography.titleSmall)
                    IconButton(onClick = { showHelp = true }) {
                        Icon(Icons.Outlined.Info, contentDescription = "Как читать таблицу")
                    }
                }
                Card(modifier = Modifier.fillMaxWidth()) {
                    Box(modifier = Modifier.padding(6.dp)) {
                        CoverageMatrix(coverage = coverage, slots = slots)
                    }
                }
            }
        }
    }
}

@Composable
private fun ThreatRow(exposure: TypeExposure) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        TypeLabel(exposure.type)
        Text(
            text = "${exposure.weakMembers.size} под ударом, до ${multiplierLabel(exposure.maxMultiplier)}",
            style = MaterialTheme.typography.bodySmall
        )
    }
}

private val labelWidth = 72.dp
private val cellSize = 34.dp

// колонка типов закреплена, вбок прокручиваются только ячейки
@Composable
private fun CoverageMatrix(coverage: TeamCoverage, slots: List<SlotUi>) {
    val members = coverage.exposures.firstOrNull()?.perMember.orEmpty()
    val spriteBySlot = slots.associate { it.slotId to it.spriteUrl }
    val threatTypes = coverage.threats.map { it.type }.toSet()

    Row {
        Column {
            Spacer(Modifier.size(width = labelWidth, height = cellSize))
            coverage.exposures.forEach { exposure ->
                Box(
                    modifier = Modifier.size(width = labelWidth, height = cellSize),
                    contentAlignment = Alignment.CenterStart
                ) {
                    TypeLabel(exposure.type)
                }
            }
        }
        Column(modifier = Modifier.horizontalScroll(rememberScrollState())) {
            Row {
                members.forEach { member ->
                    AsyncImage(
                        model = spriteBySlot[member.slotId],
                        contentDescription = member.pokemonName,
                        modifier = Modifier.size(cellSize)
                    )
                }
                HeaderCell("Слаб")
                HeaderCell("Сопр")
            }
            coverage.exposures.forEach { exposure ->
                Row {
                    exposure.perMember.forEach { MultiplierCell(it.multiplier) }
                    CountCell(
                        count = exposure.weakMembers.size,
                        color = MaterialTheme.colorScheme.error,
                        highlighted = exposure.type in threatTypes
                    )
                    CountCell(
                        count = exposure.resistantMembers.size,
                        color = ResistColor,
                        highlighted = false
                    )
                }
            }
        }
    }
}

@Composable
private fun HeaderCell(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall,
        textAlign = TextAlign.Center,
        modifier = Modifier.width(cellSize)
    )
}

@Composable
private fun MultiplierCell(multiplier: Double) {
    val color = when {
        multiplier == 0.0 -> MaterialTheme.colorScheme.onSurfaceVariant
        multiplier > 1.0 -> MaterialTheme.colorScheme.error
        multiplier < 1.0 -> ResistColor
        else -> Color.Unspecified
    }
    Box(modifier = Modifier.size(cellSize), contentAlignment = Alignment.Center) {
        Text(
            text = multiplierLabel(multiplier),
            color = color,
            fontWeight = if (multiplier >= 4.0 || multiplier == 0.25) FontWeight.Bold else null,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun CountCell(count: Int, color: Color, highlighted: Boolean) {
    Box(
        modifier = Modifier
            .size(cellSize)
            .background(if (highlighted) color.copy(alpha = 0.2f) else Color.Transparent),
        contentAlignment = Alignment.Center
    ) {
        if (count > 0) {
            Text(
                text = count.toString(),
                color = color,
                fontWeight = if (highlighted) FontWeight.Bold else null,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun TypeLabel(type: PokeType) {
    val background = typeColor(type)
    Text(
        text = type.apiName.uppercase(),
        color = if (background.luminance() > 0.5f) Color.Black else Color.White,
        style = MaterialTheme.typography.labelSmall,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .width(labelWidth - 6.dp)
            .background(background, RoundedCornerShape(4.dp))
            .padding(vertical = 4.dp)
    )
}

private val ResistColor = Color(0xFF4CAF50)

private fun multiplierLabel(multiplier: Double): String = when (multiplier) {
    0.0 -> "0"
    0.25 -> "¼"
    0.5 -> "½"
    1.0 -> ""
    else -> "${multiplier.toInt()}×"
}

private fun typeColor(type: PokeType): Color = when (type) {
    PokeType.NORMAL -> Color(0xFFA8A77A)
    PokeType.FIRE -> Color(0xFFEE8130)
    PokeType.WATER -> Color(0xFF6390F0)
    PokeType.ELECTRIC -> Color(0xFFF7D02C)
    PokeType.GRASS -> Color(0xFF7AC74C)
    PokeType.ICE -> Color(0xFF96D9D6)
    PokeType.FIGHTING -> Color(0xFFC22E28)
    PokeType.POISON -> Color(0xFFA33EA1)
    PokeType.GROUND -> Color(0xFFE2BF65)
    PokeType.FLYING -> Color(0xFFA98FF3)
    PokeType.PSYCHIC -> Color(0xFFF95587)
    PokeType.BUG -> Color(0xFFA6B91A)
    PokeType.ROCK -> Color(0xFFB6A136)
    PokeType.GHOST -> Color(0xFF735797)
    PokeType.DRAGON -> Color(0xFF6F35FC)
    PokeType.DARK -> Color(0xFF705746)
    PokeType.STEEL -> Color(0xFFB7B7CE)
    PokeType.FAIRY -> Color(0xFFD685AD)
}
