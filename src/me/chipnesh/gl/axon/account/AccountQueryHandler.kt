package me.chipnesh.gl.axon.account

import me.chipnesh.gl.core.AccountFault.AccountNotFound
import me.chipnesh.gl.core.AccountOperationResult
import me.chipnesh.gl.core.AccountOperationResult.AccountBalance
import me.chipnesh.gl.core.AccountOperationResult.Failure
import org.axonframework.queryhandling.QueryHandler
import org.slf4j.LoggerFactory

class AccountQueryHandler(
    private val store: AccountsStore
) {
    companion object {
        private val log = LoggerFactory.getLogger(AccountQueryHandler::class.java)
    }

    @QueryHandler
    fun get(qry: GetAccountBalanceQuery): AccountOperationResult {
        log.info("Got $qry query")
        val account = store.findById(qry.accountId) ?: return Failure(AccountNotFound(qry.accountId))
        return AccountBalance(account.accountId, account.balance)
    }
}