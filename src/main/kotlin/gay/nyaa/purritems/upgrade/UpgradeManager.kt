package gay.nyaa.purritems.upgrade

import gay.nyaa.purritems.domain.ItemId
import gay.nyaa.purritems.registry.ItemRegistry
import gay.nyaa.purritems.serialization.ItemSerializer
import org.bukkit.inventory.ItemStack

/**
 * Handles item upgrades.
 */
class UpgradeManager(
    private val registry: ItemRegistry,
    private val itemSerializer: ItemSerializer,
) {
    /**
     * Check if item can be upgraded.
     */
    fun canUpgrade(stack: ItemStack): Boolean {
        val definition = itemSerializer.getDefinition(stack) ?: return false
        return definition.upgradesTo != null && registry.contains(definition.upgradesTo)
    }

    /**
     * Get upgrade target for item.
     */
    fun getUpgradeTarget(stack: ItemStack): ItemId? {
        val definition = itemSerializer.getDefinition(stack) ?: return null
        return definition.upgradesTo
    }

    /**
     * Upgrade item to next tier.
     * @return upgraded ItemStack or null if can't upgrade
     */
    fun upgrade(stack: ItemStack): ItemStack? {
        val definition = itemSerializer.getDefinition(stack) ?: return null
        val upgradeToId = definition.upgradesTo ?: return null
        val upgradedDefinition = registry.get(upgradeToId) ?: return null

        // Create new upgraded item
        return itemSerializer.createItemStack(upgradedDefinition, stack.amount)
    }
}
