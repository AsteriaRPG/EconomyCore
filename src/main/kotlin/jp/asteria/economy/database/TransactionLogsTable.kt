package jp.asteria.economy.database

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.javatime.datetime
import java.time.LocalDateTime

/**
 * 取引ログテーブル
 */
object TransactionLogsTable : IntIdTable("transaction_logs") {
    val uuid = varchar("uuid", 36).index()
    val amount = long("amount")
    val balanceAfter = long("balance_after")
    val reason = varchar("reason", 255)
    val createdAt = datetime("created_at").clientDefault { LocalDateTime.now() }
}
