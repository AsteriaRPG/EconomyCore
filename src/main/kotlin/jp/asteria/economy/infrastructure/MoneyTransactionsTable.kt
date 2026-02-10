package jp.asteria.economy.infrastructure

import org.jetbrains.exposed.v1.core.dao.id.IntIdTable
import org.jetbrains.exposed.v1.datetime.CurrentDateTime
import org.jetbrains.exposed.v1.datetime.datetime

object MoneyTransactionsTable : IntIdTable("money_transactions") {
    val from = optReference("from_id", WalletsTable)
    val to = optReference("to_id", WalletsTable)
    val amount = long("amount")
    val details = text("details").nullable()
    val createdAt = datetime("created_at").defaultExpression(CurrentDateTime)
}
