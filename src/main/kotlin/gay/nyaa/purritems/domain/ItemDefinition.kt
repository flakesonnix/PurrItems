package gay.nyaa.purritems.domain

import gay.nyaa.purritems.abilities.Ability
import gay.nyaa.purritems.requirements.Requirement
import gay.nyaa.purrskills.stats.StatModifier
import org.bukkit.Material

/**
 * Immutable template describing an item.
 * This is the definition, not an instance.
 */
data class ItemDefinition(
    val id: ItemId,
    val material: Material,
    val rarity: Rarity,
    val displayName: String,
    val stats: List<StatModifier> = emptyList(),
    val requirements: List<Requirement> = emptyList(),
    val abilities: List<Ability> = emptyList(),
    val upgradesTo: ItemId? = null,
) {
    init {
        require(displayName.isNotBlank()) { "Display name cannot be blank" }
    }

    /**
     * Create a builder for upgrading this item.
     */
    fun toBuilder() = Builder(id, material, rarity, displayName)
        .withStats(stats)
        .withRequirements(requirements)
        .withAbilities(abilities)
        .apply { upgradesTo?.let { withUpgradesTo(it) } }

    class Builder(
        private val id: ItemId,
        private var material: Material,
        private var rarity: Rarity,
        private var displayName: String,
    ) {
        private var stats: List<StatModifier> = emptyList()
        private var requirements: List<Requirement> = emptyList()
        private var abilities: List<Ability> = emptyList()
        private var upgradesTo: ItemId? = null

        fun withMaterial(material: Material) = apply { this.material = material }

        fun withRarity(rarity: Rarity) = apply { this.rarity = rarity }

        fun withDisplayName(displayName: String) = apply { this.displayName = displayName }

        fun withStats(stats: List<StatModifier>) = apply { this.stats = stats.toList() }

        fun withRequirements(requirements: List<Requirement>) = apply { this.requirements = requirements.toList() }

        fun withAbilities(abilities: List<Ability>) = apply { this.abilities = abilities.toList() }

        fun withUpgradesTo(itemId: ItemId?) = apply { this.upgradesTo = itemId }

        fun build() = ItemDefinition(
            id = id,
            material = material,
            rarity = rarity,
            displayName = displayName,
            stats = stats,
            requirements = requirements,
            abilities = abilities,
            upgradesTo = upgradesTo,
        )
    }

    companion object {
        fun builder(
            id: ItemId,
            material: Material,
            rarity: Rarity,
            displayName: String,
        ) = Builder(id, material, rarity, displayName)
    }
}
