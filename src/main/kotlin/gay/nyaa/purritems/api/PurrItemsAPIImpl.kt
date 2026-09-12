package gay.nyaa.purritems.api

import gay.nyaa.purritems.ItemManager
import gay.nyaa.purritems.domain.ItemDefinition
import gay.nyaa.purritems.domain.ItemId
import gay.nyaa.purrskills.stats.PlayerStats
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

/**
 * Default implementation of PurrItemsAPI.
 * Delegates to ItemManager.
 */
internal class PurrItemsAPIImpl(private val itemManager: ItemManager) : PurrItemsAPI {
    override fun getItem(id: ItemId): ItemDefinition? = itemManager.getItem(id)

    override fun getItem(id: String): ItemDefinition? = try {
        itemManager.getItem(ItemId.parse(id))
    } catch (e: Exception) {
        null
    }

    override fun createItem(
        id: ItemId,
        amount: Int,
    ): ItemStack? = itemManager.createItem(id, amount)

    override fun createItem(
        id: String,
        amount: Int,
    ): ItemStack? = try {
        itemManager.createItem(ItemId.parse(id), amount)
    } catch (e: Exception) {
        null
    }

    override fun isPurrItem(stack: ItemStack?): Boolean = itemManager.itemSerializer.isCustomItem(stack)

    override fun getItemId(stack: ItemStack?): ItemId? = itemManager.itemSerializer.getItemId(stack)

    override fun getDefinition(stack: ItemStack?): ItemDefinition? = itemManager.itemSerializer.getDefinition(stack)

    override fun checkRequirements(
        player: Player,
        stack: ItemStack,
    ): Boolean = itemManager.canUseItem(player, stack)

    override fun calculatePlayerStats(player: Player): PlayerStats {
        itemManager.refreshPlayerStats(player)
        return itemManager.skillManager.calculateStats(player.uniqueId)
    }

    override fun refreshPlayerStats(player: Player) {
        itemManager.refreshPlayerStats(player)
    }

    override fun registerItem(definition: ItemDefinition) {
        itemManager.registerItem(definition)
    }

    override fun getAllItems(): Collection<ItemDefinition> = itemManager.getAllItems()

    override fun hasUnlockedRecipe(
        player: Player,
        itemId: ItemId,
    ): Boolean = itemManager.hasUnlockedRecipe(player, itemId)
}
