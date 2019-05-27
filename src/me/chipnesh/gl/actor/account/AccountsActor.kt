@file:Suppress("EXPERIMENTAL_API_USAGE")

package me.chipnesh.gl.actor.account

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.actor
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.launch
import me.chipnesh.gl.actor.*
import me.chipnesh.gl.core.AccountFault
import me.chipnesh.gl.core.AccountFault.*
import me.chipnesh.gl.core.AccountOperationResult
import me.chipnesh.gl.core.AccountOperationResult.*
import org.slf4j.LoggerFactory
import java.math.BigDecimal

sealed class AccountCommand(val completion: CompletableDeferred<AccountOperationResult> = CompletableDeferred())

data class CreateAccountCommand(val accountId: String) : AccountCommand()
data class GetBalanceCommand(val source: String) : AccountCommand()
data class DepositMoneyCommand(val accountId: String, val amount: BigDecimal) : AccountCommand()
data class WithdrawMoneyCommand(val accountId: String, val amount: BigDecimal) : AccountCommand()

data class DebitSourceAccountCommand(
    val transferId: String,
    val source: String,
    val destination: String,
    val amount: BigDecimal
) : AccountCommand()

data class CreditDestinationAccountCommand(
    val transferId: String,
    val source: String,
    val destination: String,
    val amount: BigDecimal
) : AccountCommand()

data class ReturnMoneyBackCommand(
    val transferId: String,
    val accountId: String,
    val amount: BigDecimal
) : AccountCommand()

class AccountsActor(
    scope: CoroutineScope,
    private val events: EventsGateway,
    private val accounts: AccountsStore
) {

    companion object {
        private val log = LoggerFactory.getLogger(AccountsActor::class.java)
    }

    private val actor = scope.actor<AccountCommand> {
        consumeEach { command ->
            log.info("AccountsActor got a command $command")
            when (command) {
                is CreateAccountCommand -> {
                    val (id) = command
                    if (accounts[id] != null) {
                        command.fail(AccountAlreadyExists(id))
                    } else {
                        accounts[id] = Account(id)
                        command.complete(AccountCreated(id))
                    }
                }
                is GetBalanceCommand -> {
                    val (id) = command
                    when (val account = accounts[id]) {
                        null -> command.fail(AccountNotFound(id))
                        else -> command.complete(account.calculateBalance())
                    }
                }
                is DepositMoneyCommand -> {
                    val (id, amount) = command
                    when (val account = accounts[id]) {
                        null -> command.fail(AccountNotFound(id))
                        else -> command.complete(account.deposit(amount))
                    }
                }
                is WithdrawMoneyCommand -> {
                    val (id, amount) = command
                    when (val account = accounts[id]) {
                        null -> command.fail(AccountNotFound(id))
                        else -> command.complete(account.withdraw(amount))
                    }
                }

                is DebitSourceAccountCommand -> {
                    val (transferId, source, destination, amount) = command
                    when (val sourceAccount = accounts[source]) {
                        null -> notify(SourceAccountNotFoundEvent(transferId, source))
                        else -> when (val result = sourceAccount.debit(amount)) {
                            Success -> notify(SourceAccountDebitedEvent(transferId, source, destination, amount))
                            is Failure -> when (result.fault) {
                                is NotEnoughMoney, is WrongAmount ->
                                    notify(SourceAccountDebitRejectedEvent(transferId, source))
                            }
                        }
                    }
                }
                is CreditDestinationAccountCommand -> {
                    val (transfer, source, destination, amount) = command
                    when (val toAccount = accounts[destination]) {
                        null -> notify(DestinationAccountNotFoundEvent(transfer, source, amount))
                        else -> when (val result = toAccount.credit(amount)) {
                            Success -> notify(DestinationAccountCreditedEvent(transfer, destination))
                            is Failure -> when (result.fault) {
                                is NotEnoughMoney, is WrongAmount ->
                                    notify(DestinationAccountCreditRejectedEvent(transfer, destination, amount))
                            }
                        }
                    }
                }
                is ReturnMoneyBackCommand -> {
                    val (transferId, accountId, amount) = command
                    when (val account = accounts[accountId]) {
                        null -> notify(DestinationAccountNotFoundEvent(transferId, accountId, amount))
                        else -> account.credit(amount)
                    }
                }
            }
        }
    }

    fun CoroutineScope.sendAsync(command: AccountCommand) = launch { actor.send(command) }
    suspend fun sendAndAwait(command: AccountCommand) = actor.send(command).run { command.completion.await() }
    private fun CoroutineScope.notify(event: DomainEvent) = with(events) { sendAsync(event) }
    private fun AccountCommand.complete(result: AccountOperationResult) = completion.complete(result)
    private fun AccountCommand.fail(fault: AccountFault) = completion.complete(Failure(fault))
}