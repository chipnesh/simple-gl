package me.chipnesh.gl.actor.account

import me.chipnesh.gl.core.AccountFault.NotEnoughMoney
import me.chipnesh.gl.core.AccountFault.WrongAmount
import me.chipnesh.gl.core.AccountOperationResult
import me.chipnesh.gl.core.AccountOperationResult.*
import org.slf4j.LoggerFactory
import java.math.BigDecimal
import java.math.BigDecimal.ZERO
import java.time.LocalDateTime

data class Account(
    val accountId: String,
    val created: LocalDateTime = LocalDateTime.now(),
    var balance: BigDecimal = ZERO,
    var version: Int = 0
) {
    companion object {
        private val log = LoggerFactory.getLogger(Account::class.java)
    }

    fun withdraw(amountToWithdraw: BigDecimal): AccountOperationResult {
        log.info("Withdrawing '$amountToWithdraw' from $this")
        if (amountToWithdraw <= ZERO) return Failure(WrongAmount(amountToWithdraw))
        if (balance < amountToWithdraw) return Failure(NotEnoughMoney(accountId))
        balance -= amountToWithdraw
        return Success
    }

    fun deposit(amountToDeposit: BigDecimal): AccountOperationResult {
        log.info("Depositing '$amountToDeposit' to $this")
        if (amountToDeposit <= ZERO) return Failure(WrongAmount(amountToDeposit))
        balance += amountToDeposit
        return Success
    }

    fun debit(amountToDebit: BigDecimal): AccountOperationResult {
        log.info("Debiting '$amountToDebit' from $this")
        if (amountToDebit <= ZERO) return Failure(WrongAmount(amountToDebit))
        if (balance < amountToDebit) return Failure(NotEnoughMoney(accountId))
        balance -= amountToDebit
        return Success
    }

    fun credit(amountToCredit: BigDecimal): AccountOperationResult {
        log.info("Crediting '$amountToCredit' to $this")
        if (amountToCredit <= ZERO) return Failure(WrongAmount(amountToCredit))
        balance += amountToCredit
        return Success
    }

    fun calculateBalance() = AccountBalance(accountId, balance)
}