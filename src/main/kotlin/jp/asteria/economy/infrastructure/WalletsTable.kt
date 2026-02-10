package jp.asteria.economy.infrastructure

import jp.asteria.player.infrastructure.PlayersTable
import org.jetbrains.exposed.v1.core.dao.id.IntIdTable
import org.jetbrains.exposed.v1.datetime.CurrentDateTime
import org.jetbrains.exposed.v1.datetime.datetime

object WalletsTable : IntIdTable("wallets") {
    val owner = reference("owner", PlayersTable).uniqueIndex()
    val balance = long("balance").default(0)
    val createdAt = datetime("created_at").defaultExpression(CurrentDateTime)
    val updatedAt = datetime("updated_at").defaultExpression(CurrentDateTime)
}
