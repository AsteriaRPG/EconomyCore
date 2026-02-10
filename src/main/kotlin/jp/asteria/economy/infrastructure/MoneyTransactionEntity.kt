package jp.asteria.economy.infrastructure

import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.dao.IntEntity
import org.jetbrains.exposed.v1.dao.IntEntityClass

class MoneyTransactionEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<MoneyTransactionEntity>(MoneyTransactionsTable)

    var from by WalletEntity optionalReferencedOn MoneyTransactionsTable.from
    var to by WalletEntity optionalReferencedOn MoneyTransactionsTable.to
    var amount by MoneyTransactionsTable.amount
    var details by MoneyTransactionsTable.details
    var createdAt by MoneyTransactionsTable.createdAt
}
