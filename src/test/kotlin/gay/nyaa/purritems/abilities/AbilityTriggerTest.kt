package gay.nyaa.purritems.abilities

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AbilityTriggerTest {
    @Test
    fun `all triggers are unique`() {
        val triggers = AbilityTrigger.entries
        assertEquals(triggers.size, triggers.distinct().size)
    }

    @Test
    fun `trigger count matches expected`() {
        assertEquals(6, AbilityTrigger.entries.size)
    }

    @Test
    fun `expected triggers exist`() {
        val triggers = AbilityTrigger.entries.map { it.name }
        assertTrue(triggers.contains("RIGHT_CLICK"))
        assertTrue(triggers.contains("LEFT_CLICK"))
        assertTrue(triggers.contains("BLOCK_BREAK"))
        assertTrue(triggers.contains("ENTITY_HIT"))
        assertTrue(triggers.contains("ENTITY_DAMAGE"))
        assertTrue(triggers.contains("PASSIVE"))
    }
}
