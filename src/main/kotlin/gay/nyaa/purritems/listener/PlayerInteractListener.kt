package gay.nyaa.purritems.listener

import gay.nyaa.purritems.ItemManager
import gay.nyaa.purritems.abilities.AbilityContext
import gay.nyaa.purritems.abilities.AbilityTrigger
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEvent

/**
 * Handles player interactions for abilities.
 */
class PlayerInteractListener(private val itemManager: ItemManager) : Listener {
    @EventHandler(priority = EventPriority.NORMAL)
    fun onPlayerInteract(event: PlayerInteractEvent) {
        val player = event.player
        val item = event.item ?: return

        // Check requirements
        if (!itemManager.canUseItem(player, item)) {
            event.isCancelled = true
            return
        }

        val definition = itemManager.itemSerializer.getDefinition(item) ?: return
        if (definition.abilities.isEmpty()) return

        val trigger =
            when (event.action) {
                org.bukkit.event.block.Action.RIGHT_CLICK_AIR,
                org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK,
                -> AbilityTrigger.RIGHT_CLICK
                org.bukkit.event.block.Action.LEFT_CLICK_AIR,
                org.bukkit.event.block.Action.LEFT_CLICK_BLOCK,
                -> AbilityTrigger.LEFT_CLICK
                else -> return
            }

        val context =
            AbilityContext(
                player = player,
                trigger = trigger,
                targetBlock = event.clickedBlock,
            )

        itemManager.abilityExecutor.executeAll(definition.abilities, context)
    }
}
