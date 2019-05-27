package me.chipnesh.gl.axon

import kotlinx.coroutines.future.await
import me.chipnesh.gl.axon.transfer.CreateTransferCommand
import me.chipnesh.gl.axon.transfer.GetTransferStatus
import me.chipnesh.gl.axon.transfer.TransfersStore
import me.chipnesh.gl.core.TransferFault.TransferAlreadyExist
import me.chipnesh.gl.core.TransferOperationResult
import me.chipnesh.gl.core.TransferOperationResult.TransferCreated
import me.chipnesh.gl.core.TransferOperationResult.Failure
import me.chipnesh.gl.core.TransfersOperations
import org.axonframework.config.Configuration
import java.math.BigDecimal

class TransfersAggregateProxy(
    configuration: Configuration,
    private val transfersStore: TransfersStore
) : TransfersOperations {
    private val commands = configuration.commandGateway()
    private val queries = configuration.queryGateway()

    override suspend fun create(id: String, from: String, to: String, amount: BigDecimal): TransferOperationResult {
        if (transfersStore.findById(id) != null) return Failure(TransferAlreadyExist(id))
        println(commands.sendAndWait<String>(CreateTransferCommand(id, from, to, amount)))
        return TransferCreated(id)
    }

    override suspend fun status(transferId: String): TransferOperationResult =
        queries.query(GetTransferStatus(transferId), TransferOperationResult::class.java).await()
}