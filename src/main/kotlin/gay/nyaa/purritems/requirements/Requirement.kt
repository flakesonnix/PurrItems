package gay.nyaa.purritems.requirements

import com.purrcore.i18n.I18n
import org.bukkit.entity.Player

/**
 * Requirement that must be met to use an item.
 */
interface Requirement {
    /**
     * Check if player meets requirement.
     */
    fun check(player: Player): Boolean

    /**
     * Get human-readable description.
     */
    fun description(i18n: I18n): String
}
