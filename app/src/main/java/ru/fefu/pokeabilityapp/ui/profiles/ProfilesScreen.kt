package ru.fefu.pokeabilityapp.ui.profiles

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import ru.fefu.pokeabilityapp.ui.common.NameDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfilesScreen(
    profiles: List<ProfileUi>,
    onSelect: (Long) -> Unit,
    onCreate: (String) -> Unit,
    onDelete: (Long) -> Unit,
    onBack: () -> Unit
) {
    var showCreate by remember { mutableStateOf(false) }
    var toDelete by remember { mutableStateOf<ProfileUi?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Профили") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showCreate = true }) {
                Icon(Icons.Default.Add, contentDescription = "Создать профиль")
            }
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding).fillMaxSize()) {
            items(items = profiles, key = { it.id }) { profile ->
                ListItem(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelect(profile.id) },
                    leadingContent = {
                        RadioButton(selected = profile.isActive, onClick = { onSelect(profile.id) })
                    },
                    headlineContent = { Text(profile.name) },
                    trailingContent = {
                        if (profiles.size > 1) {
                            IconButton(onClick = { toDelete = profile }) {
                                Icon(Icons.Default.Delete, contentDescription = "Удалить")
                            }
                        }
                    }
                )
                HorizontalDivider()
            }
        }
    }

    if (showCreate) {
        NameDialog(
            title = "Новый профиль",
            onDismiss = { showCreate = false },
            onConfirm = { name ->
                showCreate = false
                onCreate(name)
            }
        )
    }

    toDelete?.let { profile ->
        AlertDialog(
            onDismissRequest = { toDelete = null },
            title = { Text("Удалить профиль «${profile.name}»?") },
            text = { Text("Вместе с ним удалятся его команды, теги, избранное и история") },
            confirmButton = {
                TextButton(onClick = {
                    toDelete = null
                    onDelete(profile.id)
                }) {
                    Text("Удалить")
                }
            },
            dismissButton = {
                TextButton(onClick = { toDelete = null }) { Text("Отмена") }
            }
        )
    }
}
