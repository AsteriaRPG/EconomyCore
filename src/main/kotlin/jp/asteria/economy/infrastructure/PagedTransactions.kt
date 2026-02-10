package jp.asteria.economy.infrastructure

/**
 * ページングされた取引データを返す用のデータクラス
 *
 * @property total 総件数
 * @property totalPages 総ページ数
 * @property page ページ
 * @property pageSize 1ページあたりの件数
 * @property transactions 取引データ
 */
data class PagedTransactions(
    val total: Long,
    val totalPages: Long,
    val page: Int,
    val pageSize: Int,
    val transactions: List<MoneyTransactionEntity>
)
