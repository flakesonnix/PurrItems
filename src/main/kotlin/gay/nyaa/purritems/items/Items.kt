package gay.nyaa.purritems.items

import gay.nyaa.purritems.abilities.impl.FireStrikeAbility
import gay.nyaa.purritems.domain.ItemDefinition
import gay.nyaa.purritems.domain.ItemId
import gay.nyaa.purritems.domain.Rarity
import gay.nyaa.purritems.requirements.SkillRequirement
import gay.nyaa.purrskills.SkillManager
import gay.nyaa.purrskills.skill.Skill
import gay.nyaa.purrskills.stats.StatModifier
import gay.nyaa.purrskills.stats.StatSource
import gay.nyaa.purrskills.stats.StatType
import org.bukkit.Material

/**
 * Example item definitions.
 */
object Items {
    fun rookieSword(): ItemDefinition = ItemDefinition.builder(
        id = ItemId.of("purr_items", "ROOKIE_SWORD"),
        material = Material.IRON_SWORD,
        rarity = Rarity.COMMON,
        displayName = "Rookie Sword",
    ).withStats(
        listOf(
            StatModifier.additive(StatType.DAMAGE, 10.0, StatSource.ITEM_MAIN_HAND),
            StatModifier.additive(StatType.STRENGTH, 5.0, StatSource.ITEM_MAIN_HAND),
        ),
    ).build()

    fun diamondPickaxe(skillManager: SkillManager): ItemDefinition = ItemDefinition.builder(
        id = ItemId.of("purr_items", "DIAMOND_PICKAXE"),
        material = Material.DIAMOND_PICKAXE,
        rarity = Rarity.RARE,
        displayName = "Enhanced Diamond Pickaxe",
    ).withStats(
        listOf(
            StatModifier.additive(StatType.MINING_SPEED, 120.0, StatSource.ITEM_MAIN_HAND),
            StatModifier.additive(StatType.MINING_FORTUNE, 20.0, StatSource.ITEM_MAIN_HAND),
        ),
    ).withRequirements(
        listOf(
            SkillRequirement(Skill.MINING, 5, skillManager),
        ),
    ).build()

    fun emberBlade(skillManager: SkillManager): ItemDefinition = ItemDefinition.builder(
        id = ItemId.of("purr_items", "EMBER_BLADE"),
        material = Material.DIAMOND_SWORD,
        rarity = Rarity.EPIC,
        displayName = "Ember Blade",
    ).withStats(
        listOf(
            StatModifier.additive(StatType.DAMAGE, 50.0, StatSource.ITEM_MAIN_HAND),
            StatModifier.additive(StatType.STRENGTH, 20.0, StatSource.ITEM_MAIN_HAND),
        ),
    ).withRequirements(
        listOf(
            SkillRequirement(Skill.COMBAT, 10, skillManager),
        ),
    ).withAbilities(
        listOf(
            FireStrikeAbility(),
        ),
    ).build()

    fun all(skillManager: SkillManager): List<ItemDefinition> = listOf(
        rookieSword(),
        diamondPickaxe(skillManager),
        emberBlade(skillManager),
    )
}
