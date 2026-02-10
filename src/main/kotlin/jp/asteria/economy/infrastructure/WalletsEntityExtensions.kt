package jp.asteria.economy.infrastructure

import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.or

/**
 * 取引データをページングして取得
 *
 * @param page
 * @param pageSize
 * @return
 */
fun WalletEntity.getPagedTransactions(page: Int, pageSize: Int): PagedTransactions {
    val offset = (page - 1) * pageSize

    val condition = (MoneyTransactionsTable.from eq id) or
            (MoneyTransactionsTable.to eq id)

    val total = MoneyTransactionEntity.count(condition)
    val totalPages = if (total == 0L) 1L else ((total + pageSize - 1) / pageSize)
    val items = MoneyTransactionEntity.find { condition }
        .orderBy(MoneyTransactionsTable.createdAt to SortOrder.DESC)
        .offset(offset.toLong())
        .limit(pageSize)
        .toList()

    return PagedTransactions(total, totalPages, page, pageSize, items)
}
