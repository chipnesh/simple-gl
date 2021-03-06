package me.chipnesh.gl.axon.account

import org.axonframework.eventhandling.EventHandler
import org.slf4j.LoggerFactory

import java.math.BigDecimal

class AccountEventHandler(
    private val storage: AccountsStorage
) {
    companion object {
        private val log = LoggerFactory.getLogger(AccountEventHandler::class.java)
    }

    @EventHandler
    fun on(event: AccountCreatedEvent) {
        log.info("Got $event event")
        storage.save(AccountEntity(event.accountId, BigDecimal.ZERO))
    }

    @EventHandler
    fun on(event: MoneyAddedEvent) {
        log.info("Got $event event")
        storage.findById(event.accountId)?.let { account ->
            account.balance += event.amount
            storage.save(account)
        }
    }

    @EventHandler
    fun on(event: MoneySubtractedEvent) {
        log.info("Got $event event")
        storage.findById(event.accountId)?.let { account ->
            account.balance -= event.amount
            storage.save(account)
        }
    }
}