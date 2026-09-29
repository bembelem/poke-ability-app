package ru.fefu.pokeabilityapp.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import ru.fefu.pokeabilityapp.domain.model.AbilityDetail
import ru.fefu.pokeabilityapp.domain.model.AbilityItem

const val POKEMON_SEPARATOR = "\n"

@Entity(tableName = "cached_abilities")
data class CachedAbilityEntity(
    @PrimaryKey val id: Int,
    val name: String,
    val listOrder: Int,
    val fetchedAt: Long,
    val generation: String? = null,
    val isMainSeries: Boolean? = null,
    val shortEffect: String? = null,
    val fullEffect: String? = null,
    val flavorText: String? = null,
    val pokemonNames: String? = null,
    // null = подробности ещё не загружались
    val detailFetchedAt: Long? = null
)

fun CachedAbilityEntity.toItem(): AbilityItem = AbilityItem(id, name)

fun CachedAbilityEntity.toDetailOrNull(): AbilityDetail? {
    if (detailFetchedAt == null) return null
    return AbilityDetail(
        id = id,
        name = name,
        generation = generation.orEmpty(),
        isMainSeries = isMainSeries ?: false,
        shortEffect = shortEffect.orEmpty(),
        fullEffect = fullEffect.orEmpty(),
        flavorText = flavorText.orEmpty(),
        pokemonList = pokemonNames?.split(POKEMON_SEPARATOR)?.filter { it.isNotBlank() }.orEmpty()
    )
}
