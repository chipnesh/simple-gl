package me.chipnesh.gl.axon

import me.chipnesh.gl.axon.account.*
import me.chipnesh.gl.axon.transfer.*
import org.axonframework.config.Configuration
import org.axonframework.config.DefaultConfigurer
import org.axonframework.eventhandling.tokenstore.TokenStore
import org.axonframework.eventhandling.tokenstore.inmemory.InMemoryTokenStore
import org.axonframework.eventsourcing.EventSourcingRepository
import org.axonframework.eventsourcing.eventstore.inmemory.InMemoryEventStorageEngine
import org.axonframework.modelling.saga.repository.SagaStore
import org.axonframework.modelling.saga.repository.inmemory.InMemorySagaStore

fun buildInMemoryConfiguration(
    accountsStore: AccountsStore,
    transfersStore: TransfersStore
): Configuration = DefaultConfigurer.defaultConfiguration().apply {

    configureEmbeddedEventStore { InMemoryEventStorageEngine() }

    configureAggregate(AccountAggregate::class.java)
    configureAggregate(TransferAggregate::class.java)

    registerComponent(TokenStore::class.java) { InMemoryTokenStore() }
    registerComponent(SagaStore::class.java) { InMemorySagaStore() }

    registerComponent(EventSourcingRepository::class.java) {
        EventSourcingRepository
            .builder(AccountAggregate::class.java)
            .build()
    }
    registerComponent(EventSourcingRepository::class.java) {
        EventSourcingRepository
            .builder(TransferAggregate::class.java)
            .build()
    }
    registerQueryHandler { AccountQueryHandler(accountsStore) }
    registerQueryHandler { TransfersQueryHandler(transfersStore) }

    registerCommandHandler { AccountCommandHandler(it.repository(AccountAggregate::class.java), it.eventBus()) }

    eventProcessing {
        it.registerEventHandler { AccountEventHandler(accountsStore) }
        it.registerEventHandler { TransferEventHandler(transfersStore) }

        it.registerSaga(TransferSaga::class.java)
    }
}.buildConfiguration()

