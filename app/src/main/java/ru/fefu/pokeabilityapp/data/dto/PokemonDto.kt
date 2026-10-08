package ru.fefu.pokeabilityapp.data.dto

import com.google.gson.annotations.SerializedName

data class PokemonListDto(
    @SerializedName("count") val count: Int,
    @SerializedName("results") val results: List<NamedResourceDto>
)

data class PokemonDto(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("sprites") val sprites: SpritesDto,
    @SerializedName("types") val types: List<PokemonTypeSlotDto>,
    @SerializedName("abilities") val abilities: List<PokemonAbilitySlotDto>
)

data class SpritesDto(
    @SerializedName("front_default") val frontDefault: String?
)

data class PokemonTypeSlotDto(
    @SerializedName("slot") val slot: Int,
    @SerializedName("type") val type: NamedResourceDto
)

data class PokemonAbilitySlotDto(
    @SerializedName("is_hidden") val isHidden: Boolean,
    @SerializedName("slot") val slot: Int,
    @SerializedName("ability") val ability: NamedResourceDto
)

data class TypeDto(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("damage_relations") val damageRelations: DamageRelationsDto
)

// отношения даны со стороны защищающегося типа - кто и как бьёт по нему
data class DamageRelationsDto(
    @SerializedName("double_damage_from") val doubleDamageFrom: List<NamedResourceDto>,
    @SerializedName("half_damage_from") val halfDamageFrom: List<NamedResourceDto>,
    @SerializedName("no_damage_from") val noDamageFrom: List<NamedResourceDto>
)
