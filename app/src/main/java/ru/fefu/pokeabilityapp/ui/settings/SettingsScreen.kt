package ru.fefu.pokeabilityapp.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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

            Text("Срок жизни кэша", style = MaterialTheme.typography.titleMedium)
            Text(
                text = "${settings.cacheTtlHours} ч",
                style = MaterialTheme.typography.bodyMedium
            )
            Slider(
                value = settings.cacheTtlHours.toFloat(),
                onValueChange = { onCacheTtlChange(it.toInt()) },
                valueRange = 1f..72f,
                steps = 70
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
            Text(
                text = "Хранить историю: ${settings.historyRetentionDays} дн.",
                style = MaterialTheme.typography.bodyMedium
            )
            Slider(
                value = settings.historyRetentionDays.toFloat(),
                onValueChange = { onHistoryRetentionChange(it.toInt()) },
                valueRange = 1f..90f,
                steps = 88
            )
        }
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
