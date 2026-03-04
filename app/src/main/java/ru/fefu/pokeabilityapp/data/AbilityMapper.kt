package ru.fefu.pokeabilityapp.data

import ru.fefu.pokeabilityapp.data.dto.AbilityDto
import ru.fefu.pokeabilityapp.data.dto.AbilityEntryDto
import ru.fefu.pokeabilityapp.domain.model.AbilityDetail
import ru.fefu.pokeabilityapp.domain.model.AbilityItem

// Из url "https://pokeapi.co/api/v2/ability/3/" достаём id
fun AbilityEntryDto.toAbilityItem(): AbilityItem {
    val id = url.trimEnd('/').substringAfterLast('/').toInt()
    return AbilityItem(id = id, name = name)
}

fun AbilityDto.toAbilityDetail(): AbilityDetail {
    val enEffect = effectEntries.firstOrNull {it.language.name == "en"}
    val enFlavor = flavorTextEntries.firstOrNull {it.language.name == "en"}
    return AbilityDetail(
        id = id,
        name = name,
        generation = generation.name,
        isMainSeries = isMainSeries,
        shortEffect = enEffect?.shortEffect ?: "-",
        fullEffect = enEffect?.effect ?: "-",
        flavorText = enFlavor?.flavorText ?: "-",
        pokemonList = pokemon.map { it.pokemon.name },
    )
}
