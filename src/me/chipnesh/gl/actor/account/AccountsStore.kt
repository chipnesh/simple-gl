package me.chipnesh.gl.actor.account

/**
 * WARNING!
 * Should not be used out of actor's scope!
 */
interface AccountsStore {
    operator fun get(key: String): Account?
    operator fun set(key: String, value: Account)
}

object MapAccountStore : AccountsStore {
    private val accounts = mutableMapOf<String, Account>()

    override fun get(key: String) = accounts[key]
    override fun set(key: String, value: Account) {
        accounts[key] = value
    }
}