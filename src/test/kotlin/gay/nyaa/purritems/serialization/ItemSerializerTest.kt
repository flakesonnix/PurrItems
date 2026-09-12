package gay.nyaa.purritems.serialization

import gay.nyaa.purritems.domain.ItemDefinition
import gay.nyaa.purritems.domain.ItemId
import gay.nyaa.purritems.domain.Rarity
import gay.nyaa.purritems.registry.ItemRegistry
import gay.nyaa.purritems.rendering.LoreRenderer
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataType
import org.bukkit.plugin.Plugin
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.*

class ItemSerializerTest {
    private lateinit var plugin: Plugin
    private lateinit var registry: ItemRegistry
    private lateinit var loreRenderer: LoreRenderer
    private lateinit var serializer: ItemSerializer

    @BeforeEach
    fun setup() {
        plugin = mockk(relaxed = true) {
            every { name } returns "PurrItems"
        }
        registry = ItemRegistry()
        loreRenderer = mockk(relaxed = true)
        serializer = ItemSerializer(plugin, registry, loreRenderer)
    }

    @Test
    fun `createItemStack stores item ID in PDC`() {
        val id = ItemId("purr_items", "TEST_ITEM")
        val definition = ItemDefinition.builder(id, Material.DIAMOND, Rarity.COMMON, "Test Item").build()

        val pdc = mockk<PersistentDataContainer>(relaxed = true)
        val meta = mockk<ItemMeta>(relaxed = true) {
            every { persistentDataContainer } returns pdc
        }

        every { loreRenderer.render(definition) } returns LoreRenderer.RenderedItem(
            displayName = Component.text("Test Item"),
            lore = emptyList(),
        )

        // Mock ItemStack creation (real constructor needs a booted server registry)
        val stack = mockk<ItemStack>(relaxed = true) {
            every { itemMeta } returns meta
            every { amount } returns 1
            every { type } returns Material.DIAMOND
        }
        val injectable = ItemSerializer(plugin, registry, loreRenderer) { _, _ -> stack }

        val key = NamespacedKey(plugin, "item_id")
        val result = injectable.createItemStack(definition, 1)

        assertNotNull(result)
        assertEquals(Material.DIAMOND, result.type)
        assertEquals(1, result.amount)
        verify { pdc.set(key, PersistentDataType.STRING, "purr_items:TEST_ITEM") }
    }

    @Test
    fun `getItemId returns null for non-custom item`() {
        val stack = mockk<ItemStack> {
            every { hasItemMeta() } returns false
        }

        val result = serializer.getItemId(stack)

        assertNull(result)
    }

    @Test
    fun `getItemId returns null for null stack`() {
        val result = serializer.getItemId(null)

        assertNull(result)
    }

    @Test
    fun `getItemId parses ID from PDC`() {
        val key = NamespacedKey(plugin, "item_id")
        val pdc = mockk<PersistentDataContainer> {
            every { get(key, PersistentDataType.STRING) } returns "purr_items:TEST_ITEM"
        }
        val meta = mockk<ItemMeta> {
            every { persistentDataContainer } returns pdc
        }
        val stack = mockk<ItemStack> {
            every { hasItemMeta() } returns true
            every { itemMeta } returns meta
        }

        val result = serializer.getItemId(stack)

        assertNotNull(result)
        assertEquals("purr_items", result.namespace)
        assertEquals("TEST_ITEM", result.key)
    }

    @Test
    fun `getItemId handles invalid ID format`() {
        val key = NamespacedKey(plugin, "item_id")
        val pdc = mockk<PersistentDataContainer> {
            every { get(key, PersistentDataType.STRING) } returns "invalid_format"
        }
        val meta = mockk<ItemMeta> {
            every { persistentDataContainer } returns pdc
        }
        val stack = mockk<ItemStack> {
            every { hasItemMeta() } returns true
            every { itemMeta } returns meta
        }

        val result = serializer.getItemId(stack)

        assertNull(result)
    }

    @Test
    fun `getDefinition returns null for non-custom item`() {
        val stack = mockk<ItemStack> {
            every { hasItemMeta() } returns false
        }

        val result = serializer.getDefinition(stack)

        assertNull(result)
    }

    @Test
    fun `getDefinition retrieves from registry`() {
        val id = ItemId("purr_items", "TEST_ITEM")
        val definition = ItemDefinition.builder(id, Material.DIAMOND, Rarity.COMMON, "Test Item").build()
        registry.register(definition)

        val key = NamespacedKey(plugin, "item_id")
        val pdc = mockk<PersistentDataContainer> {
            every { get(key, PersistentDataType.STRING) } returns "purr_items:TEST_ITEM"
        }
        val meta = mockk<ItemMeta> {
            every { persistentDataContainer } returns pdc
        }
        val stack = mockk<ItemStack> {
            every { hasItemMeta() } returns true
            every { itemMeta } returns meta
        }

        val result = serializer.getDefinition(stack)

        assertNotNull(result)
        assertEquals(id, result.id)
    }

    @Test
    fun `getDefinition returns null for unregistered item`() {
        val key = NamespacedKey(plugin, "item_id")
        val pdc = mockk<PersistentDataContainer> {
            every { get(key, PersistentDataType.STRING) } returns "purr_items:MISSING"
        }
        val meta = mockk<ItemMeta> {
            every { persistentDataContainer } returns pdc
        }
        val stack = mockk<ItemStack> {
            every { hasItemMeta() } returns true
            every { itemMeta } returns meta
        }

        val result = serializer.getDefinition(stack)

        assertNull(result)
    }

    @Test
    fun `isCustomItem returns true for custom items`() {
        val key = NamespacedKey(plugin, "item_id")
        val pdc = mockk<PersistentDataContainer> {
            every { get(key, PersistentDataType.STRING) } returns "purr_items:TEST_ITEM"
        }
        val meta = mockk<ItemMeta> {
            every { persistentDataContainer } returns pdc
        }
        val stack = mockk<ItemStack> {
            every { hasItemMeta() } returns true
            every { itemMeta } returns meta
        }

        assertTrue(serializer.isCustomItem(stack))
    }

    @Test
    fun `isCustomItem returns false for vanilla items`() {
        val stack = mockk<ItemStack> {
            every { hasItemMeta() } returns false
        }

        assertFalse(serializer.isCustomItem(stack))
    }
}
