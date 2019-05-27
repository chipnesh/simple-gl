@file:Suppress("EXPERIMENTAL_API_USAGE")

package me.chipnesh.gl.actor.transfer

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.actor
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.launch
import me.chipnesh.gl.actor.DomainEvent
import me.chipnesh.gl.actor.EventsGateway
import me.chipnesh.gl.actor.TransferCreatedEvent
import me.chipnesh.gl.core.TransferFault
import me.chipnesh.gl.core.TransferFault.TransferAlreadyExist
import me.chipnesh.gl.core.TransferFault.TransferNotFound
import me.chipnesh.gl.core.TransferOperationResult
import me.chipnesh.gl.core.TransferOperationResult.*
import org.slf4j.LoggerFactory
import java.math.BigDecimal

sealed class TransferCommand(val completion: CompletableDeferred<TransferOperationResult> = CompletableDeferred())

data class CreateTransferCommand(val id: String, val from: String, val to: String, val amount: BigDecimal) :
    TransferCommand()

data class GetTransferStatusCommand(val transferId: String) : TransferCommand()
data class MarkTransferFailedCommand(val transferId: String) : TransferCommand()
data class MarkTransferCompletedCommand(val transferId: String) : TransferCommand()

class TransfersActor(
    scope: CoroutineScope,
    private val events: EventsGateway,
    private val transfers: TransferStorage
) {

    companion object {
        private val log = LoggerFactory.getLogger(TransfersActor::class.java)
    }

    private val actor = scope.actor<TransferCommand> {
        consumeEach { command ->
            log.info("TransfersActor got a command $command")

            when (command) {
                is CreateTransferCommand -> {
                    val (transferId, from, to, amount) = command
                    when {
                        transfers[transferId] != null -> command.fail(TransferAlreadyExist(transferId))
                        else -> {
                            transfers[transferId] = Transfer(transferId, from, to, amount)
                            notify(TransferCreatedEvent(transferId, from, to, amount))
                            command.complete(TransferCreated(transferId))
                        }
                    }
                }

                is MarkTransferFailedCommand ->
                    transfers[command.transferId]?.setFailed()

                is MarkTransferCompletedCommand ->
                    transfers[command.transferId]?.setCompleted()

                is GetTransferStatusCommand -> {
                    when (val found = transfers[command.transferId]) {
                        null -> command.fail(TransferNotFound(command.transferId))
                        else -> command.complete(TransferStatusResult(found.transferId, found.status))
                    }
                }
            }
        }
    }


    fun CoroutineScope.sendAsync(command: TransferCommand) = launch { actor.send(command) }
    suspend fun sendAndAwait(command: TransferCommand) = actor.send(command).run { command.completion.await() }
    private fun CoroutineScope.notify(event: DomainEvent) = with(events) { sendAsync(event) }
    private fun TransferCommand.complete(result: TransferOperationResult) = completion.complete(result)
    private fun TransferCommand.fail(fault: TransferFault) = completion.complete(Failure(fault))
}