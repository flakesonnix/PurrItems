package gay.nyaa.purritems.integration

import com.purrcore.i18n.I18n
import gay.nyaa.purritems.abilities.Ability
import gay.nyaa.purritems.abilities.AbilityContext
import gay.nyaa.purritems.abilities.AbilityExecutor
import gay.nyaa.purritems.abilities.AbilityTrigger
import gay.nyaa.purritems.cooldown.CooldownManager
import io.mockk.*
import org.bukkit.entity.Player
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.UUID
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Integration tests for ability + cooldown system.
 */
class AbilityCooldownIntegrationTest {
    private lateinit var cooldownManager: CooldownManager
    private lateinit var i18n: I18n
    private lateinit var abilityExecutor: AbilityExecutor
    private lateinit var player: Player
    private val playerUuid = UUID.randomUUID()

    @BeforeEach
    fun setup() {
        cooldownManager = CooldownManager()
        i18n = mockk(relaxed = true)
        abilityExecutor = AbilityExecutor(cooldownManager, i18n)
        player = mockk(relaxed = true)
        every { player.uniqueId } returns playerUuid
    }

    @Test
    fun `ability with cooldown blocks second execution`() {
        val ability =
            mockk<Ability> {
                every { id } returns "test_ability"
                every { trigger } returns AbilityTrigger.RIGHT_CLICK
                every { cooldownSeconds } returns 5
                every { execute(any()) } returns true
            }

        val context = AbilityContext(player, AbilityTrigger.RIGHT_CLICK)

        // First execution succeeds
        val first = abilityExecutor.execute(ability, context)
        assertTrue(first)
        verify(exactly = 1) { ability.execute(context) }

        // Second execution blocked by cooldown
        val second = abilityExecutor.execute(ability, context)
        assertFalse(second)
        verify(exactly = 1) { ability.execute(context) } // Still only 1 call

        // Verify cooldown message sent
        verify { i18n.send(player, "ability.cooldown", any()) }
    }

    @Test
    fun `ability without cooldown executes multiple times`() {
        val ability =
            mockk<Ability> {
                every { id } returns "no_cooldown_ability"
                every { trigger } returns AbilityTrigger.RIGHT_CLICK
                every { cooldownSeconds } returns 0
                every { execute(any()) } returns true
            }

        val context = AbilityContext(player, AbilityTrigger.RIGHT_CLICK)

        // Both executions succeed
        assertTrue(abilityExecutor.execute(ability, context))
        assertTrue(abilityExecutor.execute(ability, context))

        verify(exactly = 2) { ability.execute(context) }
    }

    @Test
    fun `failed ability execution doesn't set cooldown`() {
        val ability =
            mockk<Ability> {
                every { id } returns "failing_ability"
                every { trigger } returns AbilityTrigger.RIGHT_CLICK
                every { cooldownSeconds } returns 5
                every { execute(any()) } returns false // Fails
            }

        val context = AbilityContext(player, AbilityTrigger.RIGHT_CLICK)

        // First execution fails
        val first = abilityExecutor.execute(ability, context)
        assertFalse(first)

        // Second execution should try again (no cooldown set)
        val second = abilityExecutor.execute(ability, context)
        assertFalse(second)

        verify(exactly = 2) { ability.execute(context) }
    }

    @Test
    fun `cooldown expires after duration`() {
        val ability =
            mockk<Ability> {
                every { id } returns "short_cooldown"
                every { trigger } returns AbilityTrigger.RIGHT_CLICK
                every { cooldownSeconds } returns 0 // Instant expire
                every { execute(any()) } returns true
            }

        val context = AbilityContext(player, AbilityTrigger.RIGHT_CLICK)

        // First execution
        assertTrue(abilityExecutor.execute(ability, context))

        // Wait for cooldown expiry
        Thread.sleep(100)

        // Second execution succeeds (cooldown expired)
        assertTrue(abilityExecutor.execute(ability, context))

        verify(exactly = 2) { ability.execute(context) }
    }

    @Test
    fun `executeAll only triggers matching abilities`() {
        val rightClickAbility =
            mockk<Ability> {
                every { id } returns "right_click"
                every { trigger } returns AbilityTrigger.RIGHT_CLICK
                every { cooldownSeconds } returns 0
                every { execute(any()) } returns true
            }

        val leftClickAbility =
            mockk<Ability> {
                every { id } returns "left_click"
                every { trigger } returns AbilityTrigger.LEFT_CLICK
                every { cooldownSeconds } returns 0
                every { execute(any()) } returns true
            }

        val abilities = listOf(rightClickAbility, leftClickAbility)
        val context = AbilityContext(player, AbilityTrigger.RIGHT_CLICK)

        abilityExecutor.executeAll(abilities, context)

        verify(exactly = 1) { rightClickAbility.execute(context) }
        verify(exactly = 0) { leftClickAbility.execute(any()) }
    }
}
