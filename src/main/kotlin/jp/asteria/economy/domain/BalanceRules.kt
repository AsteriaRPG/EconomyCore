package jp.asteria.economy.domain

/** 残高の規則。保存やプレイヤー実体には依存しない。 */
internal object BalanceRules {
    data class Transfer(val from: Long, val to: Long)

    fun credit(balance: Long, amount: Long): Long {
        require(balance >= 0 && amount >= 0) { "Balance and amount must be nonnegative" }
        return Math.addExact(balance, amount)
    }

    fun debit(balance: Long, amount: Long): Long? {
        require(balance >= 0 && amount >= 0) { "Balance and amount must be nonnegative" }
        return if (balance < amount) null else balance - amount
    }

    fun transfer(from: Long, to: Long, amount: Long, sameWallet: Boolean): Transfer? {
        require(from >= 0 && to >= 0 && amount >= 0) { "Balance and amount must be nonnegative" }
        val remaining = debit(from, amount) ?: return null
        return if (sameWallet) Transfer(from, from) else Transfer(remaining, credit(to, amount))
    }
}
