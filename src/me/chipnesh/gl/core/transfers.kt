package me.chipnesh.gl.core

import java.math.BigDecimal

sealed class TransferOperationResult {
    object Success : TransferOperationResult()
    data class TransferCreated(val transferId: String) : TransferOperationResult()
    data class TransferStatusResult(val transferId: String, val status: TransferStatus) : TransferOperationResult()
    data class Failure(val fault: TransferFault) : TransferOperationResult()
}

sealed class TransferFault {
    class TransferNotFound(val transferId: String) : TransferFault()
    class TransferAlreadyExist(val transferId: String) : TransferFault()
}

enum class TransferStatus {
    STARTED,
    FAILED,
    COMPLETED
}

interface TransfersOperations {
    suspend fun create(id: String, from: String, to: String, amount: BigDecimal): TransferOperationResult
    suspend fun status(transferId: String): TransferOperationResult
}