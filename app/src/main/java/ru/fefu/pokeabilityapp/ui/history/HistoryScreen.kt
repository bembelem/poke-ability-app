package ru.fefu.pokeabilityapp.ui.history

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ru.fefu.pokeabilityapp.domain.model.HistoryEntry
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    entries: List<HistoryEntry>,
    onEntryClick: (Int) -> Unit,
    onClear: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("История") },
                actions = {
                    if (entries.isNotEmpty()) {
                        IconButton(onClick = onClear) {
                            Icon(Icons.Default.Delete, contentDescription = "Очистить")
                        }
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            if (entries.isEmpty()) {
                Text(
                    text = "Пока ничего не просмотрено",
                    textAlign = TextAlign.Center,
                    modifier = Modifier.align(Alignment.Center).padding(16.dp)
                )
            } else {
                LazyColumn {
                    items(items = entries, key = { it.abilityId }) { entry ->
                        HistoryRow(entry = entry, onClick = { onEntryClick(entry.abilityId) })
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}

@Composable
private fun HistoryRow(entry: HistoryEntry, onClick: () -> Unit) {
    ListItem(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        headlineContent = {
            Text(entry.abilityName.replaceFirstChar { it.uppercase() })
        },
        supportingContent = { Text(formatViewedAt(entry.viewedAt)) }
    )
}

private fun formatViewedAt(timestamp: Long): String =
    SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()).format(Date(timestamp))
