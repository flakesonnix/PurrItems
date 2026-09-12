package gay.nyaa.purritems.listener

import gay.nyaa.purritems.ItemManager
import gay.nyaa.purritems.abilities.AbilityContext
import gay.nyaa.purritems.abilities.AbilityTrigger
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent

/**
 * Handles block break abilities.
 */
class BlockBreakListener(private val itemManager: ItemManager) : Listener {
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    fun onBlockBreak(event: BlockBreakEvent) {
        val player = event.player
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
                trigger = AbilityTrigger.BLOCK_BREAK,
                targetBlock = event.block,
            )

        itemManager.abilityExecutor.executeAll(definition.abilities, context)
    }
}
