package me.chipnesh.gl.axon.account

import java.math.BigDecimal

data class AccountEntity(
    var accountId: String,
    var balance: BigDecimal
)