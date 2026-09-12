package gay.nyaa.purritems.stats

import gay.nyaa.purritems.domain.ItemDefinition
import gay.nyaa.purritems.domain.ItemId
import gay.nyaa.purritems.domain.Rarity
import gay.nyaa.purritems.serialization.ItemSerializer
import gay.nyaa.purrskills.stats.StatModifier
import gay.nyaa.purrskills.stats.StatSource
import gay.nyaa.purrskills.stats.StatType
import gay.nyaa.purrskills.stats.StatsManager
import gay.nyaa.purrskills.stats.ModifierType
import io.mockk.*
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.PlayerInventory
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.UUID

class ItemStatsProviderTest {
    private lateinit var itemSerializer: ItemSerializer
    private lateinit var statsManager: StatsManager
    private lateinit var provider: ItemStatsProvider
    private lateinit var player: Player
    private lateinit var playerUuid: UUID
    private lateinit var inventory: PlayerInventory

    @BeforeEach
    fun setup() {
        itemSerializer = mockk(relaxed = true)
        statsManager = mockk(relaxed = true)
        provider = ItemStatsProvider(itemSerializer, statsManager)
        playerUuid = UUID.randomUUID()
        inventory = mockk(relaxed = true)
        player = mockk(relaxed = true) {
            every { uniqueId } returns playerUuid
            every { getInventory() } returns inventory
        }
    }

    @Test
    fun `refreshPlayerStats removes old modifiers`() {
        every { inventory.getItem(any<EquipmentSlot>()) } returns null

        provider.refreshPlayerStats(player)

        // Verify all item sources are cleared
        verify { statsManager.removeModifiersFromSource(playerUuid, StatSource.ITEM_MAIN_HAND) }
        verify { statsManager.removeModifiersFromSource(playerUuid, StatSource.ITEM_OFF_HAND) }
        verify { statsManager.removeModifiersFromSource(playerUuid, StatSource.ITEM_HELMET) }
        verify { statsManager.removeModifiersFromSource(playerUuid, StatSource.ITEM_CHESTPLATE) }
        verify { statsManager.removeModifiersFromSource(playerUuid, StatSource.ITEM_LEGGINGS) }
        verify { statsManager.removeModifiersFromSource(playerUuid, StatSource.ITEM_BOOTS) }
    }

    @Test
    fun `refreshPlayerStats adds stats from main hand`() {
        val id = ItemId("purr_items", "SWORD")
        val stats = listOf(
            StatModifier(StatType.DAMAGE, 10.0, ModifierType.FLAT, StatSource.ITEM_MAIN_HAND),
        )
        val definition = ItemDefinition.builder(id, Material.DIAMOND_SWORD, Rarity.RARE, "Sword")
            .withStats(stats)
            .build()

        val stack = mockk<ItemStack>()
        every { inventory.getItem(EquipmentSlot.HAND) } returns stack
        every { inventory.getItem(not(eq(EquipmentSlot.HAND))) } returns null
        every { itemSerializer.getDefinition(stack) } returns definition

        provider.refreshPlayerStats(player)

        verify {
            statsManager.addModifiers(
                playerUuid,
                match { modifiers ->
                    modifiers.size == 1 &&
                        modifiers[0].stat == StatType.DAMAGE &&
                        modifiers[0].value == 10.0 &&
                        modifiers[0].source == StatSource.ITEM_MAIN_HAND
                },
            )
        }
    }

    @Test
    fun `refreshPlayerStats adds stats from helmet`() {
        val id = ItemId("purr_items", "HELMET")
        val stats = listOf(
            StatModifier(StatType.DEFENSE, 5.0, ModifierType.FLAT, StatSource.ITEM_HELMET),
        )
        val definition = ItemDefinition.builder(id, Material.DIAMOND_HELMET, Rarity.RARE, "Helmet")
            .withStats(stats)
            .build()

        val stack = mockk<ItemStack>()
        every { inventory.getItem(EquipmentSlot.HEAD) } returns stack
        every { inventory.getItem(not(eq(EquipmentSlot.HEAD))) } returns null
        every { itemSerializer.getDefinition(stack) } returns definition

        provider.refreshPlayerStats(player)

        verify {
            statsManager.addModifiers(
                playerUuid,
                match { modifiers ->
                    modifiers.size == 1 &&
                        modifiers[0].stat == StatType.DEFENSE &&
                        modifiers[0].source == StatSource.ITEM_HELMET
                },
            )
        }
    }

    @Test
    fun `refreshPlayerStats handles multiple equipped items`() {
        val sword = mockk<ItemStack>()
        val helmet = mockk<ItemStack>()

        val swordDef = ItemDefinition.builder(
            ItemId("purr_items", "SWORD"),
            Material.DIAMOND_SWORD,
            Rarity.RARE,
            "Sword",
        )
            .withStats(
                listOf(
                    StatModifier(StatType.DAMAGE, 10.0, ModifierType.FLAT, StatSource.ITEM_MAIN_HAND),
                ),
            )
            .build()

        val helmetDef = ItemDefinition.builder(
            ItemId("purr_items", "HELMET"),
            Material.DIAMOND_HELMET,
            Rarity.RARE,
            "Helmet",
        )
            .withStats(
                listOf(
                    StatModifier(StatType.DEFENSE, 5.0, ModifierType.FLAT, StatSource.ITEM_HELMET),
                ),
            )
            .build()

        every { inventory.getItem(EquipmentSlot.HAND) } returns sword
        every { inventory.getItem(EquipmentSlot.HEAD) } returns helmet
        every { inventory.getItem(not(match { it == EquipmentSlot.HAND || it == EquipmentSlot.HEAD })) } returns null
        every { itemSerializer.getDefinition(sword) } returns swordDef
        every { itemSerializer.getDefinition(helmet) } returns helmetDef

        provider.refreshPlayerStats(player)

        verify(exactly = 2) { statsManager.addModifiers(playerUuid, any()) }
    }

    @Test
    fun `refreshPlayerStats skips vanilla items`() {
        val stack = mockk<ItemStack>()
        every { inventory.getItem(EquipmentSlot.HAND) } returns stack
        every { inventory.getItem(not(eq(EquipmentSlot.HAND))) } returns null
        every { itemSerializer.getDefinition(stack) } returns null

        provider.refreshPlayerStats(player)

        verify(exactly = 0) { statsManager.addModifiers(any(), any()) }
    }

    @Test
    fun `refreshPlayerStats skips empty slots`() {
        every { inventory.getItem(any<EquipmentSlot>()) } returns null

        provider.refreshPlayerStats(player)

        verify(exactly = 0) { statsManager.addModifiers(any(), any()) }
    }

    @Test
    fun `clearPlayerStats removes all item sources`() {
        provider.clearPlayerStats(playerUuid)

        verify { statsManager.removeModifiersFromSource(playerUuid, StatSource.ITEM_MAIN_HAND) }
        verify { statsManager.removeModifiersFromSource(playerUuid, StatSource.ITEM_OFF_HAND) }
        verify { statsManager.removeModifiersFromSource(playerUuid, StatSource.ITEM_HELMET) }
        verify { statsManager.removeModifiersFromSource(playerUuid, StatSource.ITEM_CHESTPLATE) }
        verify { statsManager.removeModifiersFromSource(playerUuid, StatSource.ITEM_LEGGINGS) }
        verify { statsManager.removeModifiersFromSource(playerUuid, StatSource.ITEM_BOOTS) }
    }

    @Test
    fun `refreshPlayerStats converts stat sources correctly`() {
        val originalStats = listOf(
            StatModifier(StatType.DAMAGE, 10.0, ModifierType.FLAT, StatSource.SKILL_MINING),
        )
        val definition = ItemDefinition.builder(
            ItemId("purr_items", "ITEM"),
            Material.DIAMOND,
            Rarity.COMMON,
            "Item",
        )
            .withStats(originalStats)
            .build()

        val stack = mockk<ItemStack>()
        every { inventory.getItem(EquipmentSlot.HAND) } returns stack
        every { inventory.getItem(not(eq(EquipmentSlot.HAND))) } returns null
        every { itemSerializer.getDefinition(stack) } returns definition

        provider.refreshPlayerStats(player)

        verify {
            statsManager.addModifiers(
                playerUuid,
                match { modifiers ->
                    modifiers[0].source == StatSource.ITEM_MAIN_HAND
                },
            )
        }
    }
}
