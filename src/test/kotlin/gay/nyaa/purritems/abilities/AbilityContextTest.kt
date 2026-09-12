package gay.nyaa.purritems.abilities

import io.mockk.mockk
import org.bukkit.block.Block
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class AbilityContextTest {
    @Test
    fun `create minimal context`() {
        val player = mockk<Player>()
        val context = AbilityContext(
            player = player,
            trigger = AbilityTrigger.RIGHT_CLICK,
        )

        assertEquals(player, context.player)
        assertEquals(AbilityTrigger.RIGHT_CLICK, context.trigger)
        assertNull(context.targetEntity)
        assertNull(context.targetBlock)
    }

    @Test
    fun `create context with target entity`() {
        val player = mockk<Player>()
        val target = mockk<Entity>()
        val context = AbilityContext(
            player = player,
            trigger = AbilityTrigger.ENTITY_HIT,
            targetEntity = target,
        )

        assertNotNull(context.targetEntity)
        assertEquals(target, context.targetEntity)
        assertNull(context.targetBlock)
    }

    @Test
    fun `create context with target block`() {
        val player = mockk<Player>()
        val block = mockk<Block>()
        val context = AbilityContext(
            player = player,
            trigger = AbilityTrigger.BLOCK_BREAK,
            targetBlock = block,
        )

        assertNotNull(context.targetBlock)
        assertEquals(block, context.targetBlock)
        assertNull(context.targetEntity)
    }

    @Test
    fun `create context with both targets`() {
        val player = mockk<Player>()
        val entity = mockk<Entity>()
        val block = mockk<Block>()
        val context = AbilityContext(
            player = player,
            trigger = AbilityTrigger.ENTITY_HIT,
            targetEntity = entity,
            targetBlock = block,
        )

        assertNotNull(context.targetEntity)
        assertNotNull(context.targetBlock)
        assertEquals(entity, context.targetEntity)
        assertEquals(block, context.targetBlock)
    }
}
