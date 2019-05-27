package me.chipnesh.gl.axon.transfer

import me.chipnesh.gl.core.TransferStatus.COMPLETED
import me.chipnesh.gl.core.TransferStatus.FAILED
import org.axonframework.eventhandling.EventHandler
import org.slf4j.LoggerFactory

class TransferEventHandler(
    private val storage: TransfersStorage
) {

    companion object {
        private val log = LoggerFactory.getLogger(TransferEventHandler::class.java)
    }

    @EventHandler
    fun on(event: TransferCreatedEvent) {
        log.info("Got $event event")
        storage.save(
            TransferEntity(
                event.transferId,
                event.fromAccountId,
                event.toAccountId,
                event.amount
            )
        )
    }

    @EventHandler
    fun on(event: TransferFailedEvent) {
        log.info("Got $event event")
        storage.findById(event.transferId)?.let { transfer ->
            transfer.status = FAILED
            storage.save(transfer)
        }
    }

    @EventHandler
    fun on(event: TransferCompletedEvent) {
        log.info("Got $event event")
        storage.findById(event.transferId)?.let { transfer ->
            transfer.status = COMPLETED
            storage.save(transfer)
        }
    }
}