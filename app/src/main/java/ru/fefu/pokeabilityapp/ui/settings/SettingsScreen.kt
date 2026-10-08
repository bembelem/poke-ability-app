package ru.fefu.pokeabilityapp.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import ru.fefu.pokeabilityapp.domain.model.AppSettings
import ru.fefu.pokeabilityapp.domain.model.ThemeMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    settings: AppSettings,
    activeProfileName: String,
    onProfilesClick: () -> Unit,
    onThemeChange: (ThemeMode) -> Unit,
    onCacheTtlChange: (Int) -> Unit,
    onAutoRefreshChange: (Boolean) -> Unit,
    onWifiOnlyChange: (Boolean) -> Unit,
    onHistoryEnabledChange: (Boolean) -> Unit,
    onHistoryRetentionChange: (Int) -> Unit
) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("Настройки") }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Профиль", style = MaterialTheme.typography.titleMedium)
                    Text(activeProfileName, style = MaterialTheme.typography.bodyMedium)
                }
                TextButton(onClick = onProfilesClick) { Text("Сменить") }
            }

            HorizontalDivider()

            Text("Тема", style = MaterialTheme.typography.titleMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ThemeMode.entries.forEach { mode ->
                    FilterChip(
                        selected = settings.themeMode == mode,
                        onClick = { onThemeChange(mode) },
                        label = { Text(themeLabel(mode)) }
                    )
                }
            }

            HorizontalDivider()

            ChoiceRow(
                title = "Срок жизни кэша",
                value = settings.cacheTtlHours,
                options = cacheTtlOptions,
                unit = "ч",
                onSelect = onCacheTtlChange
            )
            Text(
                text = "Через столько список считается устаревшим и обновляется из сети",
                style = MaterialTheme.typography.bodySmall
            )

            HorizontalDivider()

            SwitchRow(
                title = "Обновлять в фоне",
                checked = settings.autoRefreshEnabled,
                onCheckedChange = onAutoRefreshChange
            )
            SwitchRow(
                title = "Только по Wi-Fi",
                checked = settings.refreshOnWifiOnly,
                enabled = settings.autoRefreshEnabled,
                onCheckedChange = onWifiOnlyChange
            )
            Text(
                text = "Раз в несколько часов приложение обновляет устаревшие данные, " +
                    "даже когда закрыто",
                style = MaterialTheme.typography.bodySmall
            )

            HorizontalDivider()

            SwitchRow(
                title = "Вести историю просмотров",
                checked = settings.historyEnabled,
                onCheckedChange = onHistoryEnabledChange
            )
            ChoiceRow(
                title = "Хранить историю",
                value = settings.historyRetentionDays,
                options = historyRetentionOptions,
                unit = "дн.",
                enabled = settings.historyEnabled,
                onSelect = onHistoryRetentionChange
            )
        }
    }
}

private val cacheTtlOptions = listOf(
    1 to "1 час",
    6 to "6 часов",
    12 to "12 часов",
    24 to "1 день",
    72 to "3 дня"
)

private val historyRetentionOptions = listOf(
    1 to "1 день",
    7 to "1 неделя",
    30 to "1 месяц",
    90 to "3 месяца"
)

@Composable
private fun ChoiceRow(
    title: String,
    value: Int,
    options: List<Pair<Int, String>>,
    unit: String,
    onSelect: (Int) -> Unit,
    enabled: Boolean = true
) {
    var showDialog by remember { mutableStateOf(false) }
    // значение могло остаться от старого слайдера и не совпасть ни с одним вариантом
    val label = options.firstOrNull { it.first == value }?.second ?: "$value $unit"

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(if (enabled) 1f else 0.38f)
            .clickable(enabled = enabled) { showDialog = true }
            .padding(vertical = 4.dp)
    ) {
        Text(title, style = MaterialTheme.typography.titleMedium)
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary
        )
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(title) },
            text = {
                Column {
                    options.forEach { (optionValue, optionLabel) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .selectable(
                                    selected = optionValue == value,
                                    role = Role.RadioButton,
                                    onClick = {
                                        onSelect(optionValue)
                                        showDialog = false
                                    }
                                )
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(selected = optionValue == value, onClick = null)
                            Text(optionLabel, modifier = Modifier.padding(start = 16.dp))
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showDialog = false }) { Text("Отмена") }
            }
        )
    }
}

@Composable
private fun SwitchRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    enabled: Boolean = true
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, style = MaterialTheme.typography.titleMedium)
        Switch(checked = checked, onCheckedChange = onCheckedChange, enabled = enabled)
    }
}

private fun themeLabel(mode: ThemeMode): String = when (mode) {
    ThemeMode.SYSTEM -> "Системная"
    ThemeMode.LIGHT -> "Светлая"
    ThemeMode.DARK -> "Тёмная"
}
