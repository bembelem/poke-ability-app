package ru.fefu.pokeabilityapp.data

import ru.fefu.pokeabilityapp.data.dto.AbilityDto
import ru.fefu.pokeabilityapp.data.dto.AbilityEntryDto
import ru.fefu.pokeabilityapp.data.local.CachedAbilityEntity
import ru.fefu.pokeabilityapp.data.local.NOT_IN_LIST
import ru.fefu.pokeabilityapp.data.local.POKEMON_SEPARATOR
import ru.fefu.pokeabilityapp.domain.model.AbilityDetail
import ru.fefu.pokeabilityapp.domain.model.AbilityItem

// Из url "https://pokeapi.co/api/v2/ability/3/" достаём id
fun AbilityEntryDto.toAbilityItemOrNull(): AbilityItem? {
    val id = url.trimEnd('/').substringAfterLast('/').toIntOrNull() ?: return null
    return AbilityItem(id = id, name = name)
}

fun AbilityEntryDto.toCachedOrNull(listOrder: Int, fetchedAt: Long): CachedAbilityEntity? {
    val id = url.trimEnd('/').substringAfterLast('/').toIntOrNull() ?: return null
    return CachedAbilityEntity(
        id = id,
        name = name,
        listOrder = listOrder,
        fetchedAt = fetchedAt
    )
}

fun AbilityDto.toCached(existing: CachedAbilityEntity?, now: Long): CachedAbilityEntity {
    val enEffect = effectEntries.firstOrNull { it.language.name == "en" }
    val enFlavor = flavorTextEntries.lastOrNull { it.language.name == "en" }
    val base = existing ?: CachedAbilityEntity(
        id = id,
        name = name,
        listOrder = NOT_IN_LIST,
        fetchedAt = now
    )
    return base.copy(
        name = name,
        generation = generation.name,
        isMainSeries = isMainSeries,
        shortEffect = enEffect?.shortEffect ?: "-",
        fullEffect = enEffect?.effect ?: "-",
        flavorText = enFlavor?.flavorText ?: "-",
        pokemonNames = pokemon.joinToString(POKEMON_SEPARATOR) { it.pokemon.name },
        detailFetchedAt = now
    )
}

fun AbilityDto.toAbilityDetail(): AbilityDetail {
    val enEffect = effectEntries.firstOrNull {it.language.name == "en"}
    val enFlavor = flavorTextEntries.lastOrNull { it.language.name == "en" }
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
