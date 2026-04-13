package ru.fefu.pokeabilityapp.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import ru.fefu.pokeabilityapp.data.dto.*

class MappersTest {

    @Test
    fun `toAbilityItemOrNull returns item when url contains valid id`() {
        val entry = AbilityEntryDto(name = "overgrow", url = "https://pokeapi.co/api/v2/ability/65/")
        val result = entry.toAbilityItemOrNull()
        assertEquals(65, result?.id)
        assertEquals("overgrow", result?.name)
    }

    @Test
    fun `toAbilityItemOrNull returns null when url has no id`() {
        val entry = AbilityEntryDto(name = "overgrow", url = "https://pokeapi.co/api/v2/ability/")
        val result = entry.toAbilityItemOrNull()
        assertNull(result)
    }

    @Test
    fun `toAbilityDetail uses fallback when no english entry`() {
        val dto = AbilityDto(
            id = 1,
            name = "overgrow",
            isMainSeries = true,
            generation = NamedResourceDto("generation-i", ""),
            effectEntries = emptyList(),
            flavorTextEntries = emptyList(),
            pokemon = emptyList()
        )

        val result = dto.toAbilityDetail()

        assertEquals("-", result.shortEffect)
        assertEquals("-", result.fullEffect)
        assertEquals("-", result.flavorText)
    }
}