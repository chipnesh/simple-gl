package me.chipnesh.gl.axon

import me.chipnesh.gl.axon.transfer.*
import org.axonframework.test.aggregate.AggregateTestFixture
import org.axonframework.test.aggregate.FixtureConfiguration
import org.junit.Before
import org.junit.Test
import java.math.BigDecimal

class TransferAggregateTest {

    private lateinit var fixture: FixtureConfiguration<TransferAggregate>

    @Before
    fun setUp() {
        fixture = AggregateTestFixture(TransferAggregate::class.java)
    }

    @Test
    fun testCreateTransfer() {
        fixture.givenNoPriorActivity()
            .`when`(CreateTransferCommand("transferId", "fromAccountId", "toAccountId", 20.toBigDecimal()))
            .expectEvents(TransferCreatedEvent("transferId", "fromAccountId", "toAccountId", 20.toBigDecimal()))
    }

    @Test
    fun testMarkTransferCompleted() {
        fixture.given(TransferCreatedEvent("transferId", "fromAccountId", "toAccountId", 20.toBigDecimal()))
            .`when`(MarkTransferCompletedCommand("transferId"))
            .expectEvents(TransferCompletedEvent("transferId"))
    }

    @Test
    fun testMarkTransferFailed() {
        fixture.given(TransferCreatedEvent("transferId", "fromAccountId", "toAccountId", 20.toBigDecimal()))
            .`when`(MarkTransferFailedCommand("transferId"))
            .expectEvents(TransferFailedEvent("transferId"))
    }
}