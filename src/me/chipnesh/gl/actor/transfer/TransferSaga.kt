package me.chipnesh.gl.actor.transfer

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import me.chipnesh.gl.actor.*
import me.chipnesh.gl.actor.account.*
import org.slf4j.LoggerFactory

class TransferSaga(
    private val scope: CoroutineScope,
    private val accounts: AccountsActor,
    private val transfers: TransfersActor,
    private val events: EventsGateway
) {
    companion object {
        private val log = LoggerFactory.getLogger(TransferSaga::class.java)
    }

    fun init(): TransferSaga {
        scope.launch {
            events.consumeEach { event ->
                log.info("TransferSaga got an event $event")

                when (event) {
                    is TransferCreatedEvent ->
                        sendToAccounts(DebitSourceAccountCommand(event.transferId, event.from, event.to, event.amount))

                    is SourceAccountNotFoundEvent ->
                        sendToTransfers(MarkTransferFailedCommand(event.transferId))

                    is SourceAccountDebitRejectedEvent ->
                        sendToTransfers(MarkTransferFailedCommand(event.transferId))

                    is SourceAccountDebitedEvent -> {
                        val (transferId, source, destination, amount) = event
                        sendToAccounts(CreditDestinationAccountCommand(transferId, source, destination, amount))
                    }

                    is DestinationAccountNotFoundEvent -> {
                        sendToAccounts(ReturnMoneyBackCommand(event.transferId, event.source, event.amount))
                        sendToTransfers(MarkTransferFailedCommand(event.transferId))
                    }

                    is DestinationAccountCreditRejectedEvent -> {
                        sendToAccounts(ReturnMoneyBackCommand(event.transferId, event.accountId, event.amount))
                        sendToTransfers(MarkTransferFailedCommand(event.transferId))
                    }

                    is DestinationAccountCreditedEvent -> {
                        sendToTransfers(MarkTransferCompletedCommand(event.transferId))
                    }
                }
            }
        }
        return this
    }

    private fun CoroutineScope.sendToAccounts(cmd: AccountCommand) = with(accounts) { sendAsync(cmd) }
    private fun CoroutineScope.sendToTransfers(cmd: TransferCommand) = with(transfers) { sendAsync(cmd) }
}