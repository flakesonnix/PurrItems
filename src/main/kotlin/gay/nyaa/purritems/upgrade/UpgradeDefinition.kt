package gay.nyaa.purritems.upgrade

import gay.nyaa.purritems.domain.ItemId

/**
 * Defines an upgrade path.
 */
data class UpgradeDefinition(
    val from: ItemId,
    val to: ItemId,
    val cost: UpgradeCost = UpgradeCost.None,
)

/**
 * Cost for upgrading.
 * Extensible for future economy/material requirements.
 */
sealed class UpgradeCost {
    data object None : UpgradeCost()
    // Future: data class Money(val amount: Double) : UpgradeCost()
    // Future: data class Materials(val items: Map<Material, Int>) : UpgradeCost()
}
