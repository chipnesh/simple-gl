package me.chipnesh.gl.axon.transfer

import me.chipnesh.gl.core.TransferStatus
import me.chipnesh.gl.core.TransferStatus.*
import org.axonframework.commandhandling.CommandHandler
import org.axonframework.eventhandling.EventHandler
import org.axonframework.modelling.command.AggregateIdentifier
import org.axonframework.modelling.command.AggregateLifecycle.apply
import org.axonframework.modelling.command.AggregateRoot
import org.slf4j.LoggerFactory
import java.math.BigDecimal

@AggregateRoot
class TransferAggregate() {

    companion object {
        private val log = LoggerFactory.getLogger(TransferAggregate::class.java)
    }

    @field:AggregateIdentifier
    private lateinit var transferId: String
    private lateinit var fromAccountId: String
    private lateinit var toAccountId: String
    private lateinit var amount: BigDecimal
    private lateinit var status: TransferStatus

    @CommandHandler
    constructor(command: CreateTransferCommand) : this() {
        log.info("Got $command command")

        apply(
            TransferCreatedEvent(
                command.transferId,
                command.fromAccountId,
                command.toAccountId,
                command.amount
            )
        )
    }

    @CommandHandler
    fun handle(command: MarkTransferCompletedCommand) {
        log.info("Got $command command")
        apply(TransferCompletedEvent(command.transferId))
    }

    @CommandHandler
    fun handle(command: MarkTransferFailedCommand) {
        log.info("Got $command command")
        apply(TransferFailedEvent(command.transferId))
    }

    @EventHandler
    fun on(event: TransferCreatedEvent) {
        log.info("Got $event event")
        transferId = event.transferId
        fromAccountId = event.fromAccountId
        toAccountId = event.toAccountId
        amount = event.amount
        status = STARTED
    }

    @EventHandler
    fun on(event: TransferCompletedEvent) {
        log.info("Got $event event")
        status = COMPLETED
    }

    @EventHandler
    fun on(event: TransferFailedEvent) {
        log.info("Got $event event")
        status = FAILED
    }
}