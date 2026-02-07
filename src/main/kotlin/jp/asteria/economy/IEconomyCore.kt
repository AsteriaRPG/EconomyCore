package jp.asteria.economy

import java.util.UUID

interface IEconomyCore {
    /**
     * アカウントが存在するか
     *
     * @param account
     * @return
     */
    fun existsAccount(account: UUID): Boolean

    /**
     * 所持金を取得する
     *
     * @param account
     * @return
     */
    fun getBalance(account: UUID): Long

    /**
     * 十分な所持金を所持しているか
     *
     * @param account
     * @param amount
     * @return 所持している場合はtrue
     */
    fun hasEnoughMoney(account: UUID, amount: Long): Boolean

    /**
     * 所持金を増やす
     *
     * @param player
     * @param amount
     * @param reason
     * @return 増やせた場合はtrue
     */
    fun increaseMoney(player: UUID, amount: Long, reason: String = ""): Boolean

    /**
     * 所持金を減らす
     *
     * @param player
     * @param amount
     * @param reason
     * @return 減らせた場合はtrue
     */
    fun decreaseMoney(player: UUID, amount: Long, reason: String = ""): Boolean
}
