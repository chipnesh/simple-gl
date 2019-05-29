package me.chipnesh.gl.axon

import me.chipnesh.gl.axon.account.*
import me.chipnesh.gl.axon.transfer.MarkTransferCompletedCommand
import me.chipnesh.gl.axon.transfer.MarkTransferFailedCommand
import me.chipnesh.gl.axon.transfer.TransferCreatedEvent
import me.chipnesh.gl.axon.transfer.TransferSaga
import org.axonframework.test.saga.FixtureConfiguration
import org.axonframework.test.saga.SagaTestFixture
import org.junit.Before
import org.junit.Test

class TransferSagaTest {

    private lateinit var testFixture: FixtureConfiguration

    @Before
    fun setUp() {
        testFixture = SagaTestFixture(TransferSaga::class.java)
    }

    @Test
    fun testTransferCreated() {
        testFixture.givenNoPriorActivity()
            .whenAggregate("transferId").publishes(
                TransferCreatedEvent(
                    "transferId",
                    "fromAccountId",
                    "toAccountId",
                    40.toBigDecimal()
                )
            )
            .expectActiveSagas(1)
            .expectDispatchedCommands(
                DebitSourceAccountCommand(
                    "fromAccountId",
                    "transferId",
                    40.toBigDecimal()
                )
            )
    }

    @Test
    fun testSourceAccountNotFound() {
        testFixture.givenAggregate("transferId").published(
            TransferCreatedEvent(
                "transferId",
                "fromAccountId",
                "toAccountId",
                40.toBigDecimal()
            )
        )
            .whenPublishingA(SourceAccountNotFoundEvent("transferId"))
            .expectActiveSagas(0)
            .expectDispatchedCommands(MarkTransferFailedCommand("transferId"))
    }

    @Test
    fun testSourceAccountDebitRejected() {
        testFixture.givenAggregate("transferId").published(
            TransferCreatedEvent(
                "transferId",
                "fromAccountId",
                "toAccountId",
                40.toBigDecimal()
            )
        )
            .whenAggregate("fromAccountId")
            .publishes(SourceAccountDebitRejectedEvent("transferId"))
            .expectActiveSagas(0)
            .expectDispatchedCommands(MarkTransferFailedCommand("transferId"))
    }

    @Test
    fun testSourceAccountDebited() {
        testFixture.givenAggregate("transferId").published(
            TransferCreatedEvent(
                "transferId",
                "fromAccountId",
                "toAccountId",
                40.toBigDecimal()
            )
        )
            .whenAggregate("fromAccountId").publishes(
                SourceAccountDebitedEvent(
                    "fromAccountId",
                    40.toBigDecimal(),
                    "transferId"
                )
            )
            .expectActiveSagas(1)
            .expectDispatchedCommands(
                CreditDestinationAccountCommand(
                    "toAccountId",
                    "transferId",
                    40.toBigDecimal()
                )
            )
    }

    @Test
    fun testDestinationAccountNotFound() {
        testFixture.givenAggregate("transferId").published(
            TransferCreatedEvent(
                "transferId",
                "fromAccountId",
                "toAccountId",
                40.toBigDecimal()
            )
        )
            .andThenAggregate("fromAccountId").published(
                SourceAccountDebitedEvent(
                    "fromAccountId", 40.toBigDecimal(), "transferId"
                )
            )
            .whenPublishingA(DestinationAccountNotFoundEvent("transferId"))
            .expectActiveSagas(0)
            .expectDispatchedCommands(
                ReturnMoneyCommand(
                    "fromAccountId",
                    40.toBigDecimal()
                ),
                MarkTransferFailedCommand("transferId")
            )
    }

    @Test
    fun testDestinationAccountCredited() {
        testFixture.givenAggregate("transferId").published(
            TransferCreatedEvent(
                "transferId",
                "fromAccountId",
                "toAccountId",
                40.toBigDecimal()
            )
        )
            .andThenAggregate("fromAccountId").published(
                SourceAccountDebitedEvent(
                    "fromAccountId",
                    40.toBigDecimal(),
                    "transferId"
                )
            )
            .whenAggregate("toAccountId").publishes(
                DestinationAccountCreditedEvent(
                    "toAccountId",
                    40.toBigDecimal(),
                    "transferId"
                )
            )
            .expectActiveSagas(0)
            .expectDispatchedCommands(MarkTransferCompletedCommand("transferId"))
    }
}