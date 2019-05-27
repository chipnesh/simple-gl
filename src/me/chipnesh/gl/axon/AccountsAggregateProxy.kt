package me.chipnesh.gl.axon

import kotlinx.coroutines.future.await
import me.chipnesh.gl.axon.account.*
import me.chipnesh.gl.core.AccountFault.AccountAlreadyExists
import me.chipnesh.gl.core.AccountOperationResult
import me.chipnesh.gl.core.AccountOperationResult.AccountCreated
import me.chipnesh.gl.core.AccountOperationResult.Failure
import me.chipnesh.gl.core.AccountsOperations
import org.axonframework.config.Configuration
import java.math.BigDecimal

class AccountsAggregateProxy(
    configuration: Configuration,
    private val accountsStore: AccountsStore
) : AccountsOperations {

    private val commands = configuration.commandGateway()
    private val queries = configuration.queryGateway()

    override suspend fun create(id: String): AccountOperationResult {
        if (accountsStore.findById(id) != null) return Failure(AccountAlreadyExists(id))
        commands.sendAndWait<String>(CreateAccountCommand(id))
        return AccountCreated(id)
    }

    override suspend fun depositCash(to: String, amount: BigDecimal): AccountOperationResult =
        commands.sendAndWait(DepositMoneyCommand(to, amount))

    override suspend fun withdrawCash(from: String, amount: BigDecimal): AccountOperationResult =
        commands.sendAndWait(WithdrawMoneyCommand(from, amount))

    override suspend fun getBalance(from: String): AccountOperationResult =
        queries.query(GetAccountBalanceQuery(from), AccountOperationResult::class.java).await()

}