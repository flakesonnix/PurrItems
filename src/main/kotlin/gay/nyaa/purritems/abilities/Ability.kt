package gay.nyaa.purritems.abilities

/**
 * Item ability.
 */
interface Ability {
    /**
     * Unique ability ID.
     */
    val id: String

    /**
     * When ability triggers.
     */
    val trigger: AbilityTrigger

    /**
     * Cooldown in seconds (0 = no cooldown).
     */
    val cooldownSeconds: Int

    /**
     * Execute ability.
     * @return true if ability was executed, false if execution failed
     */
    fun execute(context: AbilityContext): Boolean
}
