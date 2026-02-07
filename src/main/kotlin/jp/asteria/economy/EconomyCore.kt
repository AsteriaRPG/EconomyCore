package jp.asteria.economy

import cn.nukkit.Player
import cn.nukkit.plugin.PluginBase
import jp.asteria.dbconnector.Database
import jp.asteria.economy.database.PlayerBalance
import jp.asteria.economy.database.PlayerBalancesTable
import jp.asteria.economy.database.TransactionLog
import jp.asteria.economy.database.TransactionLogsTable
import org.jetbrains.exposed.sql.Database as ExposedDatabase
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import javax.sql.DataSource

/**
 * 経済管理プラグイン
 *
 * プレイヤーの所持金を管理
 */
class EconomyCore : PluginBase(), IEconomyCore {
    companion object {
        lateinit var instance: EconomyCore
            private set

        /** 初期所持金 */
        const val DEFAULT_BALANCE = 1000L

        /** 通貨単位 */
        const val CURRENCY_UNIT = "G"
    }

    // キャッシュ（パフォーマンス向上用）
    private val balanceCache = ConcurrentHashMap<UUID, Long>()

    override fun onLoad() {
        instance = this
    }

    override fun onEnable() {
        try {
            // リフレクションでDatabaseのdataSourceにアクセス
            val dataSourceField = Database::class.java.getDeclaredField("dataSource")
            dataSourceField.isAccessible = true
            val dataSource = dataSourceField.get(Database) as DataSource

            // DataSourceを使用してExposedに接続
            ExposedDatabase.connect(dataSource)

            // テーブル作成
            transaction {
                SchemaUtils.create(
                    PlayerBalancesTable,
                    TransactionLogsTable
                )
            }

            logger.info("EconomyCore has been enabled!")
        } catch (e: Exception) {
            logger.error("Failed to initialize EconomyCore tables", e)
            server.pluginManager.disablePlugin(this)
        }
    }

    override fun onDisable() {
        // キャッシュをクリア
        balanceCache.clear()
        logger.info("EconomyCore has been disabled!")
    }

    /**
     * アカウントが存在するか確認
     */
    override fun existsAccount(account: UUID): Boolean {
        return transaction {
            PlayerBalance.find { PlayerBalancesTable.uuid eq account.toString() }.firstOrNull() != null
        }
    }

    /**
     * アカウントを作成（存在しない場合）
     */
    fun createAccount(account: UUID): Boolean {
        if (existsAccount(account)) return false

        return transaction {
            PlayerBalance.new {
                uuid = account.toString()
                balance = DEFAULT_BALANCE
                createdAt = LocalDateTime.now()
                updatedAt = LocalDateTime.now()
            }
            balanceCache[account] = DEFAULT_BALANCE
            true
        }
    }

    /**
     * アカウントを取得または作成
     */
    private fun getOrCreateBalance(account: UUID): PlayerBalance {
        return transaction {
            PlayerBalance.find { PlayerBalancesTable.uuid eq account.toString() }.firstOrNull()
                ?: PlayerBalance.new {
                    uuid = account.toString()
                    balance = DEFAULT_BALANCE
                    createdAt = LocalDateTime.now()
                    updatedAt = LocalDateTime.now()
                }
        }
    }

    /**
     * 所持金を取得
     */
    override fun getBalance(account: UUID): Long {
        // キャッシュから取得を試みる
        balanceCache[account]?.let { return it }

        return transaction {
            val playerBalance = getOrCreateBalance(account)
            balanceCache[account] = playerBalance.balance
            playerBalance.balance
        }
    }

    /**
     * 十分な所持金があるか確認
     */
    override fun hasEnoughMoney(account: UUID, amount: Long): Boolean {
        return getBalance(account) >= amount
    }

    /**
     * 所持金を増やす
     */
    override fun increaseMoney(player: UUID, amount: Long, reason: String): Boolean {
        if (amount <= 0) return false

        return transaction {
            val playerBalance = getOrCreateBalance(player)
            playerBalance.balance += amount
            playerBalance.updatedAt = LocalDateTime.now()

            // キャッシュを更新
            balanceCache[player] = playerBalance.balance

            // 取引ログを記録
            TransactionLog.new {
                uuid = player.toString()
                this.amount = amount
                balanceAfter = playerBalance.balance
                this.reason = reason.ifEmpty { "income" }
                createdAt = LocalDateTime.now()
            }

            true
        }
    }

    /**
     * 所持金を減らす
     */
    override fun decreaseMoney(player: UUID, amount: Long, reason: String): Boolean {
        if (amount <= 0) return false

        return transaction {
            val playerBalance = getOrCreateBalance(player)

            if (playerBalance.balance < amount) {
                return@transaction false
            }

            playerBalance.balance -= amount
            playerBalance.updatedAt = LocalDateTime.now()

            // キャッシュを更新
            balanceCache[player] = playerBalance.balance

            // 取引ログを記録
            TransactionLog.new {
                uuid = player.toString()
                this.amount = -amount
                balanceAfter = playerBalance.balance
                this.reason = reason.ifEmpty { "expense" }
                createdAt = LocalDateTime.now()
            }

            true
        }
    }

    /**
     * 所持金を設定
     */
    fun setBalance(player: UUID, amount: Long, reason: String = ""): Boolean {
        if (amount < 0) return false

        return transaction {
            val playerBalance = getOrCreateBalance(player)
            val diff = amount - playerBalance.balance

            playerBalance.balance = amount
            playerBalance.updatedAt = LocalDateTime.now()

            // キャッシュを更新
            balanceCache[player] = amount

            // 取引ログを記録
            TransactionLog.new {
                uuid = player.toString()
                this.amount = diff
                balanceAfter = amount
                this.reason = reason.ifEmpty { "set_balance" }
                createdAt = LocalDateTime.now()
            }

            true
        }
    }

    /**
     * プレイヤー間送金
     */
    fun transfer(from: UUID, to: UUID, amount: Long, reason: String = ""): Boolean {
        if (amount <= 0) return false
        if (!hasEnoughMoney(from, amount)) return false

        return transaction {
            val success = decreaseMoney(from, amount, "transfer_to:$to $reason")
            if (success) {
                increaseMoney(to, amount, "transfer_from:$from $reason")
            }
            success
        }
    }

    // ---- Player便利メソッド ----

    /**
     * Playerオブジェクトから残高取得
     */
    fun getBalance(player: Player): Long = getBalance(player.uniqueId)

    /**
     * Playerオブジェクトで十分な所持金があるか確認
     */
    fun hasEnoughMoney(player: Player, amount: Long): Boolean = hasEnoughMoney(player.uniqueId, amount)

    /**
     * Playerオブジェクトで所持金を増やす
     */
    fun increaseMoney(player: Player, amount: Long, reason: String = ""): Boolean =
        increaseMoney(player.uniqueId, amount, reason)

    /**
     * Playerオブジェクトで所持金を減らす
     */
    fun decreaseMoney(player: Player, amount: Long, reason: String = ""): Boolean =
        decreaseMoney(player.uniqueId, amount, reason)

    /**
     * フォーマットされた残高文字列を取得
     */
    fun getFormattedBalance(player: Player): String {
        val balance = getBalance(player)
        return formatMoney(balance)
    }

    /**
     * 金額をフォーマット
     */
    fun formatMoney(amount: Long): String {
        return "${"%,d".format(amount)}$CURRENCY_UNIT"
    }
}
