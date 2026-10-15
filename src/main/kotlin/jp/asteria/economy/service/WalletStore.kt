package jp.asteria.economy.service

import jp.asteria.economy.api.WalletSnapshot
import org.jetbrains.exposed.v1.jdbc.transactions.TransactionManager
import java.sql.Connection

/** 財布と取引ログの保存形式を所有する。必ず呼出元のExposed transactionへ参加する。 */
internal object WalletStore {
    private fun connection() = TransactionManager.current().connection.connection as Connection

    fun find(playerId: Int, locked: Boolean = false): WalletSnapshot? {
        val connection = connection()
        val suffix = if (locked && connection.metaData.databaseProductName in listOf("MySQL", "MariaDB")) " FOR UPDATE" else ""
        return connection.prepareStatement("SELECT id,owner,balance FROM wallets WHERE owner=?$suffix").use { statement ->
            statement.setInt(1, playerId)
            statement.executeQuery().use { rows ->
                if (rows.next()) {
                    WalletSnapshot(
                        walletId = rows.getInt("id"),
                        playerId = rows.getInt("owner"),
                        balance = rows.getLong("balance")
                    )
                } else {
                    null
                }
            }
        }
    }

    fun requireWallet(playerId: Int) = find(playerId, locked = true)
        ?: throw NoSuchElementException("Wallet not found for player: $playerId")

    fun create(playerId: Int, balance: Long): WalletSnapshot {
        connection().prepareStatement("INSERT INTO wallets (owner,balance) VALUES (?,?)").use { statement ->
            statement.setInt(1, playerId)
            statement.setLong(2, balance)
            statement.executeUpdate()
        }
        return requireWallet(playerId)
    }

    fun save(wallet: WalletSnapshot, balance: Long) {
        connection().prepareStatement("UPDATE wallets SET balance=?,updated_at=CURRENT_TIMESTAMP WHERE id=? AND owner=?").use { statement ->
            statement.setLong(1, balance)
            statement.setInt(2, wallet.walletId)
            statement.setInt(3, wallet.playerId)
            statement.executeUpdate()
        }
    }

    fun log(from: Int?, to: Int?, amount: Long, reason: String) {
        connection().prepareStatement("INSERT INTO money_transactions (from_id,to_id,amount,details) VALUES (?,?,?,?)").use { statement ->
            if (from == null) statement.setNull(1, java.sql.Types.INTEGER) else statement.setInt(1, from)
            if (to == null) statement.setNull(2, java.sql.Types.INTEGER) else statement.setInt(2, to)
            statement.setLong(3, amount)
            statement.setString(4, reason)
            statement.executeUpdate()
        }
    }
}
