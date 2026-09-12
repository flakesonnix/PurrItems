package gay.nyaa.purritems.requirements

import com.purrcore.i18n.I18n
import gay.nyaa.purritems.domain.ItemDefinition
import org.bukkit.entity.Player

/**
 * Checks if player meets item requirements.
 */
class RequirementChecker(private val i18n: I18n) {
    /**
     * Check if player meets all requirements.
     */
    fun checkRequirements(
        player: Player,
        definition: ItemDefinition,
    ): RequirementCheckResult {
        val unmet = definition.requirements.filter { !it.check(player) }
        return RequirementCheckResult(
            met = unmet.isEmpty(),
            unmetRequirements = unmet,
        )
    }

    /**
     * Send requirement failure message to player.
     */
    fun sendRequirementMessage(
        player: Player,
        result: RequirementCheckResult,
    ) {
        if (!result.met) {
            i18n.send(player, "requirement.not-met")
            result.unmetRequirements.forEach { req ->
                player.sendMessage(req.description(i18n))
            }
        }
    }
}

/**
 * Result of requirement check.
 */
data class RequirementCheckResult(
    val met: Boolean,
    val unmetRequirements: List<Requirement>,
)
