package me.chipnesh.gl.axon.account

import me.chipnesh.gl.core.AccountFault.NotEnoughMoney
import me.chipnesh.gl.core.AccountFault.WrongAmount
import me.chipnesh.gl.core.AccountOperationResult
import me.chipnesh.gl.core.AccountOperationResult.Failure
import me.chipnesh.gl.core.AccountOperationResult.Success
import org.axonframework.commandhandling.CommandHandler
import org.axonframework.eventsourcing.EventSourcingHandler
import org.axonframework.modelling.command.AggregateIdentifier
import org.axonframework.modelling.command.AggregateLifecycle.apply
import org.axonframework.modelling.command.AggregateRoot
import org.slf4j.LoggerFactory
import java.math.BigDecimal
import java.time.LocalDateTime


@AggregateRoot
class AccountAggregate() {

    companion object {
        private val log = LoggerFactory.getLogger(AccountAggregate::class.java)
    }

    @field:AggregateIdentifier
    private lateinit var accountId: String
    private lateinit var created: LocalDateTime
    private lateinit var balance: BigDecimal

    @CommandHandler
    constructor(cmd: CreateAccountCommand) : this() {
        log.info("Got $cmd command")
        apply(AccountCreatedEvent(cmd.accountId))
    }

    @CommandHandler
    fun deposit(cmd: DepositMoneyCommand): AccountOperationResult {
        log.info("Got $cmd command")
        val amountToDeposit = cmd.amount
        if (amountToDeposit <= BigDecimal.ZERO) return Failure(WrongAmount(amountToDeposit))
        apply(MoneyDepositedEvent(accountId, amountToDeposit))
        return Success
    }

    @CommandHandler
    fun withdraw(cmd: WithdrawMoneyCommand): AccountOperationResult {
        log.info("Got $cmd command")
        val amountToWithdraw = cmd.amount
        if (amountToWithdraw <= BigDecimal.ZERO) return Failure(WrongAmount(amountToWithdraw))
        if (balance < amountToWithdraw) return Failure(NotEnoughMoney(accountId))
        apply(MoneyWithdrawnEvent(accountId, amountToWithdraw))
        return Success
    }

    fun debit(amount: BigDecimal, transferId: String) {
        if (balance >= amount) {
            log.info("Debiting $amount for $transferId transfer and $this account")
            apply(SourceAccountDebitedEvent(accountId, amount, transferId))
        } else {
            log.info("Debiting failed for $amount, $transferId transfer and $this account")
            apply(SourceAccountDebitRejectedEvent(transferId))
        }
    }

    fun credit(amount: BigDecimal, transferId: String) {
        log.info("Crediting $amount for $transferId transfer and $this account")
        apply(DestinationAccountCreditedEvent(accountId, amount, transferId))
    }

    @CommandHandler
    fun returnMoney(cmd: ReturnMoneyCommand) {
        log.info("Got $cmd command")
        apply(MoneyReturnedEvent(accountId, cmd.amount))
    }

    @EventSourcingHandler
    fun on(event: AccountCreatedEvent) {
        log.info("Got $event event")
        accountId = event.accountId
        created = LocalDateTime.now()
        balance = BigDecimal.ZERO
    }

    @EventSourcingHandler
    fun on(event: MoneyAddedEvent) {
        log.info("Got $event event")
        balance += event.amount
    }

    @EventSourcingHandler
    fun on(event: MoneySubtractedEvent) {
        log.info("Got $event event")
        balance -= event.amount
    }
}