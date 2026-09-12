package gay.nyaa.purritems.abilities.impl

import gay.nyaa.purritems.abilities.Ability
import gay.nyaa.purritems.abilities.AbilityContext
import gay.nyaa.purritems.abilities.AbilityTrigger
import org.bukkit.entity.LivingEntity

/**
 * Fire Strike ability - sets target on fire.
 */
class FireStrikeAbility : Ability {
    override val id: String = "fire-strike"
    override val trigger: AbilityTrigger = AbilityTrigger.ENTITY_HIT
    override val cooldownSeconds: Int = 5

    override fun execute(context: AbilityContext): Boolean {
        val target = context.targetEntity as? LivingEntity ?: return false
        target.fireTicks = 100 // 5 seconds (20 ticks/sec)
        return true
    }
}
