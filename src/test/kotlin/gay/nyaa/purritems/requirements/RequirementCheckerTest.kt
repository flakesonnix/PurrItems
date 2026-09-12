package gay.nyaa.purritems.requirements

import com.purrcore.i18n.I18n
import gay.nyaa.purritems.domain.ItemDefinition
import gay.nyaa.purritems.domain.ItemId
import gay.nyaa.purritems.domain.Rarity
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.bukkit.Material
import org.bukkit.entity.Player
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class RequirementCheckerTest {
    private lateinit var i18n: I18n
    private lateinit var checker: RequirementChecker
    private lateinit var player: Player

    @BeforeEach
    fun setup() {
        i18n = mockk(relaxed = true)
        checker = RequirementChecker(i18n)
        player = mockk(relaxed = true)
    }

    @Test
    fun `checkRequirements passes with no requirements`() {
        val id = ItemId("purr_items", "TEST_ITEM")
        val definition = ItemDefinition.builder(id, Material.DIAMOND, Rarity.COMMON, "Test Item").build()

        val result = checker.checkRequirements(player, definition)

        assertTrue(result.met)
        assertEquals(0, result.unmetRequirements.size)
    }

    @Test
    fun `checkRequirements passes when all requirements met`() {
        val req1 = mockk<Requirement> {
            every { check(player) } returns true
        }
        val req2 = mockk<Requirement> {
            every { check(player) } returns true
        }

        val id = ItemId("purr_items", "TEST_ITEM")
        val definition = ItemDefinition.builder(id, Material.DIAMOND, Rarity.COMMON, "Test Item")
            .withRequirements(listOf(req1, req2))
            .build()

        val result = checker.checkRequirements(player, definition)

        assertTrue(result.met)
        assertEquals(0, result.unmetRequirements.size)
    }

    @Test
    fun `checkRequirements fails when requirement not met`() {
        val req1 = mockk<Requirement> {
            every { check(player) } returns true
        }
        val req2 = mockk<Requirement> {
            every { check(player) } returns false
            every { description(i18n) } returns "Skill requirement not met"
        }

        val id = ItemId("purr_items", "TEST_ITEM")
        val definition = ItemDefinition.builder(id, Material.DIAMOND, Rarity.COMMON, "Test Item")
            .withRequirements(listOf(req1, req2))
            .build()

        val result = checker.checkRequirements(player, definition)

        assertFalse(result.met)
        assertEquals(1, result.unmetRequirements.size)
        assertEquals(req2, result.unmetRequirements[0])
    }

    @Test
    fun `checkRequirements collects all unmet requirements`() {
        val req1 = mockk<Requirement> {
            every { check(player) } returns false
            every { description(i18n) } returns "Requirement 1"
        }
        val req2 = mockk<Requirement> {
            every { check(player) } returns false
            every { description(i18n) } returns "Requirement 2"
        }
        val req3 = mockk<Requirement> {
            every { check(player) } returns true
        }

        val id = ItemId("purr_items", "TEST_ITEM")
        val definition = ItemDefinition.builder(id, Material.DIAMOND, Rarity.COMMON, "Test Item")
            .withRequirements(listOf(req1, req2, req3))
            .build()

        val result = checker.checkRequirements(player, definition)

        assertFalse(result.met)
        assertEquals(2, result.unmetRequirements.size)
        assertTrue(result.unmetRequirements.contains(req1))
        assertTrue(result.unmetRequirements.contains(req2))
    }

    @Test
    fun `sendRequirementMessage sends messages for unmet requirements`() {
        val req1 = mockk<Requirement> {
            every { description(i18n) } returns "Mining Level 5"
        }
        val req2 = mockk<Requirement> {
            every { description(i18n) } returns "Combat Level 10"
        }

        val result = RequirementCheckResult(
            met = false,
            unmetRequirements = listOf(req1, req2),
        )

        checker.sendRequirementMessage(player, result)

        verify { i18n.send(player, "requirement.not-met") }
        verify { player.sendMessage("Mining Level 5") }
        verify { player.sendMessage("Combat Level 10") }
    }

    @Test
    fun `sendRequirementMessage does nothing when requirements met`() {
        val result = RequirementCheckResult(
            met = true,
            unmetRequirements = emptyList(),
        )

        checker.sendRequirementMessage(player, result)

        verify(exactly = 0) { i18n.send(player, any<String>()) }
        verify(exactly = 0) { player.sendMessage(any<String>()) }
    }
}
