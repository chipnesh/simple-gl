package me.chipnesh.gl.axon.transfer

import me.chipnesh.gl.core.TransferStatus
import me.chipnesh.gl.core.TransferStatus.*
import java.math.BigDecimal

data class TransferEntity(
    var transferId: String,
    var fromId: String,
    var toId: String,
    var amount: BigDecimal,
    var status: TransferStatus = STARTED
) {

    fun markCompleted() {
        status = COMPLETED
    }

    fun markFailed() {
        status = FAILED
    }
}
