package me.chipnesh.gl.actor

import me.chipnesh.gl.actor.transfer.CreateTransferCommand
import me.chipnesh.gl.actor.transfer.GetTransferStatusCommand
import me.chipnesh.gl.actor.transfer.TransfersActor
import me.chipnesh.gl.core.TransfersOperations
import java.math.BigDecimal

class TransfersActorProxy(
    private val actor: TransfersActor
) : TransfersOperations {

    override suspend fun create(id: String, from: String, to: String, amount: BigDecimal) =
        actor.sendAndAwait(CreateTransferCommand(id, from, to, amount))

    override suspend fun status(transferId: String) =
        actor.sendAndAwait(GetTransferStatusCommand(transferId))
}