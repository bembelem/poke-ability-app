package ru.fefu.pokeabilityapp.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import ru.fefu.pokeabilityapp.data.dto.AbilityEntryDto

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
}