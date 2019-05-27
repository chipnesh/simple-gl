package me.chipnesh.gl.axon.account

import org.axonframework.commandhandling.CommandHandler
import org.axonframework.eventhandling.EventBus
import org.axonframework.eventhandling.GenericEventMessage
import org.axonframework.modelling.command.AggregateNotFoundException
import org.axonframework.modelling.command.Repository
import org.slf4j.LoggerFactory

class AccountCommandHandler(
    private val repository: Repository<AccountAggregate>,
    private val eventBus: EventBus
) {
    companion object {
        private val log = LoggerFactory.getLogger(AccountCommandHandler::class.java)
    }

    @CommandHandler
    fun handle(command: DebitSourceAccountCommand) = try {
        log.info("Got $command command")

        repository.load(command.accountId).run {
            execute { account -> account.debit(command.amount, command.transferId) }
        }
    } catch (exception: AggregateNotFoundException) {
        publish(SourceAccountNotFoundEvent(command.transferId))
    }

    @CommandHandler
    fun handle(command: CreditDestinationAccountCommand) = try {
        log.info("Got $command command")
        repository.load(command.accountId).run {
            execute { account -> account.credit(command.amount, command.transferId) }
        }
    } catch (exception: AggregateNotFoundException) {
        publish(DestinationAccountNotFoundEvent(command.transferId))
    }

    private fun publish(evt: Any) = eventBus.publish(GenericEventMessage(evt))
}