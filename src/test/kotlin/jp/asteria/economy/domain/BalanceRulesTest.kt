package jp.asteria.economy.domain

import kotlin.test.*
import org.junit.Test

class BalanceRulesTest {
    @Test fun creditsKeepLongPrecisionAndRejectOverflow() {
        assertEquals(Long.MAX_VALUE, BalanceRules.credit(Long.MAX_VALUE - 1, 1))
        assertFailsWith<ArithmeticException> { BalanceRules.credit(Long.MAX_VALUE, 1) }
    }
    @Test fun insufficientDebitHasNoResult() {
        assertEquals(0L, BalanceRules.debit(75, 75))
        assertNull(BalanceRules.debit(74, 75))
    }
    @Test fun transferConservesCurrencyAndRequiresBudget() {
        assertEquals(BalanceRules.Transfer(25, 95), BalanceRules.transfer(100, 20, 75, false))
        assertNull(BalanceRules.transfer(74, 20, 75, false))
    }
    @Test fun destinationOverflowRejectsWholeTransfer() {
        assertFailsWith<ArithmeticException> { BalanceRules.transfer(100, Long.MAX_VALUE, 1, false) }
    }
    @Test fun selfTransferCannotMintMoneyOrOverflow() {
        assertEquals(BalanceRules.Transfer(Long.MAX_VALUE, Long.MAX_VALUE), BalanceRules.transfer(Long.MAX_VALUE, Long.MAX_VALUE, 1, true))
        assertNull(BalanceRules.transfer(0, 0, 1, true))
    }
    @Test fun zeroKeepsEveryBalance() {
        assertEquals(100L, BalanceRules.credit(100, 0))
        assertEquals(100L, BalanceRules.debit(100, 0))
        assertEquals(BalanceRules.Transfer(100, 200), BalanceRules.transfer(100, 200, 0, false))
    }
    @Test fun invalidLegacyBalanceAndNegativeInputsAreRejected() {
        for (action in listOf<() -> Unit>(
            { BalanceRules.credit(-1, 1) }, { BalanceRules.debit(-1, 1) },
            { BalanceRules.credit(1, -1) }, { BalanceRules.debit(1, -1) },
            { BalanceRules.transfer(1, -1, 0, false) }, { BalanceRules.transfer(-1, 1, 0, true) }
        )) assertFailsWith<IllegalArgumentException>(block = action)
    }
}
