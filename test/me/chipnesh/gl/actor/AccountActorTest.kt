package me.chipnesh.gl.actor

import kotlinx.coroutines.*
import me.chipnesh.gl.actor.account.*
import me.chipnesh.gl.core.AccountFault.NotEnoughMoney
import me.chipnesh.gl.core.AccountOperationResult.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.math.BigDecimal.ZERO
import java.util.*
import kotlin.test.assertTrue

class AccountActorTest {

    private lateinit var job: Job
    private lateinit var actor: AccountsActor
    private lateinit var events: EventsGateway
    private val accounts = mutableMapOf<String, Account>()

    @Before
    fun setUp() {
        job = Job()
        val scope = CoroutineScope(Dispatchers.Unconfined + CoroutineName("Test scope") + job)
        events = EventsGateway(scope)
        actor = AccountsActor(scope, events, object : AccountsStore {
            override fun get(key: String) = accounts[key]
            override fun set(key: String, value: Account) {
                accounts[key] = value
            }
        })
    }

    @After
    fun tearDown() {
        job.cancel()
    }

    @Test
    fun testCreateAccount() {
        runBlocking {
            val id = UUID.randomUUID().toString()
            val command = CreateAccountCommand(id)

            val result = actor.sendAndAwait(command)

            assertTrue { result is AccountCreated }
            require(result is AccountCreated)
            assertTrue { result.accountId == id }
            assertTrue { accounts[id]?.accountId == id }
            assertTrue { accounts[id]?.balance == ZERO }
        }


    }

    @Test
    fun testDepositMoney() {
        runBlocking {
            val id = UUID.randomUUID().toString()
            accounts[id] = Account(id)

            val command = DepositMoneyCommand(id, 50.toBigDecimal())
            val result = actor.sendAndAwait(command)

            assertTrue { result is Success }
            assertTrue { accounts[id]?.balance == 50.toBigDecimal() }
        }
    }

    @Test
    fun testWithdrawMoney() {
        runBlocking {
            val id = UUID.randomUUID().toString()
            accounts[id] = Account(id, balance = 50.toBigDecimal())

            val command = WithdrawMoneyCommand(id, 50.toBigDecimal())
            val result = actor.sendAndAwait(command)

            assertTrue { result is Success }
            assertTrue { accounts[id]?.balance == ZERO }
        }
    }

    @Test
    fun testWithdrawMoney_RejectedWithdrawal() {
        runBlocking {
            val id = UUID.randomUUID().toString()
            accounts[id] = Account(id)

            val command = WithdrawMoneyCommand(id, 50.toBigDecimal())
            val result = actor.sendAndAwait(command)

            assertTrue { result is Failure }
            require(result is Failure)
            assertTrue { result.fault == NotEnoughMoney(id) }
            assertTrue { accounts[id]?.balance == ZERO }
        }
    }
}