package jp.asteria.economy.infrastructure

import jp.asteria.player.infrastructure.PlayerEntity
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.dao.IntEntity
import org.jetbrains.exposed.v1.dao.IntEntityClass

class WalletEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<WalletEntity>(WalletsTable)

    var owner by PlayerEntity referencedOn WalletsTable.owner
    var balance by WalletsTable.balance
    var createdAt by WalletsTable.createdAt
    var updatedAt by WalletsTable.updatedAt

    /**
     * 送金元としてのトランザクションデータ
     */
    val sentTransactions by MoneyTransactionEntity optionalReferrersOn MoneyTransactionsTable.from

    /**
     * 送金先としてのトランザクションデータ
     */
    val receivedTransactions by MoneyTransactionEntity optionalReferrersOn MoneyTransactionsTable.to

    /**
     * 送受信をまとめて取得(createdAt でソート)
     */
    val allTransactions by lazy { (sentTransactions + receivedTransactions).sortedBy { it.createdAt } }
}
