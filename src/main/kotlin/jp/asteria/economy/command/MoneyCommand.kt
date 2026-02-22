package jp.asteria.economy.command

import cn.nukkit.Player
import cn.nukkit.command.Command
import cn.nukkit.command.CommandSender
import cn.nukkit.utils.TextFormat
import jp.asteria.economy.EconomyCore
import jp.asteria.economy.formatMoney

class MoneyCommand : Command("money", "所持金に関するコマンド", "/money help") {
    private val subCommands = mapOf<String, Command>(
        "help" to HelpMoneyCommand(),
        "pay" to PayMoneyCommand(),
        "give" to GiveMoneyCommand(),
        "take" to TakeMoneyCommand(),
        "show" to ShowMoneyCommand(),
    )

    override fun execute(sender: CommandSender, commandLabel: String, args: Array<out String>): Boolean {
        // 引数なしの場合は所持金表示
        if (args.isEmpty()) {
            return executeBalance(sender)
        }

        // サブコマンド実行
        val subCommand = subCommands.getOrDefault(args[0], null)
        if (subCommand == null) {
            sender.sendMessage(usageMessage)
            return false
        }
        return subCommand.execute(sender, args[0], args.drop(1).toTypedArray())
    }

    private fun executeBalance(sender: CommandSender): Boolean {
        if (sender !is Player) {
            sender.sendMessage("${TextFormat.RED}このコマンドはプレイヤーのみ使用できます。")
            return false
        }

        if (!EconomyCore.api.existsData(sender)) {
            sender.sendMessage("${TextFormat.RED}${sender.name}のデータが見つかりません。")
            return false
        }

        val balance = EconomyCore.api.getBalance(sender)
        sender.sendMessage("${TextFormat.GOLD}所持金:${TextFormat.YELLOW}${balance.formatMoney()}")

        return true
    }
}
