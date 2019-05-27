package me.chipnesh.gl.actor.transfer

/**
 * WARNING!
 * Should not be used out of actor's scope!
 */
interface TransferStore {
    operator fun get(key: String): Transfer?
    operator fun set(key: String, value: Transfer)
}

object MapTransferStore : TransferStore {
    private val transfers = mutableMapOf<String, Transfer>()

    override fun get(key: String) = transfers[key]
    override fun set(key: String, value: Transfer) {
        transfers[key] = value
    }
}