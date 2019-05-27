@file:Suppress("EXPERIMENTAL_API_USAGE")

package me.chipnesh.gl

import com.fasterxml.jackson.databind.SerializationFeature
import io.ktor.application.Application
import io.ktor.application.install
import io.ktor.features.ContentNegotiation
import io.ktor.jackson.jackson
import io.ktor.routing.routing
import io.ktor.server.cio.EngineMain
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import me.chipnesh.gl.actor.AccountsActorProxy
import me.chipnesh.gl.actor.EventsGateway
import me.chipnesh.gl.actor.TransfersActorProxy
import me.chipnesh.gl.actor.account.AccountsActor
import me.chipnesh.gl.actor.account.MapAccountStore
import me.chipnesh.gl.actor.transfer.MapTransferStore
import me.chipnesh.gl.actor.transfer.TransferSaga
import me.chipnesh.gl.actor.transfer.TransfersActor
import me.chipnesh.gl.axon.AccountsAggregateProxy
import me.chipnesh.gl.axon.TransfersAggregateProxy
import me.chipnesh.gl.axon.account.AccountsStore
import me.chipnesh.gl.axon.buildInMemoryConfiguration
import me.chipnesh.gl.axon.transfer.TransfersStore
import me.chipnesh.gl.core.AccountsOperations
import me.chipnesh.gl.core.TransfersOperations
import me.chipnesh.gl.rest.account
import me.chipnesh.gl.rest.transfer
import me.chipnesh.gl.utils.getProperty

fun main(args: Array<String>) = EngineMain.main(args)

fun Application.module() {
    serializationModule()
    when {
        getProperty("mode") == "axon" -> axonModule()
        else -> actorModule()
    }
}

fun Application.serializationModule() {
    install(ContentNegotiation) {
        jackson {
            enable(SerializationFeature.INDENT_OUTPUT)
        }
    }
}

fun Application.axonModule() {
    val accountsStore = AccountsStore()
    val transfersStore = TransfersStore()

    val config = buildInMemoryConfiguration(accountsStore, transfersStore)
    config.start()

    val accounts = AccountsAggregateProxy(config, accountsStore)
    val transfers = TransfersAggregateProxy(config, transfersStore)
    routing(accounts, transfers)
}

fun Application.actorModule() {
    val scope = CoroutineScope(Dispatchers.Default + CoroutineName("Application scope"))
    val eventsGateway = EventsGateway(scope)
    val accountsActor = AccountsActor(scope, eventsGateway, MapAccountStore)
    val transfersActor = TransfersActor(scope, eventsGateway, MapTransferStore)

    TransferSaga(scope, accountsActor, transfersActor, eventsGateway).init()

    val accounts = AccountsActorProxy(accountsActor)
    val transfers = TransfersActorProxy(transfersActor)
    routing(accounts, transfers)
}

private fun Application.routing(
    accounts: AccountsOperations,
    transfers: TransfersOperations
) = routing {
    account(accounts)
    transfer(transfers)
}