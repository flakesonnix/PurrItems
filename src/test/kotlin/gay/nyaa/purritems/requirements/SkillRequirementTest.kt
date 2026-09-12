package gay.nyaa.purritems.requirements

import com.purrcore.i18n.I18n
import gay.nyaa.purrskills.SkillManager
import gay.nyaa.purrskills.skill.PlayerSkills
import gay.nyaa.purrskills.skill.Skill
import gay.nyaa.purrskills.skill.SkillProfile
import io.mockk.every
import io.mockk.mockk
import org.bukkit.entity.Player
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SkillRequirementTest {
    private lateinit var skillManager: SkillManager
    private lateinit var i18n: I18n
    private lateinit var player: Player
    private lateinit var playerUuid: UUID

    @BeforeEach
    fun setup() {
        skillManager = mockk(relaxed = true)
        i18n = mockk(relaxed = true) {
            every { t(any(), *anyVararg()) } answers {
                val key = firstArg<String>()
                val args = secondArg<Array<out Pair<String, String>>>()
                "$key: ${args.joinToString { "${it.first}=${it.second}" }}"
            }
        }
        playerUuid = UUID.randomUUID()
        player = mockk(relaxed = true) {
            every { uniqueId } returns playerUuid
        }
    }

    @Test
    fun `requirement passes when player meets level`() {
        val playerSkills = mockk<PlayerSkills>()
        val skillProfile = mockk<SkillProfile> {
            every { level } returns 10
        }
        every { skillManager.getPlayerSkills(playerUuid) } returns playerSkills
        every { playerSkills.getSkill(Skill.MINING) } returns skillProfile

        val requirement = SkillRequirement(Skill.MINING, 10, skillManager)

        assertTrue(requirement.check(player))
    }

    @Test
    fun `requirement passes when player exceeds level`() {
        val playerSkills = mockk<PlayerSkills>()
        val skillProfile = mockk<SkillProfile> {
            every { level } returns 15
        }
        every { skillManager.getPlayerSkills(playerUuid) } returns playerSkills
        every { playerSkills.getSkill(Skill.MINING) } returns skillProfile

        val requirement = SkillRequirement(Skill.MINING, 10, skillManager)

        assertTrue(requirement.check(player))
    }

    @Test
    fun `requirement fails when player below level`() {
        val playerSkills = mockk<PlayerSkills>()
        val skillProfile = mockk<SkillProfile> {
            every { level } returns 5
        }
        every { skillManager.getPlayerSkills(playerUuid) } returns playerSkills
        every { playerSkills.getSkill(Skill.MINING) } returns skillProfile

        val requirement = SkillRequirement(Skill.MINING, 10, skillManager)

        assertFalse(requirement.check(player))
    }

    @Test
    fun `reject zero or negative level`() {
        assertThrows<IllegalArgumentException> {
            SkillRequirement(Skill.MINING, 0, skillManager)
        }

        assertThrows<IllegalArgumentException> {
            SkillRequirement(Skill.MINING, -1, skillManager)
        }
    }

    @Test
    fun `description includes skill and level`() {
        val requirement = SkillRequirement(Skill.MINING, 10, skillManager)

        val description = requirement.description(i18n)

        assertTrue(description.contains("Mining"))
        assertTrue(description.contains("10"))
    }

    @Test
    fun `description capitalizes skill name`() {
        val requirement = SkillRequirement(Skill.MINING, 5, skillManager)

        val description = requirement.description(i18n)

        assertTrue(description.contains("Mining"))
        assertFalse(description.contains("mining"))
    }

    @Test
    fun `works with all skill types`() {
        Skill.entries.forEach { skill ->
            val playerSkills = mockk<PlayerSkills>()
            val skillProfile = mockk<SkillProfile> {
                every { level } returns 10
            }
            every { skillManager.getPlayerSkills(playerUuid) } returns playerSkills
            every { playerSkills.getSkill(skill) } returns skillProfile

            val requirement = SkillRequirement(skill, 5, skillManager)

            assertTrue(requirement.check(player))
        }
    }
}
