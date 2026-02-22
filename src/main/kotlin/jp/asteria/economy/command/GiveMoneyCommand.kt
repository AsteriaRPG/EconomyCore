package jp.asteria.economy.command

import cn.nukkit.Player
import cn.nukkit.command.Command
import cn.nukkit.command.CommandSender
import cn.nukkit.utils.TextFormat
import jp.asteria.economy.EconomyCore
import jp.asteria.economy.formatMoney

class GiveMoneyCommand : Command("money", "お金を付与する", "/money give <プレイヤー> <金額>") {
    init {
        permission = "jp.asteria.economy.commands.give"
    }

    override fun execute(sender: CommandSender, commandLabel: String, args: Array<out String?>): Boolean {
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

        if (amount < 0) {
            sender.sendMessage("金額は0以上を入力してください。")
            return false
        }

        val target = sender.server.getOfflinePlayer(args[0])
        if (!EconomyCore.api.existsData(target)) {
            sender.sendMessage("${TextFormat.RED}${target.name}のデータが見つかりません。")
            return false
        }

        if (EconomyCore.api.increaseMoney(target, amount, "管理者(${sender.name})からの付与")) {
            sender.sendMessage("${TextFormat.GREEN}${target.name}に${amount.formatMoney()}を付与しました。")
            if (target is Player) target.sendMessage("管理者(${sender.name})から${amount.formatMoney()}を受け取りました。")
            return true
        } else {
            sender.sendMessage("${TextFormat.RED}金額の付与に失敗しました")
            return false
        }
    }
}
