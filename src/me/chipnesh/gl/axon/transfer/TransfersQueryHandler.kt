package me.chipnesh.gl.axon.transfer

import me.chipnesh.gl.core.TransferFault.TransferNotFound
import me.chipnesh.gl.core.TransferOperationResult
import me.chipnesh.gl.core.TransferOperationResult.Failure
import me.chipnesh.gl.core.TransferOperationResult.TransferStatusResult
import org.axonframework.queryhandling.QueryHandler
import org.slf4j.LoggerFactory

class TransfersQueryHandler(
    private val storage: TransfersStorage
) {
    companion object {
        private val log = LoggerFactory.getLogger(TransfersQueryHandler::class.java)
    }

    @QueryHandler
    fun get(qry: GetTransferStatus): TransferOperationResult {
        log.info("Got $qry query")
        val transfer = storage.findById(qry.transferId)
            ?: return Failure(TransferNotFound(qry.transferId))
        return TransferStatusResult(transfer.transferId, transfer.status)
    }
}