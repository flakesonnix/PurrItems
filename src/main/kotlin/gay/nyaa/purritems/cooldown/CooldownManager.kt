package gay.nyaa.purritems.cooldown

import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

/**
 * Manages per-player cooldowns.
 */
class CooldownManager {
    // UUID -> (cooldownKey -> expiryTimeMillis)
    private val cooldowns = ConcurrentHashMap<UUID, MutableMap<String, Long>>()

    /**
     * Set cooldown for player.
     * @param seconds cooldown duration
     */
    fun setCooldown(
        uuid: UUID,
        key: String,
        seconds: Int,
    ) {
        val expiryTime = System.currentTimeMillis() + (seconds * 1000L)
        cooldowns.computeIfAbsent(uuid) { ConcurrentHashMap() }[key] = expiryTime
    }

    /**
     * Get remaining cooldown in seconds.
     * @return remaining seconds, or 0 if no cooldown
     */
    fun getRemainingCooldown(
        uuid: UUID,
        key: String,
    ): Int {
        val playerCooldowns = cooldowns[uuid] ?: return 0
        val expiryTime = playerCooldowns[key] ?: return 0
        val remaining = ((expiryTime - System.currentTimeMillis()) / 1000.0).toInt()
        return if (remaining > 0) remaining else 0
    }

    /**
     * Check if cooldown is active.
     */
    fun hasCooldown(
        uuid: UUID,
        key: String,
    ): Boolean = getRemainingCooldown(uuid, key) > 0

    /**
     * Clear specific cooldown.
     */
    fun clearCooldown(
        uuid: UUID,
        key: String,
    ) {
        cooldowns[uuid]?.remove(key)
    }

    /**
     * Clear all cooldowns for player (on logout).
     */
    fun clearAllCooldowns(uuid: UUID) {
        cooldowns.remove(uuid)
    }

    /**
     * Cleanup expired cooldowns (called periodically).
     */
    fun cleanupExpired() {
        val now = System.currentTimeMillis()
        cooldowns.values.forEach { playerCooldowns ->
            playerCooldowns.entries.removeIf { (_, expiryTime) ->
                expiryTime <= now
            }
        }
        cooldowns.entries.removeIf { (_, playerCooldowns) ->
            playerCooldowns.isEmpty()
        }
    }
}
