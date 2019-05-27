@file:Suppress("EXPERIMENTAL_API_USAGE")

package me.chipnesh.gl.actor

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import java.math.BigDecimal

sealed class DomainEvent

data class TransferCreatedEvent(
    val transferId: String,
    val from: String,
    val to: String,
    val amount: BigDecimal
) : DomainEvent()

data class SourceAccountNotFoundEvent(
    val transferId: String,
    val accountId: String
) : DomainEvent()

data class DestinationAccountNotFoundEvent(
    val transferId: String,
    val source: String,
    val amount: BigDecimal
) : DomainEvent()

data class SourceAccountDebitedEvent(
    val transferId: String,
    val source: String,
    val destination: String,
    val amount: BigDecimal
) : DomainEvent()

data class DestinationAccountCreditedEvent(
    val transferId: String,
    val accountId: String
) : DomainEvent()

data class SourceAccountDebitRejectedEvent(
    val transferId: String,
    val accountId: String
) : DomainEvent()

data class DestinationAccountCreditRejectedEvent(
    val transferId: String,
    val accountId: String,
    val amount: BigDecimal
) : DomainEvent()

class EventsGateway(
    scope: CoroutineScope
) {
    private val log = LoggerFactory.getLogger(EventsGateway::class.java)
    private val events = BroadcastChannel<DomainEvent>(1)

    init {
        scope.launch {
            // listen for debugging purposes
            events.consumeEach { event -> log.debug("EventsGateway got an event: $event") }
        }
    }

    fun CoroutineScope.sendAsync(event: DomainEvent) = launch { events.send(event) }
    suspend fun consumeEach(handler: (DomainEvent) -> Unit) = events.consumeEach(handler)
    fun subscription() = events.openSubscription()
}