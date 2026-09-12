package gay.nyaa.purritems.abilities

import org.bukkit.block.Block
import org.bukkit.entity.Entity
import org.bukkit.entity.Player

/**
 * Context data for ability execution.
 */
data class AbilityContext(
    val player: Player,
    val trigger: AbilityTrigger,
    val targetEntity: Entity? = null,
    val targetBlock: Block? = null,
)
