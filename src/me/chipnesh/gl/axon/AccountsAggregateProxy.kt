package me.chipnesh.gl.axon

import kotlinx.coroutines.future.await
import me.chipnesh.gl.axon.account.*
import me.chipnesh.gl.core.AccountFault.AccountAlreadyExists
import me.chipnesh.gl.core.AccountFault.AccountNotFound
import me.chipnesh.gl.core.AccountOperationResult
import me.chipnesh.gl.core.AccountOperationResult.AccountCreated
import me.chipnesh.gl.core.AccountOperationResult.Failure
import me.chipnesh.gl.core.AccountsOperations
import org.axonframework.config.Configuration
import org.axonframework.modelling.command.AggregateNotFoundException
import java.math.BigDecimal

class AccountsAggregateProxy(
    configuration: Configuration,
    private val accountsStorage: AccountsStorage
) : AccountsOperations {

    private val commands = configuration.commandGateway()
    private val queries = configuration.queryGateway()

    override suspend fun create(id: String): AccountOperationResult {
        if (accountsStorage.findById(id) != null) return Failure(AccountAlreadyExists(id))
        commands.sendAndWait<String>(CreateAccountCommand(id))
        return AccountCreated(id)
    }

    override suspend fun depositCash(to: String, amount: BigDecimal): AccountOperationResult = try {
        commands.sendAndWait(DepositMoneyCommand(to, amount))
    } catch (e: AggregateNotFoundException) {
        Failure(AccountNotFound(to))
    }


    override suspend fun withdrawCash(from: String, amount: BigDecimal): AccountOperationResult = try {
        commands.sendAndWait(WithdrawMoneyCommand(from, amount))
    } catch (e: AggregateNotFoundException) {
        Failure(AccountNotFound(from))
    }

    override suspend fun getBalance(from: String): AccountOperationResult =
        try {
            queries.query(GetAccountBalanceQuery(from), AccountOperationResult::class.java).await()
        } catch (e: AggregateNotFoundException) {
            Failure(AccountNotFound(from))
        }

}