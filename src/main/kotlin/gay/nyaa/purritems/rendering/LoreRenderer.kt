package gay.nyaa.purritems.rendering

import com.purrcore.i18n.I18n
import gay.nyaa.purritems.domain.ItemDefinition
import gay.nyaa.purrskills.stats.StatType
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration

/**
 * Renders item lore from definition.
 */
class LoreRenderer(private val i18n: I18n) {
    /**
     * Rendered item display.
     */
    data class RenderedItem(
        val displayName: Component,
        val lore: List<Component>,
    )

    fun render(definition: ItemDefinition): RenderedItem {
        val lore = mutableListOf<Component>()

        // Blank line after name
        lore.add(Component.empty())

        // Stats
        if (definition.stats.isNotEmpty()) {
            definition.stats
                .sortedBy { it.stat.ordinal }
                .forEach { modifier ->
                    val statName = formatStatName(modifier.stat)
                    val value = formatStatValue(modifier.stat, modifier.value)
                    lore.add(
                        Component
                            .text("$statName: ", NamedTextColor.GRAY)
                            .append(Component.text(value, NamedTextColor.GREEN))
                            .decoration(TextDecoration.ITALIC, false),
                    )
                }
            lore.add(Component.empty())
        }

        // Abilities
        if (definition.abilities.isNotEmpty()) {
            definition.abilities.forEach { ability ->
                val abilityName = i18n.t("ability.${ability.id}.name")
                val abilityDesc = i18n.t("ability.${ability.id}.desc")
                lore.add(
                    Component
                        .text("Ability: ", NamedTextColor.GOLD)
                        .append(Component.text(abilityName))
                        .decoration(TextDecoration.ITALIC, false),
                )
                lore.add(
                    Component
                        .text(abilityDesc, NamedTextColor.GRAY)
                        .decoration(TextDecoration.ITALIC, false),
                )
            }
            lore.add(Component.empty())
        }

        // Requirements
        if (definition.requirements.isNotEmpty()) {
            definition.requirements.forEach { requirement ->
                lore.add(
                    Component
                        .text(requirement.description(i18n), NamedTextColor.RED)
                        .decoration(TextDecoration.ITALIC, false)
                        .decoration(TextDecoration.BOLD, true),
                )
            }
            lore.add(Component.empty())
        }

        // Rarity (last line)
        val rarityText = i18n.t(definition.rarity.displayKey)
        lore.add(
            Component
                .text(rarityText)
                .decoration(TextDecoration.ITALIC, false)
                .decoration(TextDecoration.BOLD, true),
        )

        val displayName =
            Component
                .text(definition.displayName)
                .decoration(TextDecoration.ITALIC, false)

        return RenderedItem(displayName, lore)
    }

    private fun formatStatName(stat: StatType): String = when (stat) {
        StatType.MINING_SPEED -> "Mining Speed"
        StatType.MINING_FORTUNE -> "Mining Fortune"
        StatType.FARMING_SPEED -> "Farming Speed"
        StatType.FARMING_FORTUNE -> "Farming Fortune"
        StatType.FORAGING_SPEED -> "Foraging Speed"
        StatType.FORAGING_FORTUNE -> "Foraging Fortune"
        StatType.FISHING_SPEED -> "Fishing Speed"
        StatType.SEA_CREATURE_CHANCE -> "Sea Creature Chance"
        StatType.HEALTH -> "Health"
        StatType.DAMAGE -> "Damage"
        StatType.DEFENSE -> "Defense"
        StatType.STRENGTH -> "Strength"
        StatType.CRIT_CHANCE -> "Crit Chance"
        StatType.CRIT_DAMAGE -> "Crit Damage"
        StatType.SPEED -> "Speed"
        StatType.MAGIC_FIND -> "Magic Find"
    }

    private fun formatStatValue(
        stat: StatType,
        value: Double,
    ): String {
        val prefix = if (value > 0) "+" else ""
        return when {
            value % 1.0 == 0.0 -> "$prefix${value.toInt()}"
            else -> "$prefix%.1f".format(value)
        }
    }
}
