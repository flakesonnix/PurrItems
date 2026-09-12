package gay.nyaa.purritems.listener

import gay.nyaa.purritems.ItemManager
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.player.PlayerItemHeldEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent

/**
 * Handles player lifecycle and inventory changes for stat refresh.
 */
class PlayerLifecycleListener(private val itemManager: ItemManager) : Listener {
    @EventHandler(priority = EventPriority.MONITOR)
    fun onPlayerJoin(event: PlayerJoinEvent) {
        itemManager.refreshPlayerStats(event.player)
    }

    @EventHandler(priority = EventPriority.MONITOR)
    fun onPlayerQuit(event: PlayerQuitEvent) {
        itemManager.statsProvider.clearPlayerStats(event.player.uniqueId)
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onItemHeld(event: PlayerItemHeldEvent) {
        // Refresh stats on next tick (after item change)
        org.bukkit.Bukkit.getScheduler().runTask(
            itemManager.registry.javaClass.classLoader.let {
                org.bukkit.Bukkit.getPluginManager().plugins.first { p ->
                    p.javaClass.classLoader == it
                }
            },
            Runnable {
                itemManager.refreshPlayerStats(event.player)
            },
        )
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onInventoryClick(event: InventoryClickEvent) {
        val player = event.whoClicked as? org.bukkit.entity.Player ?: return
        // Refresh stats on next tick (after inventory change)
        org.bukkit.Bukkit.getScheduler().runTask(
            itemManager.registry.javaClass.classLoader.let {
                org.bukkit.Bukkit.getPluginManager().plugins.first { p ->
                    p.javaClass.classLoader == it
                }
            },
            Runnable {
                itemManager.refreshPlayerStats(player)
            },
        )
    }
}
