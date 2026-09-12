package gay.nyaa.purritems.domain

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class RarityTest {
    @Test
    fun `all rarities have unique sort order`() {
        val sortOrders = Rarity.entries.map { it.sortOrder }
        assertEquals(sortOrders.size, sortOrders.distinct().size)
    }

    @Test
    fun `sort order is ascending`() {
        val sortOrders = Rarity.entries.map { it.sortOrder }
        assertEquals(sortOrders, sortOrders.sorted())
    }

    @Test
    fun `fromString case insensitive`() {
        assertEquals(Rarity.COMMON, Rarity.fromString("common"))
        assertEquals(Rarity.COMMON, Rarity.fromString("COMMON"))
        assertEquals(Rarity.COMMON, Rarity.fromString("Common"))
        assertEquals(Rarity.LEGENDARY, Rarity.fromString("legendary"))
    }

    @Test
    fun `fromString returns null for invalid`() {
        assertNull(Rarity.fromString("invalid"))
        assertNull(Rarity.fromString(""))
        assertNull(Rarity.fromString("ultra"))
    }

    @Test
    fun `all rarities have display keys`() {
        Rarity.entries.forEach { rarity ->
            assertNotNull(rarity.displayKey)
            assert(rarity.displayKey.isNotBlank())
        }
    }

    @Test
    fun `rarity order verification`() {
        assertEquals(0, Rarity.COMMON.sortOrder)
        assertEquals(1, Rarity.UNCOMMON.sortOrder)
        assertEquals(2, Rarity.RARE.sortOrder)
        assertEquals(3, Rarity.EPIC.sortOrder)
        assertEquals(4, Rarity.LEGENDARY.sortOrder)
        assertEquals(5, Rarity.MYTHIC.sortOrder)
    }
}
