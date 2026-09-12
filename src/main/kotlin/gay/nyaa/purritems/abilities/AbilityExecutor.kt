package gay.nyaa.purritems.abilities

import com.purrcore.i18n.I18n
import gay.nyaa.purritems.cooldown.CooldownManager

/**
 * Executes abilities with cooldown checks.
 */
class AbilityExecutor(
    private val cooldownManager: CooldownManager,
    private val i18n: I18n,
) {
    /**
     * Execute ability if not on cooldown.
     * @return true if executed, false if on cooldown or execution failed
     */
    fun execute(
        ability: Ability,
        context: AbilityContext,
    ): Boolean {
        val player = context.player

        // Check cooldown
        if (ability.cooldownSeconds > 0) {
            val remaining = cooldownManager.getRemainingCooldown(player.uniqueId, ability.id)
            if (remaining > 0) {
                i18n.send(player, "ability.cooldown", "remaining" to remaining.toString())
                return false
            }
        }

        // Execute ability
        val success = ability.execute(context)

        // Start cooldown if execution succeeded
        if (success && ability.cooldownSeconds > 0) {
            cooldownManager.setCooldown(player.uniqueId, ability.id, ability.cooldownSeconds)
        }

        return success
    }

    /**
     * Execute all abilities matching trigger.
     */
    fun executeAll(
        abilities: List<Ability>,
        context: AbilityContext,
    ) {
        abilities
            .filter { it.trigger == context.trigger }
            .forEach { execute(it, context) }
    }
}
