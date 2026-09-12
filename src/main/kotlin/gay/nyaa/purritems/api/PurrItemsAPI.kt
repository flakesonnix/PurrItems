package gay.nyaa.purritems.api

import gay.nyaa.purritems.domain.ItemDefinition
import gay.nyaa.purritems.domain.ItemId
import gay.nyaa.purrskills.stats.PlayerStats
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

/**
 * Public API for PurrItems.
 * Other plugins should depend only on this interface.
 */
interface PurrItemsAPI {
    /**
     * Get item definition by ID.
     * @return definition or null if not registered
     */
    fun getItem(id: ItemId): ItemDefinition?

    /**
     * Get item definition by string ID.
     * @return definition or null if invalid format or not registered
     */
    fun getItem(id: String): ItemDefinition?

    /**
     * Create ItemStack from item ID.
     * @return ItemStack or null if item not registered
     */
    fun createItem(
        id: ItemId,
        amount: Int = 1,
    ): ItemStack?

    /**
     * Create ItemStack from string ID.
     * @return ItemStack or null if invalid format or not registered
     */
    fun createItem(
        id: String,
        amount: Int = 1,
    ): ItemStack?

    /**
     * Check if ItemStack is a custom PurrItem.
     */
    fun isPurrItem(stack: ItemStack?): Boolean

    /**
     * Get item ID from ItemStack.
     * @return ItemId or null if not a PurrItem
     */
    fun getItemId(stack: ItemStack?): ItemId?

    /**
     * Get item definition from ItemStack.
     * @return ItemDefinition or null if not a PurrItem or not registered
     */
    fun getDefinition(stack: ItemStack?): ItemDefinition?

    /**
     * Check if player meets requirements for item.
     * Sends failure message to player if requirements not met.
     * @return true if player can use item
     */
    fun checkRequirements(
        player: Player,
        stack: ItemStack,
    ): Boolean

    /**
     * Calculate player stats including equipped items.
     * @return computed stats
     */
    fun calculatePlayerStats(player: Player): PlayerStats

    /**
     * Refresh player stats after equipment change.
     * Called automatically by PurrItems listeners.
     */
    fun refreshPlayerStats(player: Player)

    /**
     * Register custom item definition.
     * @throws IllegalArgumentException if ID already registered
     */
    fun registerItem(definition: ItemDefinition)

    /**
     * Get all registered item definitions.
     */
    fun getAllItems(): Collection<ItemDefinition>

    /**
     * Check if player has unlocked recipe for item.
     * Used by PurrCollections to gate crafting.
     * @return true if unlocked or no recipe requirement
     */
    fun hasUnlockedRecipe(
        player: Player,
        itemId: ItemId,
    ): Boolean
}
