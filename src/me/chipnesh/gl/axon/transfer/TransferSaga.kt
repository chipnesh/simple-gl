package me.chipnesh.gl.axon.transfer

import me.chipnesh.gl.axon.account.*
import org.axonframework.commandhandling.CommandBus
import org.axonframework.commandhandling.GenericCommandMessage
import org.axonframework.modelling.saga.EndSaga
import org.axonframework.modelling.saga.SagaEventHandler
import org.axonframework.modelling.saga.StartSaga
import org.slf4j.LoggerFactory
import java.math.BigDecimal
import javax.inject.Inject

class TransferSaga {

    companion object {
        private val log = LoggerFactory.getLogger(TransferSaga::class.java)
    }

    @Transient
    @Inject
    lateinit var commandBus: CommandBus
    lateinit var fromAccountId: String
    lateinit var toAccountId: String
    lateinit var amount: BigDecimal

    @StartSaga
    @SagaEventHandler(associationProperty = "transferId")
    fun on(event: TransferCreatedEvent) {
        log.info("Got $event event")
        fromAccountId = event.fromAccountId
        toAccountId = event.toAccountId
        amount = event.amount

        dispatch(DebitSourceAccountCommand(event.fromAccountId, event.transferId, event.amount))
    }

    @SagaEventHandler(associationProperty = "transferId")
    @EndSaga
    fun on(event: SourceAccountNotFoundEvent) {
        log.info("Got $event event")
        dispatch(MarkTransferFailedCommand(event.transferId))
    }

    @SagaEventHandler(associationProperty = "transferId")
    @EndSaga
    fun on(event: SourceAccountDebitRejectedEvent) {
        log.info("Got $event event")
        dispatch(MarkTransferFailedCommand(event.transferId))
    }


    @SagaEventHandler(associationProperty = "transferId")
    fun on(event: SourceAccountDebitedEvent) {
        log.info("Got $event event")
        dispatch(CreditDestinationAccountCommand(toAccountId, event.transferId, event.amount))
    }

    @SagaEventHandler(associationProperty = "transferId")
    @EndSaga
    fun on(event: DestinationAccountNotFoundEvent) {
        log.info("Got $event event")
        dispatch(ReturnMoneyCommand(fromAccountId, amount))
        dispatch(MarkTransferFailedCommand(event.transferId))
    }

    @EndSaga
    @SagaEventHandler(associationProperty = "transferId")
    fun on(event: DestinationAccountCreditedEvent) {
        log.info("Got $event event")
        dispatch(MarkTransferCompletedCommand(event.transferId))
    }

    private fun dispatch(command: Any) {
        commandBus.dispatch(GenericCommandMessage(command))
    }
}