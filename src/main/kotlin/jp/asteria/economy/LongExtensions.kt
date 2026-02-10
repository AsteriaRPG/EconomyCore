package jp.asteria.economy

fun Long.formatMoney(): String = "${"%,d".format(this)}${EconomyCore.CURRENCY_UNIT}"
