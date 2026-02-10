package jp.asteria.economy

import cn.nukkit.IPlayer
import cn.nukkit.event.EventHandler
import cn.nukkit.event.EventPriority
import cn.nukkit.event.Listener
import cn.nukkit.event.player.PlayerJoinEvent
import cn.nukkit.plugin.PluginBase
import jp.asteria.dbconnector.Database
import jp.asteria.economy.infrastructure.MoneyTransactionEntity
import jp.asteria.economy.infrastructure.MoneyTransactionsTable
import jp.asteria.economy.infrastructure.WalletEntity
import jp.asteria.economy.infrastructure.WalletsTable
import jp.asteria.player.infrastructure.PlayerEntity
import jp.asteria.player.primaryId
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.exists
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import kotlin.time.Clock
import org.jetbrains.exposed.v1.jdbc.Database as ExposedDatabase

/**
 * 経済管理プラグイン
 *
 * プレイヤーの所持金を管理
 */
class EconomyCore : PluginBase(), Listener, IEconomyCore {
    companion object {
        lateinit var api: IEconomyCore
            private set

        /** 初期所持金 */
        const val DEFAULT_BALANCE = 1000L

        /** 通貨単位 */
        const val CURRENCY_UNIT = "円"
    }

    override fun onLoad() {
        api = this
    }

    override fun onEnable() {
        ExposedDatabase.connect(Database.getDataSource())
        transaction {
            SchemaUtils.create(WalletsTable, MoneyTransactionsTable)
        }

        server.pluginManager.registerEvents(this, this)
    }

    @EventHandler(priority = EventPriority.LOW)
    fun onJoin(event: PlayerJoinEvent) {
        val player = event.player
        server.broadcastMessage(existsData(player).toString())
        if (!existsData(player)) {
            createWallet(player)
            increaseMoney(player, DEFAULT_BALANCE, "初期所持金")
        }
    }

    override fun existsData(player: IPlayer): Boolean {
        return transaction {
            WalletEntity.find { WalletsTable.owner eq player.primaryId }.firstOrNull() != null
        }
    }

    override fun getBalance(player: IPlayer) = getWallet(player).balance

    override fun hasEnoughMoney(player: IPlayer, amount: Long) = getBalance(player) >= amount

    override fun increaseMoney(player: IPlayer, amount: Long, reason: String): Boolean {
        if (amount < 0) return false

        return transaction {
            val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())

            // 所持金を更新
            val wallet = getWallet(player)
            wallet.balance += amount
            wallet.updatedAt = now

            // 取引ログを記録
            MoneyTransactionEntity.new {
                from = null
                to = wallet
                this.amount = amount
                details = reason
                createdAt = now
            }

            true
        }
    }

    override fun decreaseMoney(player: IPlayer, amount: Long, reason: String): Boolean {
        if (amount < 0) return false

        return transaction {
            val wallet = getWallet(player)

            // 所持金より少ない場合はfalse
            if (wallet.balance < amount) return@transaction false

            val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())

            wallet.balance -= amount
            wallet.updatedAt = now

            // 取引ログを記録
            MoneyTransactionEntity.new {
                from = wallet
                to = null
                this.amount = amount
                details = reason
                createdAt = now
            }

            true
        }
    }

    override fun transferMoney(
        from: IPlayer,
        to: IPlayer,
        amount: Long,
        reason: String
    ): Boolean {
        if (amount < 0) return false
        if (!hasEnoughMoney(from, amount)) return false

        return transaction {
            val success = decreaseMoney(from, amount, reason.ifEmpty { "${to.name}へ支払い" })
            if (success) {
                increaseMoney(to, amount, reason.ifEmpty { "${from.name}から受け取り" })
            }
            success
        }
    }

    private fun getWallet(player: IPlayer) =
        transaction { WalletEntity.find { WalletsTable.owner eq player.primaryId }.first() }

    private fun createWallet(player: IPlayer) =
        transaction { WalletEntity.new { owner = PlayerEntity.findById(player.primaryId!!)!! } }
}
