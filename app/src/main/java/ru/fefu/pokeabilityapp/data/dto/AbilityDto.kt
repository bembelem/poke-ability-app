package ru.fefu.pokeabilityapp.data.dto

import com.google.gson.annotations.SerializedName

data class AbilityDto(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("is_main_series") val isMainSeries: Boolean,
    @SerializedName("generation") val generation: NamedResourceDto,
    @SerializedName("effect_entries") val effectEntries: List<EffectEntryDto>,
    @SerializedName("flavor_text_entries") val flavorTextEntries: List<FlavorTextEntryDto>,
    @SerializedName("pokemon") val pokemon: List<AbilityPokemonDto>
)

data class NamedResourceDto(
    @SerializedName("name") val name: String,
    @SerializedName("url") val url: String
)

data class EffectEntryDto(
    @SerializedName("effect") val effect: String,
    @SerializedName("short_effect") val shortEffect: String,
    @SerializedName("language") val language: NamedResourceDto
)

data class FlavorTextEntryDto(
    @SerializedName("flavor_text") val flavorText: String,
    @SerializedName("language") val language: NamedResourceDto,
    @SerializedName("version_group") val versionGroup: NamedResourceDto
)

data class AbilityPokemonDto(
    @SerializedName("is_hidden") val isHidden: Boolean,
    @SerializedName("slot") val slot: Int,
    @SerializedName("pokemon") val pokemon: NamedResourceDto
)
