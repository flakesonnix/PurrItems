package gay.nyaa.purritems.requirements

import com.purrcore.i18n.I18n
import gay.nyaa.purrskills.SkillManager
import gay.nyaa.purrskills.skill.Skill
import org.bukkit.entity.Player

/**
 * Requires specific skill level.
 */
data class SkillRequirement(
    val skill: Skill,
    val level: Int,
    private val skillManager: SkillManager,
) : Requirement {
    init {
        require(level > 0) { "Level must be positive" }
    }

    override fun check(player: Player): Boolean {
        val playerSkills = skillManager.getPlayerSkills(player.uniqueId)
        val skillProfile = playerSkills.getSkill(skill)
        return skillProfile.level >= level
    }

    override fun description(i18n: I18n): String = i18n.t(
        "requirement.skill",
        "skill" to skill.name.lowercase().replaceFirstChar { it.uppercase() },
        "level" to level.toString(),
    )
}
