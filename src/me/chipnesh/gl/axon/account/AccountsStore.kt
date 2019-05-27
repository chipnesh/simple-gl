package me.chipnesh.gl.axon.account

import java.util.concurrent.ConcurrentHashMap

class AccountsStore {
    private val accountsMap = ConcurrentHashMap<String, AccountEntity>()

    fun save(account: AccountEntity): AccountEntity {
        accountsMap.merge(account.accountId, account) { first, second ->
            first.balance = second.balance
            first
        }
        return account
    }

    fun findById(accountId: String) = accountsMap[accountId]
}