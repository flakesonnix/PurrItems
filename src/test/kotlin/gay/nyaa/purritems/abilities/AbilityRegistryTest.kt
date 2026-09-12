package gay.nyaa.purritems.abilities

import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.*

class AbilityRegistryTest {
    @Test
    fun `register and retrieve ability`() {
        val registry = AbilityRegistry()
        val ability = mockk<Ability> {
            every { id } returns "test_ability"
        }

        registry.register(ability)

        val retrieved = registry.get("test_ability")
        assertNotNull(retrieved)
        assertEquals("test_ability", retrieved.id)
    }

    @Test
    fun `reject duplicate registration`() {
        val registry = AbilityRegistry()
        val ability1 = mockk<Ability> {
            every { id } returns "test_ability"
        }
        val ability2 = mockk<Ability> {
            every { id } returns "test_ability"
        }

        registry.register(ability1)

        assertThrows<IllegalArgumentException> {
            registry.register(ability2)
        }
    }

    @Test
    fun `contains check`() {
        val registry = AbilityRegistry()
        val ability = mockk<Ability> {
            every { id } returns "test_ability"
        }

        assertFalse(registry.contains("test_ability"))
        registry.register(ability)
        assertTrue(registry.contains("test_ability"))
    }

    @Test
    fun `return null for missing ability`() {
        val registry = AbilityRegistry()
        assertNull(registry.get("missing"))
    }

    @Test
    fun `all abilities retrieval`() {
        val registry = AbilityRegistry()
        val ability1 = mockk<Ability> {
            every { id } returns "ability_1"
        }
        val ability2 = mockk<Ability> {
            every { id } returns "ability_2"
        }

        registry.register(ability1)
        registry.register(ability2)

        val all = registry.all()
        assertEquals(2, all.size)
        assertTrue(all.any { it.id == "ability_1" })
        assertTrue(all.any { it.id == "ability_2" })
    }

    @Test
    fun `thread-safe registration`() {
        val registry = AbilityRegistry()
        val abilities = (1..100).map { i ->
            mockk<Ability> {
                every { id } returns "ability_$i"
            }
        }

        // Register abilities concurrently
        abilities.parallelStream().forEach { ability ->
            registry.register(ability)
        }

        assertEquals(100, registry.all().size)
    }
}
