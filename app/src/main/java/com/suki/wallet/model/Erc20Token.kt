package com.suki.wallet.model

import io.horizontalsystems.ethereumkit.models.Address

data class Erc20Token(
    val name: String,
    val code: String,
    val contractAddress: Address,
    val decimals: Int)