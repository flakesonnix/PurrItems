package gay.nyaa.purritems.upgrade

import gay.nyaa.purritems.domain.ItemId
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class UpgradeDefinitionTest {
    @Test
    fun `create upgrade definition with no cost`() {
        val from = ItemId("purr_items", "TIER_1")
        val to = ItemId("purr_items", "TIER_2")

        val definition = UpgradeDefinition(from, to)

        assertEquals(from, definition.from)
        assertEquals(to, definition.to)
        assertEquals(UpgradeCost.None, definition.cost)
    }

    @Test
    fun `create upgrade definition with explicit no cost`() {
        val from = ItemId("purr_items", "TIER_1")
        val to = ItemId("purr_items", "TIER_2")

        val definition = UpgradeDefinition(from, to, UpgradeCost.None)

        assertEquals(from, definition.from)
        assertEquals(to, definition.to)
        assertEquals(UpgradeCost.None, definition.cost)
    }

    @Test
    fun `data class copy works`() {
        val from = ItemId("purr_items", "TIER_1")
        val to = ItemId("purr_items", "TIER_2")
        val original = UpgradeDefinition(from, to)

        val newTo = ItemId("purr_items", "TIER_3")
        val copied = original.copy(to = newTo)

        assertEquals(from, copied.from)
        assertEquals(newTo, copied.to)
        assertEquals(UpgradeCost.None, copied.cost)
    }

    @Test
    fun `upgrade cost none is singleton`() {
        val cost1 = UpgradeCost.None
        val cost2 = UpgradeCost.None

        assertEquals(cost1, cost2)
    }

    @Test
    fun `upgrade definition with different costs are different`() {
        val from = ItemId("purr_items", "TIER_1")
        val to = ItemId("purr_items", "TIER_2")

        val def1 = UpgradeDefinition(from, to, UpgradeCost.None)
        val def2 = UpgradeDefinition(from, to, UpgradeCost.None)

        assertEquals(def1, def2)
    }

    @Test
    fun `toString works`() {
        val from = ItemId("purr_items", "TIER_1")
        val to = ItemId("purr_items", "TIER_2")
        val definition = UpgradeDefinition(from, to)

        val str = definition.toString()

        assertNotNull(str)
        assert(str.contains("TIER_1"))
        assert(str.contains("TIER_2"))
    }
}
