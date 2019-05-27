package me.chipnesh.gl.axon.transfer

import java.util.concurrent.ConcurrentHashMap

class TransfersStorage {
    private val transfersMap = ConcurrentHashMap<String, TransferEntity>()

    fun save(transfer: TransferEntity): TransferEntity {
        transfersMap.merge(transfer.transferId, transfer) { first, second ->
            first.status = second.status
            first
        }
        return transfer
    }

    fun findById(transferId: String) = transfersMap[transferId]
}