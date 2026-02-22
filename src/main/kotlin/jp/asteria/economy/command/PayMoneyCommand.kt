package jp.asteria.economy.command

import cn.nukkit.Player
import cn.nukkit.command.Command
import cn.nukkit.command.CommandSender
import cn.nukkit.utils.TextFormat
import jp.asteria.economy.EconomyCore
import jp.asteria.economy.formatMoney

class PayMoneyCommand : Command("money", "お金を支払う", "/money pay <プレイヤー> <金額> [支払理由]") {
    init {
        permission = "jp.asteria.economy.commands.pay"
    }

    override fun execute(sender: CommandSender, commandLabel: String, args: Array<out String?>): Boolean {
        if (sender !is Player) {
            sender.sendMessage("${TextFormat.RED}このコマンドはプレイヤーのみ使用できます。")
            return false
        }

        if (args.size < 2) {
            sender.sendMessage(usageMessage)
            return false
        }

        val amount = args[1].runCatching {
            this?.toLong() ?: 0
        }.onFailure {
            sender.sendMessage("${TextFormat.RED}金額は数値で入力してください。")
            return false
        }.getOrDefault(0)

        val target = sender.server.getOfflinePlayer(args[0])
        if (!EconomyCore.api.existsData(sender)) {
            sender.sendMessage("${TextFormat.RED}${sender.name}のデータが見つかりません。")
            return false
        }
        if (!EconomyCore.api.existsData(target)) {
            sender.sendMessage("${TextFormat.RED}${target.name}のデータが見つかりません。")
            return false
        }

        if (EconomyCore.api.transferMoney(sender, target, amount, args.getOrNull(2) ?: "")) {
            sender.sendMessage("${TextFormat.GREEN}${sender.name}から${target.name}に${amount.formatMoney()}を送金しました。")
            if (target is Player) target.sendMessage("${TextFormat.GREEN}${sender.name}から${amount.formatMoney()}を受け取りました。")
            return true
        } else {
            sender.sendMessage("${TextFormat.RED}送金に失敗しました。")
            return false
        }
    }
}
