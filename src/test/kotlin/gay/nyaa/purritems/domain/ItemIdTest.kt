package gay.nyaa.purritems.domain

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

class ItemIdTest {
    @Test
    fun `parse valid ItemId`() {
        val id = ItemId.parse("purr_items:DIAMOND_SWORD")
        assertEquals("purr_items", id.namespace)
        assertEquals("DIAMOND_SWORD", id.key)
    }

    @Test
    fun `toString format`() {
        val id = ItemId("purr_items", "DIAMOND_SWORD")
        assertEquals("purr_items:DIAMOND_SWORD", id.toString())
    }

    @Test
    fun `reject invalid format`() {
        assertThrows<IllegalArgumentException> {
            ItemId.parse("invalid")
        }
    }

    @Test
    fun `reject blank namespace`() {
        assertThrows<IllegalArgumentException> {
            ItemId("", "KEY")
        }
    }

    @Test
    fun `reject blank key`() {
        assertThrows<IllegalArgumentException> {
            ItemId("namespace", "")
        }
    }

    @Test
    fun `reject uppercase namespace`() {
        assertThrows<IllegalArgumentException> {
            ItemId("PURR_ITEMS", "KEY")
        }
    }

    @Test
    fun `reject lowercase key`() {
        assertThrows<IllegalArgumentException> {
            ItemId("purr_items", "key")
        }
    }
}
