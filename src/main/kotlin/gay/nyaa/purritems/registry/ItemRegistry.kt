package gay.nyaa.purritems.registry

import gay.nyaa.purritems.domain.ItemDefinition
import gay.nyaa.purritems.domain.ItemId
import java.util.concurrent.ConcurrentHashMap

/**
 * Central registry for item definitions.
 * Thread-safe.
 */
class ItemRegistry {
    private val items = ConcurrentHashMap<ItemId, ItemDefinition>()

    /**
     * Register an item definition.
     * @throws IllegalArgumentException if ID already registered
     */
    fun register(definition: ItemDefinition) {
        val existing = items.putIfAbsent(definition.id, definition)
        require(existing == null) {
            "Item ${definition.id} already registered"
        }
    }

    /**
     * Get item definition by ID.
     * @return definition or null if not found
     */
    fun get(id: ItemId): ItemDefinition? = items[id]

    /**
     * Check if item ID is registered.
     */
    fun contains(id: ItemId): Boolean = items.containsKey(id)

    /**
     * Get all registered item definitions.
     */
    fun all(): Collection<ItemDefinition> = items.values.toList()

    /**
     * Get count of registered items.
     */
    fun size(): Int = items.size
}
