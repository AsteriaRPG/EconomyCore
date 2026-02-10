package jp.asteria.economy

import cn.nukkit.IPlayer

interface IEconomyCore {
    /**
     * データが存在するか
     *
     * @param player
     * @return
     */
    fun existsData(player: IPlayer): Boolean

    /**
     * 所持金を取得する
     *
     * @param player
     * @return
     */
    fun getBalance(player: IPlayer): Long

    /**
     * 十分な所持金を所持しているか
     *
     * @param player
     * @param amount
     * @return 所持している場合はtrue
     */
    fun hasEnoughMoney(player: IPlayer, amount: Long): Boolean

    /**
     * 所持金を増やす
     *
     * @param player
     * @param amount
     * @param reason
     * @return 増やせた場合はtrue
     */
    fun increaseMoney(player: IPlayer, amount: Long, reason: String = ""): Boolean

    /**
     * 所持金を減らす
     *
     * @param player
     * @param amount
     * @param reason
     * @return 減らせた場合はtrue
     */
    fun decreaseMoney(player: IPlayer, amount: Long, reason: String = ""): Boolean

    /**
     * 送金する
     *
     * @param from 送金元
     * @param to   送金先
     * @param amount
     * @param reason
     * @return
     */
    fun transferMoney(from: IPlayer, to: IPlayer, amount: Long, reason: String = ""): Boolean
}
