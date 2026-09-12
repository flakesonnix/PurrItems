package gay.nyaa.purritems.abilities

import com.purrcore.i18n.I18n
import gay.nyaa.purritems.cooldown.CooldownManager
import io.mockk.*
import org.bukkit.entity.Player
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.UUID
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AbilityExecutorTest {
    private lateinit var cooldownManager: CooldownManager
    private lateinit var i18n: I18n
    private lateinit var executor: AbilityExecutor
    private lateinit var player: Player

    @BeforeEach
    fun setup() {
        cooldownManager = mockk(relaxed = true)
        i18n = mockk(relaxed = true)
        executor = AbilityExecutor(cooldownManager, i18n)
        player = mockk(relaxed = true) {
            every { uniqueId } returns UUID.randomUUID()
        }
    }

    @Test
    fun `execute ability without cooldown`() {
        val ability = mockk<Ability> {
            every { id } returns "test_ability"
            every { cooldownSeconds } returns 0
            every { execute(any()) } returns true
        }
        val context = mockk<AbilityContext> {
            every { player } returns this@AbilityExecutorTest.player
        }

        val result = executor.execute(ability, context)

        assertTrue(result)
        verify(exactly = 1) { ability.execute(context) }
        verify(exactly = 0) { cooldownManager.setCooldown(any(), any(), any()) }
    }

    @Test
    fun `execute ability with cooldown`() {
        val ability = mockk<Ability> {
            every { id } returns "test_ability"
            every { cooldownSeconds } returns 10
            every { execute(any()) } returns true
        }
        val context = mockk<AbilityContext> {
            every { player } returns this@AbilityExecutorTest.player
        }

        every { cooldownManager.getRemainingCooldown(any(), any()) } returns 0

        val result = executor.execute(ability, context)

        assertTrue(result)
        verify(exactly = 1) { ability.execute(context) }
        verify(exactly = 1) { cooldownManager.setCooldown(player.uniqueId, "test_ability", 10) }
    }

    @Test
    fun `block ability on cooldown`() {
        val ability = mockk<Ability> {
            every { id } returns "test_ability"
            every { cooldownSeconds } returns 10
            every { execute(any()) } returns true
        }
        val context = mockk<AbilityContext> {
            every { player } returns this@AbilityExecutorTest.player
        }

        every { cooldownManager.getRemainingCooldown(player.uniqueId, "test_ability") } returns 5

        val result = executor.execute(ability, context)

        assertFalse(result)
        verify(exactly = 0) { ability.execute(context) }
        verify { i18n.send(player, "ability.cooldown", "remaining" to "5") }
    }

    @Test
    fun `do not set cooldown on failed execution`() {
        val ability = mockk<Ability> {
            every { id } returns "test_ability"
            every { cooldownSeconds } returns 10
            every { execute(any()) } returns false
        }
        val context = mockk<AbilityContext> {
            every { player } returns this@AbilityExecutorTest.player
        }

        every { cooldownManager.getRemainingCooldown(any(), any()) } returns 0

        val result = executor.execute(ability, context)

        assertFalse(result)
        verify(exactly = 1) { ability.execute(context) }
        verify(exactly = 0) { cooldownManager.setCooldown(any(), any(), any()) }
    }

    @Test
    fun `executeAll filters by trigger`() {
        val ability1 = mockk<Ability> {
            every { id } returns "ability_1"
            every { trigger } returns AbilityTrigger.RIGHT_CLICK
            every { cooldownSeconds } returns 0
            every { execute(any()) } returns true
        }
        val ability2 = mockk<Ability> {
            every { id } returns "ability_2"
            every { trigger } returns AbilityTrigger.LEFT_CLICK
            every { cooldownSeconds } returns 0
            every { execute(any()) } returns true
        }
        val ability3 = mockk<Ability> {
            every { id } returns "ability_3"
            every { trigger } returns AbilityTrigger.RIGHT_CLICK
            every { cooldownSeconds } returns 0
            every { execute(any()) } returns true
        }

        val context = mockk<AbilityContext> {
            every { player } returns this@AbilityExecutorTest.player
            every { trigger } returns AbilityTrigger.RIGHT_CLICK
        }

        executor.executeAll(listOf(ability1, ability2, ability3), context)

        verify(exactly = 1) { ability1.execute(context) }
        verify(exactly = 0) { ability2.execute(context) }
        verify(exactly = 1) { ability3.execute(context) }
    }

    @Test
    fun `executeAll handles empty list`() {
        val context = mockk<AbilityContext> {
            every { player } returns this@AbilityExecutorTest.player
            every { trigger } returns AbilityTrigger.RIGHT_CLICK
        }

        executor.executeAll(emptyList(), context)

        // Should not throw exception
    }

    @Test
    fun `executeAll continues on failure`() {
        val ability1 = mockk<Ability> {
            every { id } returns "ability_1"
            every { trigger } returns AbilityTrigger.RIGHT_CLICK
            every { cooldownSeconds } returns 0
            every { execute(any()) } returns false
        }
        val ability2 = mockk<Ability> {
            every { id } returns "ability_2"
            every { trigger } returns AbilityTrigger.RIGHT_CLICK
            every { cooldownSeconds } returns 0
            every { execute(any()) } returns true
        }

        val context = mockk<AbilityContext> {
            every { player } returns this@AbilityExecutorTest.player
            every { trigger } returns AbilityTrigger.RIGHT_CLICK
        }

        executor.executeAll(listOf(ability1, ability2), context)

        verify(exactly = 1) { ability1.execute(context) }
        verify(exactly = 1) { ability2.execute(context) }
    }
}
