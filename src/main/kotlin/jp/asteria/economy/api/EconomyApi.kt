package jp.asteria.economy.api

import jp.asteria.dbconnector.Database
import jp.asteria.economy.EconomyCore
import jp.asteria.economy.domain.BalanceRules
import jp.asteria.economy.infrastructure.MoneyTransactionsTable
import jp.asteria.economy.infrastructure.WalletsTable
import jp.asteria.economy.service.WalletStore
import jp.asteria.player.infrastructure.PlayerTransactions
import org.jetbrains.exposed.v1.jdbc.transactions.transaction

/** IDだけで呼べる通貨API。金融更新と取引ログを同じplayer root・SQL境界で確定する。 */
object EconomyApi {
    @Volatile private var active = false
    internal fun activate() { active = true }
    internal fun deactivate() { active = false }
    private val tables = listOf(WalletsTable, MoneyTransactionsTable)

    private fun <T> write(playerIds: Collection<Int>, action: () -> T): T {
        check(active) { "Economy service is not active" }
        return PlayerTransactions.write(playerIds, tables) {
            check(active) { "Economy service is not active" }
            action()
        }
    }

    /** 読取では財布や初期所持金を作成しない。 */
    fun getWallet(playerId: Int): WalletSnapshot? {
        check(Database.getTransactionConnection() == null) { "Economy reads cannot join Database.transaction" }
        return transaction { WalletStore.find(playerId) }
    }
    fun existsData(playerId: Int) = getWallet(playerId) != null
    fun getBalance(playerId: Int): Long = getWallet(playerId)?.balance
        ?: throw NoSuchElementException("Wallet not found for player: $playerId")
    fun hasEnoughMoney(playerId: Int, amount: Long): Boolean = amount >= 0 && getBalance(playerId) >= amount

    /** 初期所持金は財布の作成時だけ付与し、既存の残高には触れない。 */
    fun ensureWallet(playerId: Int): WalletSnapshot = write(listOf(playerId)) {
        WalletStore.find(playerId, locked = true) ?: WalletStore.create(playerId, EconomyCore.DEFAULT_BALANCE).also {
            WalletStore.log(null, it.walletId, EconomyCore.DEFAULT_BALANCE, "初期所持金")
        }
    }

    @JvmOverloads
    fun increaseMoney(playerId: Int, amount: Long, reason: String = ""): Boolean {
        if (amount < 0) return false
        return write(listOf(playerId)) {
            val wallet = WalletStore.requireWallet(playerId)
            val balance = BalanceRules.credit(wallet.balance, amount)
            WalletStore.save(wallet, balance)
            WalletStore.log(null, wallet.walletId, amount, reason)
            true
        }
    }

    @JvmOverloads
    fun decreaseMoney(playerId: Int, amount: Long, reason: String = ""): Boolean {
        if (amount < 0) return false
        return write(listOf(playerId)) {
            val wallet = WalletStore.requireWallet(playerId)
            val balance = BalanceRules.debit(wallet.balance, amount) ?: return@write false
            WalletStore.save(wallet, balance)
            WalletStore.log(wallet.walletId, null, amount, reason)
            true
        }
    }

    @JvmOverloads
    fun transferMoney(from: Int, to: Int, amount: Long, reason: String = ""): Boolean {
        if (amount < 0) return false
        return write(listOf(from, to)) {
            val source = WalletStore.requireWallet(from)
            val destination = if (from == to) source else WalletStore.requireWallet(to)
            val balances = BalanceRules.transfer(source.balance, destination.balance, amount, from == to) ?: return@write false
            WalletStore.save(source, balances.from)
            if (from != to) WalletStore.save(destination, balances.to)
            WalletStore.log(source.walletId, destination.walletId, amount, reason)
            true
        }
    }
}
