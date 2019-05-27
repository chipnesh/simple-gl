package me.chipnesh.gl.axon.account

import org.axonframework.modelling.command.TargetAggregateIdentifier
import java.math.BigDecimal


data class CreateAccountCommand(
    @field:TargetAggregateIdentifier
    val accountId: String
)

data class AccountCreatedEvent(
    val accountId: String
)

data class CreditDestinationAccountCommand(
    @field:TargetAggregateIdentifier
    val accountId: String,
    val transferId: String,
    val amount: BigDecimal
)

data class DestinationAccountCreditedEvent(
    override val accountId: String,
    override val amount: BigDecimal,
    val transferId: String
) : MoneyAddedEvent(accountId, amount)

data class DebitSourceAccountCommand(
    @field:TargetAggregateIdentifier
    val accountId: String,
    val transferId: String,
    val amount: BigDecimal
)


sealed class MoneyAddedEvent(
    open val accountId: String,
    open val amount: BigDecimal
)

data class DestinationAccountNotFoundEvent(
    val transferId: String
)

sealed class MoneySubtractedEvent(
    open val accountId: String,
    open val amount: BigDecimal
)

data class DepositMoneyCommand(
    @field:TargetAggregateIdentifier
    val accountId: String,
    val amount: BigDecimal
)

data class MoneyDepositedEvent(
    override val accountId: String,
    override val amount: BigDecimal
) : MoneyAddedEvent(accountId, amount)

data class WithdrawMoneyCommand(
    @field:TargetAggregateIdentifier
    val accountId: String,
    val amount: BigDecimal
)

data class MoneyWithdrawnEvent(
    override val accountId: String,
    override val amount: BigDecimal
) : MoneySubtractedEvent(accountId, amount)

data class ReturnMoneyCommand(
    @field:TargetAggregateIdentifier
    val accountId: String,
    val amount: BigDecimal
)

data class MoneyReturnedEvent(
    override val accountId: String,
    override val amount: BigDecimal
) : MoneyAddedEvent(accountId, amount)


data class SourceAccountDebitRejectedEvent(
    val transferId: String
)

data class SourceAccountDebitedEvent(
    override val accountId: String,
    override val amount: BigDecimal,
    val transferId: String
) : MoneySubtractedEvent(accountId, amount)


data class SourceAccountNotFoundEvent(
    val transferId: String
)
