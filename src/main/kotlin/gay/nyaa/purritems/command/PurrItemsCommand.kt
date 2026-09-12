package gay.nyaa.purritems.command

import com.purrcore.i18n.I18n
import gay.nyaa.purritems.ItemManager
import gay.nyaa.purritems.domain.ItemId
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player

/**
 * /purritems command executor.
 */
class PurrItemsCommand(
    private val itemManager: ItemManager,
    private val i18n: I18n,
) : CommandExecutor,
    TabCompleter {
    override fun onCommand(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>,
    ): Boolean {
        if (args.isEmpty()) {
            sender.sendMessage("§6PurrItems commands:")
            sender.sendMessage("§7/purritems give <player> <item> [amount]")
            sender.sendMessage("§7/purritems info")
            return true
        }

        return when (args[0].lowercase()) {
            "give" -> handleGive(sender, args)
            "info" -> handleInfo(sender)
            else -> {
                sender.sendMessage("§cUnknown subcommand: ${args[0]}")
                false
            }
        }
    }

    private fun handleGive(
        sender: CommandSender,
        args: Array<out String>,
    ): Boolean {
        if (args.size < 3) {
            sender.sendMessage("§cUsage: /purritems give <player> <item> [amount]")
            return false
        }

        val playerName = args[1]
        val itemIdStr = args[2]
        val amount = args.getOrNull(3)?.toIntOrNull() ?: 1

        val target = Bukkit.getPlayerExact(playerName)
        if (target == null) {
            i18n.send(sender, "command.give.invalid-player", "player" to playerName)
            return false
        }

        val itemId =
            try {
                ItemId.parse(itemIdStr)
            } catch (e: Exception) {
                i18n.send(sender, "command.give.invalid-item", "item" to itemIdStr)
                return false
            }

        val itemStack = itemManager.createItem(itemId, amount)
        if (itemStack == null) {
            i18n.send(sender, "command.give.invalid-item", "item" to itemIdStr)
            return false
        }

        target.inventory.addItem(itemStack)
        i18n.send(
            sender,
            "command.give.success",
            "amount" to amount.toString(),
            "item" to itemId.toString(),
            "player" to target.name,
        )
        return true
    }

    private fun handleInfo(sender: CommandSender): Boolean {
        if (sender !is Player) {
            sender.sendMessage("§cOnly players can use this command")
            return false
        }

        val item = sender.inventory.itemInMainHand
        val definition = itemManager.itemSerializer.getDefinition(item)
        if (definition == null) {
            i18n.send(sender, "command.info.not-custom")
            return false
        }

        sender.sendMessage(i18n.t("command.info.header"))
        sender.sendMessage(i18n.t("command.info.id", "id" to definition.id.toString()))
        sender.sendMessage(i18n.t("command.info.rarity", "rarity" to definition.rarity.name))
        return true
    }

    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>,
    ): List<String>? = when (args.size) {
        1 -> listOf("give", "info").filter { it.startsWith(args[0].lowercase()) }
        2 ->
            if (args[0].equals("give", ignoreCase = true)) {
                Bukkit.getOnlinePlayers().map { it.name }.filter { it.startsWith(args[1], ignoreCase = true) }
            } else {
                null
            }
        3 ->
            if (args[0].equals("give", ignoreCase = true)) {
                itemManager.getAllItems().map { it.id.toString() }.filter { it.startsWith(args[2], ignoreCase = true) }
            } else {
                null
            }
        else -> null
    }
}
