package gay.nyaa.purritems.domain

import gay.nyaa.purrskills.stats.StatModifier
import gay.nyaa.purrskills.stats.StatSource
import gay.nyaa.purrskills.stats.StatType
import org.bukkit.Material
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class ItemDefinitionTest {
    @Test
    fun `build minimal item definition`() {
        val id = ItemId("purr_items", "TEST_ITEM")
        val definition = ItemDefinition.builder(id, Material.DIAMOND, Rarity.COMMON, "Test Item").build()

        assertEquals(id, definition.id)
        assertEquals(Material.DIAMOND, definition.material)
        assertEquals(Rarity.COMMON, definition.rarity)
        assertEquals("Test Item", definition.displayName)
        assertEquals(emptyList(), definition.stats)
        assertEquals(emptyList(), definition.requirements)
        assertEquals(emptyList(), definition.abilities)
        assertNull(definition.upgradesTo)
    }

    @Test
    fun `build with stats`() {
        val id = ItemId("purr_items", "TEST_ITEM")
        val stats = listOf(
            StatModifier(StatType.DAMAGE, 10.0, StatModifier.ModifierType.ADDITIVE, StatSource.ITEM_MAIN_HAND),
            StatModifier(StatType.HEALTH, 20.0, StatModifier.ModifierType.ADDITIVE, StatSource.ITEM_MAIN_HAND),
        )

        val definition = ItemDefinition.builder(id, Material.DIAMOND_SWORD, Rarity.RARE, "Sword")
            .withStats(stats)
            .build()

        assertEquals(2, definition.stats.size)
        assertEquals(stats, definition.stats)
    }

    @Test
    fun `build with upgrade path`() {
        val id = ItemId("purr_items", "TIER_1")
        val upgradeId = ItemId("purr_items", "TIER_2")

        val definition = ItemDefinition.builder(id, Material.DIAMOND, Rarity.COMMON, "Tier 1")
            .withUpgradesTo(upgradeId)
            .build()

        assertEquals(upgradeId, definition.upgradesTo)
    }

    @Test
    fun `reject blank display name`() {
        val id = ItemId("purr_items", "TEST_ITEM")

        assertThrows<IllegalArgumentException> {
            ItemDefinition.builder(id, Material.DIAMOND, Rarity.COMMON, "").build()
        }

        assertThrows<IllegalArgumentException> {
            ItemDefinition.builder(id, Material.DIAMOND, Rarity.COMMON, "   ").build()
        }
    }

    @Test
    fun `toBuilder preserves all properties`() {
        val id = ItemId("purr_items", "TEST_ITEM")
        val upgradeId = ItemId("purr_items", "UPGRADED")
        val stats = listOf(
            StatModifier(StatType.DAMAGE, 10.0, StatModifier.ModifierType.ADDITIVE, StatSource.ITEM_MAIN_HAND),
        )

        val original = ItemDefinition.builder(id, Material.DIAMOND_SWORD, Rarity.EPIC, "Original")
            .withStats(stats)
            .withUpgradesTo(upgradeId)
            .build()

        val modified = original.toBuilder()
            .withDisplayName("Modified")
            .build()

        assertEquals(id, modified.id)
        assertEquals(Material.DIAMOND_SWORD, modified.material)
        assertEquals(Rarity.EPIC, modified.rarity)
        assertEquals("Modified", modified.displayName)
        assertEquals(stats, modified.stats)
        assertEquals(upgradeId, modified.upgradesTo)
    }

    @Test
    fun `builder allows material change`() {
        val id = ItemId("purr_items", "TEST_ITEM")
        val definition = ItemDefinition.builder(id, Material.DIAMOND, Rarity.COMMON, "Test")
            .withMaterial(Material.EMERALD)
            .build()

        assertEquals(Material.EMERALD, definition.material)
    }

    @Test
    fun `builder allows rarity change`() {
        val id = ItemId("purr_items", "TEST_ITEM")
        val definition = ItemDefinition.builder(id, Material.DIAMOND, Rarity.COMMON, "Test")
            .withRarity(Rarity.LEGENDARY)
            .build()

        assertEquals(Rarity.LEGENDARY, definition.rarity)
    }

    @Test
    fun `stats list is immutable copy`() {
        val id = ItemId("purr_items", "TEST_ITEM")
        val stats = mutableListOf(
            StatModifier(StatType.DAMAGE, 10.0, StatModifier.ModifierType.ADDITIVE, StatSource.ITEM_MAIN_HAND),
        )

        val definition = ItemDefinition.builder(id, Material.DIAMOND, Rarity.COMMON, "Test")
            .withStats(stats)
            .build()

        stats.add(StatModifier(StatType.HEALTH, 20.0, StatModifier.ModifierType.ADDITIVE, StatSource.ITEM_MAIN_HAND))

        assertEquals(1, definition.stats.size)
    }

    @Test
    fun `clear upgrade path`() {
        val id = ItemId("purr_items", "TEST_ITEM")
        val upgradeId = ItemId("purr_items", "UPGRADED")

        val definition = ItemDefinition.builder(id, Material.DIAMOND, Rarity.COMMON, "Test")
            .withUpgradesTo(upgradeId)
            .withUpgradesTo(null)
            .build()

        assertNull(definition.upgradesTo)
    }
}
