package ru.fefu.pokeabilityapp.domain.coverage

import ru.fefu.pokeabilityapp.domain.model.PokeType
import ru.fefu.pokeabilityapp.domain.model.TypeChart

// В PokeAPI иммунитет от способности есть только в тексте эффекта
private val abilityImmunities = mapOf(
    "levitate" to PokeType.GROUND,
    "flash-fire" to PokeType.FIRE,
    "water-absorb" to PokeType.WATER,
    "storm-drain" to PokeType.WATER,
    "dry-skin" to PokeType.WATER,
    "volt-absorb" to PokeType.ELECTRIC,
    "motor-drive" to PokeType.ELECTRIC,
    "lightning-rod" to PokeType.ELECTRIC,
    "sap-sipper" to PokeType.GRASS,
)

data class TeamMember(
    val slotId: Long,
    val pokemonName: String,
    val types: List<PokeType>,
    val abilityName: String? = null,
)

data class MemberMultiplier(
    val slotId: Long,
    val pokemonName: String,
    val multiplier: Double,
)

data class TypeExposure(
    val type: PokeType,
    val perMember: List<MemberMultiplier>,
) {
    val weakMembers: List<String>
        get() = perMember.filter { it.multiplier > 1.0 }.map { it.pokemonName }

    val resistantMembers: List<String>
        get() = perMember.filter { it.multiplier < 1.0 }.map { it.pokemonName }

    val maxMultiplier: Double
        get() = perMember.maxOfOrNull { it.multiplier } ?: 0.0
}

data class TeamCoverage(
    val exposures: List<TypeExposure>,
    val threats: List<TypeExposure>,
)

fun immunityOf(abilityName: String?): PokeType? {
    if (abilityName == null) return null
    return abilityImmunities[abilityName.lowercase()]
}

fun defensiveMultiplier(
    attacking: PokeType,
    defenderTypes: List<PokeType>,
    abilityName: String?,
    chart: TypeChart,
): Double {
    if (immunityOf(abilityName) == attacking) return 0.0
    return defenderTypes.distinct()
        .fold(1.0) { acc, type -> acc * chart.multiplier(attacking, type) }
}

fun analyzeTeam(
    members: List<TeamMember>,
    chart: TypeChart,
    threatThreshold: Int,
): TeamCoverage {
    val threshold = threatThreshold.coerceAtLeast(1)

    val exposures = PokeType.entries.map { attacking ->
        TypeExposure(
            type = attacking,
            perMember = members.map { member ->
                MemberMultiplier(
                    slotId = member.slotId,
                    pokemonName = member.pokemonName,
                    multiplier = defensiveMultiplier(
                        attacking,
                        member.types,
                        member.abilityName,
                        chart
                    ),
                )
            },
        )
    }

    val threats = exposures
        .filter { it.weakMembers.size >= threshold }
        .sortedWith(
            compareByDescending<TypeExposure> { it.weakMembers.size }
                .thenByDescending { it.maxMultiplier }
        )

    return TeamCoverage(exposures = exposures, threats = threats)
}
