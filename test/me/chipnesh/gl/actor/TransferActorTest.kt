@file:Suppress("EXPERIMENTAL_API_USAGE")

package me.chipnesh.gl.actor

import kotlinx.coroutines.*
import me.chipnesh.gl.actor.transfer.*
import me.chipnesh.gl.core.TransferOperationResult.Success
import me.chipnesh.gl.core.TransferOperationResult.TransferCreated
import me.chipnesh.gl.core.TransferStatus.COMPLETED
import me.chipnesh.gl.core.TransferStatus.FAILED
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.util.*
import kotlin.test.assertTrue

class TransferActorTest {

    private lateinit var job: Job
    private lateinit var actor: TransfersActor
    private lateinit var events: EventsGateway
    private val transfers = mutableMapOf<String, Transfer>()

    @Before
    fun setUp() {
        job = Job()
        val scope = CoroutineScope(Dispatchers.Unconfined + CoroutineName("Test scope") + job)
        events = EventsGateway(scope)
        actor = TransfersActor(scope, events, object : TransferStore {
            override fun get(key: String) = transfers[key]
            override fun set(key: String, value: Transfer) {
                transfers[key] = value
            }
        })
    }

    @After
    fun tearDown() {
        job.cancel()
    }

    @Test
    fun testCreateTransfer() {
        runBlocking {
            val id = UUID.randomUUID().toString()
            val from = UUID.randomUUID().toString()
            val to = UUID.randomUUID().toString()
            val amount = 50.toBigDecimal()
            val eventsStream = events.subscription()

            val command = CreateTransferCommand(id, from, to, amount)
            val result = actor.sendAndAwait(command)

            assertTrue { result is TransferCreated }
            require(result is TransferCreated)
            assertTrue { result.transferId == id }

            val event = eventsStream.receive()
            assertTrue { event is TransferCreatedEvent }
            require(event is TransferCreatedEvent)
            assertTrue { event.transferId == id }
            assertTrue { event.amount == amount }
            assertTrue { event.to == to }
            assertTrue { event.from == from }
        }
    }

    @Test
    fun testMarkTransferCompleted() {
        runBlocking {
            val id = UUID.randomUUID().toString()
            val from = UUID.randomUUID().toString()
            val to = UUID.randomUUID().toString()
            val amount = 50.toBigDecimal()
            transfers[id] = Transfer(id, from, to, amount)
            val eventsStream = events.subscription()

            val command = MarkTransferCompletedCommand(id)
            command.completion.complete(Success)
            actor.sendAndAwait(command)

            delay(100)
            assertTrue { eventsStream.isEmpty }
            assertTrue { transfers[id]?.status == COMPLETED }
        }
    }

    @Test
    fun testMarkTransferFailed() {
        runBlocking {
            val id = UUID.randomUUID().toString()
            val from = UUID.randomUUID().toString()
            val to = UUID.randomUUID().toString()
            val amount = 50.toBigDecimal()
            transfers[id] = Transfer(id, from, to, amount)
            val eventsStream = events.subscription()

            val command = MarkTransferFailedCommand(id)
            command.completion.complete(Success)
            actor.sendAndAwait(command)

            assertTrue { eventsStream.isEmpty }
            assertTrue { transfers[id]?.status == FAILED }
        }
    }
}