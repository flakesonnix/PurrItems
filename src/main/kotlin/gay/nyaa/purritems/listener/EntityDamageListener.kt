package gay.nyaa.purritems.listener

import gay.nyaa.purritems.ItemManager
import gay.nyaa.purritems.abilities.AbilityContext
import gay.nyaa.purritems.abilities.AbilityTrigger
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent

/**
 * Handles entity damage for abilities.
 */
class EntityDamageListener(private val itemManager: ItemManager) : Listener {
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    fun onEntityDamage(event: EntityDamageByEntityEvent) {
        val player = event.damager as? Player ?: return
        val item = player.inventory.itemInMainHand

        // Check requirements
        if (!itemManager.canUseItem(player, item)) {
            return
        }

        val definition = itemManager.itemSerializer.getDefinition(item) ?: return
        if (definition.abilities.isEmpty()) return

        val context =
            AbilityContext(
                player = player,
                trigger = AbilityTrigger.ENTITY_HIT,
                targetEntity = event.entity,
            )

        itemManager.abilityExecutor.executeAll(definition.abilities, context)
    }
}
