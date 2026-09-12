package gay.nyaa.purritems.stats

import gay.nyaa.purritems.serialization.ItemSerializer
import gay.nyaa.purrskills.stats.StatModifier
import gay.nyaa.purrskills.stats.StatSource
import gay.nyaa.purrskills.stats.StatsManager
import org.bukkit.entity.Player
import org.bukkit.inventory.EquipmentSlot
import java.util.UUID

/**
 * Provides item stats to PurrSkills StatsManager.
 */
class ItemStatsProvider(
    private val itemSerializer: ItemSerializer,
    private val statsManager: StatsManager,
) {
    /**
     * Refresh all item stats for player.
     * Removes old item modifiers and applies new ones.
     */
    fun refreshPlayerStats(player: Player) {
        refreshPlayerStats(player.uniqueId, player)
    }

    /**
     * Refresh stats for player UUID (can be called async with player object).
     */
    fun refreshPlayerStats(
        uuid: UUID,
        player: Player,
    ) {
        // Remove all existing item modifiers
        StatSource.entries
            .filter { it.name.startsWith("ITEM_") }
            .forEach { source ->
                statsManager.removeModifiersFromSource(uuid, source)
            }

        // Add modifiers from equipped items
        val equipment =
            mapOf(
                EquipmentSlot.HAND to StatSource.ITEM_MAIN_HAND,
                EquipmentSlot.OFF_HAND to StatSource.ITEM_OFF_HAND,
                EquipmentSlot.HEAD to StatSource.ITEM_HELMET,
                EquipmentSlot.CHEST to StatSource.ITEM_CHESTPLATE,
                EquipmentSlot.LEGS to StatSource.ITEM_LEGGINGS,
                EquipmentSlot.FEET to StatSource.ITEM_BOOTS,
            )

        equipment.forEach { (slot, source) ->
            val item = player.inventory.getItem(slot)
            val definition = itemSerializer.getDefinition(item)
            if (definition != null) {
                // Convert item stats to use item source
                val modifiers =
                    definition.stats.map { modifier ->
                        StatModifier(
                            stat = modifier.stat,
                            value = modifier.value,
                            type = modifier.type,
                            source = source,
                        )
                    }
                statsManager.addModifiers(uuid, modifiers)
            }
        }
    }

    /**
     * Clear all item stats for player (on logout).
     */
    fun clearPlayerStats(uuid: UUID) {
        StatSource.entries
            .filter { it.name.startsWith("ITEM_") }
            .forEach { source ->
                statsManager.removeModifiersFromSource(uuid, source)
            }
    }
}
