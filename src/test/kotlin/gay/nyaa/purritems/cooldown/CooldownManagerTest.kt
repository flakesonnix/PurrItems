package gay.nyaa.purritems.cooldown

import org.junit.jupiter.api.Test
import java.util.UUID
import kotlin.test.*

class CooldownManagerTest {
    @Test
    fun `set and check cooldown`() {
        val manager = CooldownManager()
        val uuid = UUID.randomUUID()
        val key = "test_ability"

        assertFalse(manager.hasCooldown(uuid, key))

        manager.setCooldown(uuid, key, 5)
        assertTrue(manager.hasCooldown(uuid, key))

        val remaining = manager.getRemainingCooldown(uuid, key)
        assertTrue(remaining in 1..5)
    }

    @Test
    fun `expired cooldown returns zero`() {
        val manager = CooldownManager()
        val uuid = UUID.randomUUID()
        val key = "test_ability"

        manager.setCooldown(uuid, key, 0)
        Thread.sleep(100)

        assertEquals(0, manager.getRemainingCooldown(uuid, key))
        assertFalse(manager.hasCooldown(uuid, key))
    }

    @Test
    fun `clear specific cooldown`() {
        val manager = CooldownManager()
        val uuid = UUID.randomUUID()
        val key = "test_ability"

        manager.setCooldown(uuid, key, 10)
        assertTrue(manager.hasCooldown(uuid, key))

        manager.clearCooldown(uuid, key)
        assertFalse(manager.hasCooldown(uuid, key))
    }

    @Test
    fun `clear all cooldowns for player`() {
        val manager = CooldownManager()
        val uuid = UUID.randomUUID()

        manager.setCooldown(uuid, "ability_1", 10)
        manager.setCooldown(uuid, "ability_2", 10)

        assertTrue(manager.hasCooldown(uuid, "ability_1"))
        assertTrue(manager.hasCooldown(uuid, "ability_2"))

        manager.clearAllCooldowns(uuid)

        assertFalse(manager.hasCooldown(uuid, "ability_1"))
        assertFalse(manager.hasCooldown(uuid, "ability_2"))
    }

    @Test
    fun `cleanup expired cooldowns`() {
        val manager = CooldownManager()
        val uuid = UUID.randomUUID()

        manager.setCooldown(uuid, "short", 0)
        manager.setCooldown(uuid, "long", 100)

        Thread.sleep(100)
        manager.cleanupExpired()

        assertFalse(manager.hasCooldown(uuid, "short"))
        assertTrue(manager.hasCooldown(uuid, "long"))
    }
}
