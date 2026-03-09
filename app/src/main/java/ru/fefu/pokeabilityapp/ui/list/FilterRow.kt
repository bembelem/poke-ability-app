package ru.fefu.pokeabilityapp.ui.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import ru.fefu.pokeabilityapp.domain.model.AbilityFilter

@Composable
fun FilterRow(
    filter: AbilityFilter,
    onFilterChange: (AbilityFilter) -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        TextButton(
            onClick = { onFilterChange(AbilityFilter.ALL) },
            enabled = filter != AbilityFilter.ALL
        ) { Text("All") }

        TextButton(
            onClick = { onFilterChange(AbilityFilter.FAVOURITES) },
            enabled = filter != AbilityFilter.FAVOURITES
        ) { Text("Favourites") }
    }
}