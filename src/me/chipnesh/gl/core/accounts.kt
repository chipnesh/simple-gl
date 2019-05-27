package me.chipnesh.gl.core

import java.math.BigDecimal

sealed class AccountOperationResult {
    object Success : AccountOperationResult()
    data class AccountCreated(val accountId: String) : AccountOperationResult()
    data class AccountBalance(val accountId: String, val balance: BigDecimal) : AccountOperationResult()
    data class Failure(val fault: AccountFault) : AccountOperationResult()
}

sealed class AccountFault {
    data class AccountNotFound(val accountId: String) : AccountFault()
    data class AccountAlreadyExists(val accountId: String) : AccountFault()
    data class NotEnoughMoney(val accountId: String) : AccountFault()
    data class WrongAmount(val amount: BigDecimal) : AccountFault()
}

interface AccountsOperations {
    suspend fun depositCash(to: String, amount: BigDecimal): AccountOperationResult
    suspend fun withdrawCash(from: String, amount: BigDecimal): AccountOperationResult
    suspend fun getBalance(from: String): AccountOperationResult
    suspend fun create(id: String): AccountOperationResult
}