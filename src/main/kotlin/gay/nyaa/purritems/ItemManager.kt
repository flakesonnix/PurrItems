package gay.nyaa.purritems

import com.purrcore.i18n.I18n
import gay.nyaa.purritems.abilities.AbilityExecutor
import gay.nyaa.purritems.abilities.AbilityRegistry
import gay.nyaa.purritems.domain.ItemDefinition
import gay.nyaa.purritems.domain.ItemId
import gay.nyaa.purritems.registry.ItemRegistry
import gay.nyaa.purritems.requirements.RequirementChecker
import gay.nyaa.purritems.serialization.ItemSerializer
import gay.nyaa.purritems.stats.ItemStatsProvider
import gay.nyaa.purritems.upgrade.UpgradeManager
import gay.nyaa.purrskills.SkillManager
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

/**
 * Central manager orchestrating item operations.
 */
class ItemManager(
    val registry: ItemRegistry,
    val itemSerializer: ItemSerializer,
    val statsProvider: ItemStatsProvider,
    val requirementChecker: RequirementChecker,
    val abilityRegistry: AbilityRegistry,
    val abilityExecutor: AbilityExecutor,
    val upgradeManager: UpgradeManager,
    val skillManager: SkillManager,
    private val i18n: I18n,
) {
    private var recipeUnlockCheck: ((Player, ItemId) -> Boolean)? = null

    /**
     * Set recipe unlock checker (injected by PurrCollections).
     */
    fun setRecipeUnlockChecker(checker: (Player, ItemId) -> Boolean) {
        recipeUnlockCheck = checker
    }

    /**
     * Register item definition.
     */
    fun registerItem(definition: ItemDefinition) {
        registry.register(definition)
    }

    /**
     * Get item by ID.
     */
    fun getItem(id: ItemId): ItemDefinition? = registry.get(id)

    /**
     * Create ItemStack from ID.
     */
    fun createItem(
        id: ItemId,
        amount: Int = 1,
    ): ItemStack? {
        val definition = registry.get(id) ?: return null
        return itemSerializer.createItemStack(definition, amount)
    }

    /**
     * Check if player can use item.
     */
    fun canUseItem(
        player: Player,
        stack: ItemStack,
    ): Boolean {
        val definition = itemSerializer.getDefinition(stack) ?: return true
        val result = requirementChecker.checkRequirements(player, definition)
        if (!result.met) {
            requirementChecker.sendRequirementMessage(player, result)
        }
        return result.met
    }

    /**
     * Refresh player item stats (called on equip/unequip).
     */
    fun refreshPlayerStats(player: Player) {
        statsProvider.refreshPlayerStats(player)
    }

    /**
     * Get all registered items.
     */
    fun getAllItems(): Collection<ItemDefinition> = registry.all()

    /**
     * Check if player has unlocked recipe for item.
     */
    fun hasUnlockedRecipe(
        player: Player,
        itemId: ItemId,
    ): Boolean = recipeUnlockCheck?.invoke(player, itemId) ?: true
}
