package gay.nyaa.purritems

import com.purrcore.i18n.I18n
import gay.nyaa.purritems.abilities.AbilityExecutor
import gay.nyaa.purritems.abilities.AbilityRegistry
import gay.nyaa.purritems.domain.ItemDefinition
import gay.nyaa.purritems.domain.ItemId
import gay.nyaa.purritems.domain.Rarity
import gay.nyaa.purritems.registry.ItemRegistry
import gay.nyaa.purritems.requirements.RequirementCheckResult
import gay.nyaa.purritems.requirements.RequirementChecker
import gay.nyaa.purritems.serialization.ItemSerializer
import gay.nyaa.purritems.stats.ItemStatsProvider
import gay.nyaa.purritems.upgrade.UpgradeManager
import gay.nyaa.purrskills.SkillManager
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.*

class ItemManagerTest {
    private lateinit var registry: ItemRegistry
    private lateinit var itemSerializer: ItemSerializer
    private lateinit var statsProvider: ItemStatsProvider
    private lateinit var requirementChecker: RequirementChecker
    private lateinit var abilityRegistry: AbilityRegistry
    private lateinit var abilityExecutor: AbilityExecutor
    private lateinit var upgradeManager: UpgradeManager
    private lateinit var skillManager: SkillManager
    private lateinit var i18n: I18n
    private lateinit var itemManager: ItemManager

    @BeforeEach
    fun setup() {
        registry = ItemRegistry()
        itemSerializer = mockk(relaxed = true)
        statsProvider = mockk(relaxed = true)
        requirementChecker = mockk(relaxed = true)
        abilityRegistry = AbilityRegistry()
        abilityExecutor = mockk(relaxed = true)
        upgradeManager = mockk(relaxed = true)
        skillManager = mockk(relaxed = true)
        i18n = mockk(relaxed = true)

        itemManager = ItemManager(
            registry,
            itemSerializer,
            statsProvider,
            requirementChecker,
            abilityRegistry,
            abilityExecutor,
            upgradeManager,
            skillManager,
            i18n,
        )
    }

    @Test
    fun `registerItem adds to registry`() {
        val id = ItemId("purr_items", "TEST_ITEM")
        val definition = ItemDefinition.builder(id, Material.DIAMOND, Rarity.COMMON, "Test").build()

        itemManager.registerItem(definition)

        assertTrue(registry.contains(id))
        assertEquals(definition, registry.get(id))
    }

    @Test
    fun `getItem retrieves from registry`() {
        val id = ItemId("purr_items", "TEST_ITEM")
        val definition = ItemDefinition.builder(id, Material.DIAMOND, Rarity.COMMON, "Test").build()
        registry.register(definition)

        val result = itemManager.getItem(id)

        assertNotNull(result)
        assertEquals(definition, result)
    }

    @Test
    fun `getItem returns null for unregistered item`() {
        val id = ItemId("purr_items", "MISSING")

        val result = itemManager.getItem(id)

        assertNull(result)
    }

    @Test
    fun `createItem returns null for unregistered item`() {
        val id = ItemId("purr_items", "MISSING")

        val result = itemManager.createItem(id)

        assertNull(result)
    }

    @Test
    fun `createItem creates ItemStack from definition`() {
        val id = ItemId("purr_items", "TEST_ITEM")
        val definition = ItemDefinition.builder(id, Material.DIAMOND, Rarity.COMMON, "Test").build()
        registry.register(definition)

        val stack = mockk<ItemStack>()
        every { itemSerializer.createItemStack(definition, 1) } returns stack

        val result = itemManager.createItem(id)

        assertNotNull(result)
        assertEquals(stack, result)
        verify { itemSerializer.createItemStack(definition, 1) }
    }

    @Test
    fun `createItem respects amount parameter`() {
        val id = ItemId("purr_items", "TEST_ITEM")
        val definition = ItemDefinition.builder(id, Material.DIAMOND, Rarity.COMMON, "Test").build()
        registry.register(definition)

        val stack = mockk<ItemStack>()
        every { itemSerializer.createItemStack(definition, 5) } returns stack

        itemManager.createItem(id, 5)

        verify { itemSerializer.createItemStack(definition, 5) }
    }

    @Test
    fun `canUseItem returns true for vanilla items`() {
        val player = mockk<Player>()
        val stack = mockk<ItemStack>()

        every { itemSerializer.getDefinition(stack) } returns null

        assertTrue(itemManager.canUseItem(player, stack))
    }

    @Test
    fun `canUseItem returns true when requirements met`() {
        val player = mockk<Player>()
        val stack = mockk<ItemStack>()
        val id = ItemId("purr_items", "TEST_ITEM")
        val definition = ItemDefinition.builder(id, Material.DIAMOND, Rarity.COMMON, "Test").build()

        every { itemSerializer.getDefinition(stack) } returns definition
        every { requirementChecker.checkRequirements(player, definition) } returns RequirementCheckResult(
            met = true,
            unmetRequirements = emptyList(),
        )

        assertTrue(itemManager.canUseItem(player, stack))
    }

    @Test
    fun `canUseItem returns false when requirements not met`() {
        val player = mockk<Player>()
        val stack = mockk<ItemStack>()
        val id = ItemId("purr_items", "TEST_ITEM")
        val definition = ItemDefinition.builder(id, Material.DIAMOND, Rarity.COMMON, "Test").build()

        val result = RequirementCheckResult(met = false, unmetRequirements = emptyList())

        every { itemSerializer.getDefinition(stack) } returns definition
        every { requirementChecker.checkRequirements(player, definition) } returns result

        assertFalse(itemManager.canUseItem(player, stack))
        verify { requirementChecker.sendRequirementMessage(player, result) }
    }

    @Test
    fun `refreshPlayerStats delegates to stats provider`() {
        val player = mockk<Player>()

        itemManager.refreshPlayerStats(player)

        verify { statsProvider.refreshPlayerStats(player) }
    }

    @Test
    fun `getAllItems returns all registered items`() {
        val id1 = ItemId("purr_items", "ITEM_1")
        val id2 = ItemId("purr_items", "ITEM_2")
        val def1 = ItemDefinition.builder(id1, Material.DIAMOND, Rarity.COMMON, "Item 1").build()
        val def2 = ItemDefinition.builder(id2, Material.EMERALD, Rarity.RARE, "Item 2").build()

        registry.register(def1)
        registry.register(def2)

        val items = itemManager.getAllItems()

        assertEquals(2, items.size)
        assertTrue(items.any { it.id == id1 })
        assertTrue(items.any { it.id == id2 })
    }

    @Test
    fun `setRecipeUnlockChecker sets checker`() {
        val player = mockk<Player>()
        val itemId = ItemId("purr_items", "TEST")
        val checker: (Player, ItemId) -> Boolean = { _, _ -> true }

        itemManager.setRecipeUnlockChecker(checker)

        assertTrue(itemManager.hasUnlockedRecipe(player, itemId))
    }

    @Test
    fun `hasUnlockedRecipe returns true when no checker set`() {
        val player = mockk<Player>()
        val itemId = ItemId("purr_items", "TEST")

        assertTrue(itemManager.hasUnlockedRecipe(player, itemId))
    }

    @Test
    fun `hasUnlockedRecipe delegates to checker`() {
        val player = mockk<Player>()
        val itemId = ItemId("purr_items", "TEST")
        var called = false

        itemManager.setRecipeUnlockChecker { p, id ->
            called = true
            assertEquals(player, p)
            assertEquals(itemId, id)
            false
        }

        assertFalse(itemManager.hasUnlockedRecipe(player, itemId))
        assertTrue(called)
    }
}
