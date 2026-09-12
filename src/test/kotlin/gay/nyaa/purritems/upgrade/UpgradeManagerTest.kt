package gay.nyaa.purritems.upgrade

import gay.nyaa.purritems.domain.ItemDefinition
import gay.nyaa.purritems.domain.ItemId
import gay.nyaa.purritems.domain.Rarity
import gay.nyaa.purritems.registry.ItemRegistry
import gay.nyaa.purritems.serialization.ItemSerializer
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.*

class UpgradeManagerTest {
    private lateinit var registry: ItemRegistry
    private lateinit var itemSerializer: ItemSerializer
    private lateinit var upgradeManager: UpgradeManager

    @BeforeEach
    fun setup() {
        registry = ItemRegistry()
        itemSerializer = mockk(relaxed = true)
        upgradeManager = UpgradeManager(registry, itemSerializer)
    }

    @Test
    fun `canUpgrade returns false for non-custom item`() {
        val stack = mockk<ItemStack>()
        every { itemSerializer.getDefinition(stack) } returns null

        assertFalse(upgradeManager.canUpgrade(stack))
    }

    @Test
    fun `canUpgrade returns false when no upgrade path`() {
        val id = ItemId("purr_items", "BASIC_ITEM")
        val definition = ItemDefinition.builder(id, Material.DIAMOND, Rarity.COMMON, "Basic").build()

        val stack = mockk<ItemStack>()
        every { itemSerializer.getDefinition(stack) } returns definition

        assertFalse(upgradeManager.canUpgrade(stack))
    }

    @Test
    fun `canUpgrade returns false when upgrade target not registered`() {
        val id = ItemId("purr_items", "TIER_1")
        val upgradeId = ItemId("purr_items", "TIER_2")
        val definition = ItemDefinition.builder(id, Material.DIAMOND, Rarity.COMMON, "Tier 1")
            .withUpgradesTo(upgradeId)
            .build()

        val stack = mockk<ItemStack>()
        every { itemSerializer.getDefinition(stack) } returns definition

        assertFalse(upgradeManager.canUpgrade(stack))
    }

    @Test
    fun `canUpgrade returns true when upgrade path exists`() {
        val id = ItemId("purr_items", "TIER_1")
        val upgradeId = ItemId("purr_items", "TIER_2")

        val tier1 = ItemDefinition.builder(id, Material.DIAMOND, Rarity.COMMON, "Tier 1")
            .withUpgradesTo(upgradeId)
            .build()
        val tier2 = ItemDefinition.builder(upgradeId, Material.DIAMOND, Rarity.RARE, "Tier 2").build()

        registry.register(tier1)
        registry.register(tier2)

        val stack = mockk<ItemStack>()
        every { itemSerializer.getDefinition(stack) } returns tier1

        assertTrue(upgradeManager.canUpgrade(stack))
    }

    @Test
    fun `getUpgradeTarget returns null for non-custom item`() {
        val stack = mockk<ItemStack>()
        every { itemSerializer.getDefinition(stack) } returns null

        assertNull(upgradeManager.getUpgradeTarget(stack))
    }

    @Test
    fun `getUpgradeTarget returns null when no upgrade path`() {
        val id = ItemId("purr_items", "BASIC_ITEM")
        val definition = ItemDefinition.builder(id, Material.DIAMOND, Rarity.COMMON, "Basic").build()

        val stack = mockk<ItemStack>()
        every { itemSerializer.getDefinition(stack) } returns definition

        assertNull(upgradeManager.getUpgradeTarget(stack))
    }

    @Test
    fun `getUpgradeTarget returns upgrade ID`() {
        val id = ItemId("purr_items", "TIER_1")
        val upgradeId = ItemId("purr_items", "TIER_2")
        val definition = ItemDefinition.builder(id, Material.DIAMOND, Rarity.COMMON, "Tier 1")
            .withUpgradesTo(upgradeId)
            .build()

        val stack = mockk<ItemStack>()
        every { itemSerializer.getDefinition(stack) } returns definition

        val result = upgradeManager.getUpgradeTarget(stack)

        assertNotNull(result)
        assertEquals(upgradeId, result)
    }

    @Test
    fun `upgrade returns null for non-custom item`() {
        val stack = mockk<ItemStack>()
        every { itemSerializer.getDefinition(stack) } returns null

        assertNull(upgradeManager.upgrade(stack))
    }

    @Test
    fun `upgrade returns null when no upgrade path`() {
        val id = ItemId("purr_items", "BASIC_ITEM")
        val definition = ItemDefinition.builder(id, Material.DIAMOND, Rarity.COMMON, "Basic").build()

        val stack = mockk<ItemStack>()
        every { itemSerializer.getDefinition(stack) } returns definition

        assertNull(upgradeManager.upgrade(stack))
    }

    @Test
    fun `upgrade returns null when target not registered`() {
        val id = ItemId("purr_items", "TIER_1")
        val upgradeId = ItemId("purr_items", "TIER_2")
        val definition = ItemDefinition.builder(id, Material.DIAMOND, Rarity.COMMON, "Tier 1")
            .withUpgradesTo(upgradeId)
            .build()

        val stack = mockk<ItemStack>()
        every { itemSerializer.getDefinition(stack) } returns definition

        assertNull(upgradeManager.upgrade(stack))
    }

    @Test
    fun `upgrade creates new item with upgraded definition`() {
        val id = ItemId("purr_items", "TIER_1")
        val upgradeId = ItemId("purr_items", "TIER_2")

        val tier1 = ItemDefinition.builder(id, Material.DIAMOND, Rarity.COMMON, "Tier 1")
            .withUpgradesTo(upgradeId)
            .build()
        val tier2 = ItemDefinition.builder(upgradeId, Material.DIAMOND, Rarity.RARE, "Tier 2").build()

        registry.register(tier1)
        registry.register(tier2)

        val originalStack = mockk<ItemStack> {
            every { amount } returns 1
        }
        val upgradedStack = mockk<ItemStack>()

        every { itemSerializer.getDefinition(originalStack) } returns tier1
        every { itemSerializer.createItemStack(tier2, 1) } returns upgradedStack

        val result = upgradeManager.upgrade(originalStack)

        assertNotNull(result)
        assertEquals(upgradedStack, result)
        verify { itemSerializer.createItemStack(tier2, 1) }
    }

    @Test
    fun `upgrade preserves item amount`() {
        val id = ItemId("purr_items", "TIER_1")
        val upgradeId = ItemId("purr_items", "TIER_2")

        val tier1 = ItemDefinition.builder(id, Material.DIAMOND, Rarity.COMMON, "Tier 1")
            .withUpgradesTo(upgradeId)
            .build()
        val tier2 = ItemDefinition.builder(upgradeId, Material.DIAMOND, Rarity.RARE, "Tier 2").build()

        registry.register(tier1)
        registry.register(tier2)

        val originalStack = mockk<ItemStack> {
            every { amount } returns 5
        }
        val upgradedStack = mockk<ItemStack>()

        every { itemSerializer.getDefinition(originalStack) } returns tier1
        every { itemSerializer.createItemStack(tier2, 5) } returns upgradedStack

        upgradeManager.upgrade(originalStack)

        verify { itemSerializer.createItemStack(tier2, 5) }
    }

    @Test
    fun `upgrade chain works`() {
        val tier1Id = ItemId("purr_items", "TIER_1")
        val tier2Id = ItemId("purr_items", "TIER_2")
        val tier3Id = ItemId("purr_items", "TIER_3")

        val tier1 = ItemDefinition.builder(tier1Id, Material.IRON_SWORD, Rarity.COMMON, "Tier 1")
            .withUpgradesTo(tier2Id)
            .build()
        val tier2 = ItemDefinition.builder(tier2Id, Material.DIAMOND_SWORD, Rarity.RARE, "Tier 2")
            .withUpgradesTo(tier3Id)
            .build()
        val tier3 = ItemDefinition.builder(tier3Id, Material.NETHERITE_SWORD, Rarity.LEGENDARY, "Tier 3")
            .build()

        registry.register(tier1)
        registry.register(tier2)
        registry.register(tier3)

        // Verify tier 1 can upgrade to tier 2
        val stack1 = mockk<ItemStack> {
            every { amount } returns 1
        }
        every { itemSerializer.getDefinition(stack1) } returns tier1
        every { itemSerializer.createItemStack(tier2, 1) } returns mockk()

        assertTrue(upgradeManager.canUpgrade(stack1))
        assertEquals(tier2Id, upgradeManager.getUpgradeTarget(stack1))

        // Verify tier 2 can upgrade to tier 3
        val stack2 = mockk<ItemStack> {
            every { amount } returns 1
        }
        every { itemSerializer.getDefinition(stack2) } returns tier2
        every { itemSerializer.createItemStack(tier3, 1) } returns mockk()

        assertTrue(upgradeManager.canUpgrade(stack2))
        assertEquals(tier3Id, upgradeManager.getUpgradeTarget(stack2))

        // Verify tier 3 cannot upgrade
        val stack3 = mockk<ItemStack>()
        every { itemSerializer.getDefinition(stack3) } returns tier3

        assertFalse(upgradeManager.canUpgrade(stack3))
        assertNull(upgradeManager.getUpgradeTarget(stack3))
    }
}
