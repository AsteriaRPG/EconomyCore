package jp.asteria.economy.database

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID

/**
 * プレイヤー残高エンティティ
 */
class PlayerBalance(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<PlayerBalance>(PlayerBalancesTable)

    var uuid by PlayerBalancesTable.uuid
    var balance by PlayerBalancesTable.balance
    var createdAt by PlayerBalancesTable.createdAt
    var updatedAt by PlayerBalancesTable.updatedAt
}
