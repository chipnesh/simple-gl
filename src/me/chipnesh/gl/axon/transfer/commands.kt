package me.chipnesh.gl.axon.transfer

import org.axonframework.modelling.command.TargetAggregateIdentifier
import java.math.BigDecimal

data class CreateTransferCommand(
    @field:TargetAggregateIdentifier
    val transferId: String,
    val fromAccountId: String,
    val toAccountId: String,
    val amount: BigDecimal
)

data class TransferCreatedEvent(
    val transferId: String,
    val fromAccountId: String,
    val toAccountId: String,
    val amount: BigDecimal
)

data class MarkTransferCompletedCommand(
    @field:TargetAggregateIdentifier
    val transferId: String
)

data class TransferCompletedEvent(
    val transferId: String
)

data class MarkTransferFailedCommand(
    @field:TargetAggregateIdentifier
    val transferId: String
)

data class TransferFailedEvent(
    val transferId: String
)
