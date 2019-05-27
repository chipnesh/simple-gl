package me.chipnesh.gl.rest

import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.http.HttpStatusCode.Companion.BadRequest
import io.ktor.http.HttpStatusCode.Companion.Conflict
import io.ktor.http.HttpStatusCode.Companion.Created
import io.ktor.http.HttpStatusCode.Companion.NotFound
import io.ktor.http.HttpStatusCode.Companion.OK
import io.ktor.http.HttpStatusCode.Companion.UnprocessableEntity
import io.ktor.request.receiveOrNull
import io.ktor.response.respond
import io.ktor.routing.*
import me.chipnesh.gl.core.AccountFault.*
import me.chipnesh.gl.core.AccountOperationResult.*
import me.chipnesh.gl.core.AccountsOperations
import java.math.BigDecimal

data class CreateAccountRequest(val id: String)
data class DepositCashRequest(val amount: BigDecimal)
data class WithdrawCashRequest(val amount: BigDecimal)
data class AccountBalanceResponse(val id: String, val balance: BigDecimal) {
    constructor(result: AccountBalance) : this(result.accountId, result.balance)
}

fun Routing.account(accounts: AccountsOperations) {
    route("account") {
        post {
            val request = call.receiveOrNull<CreateAccountRequest>()
            if (request == null) {
                call.respond(BadRequest, "Missing create account request body")
                return@post
            }
            when (val result = accounts.create(request.id)) {
                is AccountCreated -> call.respond(Created)
                is Failure -> call.respondFailure(result)
            }
        }
        route("{id?}") {
            get {
                val id = call.parameters["id"]
                if (id == null) {
                    call.respond(BadRequest, "Missing 'id' parameter")
                    return@get
                }
                when (val result = accounts.getBalance(id)) {
                    is AccountBalance -> call.respond(OK, AccountBalanceResponse(result))
                    is Failure -> call.respondFailure(result)
                }
            }
            put("deposit") {
                val id = call.parameters["id"]
                if (id == null) {
                    call.respond(BadRequest, "Missing 'id' parameter")
                    return@put
                }
                val request = call.receiveOrNull<DepositCashRequest>()
                if (request == null) {
                    call.respond(BadRequest, "Missing deposit cash request body")
                    return@put
                }
                when (val result = accounts.depositCash(id, request.amount)) {
                    is Success -> call.respond(OK)
                    is Failure -> call.respondFailure(result)
                }
            }
            put("withdraw") {
                val id = call.parameters["id"]
                if (id == null) {
                    call.respond(BadRequest, "Missing 'id' parameter")
                    return@put
                }
                val request = call.receiveOrNull<WithdrawCashRequest>()
                if (request == null) {
                    call.respond(BadRequest, "Missing withdraw cash request body")
                    return@put
                }
                when (val result = accounts.withdrawCash(id, request.amount)) {
                    is Success -> call.respond(OK)
                    is Failure -> call.respondFailure(result)
                }
            }
        }
    }
}


suspend fun ApplicationCall.respondFailure(failure: Failure) = when (val fault = failure.fault) {
    is AccountNotFound -> respond(NotFound, "Account '${fault.accountId}' not found")
    is AccountAlreadyExists -> respond(Conflict, "Account '${fault.accountId}' already exists")
    is NotEnoughMoney -> respond(UnprocessableEntity, "Not enough money in account '${fault.accountId}")
    is WrongAmount -> respond(BadRequest, "Got wrong amount '${fault.amount}")
}
