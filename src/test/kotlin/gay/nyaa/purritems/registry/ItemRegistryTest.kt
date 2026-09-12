package gay.nyaa.purritems.registry

import gay.nyaa.purritems.domain.ItemDefinition
import gay.nyaa.purritems.domain.ItemId
import gay.nyaa.purritems.domain.Rarity
import org.bukkit.Material
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.*

class ItemRegistryTest {
    @Test
    fun `register and retrieve item`() {
        val registry = ItemRegistry()
        val id = ItemId("purr_items", "TEST_ITEM")
        val definition =
            ItemDefinition.builder(id, Material.DIAMOND, Rarity.COMMON, "Test Item")
                .build()

        registry.register(definition)

        val retrieved = registry.get(id)
        assertNotNull(retrieved)
        assertEquals(id, retrieved.id)
    }

    @Test
    fun `reject duplicate registration`() {
        val registry = ItemRegistry()
        val id = ItemId("purr_items", "TEST_ITEM")
        val definition =
            ItemDefinition.builder(id, Material.DIAMOND, Rarity.COMMON, "Test Item")
                .build()

        registry.register(definition)

        assertThrows<IllegalArgumentException> {
            registry.register(definition)
        }
    }

    @Test
    fun `contains check`() {
        val registry = ItemRegistry()
        val id = ItemId("purr_items", "TEST_ITEM")
        val definition =
            ItemDefinition.builder(id, Material.DIAMOND, Rarity.COMMON, "Test Item")
                .build()

        assertFalse(registry.contains(id))
        registry.register(definition)
        assertTrue(registry.contains(id))
    }

    @Test
    fun `return null for missing item`() {
        val registry = ItemRegistry()
        val id = ItemId("purr_items", "MISSING")
        assertNull(registry.get(id))
    }

    @Test
    fun `size tracking`() {
        val registry = ItemRegistry()
        assertEquals(0, registry.size())

        val id1 = ItemId("purr_items", "ITEM_1")
        val def1 =
            ItemDefinition.builder(id1, Material.DIAMOND, Rarity.COMMON, "Item 1")
                .build()
        registry.register(def1)
        assertEquals(1, registry.size())

        val id2 = ItemId("purr_items", "ITEM_2")
        val def2 =
            ItemDefinition.builder(id2, Material.DIAMOND, Rarity.RARE, "Item 2")
                .build()
        registry.register(def2)
        assertEquals(2, registry.size())
    }

    @Test
    fun `all items retrieval`() {
        val registry = ItemRegistry()
        val id1 = ItemId("purr_items", "ITEM_1")
        val def1 =
            ItemDefinition.builder(id1, Material.DIAMOND, Rarity.COMMON, "Item 1")
                .build()
        val id2 = ItemId("purr_items", "ITEM_2")
        val def2 =
            ItemDefinition.builder(id2, Material.DIAMOND, Rarity.RARE, "Item 2")
                .build()

        registry.register(def1)
        registry.register(def2)

        val all = registry.all()
        assertEquals(2, all.size)
        assertTrue(all.any { it.id == id1 })
        assertTrue(all.any { it.id == id2 })
    }
}
