package jp.asteria.economy

import org.powernukkitx.IPlayer
import org.powernukkitx.Server
import org.powernukkitx.event.EventHandler
import org.powernukkitx.event.EventPriority
import org.powernukkitx.event.Listener
import org.powernukkitx.event.player.PlayerJoinEvent
import org.powernukkitx.plugin.PluginBase
import jp.asteria.dbconnector.Database
import jp.asteria.economy.api.EconomyApi
import jp.asteria.economy.command.MoneyCommand
import jp.asteria.economy.infrastructure.MoneyTransactionsTable
import jp.asteria.economy.infrastructure.WalletsTable
import jp.asteria.player.primaryId
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.jdbc.Database as ExposedDatabase

/** サーバーへの登録と旧IPlayer APIの接続。通貨規則・保存はEconomyApiが所有する。 */
class EconomyCore : PluginBase(), Listener, IEconomyCore {
    companion object {
        lateinit var api: IEconomyCore
            private set
        const val DEFAULT_BALANCE = 1000L
        const val CURRENCY_UNIT = "円"
    }

    override fun onLoad() { api = this }

    override fun onEnable() {
        try {
            ExposedDatabase.connect(Database.getDataSource())
            transaction { SchemaUtils.create(WalletsTable, MoneyTransactionsTable) }
            EconomyApi.activate()
            server.pluginManager.registerEvents(this, this)
            server.commandMap.register("money", MoneyCommand())
        } catch (failure: Throwable) {
            EconomyApi.deactivate()
            throw failure
        }
    }

    override fun onDisable() { EconomyApi.deactivate() }

    @EventHandler(priority = EventPriority.LOW)
    fun onJoin(event: PlayerJoinEvent) { EconomyApi.ensureWallet(requireIdentity(event.player)) }

    private fun identity(player: IPlayer): Int? {
        check(Database.getTransactionConnection() == null) { "IPlayer economy APIs cannot join Database.transaction" }
        check(Server.getInstance()?.isPrimaryThread == true) { "IPlayer economy APIs require the server primary thread; use EconomyApi with a player ID" }
        return player.primaryId
    }
    private fun requireIdentity(player: IPlayer): Int = requireNotNull(identity(player)) { "Missing player identity" }
    private inline fun legacyMutation(action: () -> Boolean): Boolean = try { action() }
        catch (_: ArithmeticException) { false }

    override fun existsData(player: IPlayer): Boolean = identity(player)?.let(EconomyApi::existsData) ?: false
    override fun getBalance(player: IPlayer): Long = EconomyApi.getBalance(requireIdentity(player))
    override fun hasEnoughMoney(player: IPlayer, amount: Long): Boolean = amount >= 0 &&
        EconomyApi.hasEnoughMoney(requireIdentity(player), amount)

    override fun increaseMoney(player: IPlayer, amount: Long, reason: String): Boolean {
        if (amount < 0) return false
        val playerId = requireIdentity(player)
        return legacyMutation { EconomyApi.increaseMoney(playerId, amount, reason) }
    }
    override fun decreaseMoney(player: IPlayer, amount: Long, reason: String): Boolean {
        if (amount < 0) return false
        val playerId = requireIdentity(player)
        return legacyMutation { EconomyApi.decreaseMoney(playerId, amount, reason) }
    }
    override fun transferMoney(from: IPlayer, to: IPlayer, amount: Long, reason: String): Boolean {
        if (amount < 0) return false
        val source = requireIdentity(from)
        val destination = requireIdentity(to)
        val details = reason.ifEmpty { "${from.name}から${to.name}へ送金" }
        return legacyMutation { EconomyApi.transferMoney(source, destination, amount, details) }
    }
}
