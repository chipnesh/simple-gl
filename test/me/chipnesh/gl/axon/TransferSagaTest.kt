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
    private val transferId = "transferId"
    private val fromAccountId = "fromAccountId"
    private val toAccountId = "toAccountId"
    private val amountOfMoneyToTransfer = 40.toBigDecimal()

    @Before
    fun setUp() {
        testFixture = SagaTestFixture(TransferSaga::class.java)
    }

    @Test
    fun testTransferCreated() {
        testFixture.givenNoPriorActivity()
            .whenAggregate(transferId).publishes(
                TransferCreatedEvent(
                    transferId,
                    fromAccountId,
                    toAccountId,
                    amountOfMoneyToTransfer
                )
            )
            .expectActiveSagas(1)
            .expectDispatchedCommands(
                DebitSourceAccountCommand(
                    fromAccountId,
                    transferId,
                    amountOfMoneyToTransfer
                )
            )
    }

    @Test
    fun testSourceAccountNotFound() {
        testFixture.givenAggregate(transferId).published(
            TransferCreatedEvent(
                transferId,
                fromAccountId,
                toAccountId,
                amountOfMoneyToTransfer
            )
        )
            .whenPublishingA(SourceAccountNotFoundEvent(transferId))
            .expectActiveSagas(0)
            .expectDispatchedCommands(MarkTransferFailedCommand(transferId))
    }

    @Test
    fun testSourceAccountDebitRejected() {
        testFixture.givenAggregate(transferId).published(
            TransferCreatedEvent(
                transferId,
                fromAccountId,
                toAccountId,
                amountOfMoneyToTransfer
            )
        )
            .whenAggregate(fromAccountId)
            .publishes(SourceAccountDebitRejectedEvent(transferId))
            .expectActiveSagas(0)
            .expectDispatchedCommands(MarkTransferFailedCommand(transferId))
    }

    @Test
    fun testSourceAccountDebited() {
        testFixture.givenAggregate(transferId).published(
            TransferCreatedEvent(
                transferId,
                fromAccountId,
                toAccountId,
                amountOfMoneyToTransfer
            )
        )
            .whenAggregate(fromAccountId).publishes(
                SourceAccountDebitedEvent(
                    fromAccountId,
                    amountOfMoneyToTransfer,
                    transferId
                )
            )
            .expectActiveSagas(1)
            .expectDispatchedCommands(
                CreditDestinationAccountCommand(
                    toAccountId,
                    transferId,
                    amountOfMoneyToTransfer
                )
            )
    }

    @Test
    fun testDestinationAccountNotFound() {
        testFixture.givenAggregate(transferId).published(
            TransferCreatedEvent(
                transferId,
                fromAccountId,
                toAccountId,
                amountOfMoneyToTransfer
            )
        )
            .andThenAggregate(fromAccountId).published(
                SourceAccountDebitedEvent(
                    fromAccountId, amountOfMoneyToTransfer, transferId
                )
            )
            .whenPublishingA(DestinationAccountNotFoundEvent(transferId))
            .expectActiveSagas(0)
            .expectDispatchedCommands(
                ReturnMoneyCommand(
                    fromAccountId,
                    amountOfMoneyToTransfer
                ),
                MarkTransferFailedCommand(transferId)
            )
    }

    @Test
    fun testDestinationAccountCredited() {
        testFixture.givenAggregate(transferId).published(
            TransferCreatedEvent(
                transferId,
                fromAccountId,
                toAccountId,
                amountOfMoneyToTransfer
            )
        )
            .andThenAggregate(fromAccountId).published(
                SourceAccountDebitedEvent(
                    fromAccountId,
                    amountOfMoneyToTransfer,
                    transferId
                )
            )
            .whenAggregate(toAccountId).publishes(
                DestinationAccountCreditedEvent(
                    toAccountId,
                    amountOfMoneyToTransfer,
                    transferId
                )
            )
            .expectActiveSagas(0)
            .expectDispatchedCommands(MarkTransferCompletedCommand(transferId))
    }
}