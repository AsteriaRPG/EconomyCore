package jp.asteria.economy.database

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID

/**
 * 取引ログエンティティ
 */
class TransactionLog(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<TransactionLog>(TransactionLogsTable)

    var uuid by TransactionLogsTable.uuid
    var amount by TransactionLogsTable.amount
    var balanceAfter by TransactionLogsTable.balanceAfter
    var reason by TransactionLogsTable.reason
    var createdAt by TransactionLogsTable.createdAt
}
