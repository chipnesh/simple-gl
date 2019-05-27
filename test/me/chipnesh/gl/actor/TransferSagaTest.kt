package me.chipnesh.gl.actor

import kotlinx.coroutines.*
import me.chipnesh.gl.actor.account.Account
import me.chipnesh.gl.actor.account.AccountsActor
import me.chipnesh.gl.actor.account.AccountsStore
import me.chipnesh.gl.actor.transfer.*
import me.chipnesh.gl.core.TransferOperationResult.TransferCreated
import me.chipnesh.gl.core.TransferStatus
import me.chipnesh.gl.core.TransferStatus.FAILED
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.math.BigDecimal.ZERO
import java.util.*
import kotlin.test.assertTrue

class TransferSagaTest {
    private lateinit var job: Job
    private lateinit var transfersActor: TransfersActor
    private lateinit var accountsActor: AccountsActor
    private lateinit var events: EventsGateway
    private val transfers = mutableMapOf<String, Transfer>()
    private val accounts = mutableMapOf<String, Account>()

    @Before
    fun setUp() {
        job = Job()
        val scope = CoroutineScope(Dispatchers.Default + CoroutineName("Test scope") + job)
        events = EventsGateway(scope)
        transfersActor = TransfersActor(scope, events, object : TransferStore {
            override fun get(key: String) = transfers[key]
            override fun set(key: String, value: Transfer) {
                transfers[key] = value
            }
        })
        accountsActor = AccountsActor(scope, events, object : AccountsStore {
            override fun get(key: String) = accounts[key]
            override fun set(key: String, value: Account) {
                accounts[key] = value
            }
        })
        TransferSaga(scope, accountsActor, transfersActor, events).init()
    }

    @After
    fun tearDown() {
        job.cancel()
    }

    @Test
    fun testTransferCreated() {
        runBlocking {
            val transferId = UUID.randomUUID().toString()
            val from = UUID.randomUUID().toString()
            val to = UUID.randomUUID().toString()
            val amount = 50.toBigDecimal()
            val eventStream = events.subscription()
            accounts[from] = Account(from)
            accounts[to] = Account(to)

            val result = transfersActor.sendAndAwait(CreateTransferCommand(transferId, from, to, amount))

            assertTrue { result is TransferCreated }

            assertTrue(eventStream.receive() is TransferCreatedEvent)
        }
    }

    @Test
    fun testSourceAccountNotFound() {
        runBlocking {
            val transferId = UUID.randomUUID().toString()
            val from = UUID.randomUUID().toString()
            val to = UUID.randomUUID().toString()
            val amount = 50.toBigDecimal()
            val eventStream = events.subscription()

            val result = transfersActor.sendAndAwait(CreateTransferCommand(transferId, from, to, amount))

            assertTrue { result is TransferCreated }

            assertTrue(eventStream.receive() is TransferCreatedEvent)
            assertTrue(eventStream.receive() is SourceAccountNotFoundEvent)

            delay(100)
            assertTrue { transfers[transferId]?.status == FAILED }
        }
    }

    @Test
    fun testSourceAccountDebitRejected() {
        runBlocking {
            val transferId = UUID.randomUUID().toString()
            val from = UUID.randomUUID().toString()
            val to = UUID.randomUUID().toString()
            val amount = 50.toBigDecimal()
            val eventStream = events.subscription()
            accounts[from] = Account(from, balance = 10.toBigDecimal())
            accounts[to] = Account(to)

            val result = transfersActor.sendAndAwait(CreateTransferCommand(transferId, from, to, amount))

            assertTrue { result is TransferCreated }

            assertTrue(eventStream.receive() is TransferCreatedEvent)
            assertTrue(eventStream.receive() is SourceAccountDebitRejectedEvent)

            delay(100)
            assertTrue { transfers[transferId]?.status == FAILED }
        }
    }

    @Test
    fun testSourceAccountDebited() {
        runBlocking {
            val transferId = UUID.randomUUID().toString()
            val from = UUID.randomUUID().toString()
            val to = UUID.randomUUID().toString()
            val amount = 50.toBigDecimal()
            val eventStream = events.subscription()
            accounts[from] = Account(from, balance = amount)
            accounts[to] = Account(to)

            val result = transfersActor.sendAndAwait(CreateTransferCommand(transferId, from, to, amount))

            assertTrue { result is TransferCreated }

            assertTrue(eventStream.receive() is TransferCreatedEvent)
            assertTrue(eventStream.receive() is SourceAccountDebitedEvent)

            assertTrue { accounts[from]?.balance == ZERO }
        }
    }

    @Test
    fun testDestinationAccountNotFound() {
        runBlocking {
            val transferId = UUID.randomUUID().toString()
            val from = UUID.randomUUID().toString()
            val to = UUID.randomUUID().toString()
            val amount = 50.toBigDecimal()
            val eventStream = events.subscription()
            accounts[from] = Account(from, balance = amount)

            val result = transfersActor.sendAndAwait(CreateTransferCommand(transferId, from, to, amount))

            assertTrue { result is TransferCreated }

            assertTrue(eventStream.receive() is TransferCreatedEvent)
            assertTrue(eventStream.receive() is SourceAccountDebitedEvent)
            assertTrue(eventStream.receive() is DestinationAccountNotFoundEvent)

            delay(100)
            assertTrue { accounts[from]?.balance == amount }
            assertTrue { transfers[transferId]?.status == FAILED }
        }
    }

    @Test
    fun testDestinationAccountCredited() {
        runBlocking {
            val transferId = UUID.randomUUID().toString()
            val from = UUID.randomUUID().toString()
            val to = UUID.randomUUID().toString()
            val amount = 50.toBigDecimal()
            val eventStream = events.subscription()
            accounts[from] = Account(from, balance = amount)
            accounts[to] = Account(to)

            val result = transfersActor.sendAndAwait(CreateTransferCommand(transferId, from, to, amount))

            assertTrue { result is TransferCreated }

            assertTrue(eventStream.receive() is TransferCreatedEvent)
            assertTrue(eventStream.receive() is SourceAccountDebitedEvent)
            assertTrue(eventStream.receive() is DestinationAccountCreditedEvent)

            delay(100)
            assertTrue { accounts[from]?.balance == ZERO }
            assertTrue { accounts[to]?.balance == amount }
            assertTrue { transfers[transferId]?.status == TransferStatus.COMPLETED }
        }
    }
}