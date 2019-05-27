package me.chipnesh.gl.rest

import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.http.HttpStatusCode.Companion.BadRequest
import io.ktor.http.HttpStatusCode.Companion.Conflict
import io.ktor.http.HttpStatusCode.Companion.Created
import io.ktor.http.HttpStatusCode.Companion.NotFound
import io.ktor.http.HttpStatusCode.Companion.OK
import io.ktor.request.receiveOrNull
import io.ktor.response.respond
import io.ktor.routing.Routing
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.route
import me.chipnesh.gl.core.TransferFault
import me.chipnesh.gl.core.TransferOperationResult.*
import me.chipnesh.gl.core.TransfersOperations
import java.math.BigDecimal

data class TransferRequest(val id: String, val from: String, val to: String, val amount: BigDecimal)

fun Routing.transfer(transfers: TransfersOperations) {
    route("transfer") {
        post {
            val request = call.receiveOrNull<TransferRequest>()
            if (request == null) {
                call.respond(BadRequest, "Missing transfer request body")
                return@post
            }
            when (val result = transfers.create(request.id, request.from, request.to, request.amount)) {
                is TransferCreated -> call.respond(Created)
                is Failure -> call.respondFailure(result)
            }

        }
        get("{id?}") {
            val id = call.parameters["id"]
            if (id == null) {
                call.respond(BadRequest, "Missing 'id' parameter")
                return@get
            }
            when (val result = transfers.status(id)) {
                is TransferStatusResult -> call.respond(OK, result)
                is Failure -> call.respondFailure(result)
            }
        }
    }
}


suspend fun ApplicationCall.respondFailure(failure: Failure) = when (val fault = failure.fault) {
    is TransferFault.TransferNotFound -> respond(NotFound, "Transfer '${fault.transferId}' not found")
    is TransferFault.TransferAlreadyExist -> respond(Conflict, "Transfer '${fault.transferId}' already exists")
}