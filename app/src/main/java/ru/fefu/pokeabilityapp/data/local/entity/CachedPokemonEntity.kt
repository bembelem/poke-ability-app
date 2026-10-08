package ru.fefu.pokeabilityapp.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(tableName = "cached_pokemon")
data class CachedPokemonEntity(
    @PrimaryKey val id: Int,
    val name: String,
    val listOrder: Int,
    val fetchedAt: Long,
    val spriteUrl: String? = null,
    val type1: String? = null,
    val type2: String? = null,
    // null = типы и способности ещё не загружались
    val detailFetchedAt: Long? = null
)

@Entity(
    tableName = "pokemon_abilities",
    primaryKeys = ["pokemonId", "abilityId"],
    foreignKeys = [
        ForeignKey(
            entity = CachedPokemonEntity::class,
            parentColumns = ["id"],
            childColumns = ["pokemonId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class PokemonAbilityEntity(
    val pokemonId: Int,
    val abilityId: Int,
    val abilityName: String,
    val isHidden: Boolean,
    val slot: Int
)

@Entity(tableName = "type_effectiveness", primaryKeys = ["attacking", "defending"])
data class TypeEffectivenessEntity(
    val attacking: String,
    val defending: String,
    val multiplier: Double,
    val fetchedAt: Long
)
