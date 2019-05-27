package me.chipnesh.gl.axon

import me.chipnesh.gl.axon.account.*
import org.axonframework.test.aggregate.AggregateTestFixture
import org.axonframework.test.aggregate.FixtureConfiguration
import org.junit.Before
import org.junit.Test
import java.math.BigDecimal

class AccountAggregateTest {

    private lateinit var testFixture: FixtureConfiguration<AccountAggregate>
    private val id = "id"

    @Before
    fun setUp() {
        testFixture = AggregateTestFixture(AccountAggregate::class.java)
        testFixture.registerAnnotatedCommandHandler(
            AccountCommandHandler(testFixture.repository, testFixture.eventBus)
        )
        testFixture.setReportIllegalStateChange(false)
    }

    @Test
    fun testCreateAccount() {
        testFixture.givenNoPriorActivity()
            .`when`(CreateAccountCommand(id))
            .expectEvents(AccountCreatedEvent(id))
    }

    @Test
    fun testDepositMoney() {
        testFixture.given(AccountCreatedEvent(id))
            .`when`(DepositMoneyCommand(id, BigDecimal(1000)))
            .expectEvents(MoneyDepositedEvent(id, BigDecimal(1000)))
    }

    @Test
    fun testWithdrawMoney() {
        testFixture.given(AccountCreatedEvent(id), MoneyDepositedEvent(id, BigDecimal(50)))
            .`when`(WithdrawMoneyCommand(id, BigDecimal(50)))
            .expectEvents(MoneyWithdrawnEvent(id, BigDecimal(50)))
    }

    @Test
    fun testWithdrawMoney_RejectWithdrawal() {
        testFixture.given(AccountCreatedEvent(id), MoneyDepositedEvent(id, BigDecimal(50)))
            .`when`(WithdrawMoneyCommand(id, BigDecimal(51)))
            .expectNoEvents()
    }
}