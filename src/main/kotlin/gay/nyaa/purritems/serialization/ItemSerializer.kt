package gay.nyaa.purritems.serialization

import gay.nyaa.purritems.domain.ItemDefinition
import gay.nyaa.purritems.domain.ItemId
import gay.nyaa.purritems.registry.ItemRegistry
import gay.nyaa.purritems.rendering.LoreRenderer
import org.bukkit.NamespacedKey
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import org.bukkit.plugin.Plugin

/**
 * Serializes/deserializes custom items to/from ItemStack using PDC.
 */
class ItemSerializer(
    private val plugin: Plugin,
    private val registry: ItemRegistry,
    private val loreRenderer: LoreRenderer,
) {
    private val itemIdKey = NamespacedKey(plugin, "item_id")

    /**
     * Create ItemStack from definition.
     */
    fun createItemStack(
        definition: ItemDefinition,
        amount: Int = 1,
    ): ItemStack {
        val stack = ItemStack(definition.material, amount)
        val meta = stack.itemMeta ?: return stack

        // Store item ID in PDC
        meta.persistentDataContainer.set(itemIdKey, PersistentDataType.STRING, definition.id.toString())

        // Apply rendered lore and display name
        val rendered = loreRenderer.render(definition)
        meta.displayName(rendered.displayName)
        meta.lore(rendered.lore)

        stack.itemMeta = meta
        return stack
    }

    /**
     * Get item ID from ItemStack.
     * @return ItemId or null if not a custom item
     */
    fun getItemId(stack: ItemStack?): ItemId? {
        if (stack == null || !stack.hasItemMeta()) return null
        val meta = stack.itemMeta ?: return null
        val idStr = meta.persistentDataContainer.get(itemIdKey, PersistentDataType.STRING) ?: return null
        return try {
            ItemId.parse(idStr)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Get item definition from ItemStack.
     * @return ItemDefinition or null if not a custom item or not registered
     */
    fun getDefinition(stack: ItemStack?): ItemDefinition? {
        val id = getItemId(stack) ?: return null
        return registry.get(id)
    }

    /**
     * Check if ItemStack is a custom item.
     */
    fun isCustomItem(stack: ItemStack?): Boolean = getItemId(stack) != null

    /**
     * Refresh lore on existing item (useful after definition changes).
     */
    fun refreshLore(stack: ItemStack): ItemStack {
        val definition = getDefinition(stack) ?: return stack
        val meta = stack.itemMeta ?: return stack

        val rendered = loreRenderer.render(definition)
        meta.displayName(rendered.displayName)
        meta.lore(rendered.lore)

        stack.itemMeta = meta
        return stack
    }
}
