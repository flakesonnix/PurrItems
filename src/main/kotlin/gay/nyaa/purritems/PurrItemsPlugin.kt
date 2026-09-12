package gay.nyaa.purritems

import com.purrcore.PurrCorePlugin
import gay.nyaa.purritems.abilities.AbilityExecutor
import gay.nyaa.purritems.abilities.AbilityRegistry
import gay.nyaa.purritems.api.PurrItemsAPI
import gay.nyaa.purritems.api.PurrItemsAPIImpl
import gay.nyaa.purritems.command.PurrItemsCommand
import gay.nyaa.purritems.cooldown.CooldownManager
import gay.nyaa.purritems.items.Items
import gay.nyaa.purritems.listener.BlockBreakListener
import gay.nyaa.purritems.listener.EntityDamageListener
import gay.nyaa.purritems.listener.PlayerInteractListener
import gay.nyaa.purritems.listener.PlayerLifecycleListener
import gay.nyaa.purritems.registry.ItemRegistry
import gay.nyaa.purritems.rendering.LoreRenderer
import gay.nyaa.purritems.requirements.RequirementChecker
import gay.nyaa.purritems.serialization.ItemSerializer
import gay.nyaa.purritems.stats.ItemStatsProvider
import gay.nyaa.purritems.upgrade.UpgradeManager
import gay.nyaa.purrskills.PurrSkillsPlugin
import org.bukkit.plugin.java.JavaPlugin

/**
 * PurrItems plugin main class.
 */
class PurrItemsPlugin : JavaPlugin() {
    internal lateinit var itemManager: ItemManager
        private set

    lateinit var api: PurrItemsAPI
        private set

    private lateinit var cooldownManager: CooldownManager

    override fun onEnable() {
        try {
            // Get dependencies
            val purrCore = PurrCorePlugin.get()
            val i18n = purrCore.i18n
            val purrSkills = server.pluginManager.getPlugin("PurrSkills") as? PurrSkillsPlugin
            requireNotNull(purrSkills) { "PurrSkills plugin not found" }
            val skillManager = purrSkills.skillManager
            val statsManager = purrSkills.statsManager

            // Initialize core components
            val itemRegistry = ItemRegistry()
            val abilityRegistry = AbilityRegistry()
            val loreRenderer = LoreRenderer(i18n)
            val itemSerializer = ItemSerializer(this, itemRegistry, loreRenderer)
            val statsProvider = ItemStatsProvider(itemSerializer, statsManager)
            val requirementChecker = RequirementChecker(i18n)
            cooldownManager = CooldownManager()
            val abilityExecutor = AbilityExecutor(cooldownManager, i18n)
            val upgradeManager = UpgradeManager(itemRegistry, itemSerializer)

            // Create ItemManager
            itemManager =
                ItemManager(
                    registry = itemRegistry,
                    itemSerializer = itemSerializer,
                    statsProvider = statsProvider,
                    requirementChecker = requirementChecker,
                    abilityRegistry = abilityRegistry,
                    abilityExecutor = abilityExecutor,
                    upgradeManager = upgradeManager,
                    skillManager = skillManager,
                    i18n = i18n,
                )

            // Register example items
            Items.all(skillManager).forEach { itemManager.registerItem(it) }
            logger.info("Registered ${itemRegistry.size()} items")

            // Register listeners
            server.pluginManager.apply {
                registerEvents(PlayerLifecycleListener(itemManager), this@PurrItemsPlugin)
                registerEvents(PlayerInteractListener(itemManager), this@PurrItemsPlugin)
                registerEvents(BlockBreakListener(itemManager), this@PurrItemsPlugin)
                registerEvents(EntityDamageListener(itemManager), this@PurrItemsPlugin)
            }

            // Register commands
            getCommand("purritems")?.apply {
                val cmdExecutor = PurrItemsCommand(itemManager, i18n)
                setExecutor(cmdExecutor)
                tabCompleter = cmdExecutor
            }

            // Schedule cooldown cleanup task (every 60 seconds)
            server.scheduler.runTaskTimer(
                this,
                Runnable { cooldownManager.cleanupExpired() },
                1200L, // 60 seconds
                1200L,
            )

            // Create public API
            api = PurrItemsAPIImpl(itemManager)

            // Store instance
            Instance = this

            // Hook PurrCollections if available (deferred one tick: it loads after us).
            // Uses the public PurrCollectionsAPI — no reflection, compile-time checked.
            server.scheduler.runTask(
                this,
                Runnable {
                    val purrCollections =
                        server.pluginManager.getPlugin("PurrCollections")
                            as? gay.nyaa.purrcollections.PurrCollectionsPlugin
                    if (purrCollections == null || !purrCollections.isEnabled) {
                        logger.info("PurrCollections not found - recipe unlock gating disabled (all recipes open)")
                        return@Runnable
                    }
                    try {
                        val api = purrCollections.getAPI()
                        itemManager.setRecipeUnlockChecker { player, itemId ->
                            api.hasUnlockedRecipe(player.uniqueId, itemId.toString())
                        }
                        logger.info("PurrCollections recipe unlock integration enabled")
                    } catch (e: Exception) {
                        logger.warning("Failed to integrate with PurrCollections: ${e.message}")
                    }
                },
            )

            logger.info("PurrItems enabled successfully!")
        } catch (e: Exception) {
            logger.severe("Failed to enable PurrItems: ${e.message}")
            e.printStackTrace()
            server.pluginManager.disablePlugin(this)
        }
    }

    override fun onDisable() {
        // Clear cooldowns
        if (::cooldownManager.isInitialized) {
            server.onlinePlayers.forEach { player ->
                cooldownManager.clearAllCooldowns(player.uniqueId)
            }
        }

        // Clear stats
        if (::itemManager.isInitialized) {
            server.onlinePlayers.forEach { player ->
                itemManager.statsProvider.clearPlayerStats(player.uniqueId)
            }
        }

        Instance = null
        logger.info("PurrItems disabled")
    }

    companion object {
        @Suppress("ktlint:standard:property-naming")
        var Instance: PurrItemsPlugin? = null
            private set

        fun get(): PurrItemsPlugin = Instance ?: throw IllegalStateException("PurrItems not enabled")

        fun isAvailable(): Boolean = Instance != null
    }
}
