package me.chipnesh.gl.actor

import me.chipnesh.gl.actor.account.*
import me.chipnesh.gl.core.AccountsOperations
import java.math.BigDecimal

class AccountsActorProxy(
    private val actor: AccountsActor
) : AccountsOperations {

    override suspend fun depositCash(to: String, amount: BigDecimal) =
        actor.sendAndAwait(DepositMoneyCommand(to, amount))

    override suspend fun withdrawCash(from: String, amount: BigDecimal) =
        actor.sendAndAwait(WithdrawMoneyCommand(from, amount))

    override suspend fun getBalance(from: String) =
        actor.sendAndAwait(GetBalanceCommand(from))

    override suspend fun create(id: String) =
        actor.sendAndAwait(CreateAccountCommand(id))
}