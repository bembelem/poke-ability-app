package ru.fefu.pokeabilityapp.data.dto

import com.google.gson.annotations.SerializedName

data class AbilityListDto(
    @SerializedName("count") val count: Int,
    @SerializedName("next") val next: String?,
    @SerializedName("previous") val previous: String?,
    @SerializedName("results") val results: List<AbilityEntryDto>
)

data class AbilityEntryDto(
    @SerializedName("name") val name: String,
    @SerializedName("url") val url: String
)