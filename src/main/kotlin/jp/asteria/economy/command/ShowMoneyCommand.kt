package jp.asteria.economy.command

import cn.nukkit.command.Command
import cn.nukkit.command.CommandSender
import cn.nukkit.utils.TextFormat
import jp.asteria.economy.EconomyCore
import jp.asteria.economy.formatMoney

class ShowMoneyCommand : Command("money", "所持金を確認する", "/money show <プレイヤー>") {
    init {
        permission = "jp.asteria.economy.commands.show"
    }

    override fun execute(sender: CommandSender, commandLabel: String, args: Array<out String?>): Boolean {
        if (args.size < 1) {
            sender.sendMessage(usageMessage)
            return false
        }

        val target = sender.server.getOfflinePlayer(args[0])
        if (!EconomyCore.api.existsData(target)) {
            sender.sendMessage("${TextFormat.RED}${target.name}のデータが見つかりません。")
            return false
        }

        val balance = EconomyCore.api.getBalance(target)
        sender.sendMessage("${TextFormat.GOLD}${target.name}の所持金：${TextFormat.YELLOW}${balance.formatMoney()}")

        return true
    }
}
