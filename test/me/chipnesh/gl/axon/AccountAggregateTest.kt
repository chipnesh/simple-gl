package me.chipnesh.gl.axon

import me.chipnesh.gl.axon.account.*
import org.axonframework.test.aggregate.AggregateTestFixture
import org.axonframework.test.aggregate.FixtureConfiguration
import org.junit.Before
import org.junit.Test

class AccountAggregateTest {

    private lateinit var testFixture: FixtureConfiguration<AccountAggregate>

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
            .`when`(CreateAccountCommand("id"))
            .expectEvents(AccountCreatedEvent("id"))
    }

    @Test
    fun testDepositMoney() {
        testFixture.given(AccountCreatedEvent("id"))
            .`when`(DepositMoneyCommand("id", 1000.toBigDecimal()))
            .expectEvents(MoneyDepositedEvent("id", 1000.toBigDecimal()))
    }

    @Test
    fun testWithdrawMoney() {
        testFixture.given(AccountCreatedEvent("id"), MoneyDepositedEvent("id", 50.toBigDecimal()))
            .`when`(WithdrawMoneyCommand("id", 50.toBigDecimal()))
            .expectEvents(MoneyWithdrawnEvent("id", 50.toBigDecimal()))
    }

    @Test
    fun testWithdrawMoney_RejectWithdrawal() {
        testFixture.given(AccountCreatedEvent("id"), MoneyDepositedEvent("id", 50.toBigDecimal()))
            .`when`(WithdrawMoneyCommand("id", 51.toBigDecimal()))
            .expectNoEvents()
    }
}