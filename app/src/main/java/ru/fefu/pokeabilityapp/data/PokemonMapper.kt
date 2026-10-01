package ru.fefu.pokeabilityapp.data

import ru.fefu.pokeabilityapp.data.dto.NamedResourceDto
import ru.fefu.pokeabilityapp.data.dto.PokemonDto
import ru.fefu.pokeabilityapp.data.dto.TypeDto
import ru.fefu.pokeabilityapp.data.local.CachedPokemonEntity
import ru.fefu.pokeabilityapp.data.local.NOT_IN_LIST
import ru.fefu.pokeabilityapp.data.local.PokemonAbilityEntity
import ru.fefu.pokeabilityapp.data.local.TypeEffectivenessEntity
import ru.fefu.pokeabilityapp.domain.model.PokeType
import ru.fefu.pokeabilityapp.domain.model.PokemonAbilityOption
import ru.fefu.pokeabilityapp.domain.model.PokemonDetail
import ru.fefu.pokeabilityapp.domain.model.PokemonItem

fun NamedResourceDto.idOrNull(): Int? =
    url.trimEnd('/').substringAfterLast('/').toIntOrNull()

fun NamedResourceDto.toCachedPokemonOrNull(listOrder: Int, fetchedAt: Long): CachedPokemonEntity? {
    val id = idOrNull() ?: return null
    return CachedPokemonEntity(id = id, name = name, listOrder = listOrder, fetchedAt = fetchedAt)
}

fun PokemonDto.toCached(existing: CachedPokemonEntity?, now: Long): CachedPokemonEntity {
    val ordered = types.sortedBy { it.slot }
    val base = existing ?: CachedPokemonEntity(
        id = id,
        name = name,
        listOrder = NOT_IN_LIST,
        fetchedAt = now
    )
    return base.copy(
        name = name,
        spriteUrl = sprites.frontDefault,
        type1 = ordered.getOrNull(0)?.type?.name,
        type2 = ordered.getOrNull(1)?.type?.name,
        detailFetchedAt = now
    )
}

fun PokemonDto.toAbilityEntities(): List<PokemonAbilityEntity> =
    abilities.mapNotNull { entry ->
        val abilityId = entry.ability.idOrNull() ?: return@mapNotNull null
        PokemonAbilityEntity(
            pokemonId = id,
            abilityId = abilityId,
            abilityName = entry.ability.name,
            isHidden = entry.isHidden,
            slot = entry.slot
        )
    }

fun CachedPokemonEntity.toItem(): PokemonItem = PokemonItem(id, name, spriteUrl)

fun CachedPokemonEntity.toDetail(abilities: List<PokemonAbilityEntity>): PokemonDetail =
    PokemonDetail(
        id = id,
        name = name,
        spriteUrl = spriteUrl,
        types = listOfNotNull(type1, type2).mapNotNull { PokeType.fromApiName(it) },
        abilities = abilities.map {
            PokemonAbilityOption(it.abilityId, it.abilityName, it.isHidden)
        }
    )

fun TypeDto.toRelations(now: Long): List<TypeEffectivenessEntity> {
    val relations = damageRelations.doubleDamageFrom.map { it.name to 2.0 } +
        damageRelations.halfDamageFrom.map { it.name to 0.5 } +
        damageRelations.noDamageFrom.map { it.name to 0.0 }
    return relations.map { (attacking, multiplier) ->
        TypeEffectivenessEntity(
            attacking = attacking,
            defending = name,
            multiplier = multiplier,
            fetchedAt = now
        )
    }
}
