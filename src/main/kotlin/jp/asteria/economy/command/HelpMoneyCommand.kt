package jp.asteria.economy.command

import org.powernukkitx.command.Command
import org.powernukkitx.command.CommandSender

class HelpMoneyCommand : Command("money", "使い方表示", "/money help") {
    init {
        permission = "jp.asteria.economy.commands.help"
    }

    override fun execute(sender: CommandSender, commandLabel: String, args: Array<out String?>): Boolean {
        listOf<Command>(
            MoneyCommand(),
            HelpMoneyCommand(),
            PayMoneyCommand(),
            GiveMoneyCommand(),
            TakeMoneyCommand(),
            ShowMoneyCommand()
        ).forEach {
            if (it.testPermission(sender)) sender.sendMessage("${it.description}：${it.usage}")
        }
        return true
    }
}
