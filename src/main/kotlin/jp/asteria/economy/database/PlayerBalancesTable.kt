package jp.asteria.economy.database

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.javatime.datetime
import java.time.LocalDateTime

/**
 * プレイヤー残高テーブル
 */
object PlayerBalancesTable : IntIdTable("player_balances") {
    val uuid = varchar("uuid", 36).uniqueIndex()
    val balance = long("balance").default(0L)
    val createdAt = datetime("created_at").clientDefault { LocalDateTime.now() }
    val updatedAt = datetime("updated_at").clientDefault { LocalDateTime.now() }
}
