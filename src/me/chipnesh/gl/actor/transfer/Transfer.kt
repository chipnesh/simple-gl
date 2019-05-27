package me.chipnesh.gl.actor.transfer

import me.chipnesh.gl.core.TransferStatus
import org.slf4j.LoggerFactory
import java.math.BigDecimal

data class Transfer(
    val transferId: String,
    val fromId: String,
    val toId: String,
    val amount: BigDecimal,
    var status: TransferStatus = TransferStatus.STARTED
) {
    companion object {
        private val log = LoggerFactory.getLogger(Transfer::class.java)
    }

    fun setFailed() {
        log.info("Setting transfer '$this' failed")
        status = TransferStatus.FAILED
    }

    fun setCompleted() {
        log.info("Setting transfer '$this' completed")
        status = TransferStatus.COMPLETED
    }
}