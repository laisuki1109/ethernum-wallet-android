package com.suki.wallet

import com.suki.wallet.model.Erc20Token
import io.horizontalsystems.ethereumkit.models.Address
import io.horizontalsystems.ethereumkit.models.Chain


object Configuration {
    const val webSocket: Boolean = false
    val chain: Chain = Chain.EthereumRopsten
    const val walletId = "walletId"

    const val infuraProjectId = "2a1306f1d12f4c109a4d4fb9be46b02e"
    const val infuraSecret = "fc479a9290b64a84a15fa6544a130218"
    const val etherscanKey = "GKNHXT22ED7PRVCKZATFZQD1YI7FK9AAYE"
    const val bscScanKey = "5ZGSHWYHZVA8XZHB8PF6UUTRNNB4KT43ZZ"

    val erc20Tokens: List<Erc20Token>
        get() {
            return when (chain) {
                Chain.Ethereum -> listOf(
                    Erc20Token("DAI", "DAI", Address("0x6b175474e89094c44da98b954eedeac495271d0f"), 18),
                    Erc20Token("USD Coin", "USDC", Address("0xA0b86991c6218b36c1d19D4a2e9Eb0cE3606eB48"), 6)
                )
                Chain.EthereumRopsten -> listOf(
                    Erc20Token("USD Coin", "USDC", Address("0x617e2a9e839ea3bc55068828bd4e2036e84a1fd3"), 18),
                    Erc20Token("WEENUS", "WEENUS", Address("0x101848d5c5bbca18e6b4431eedf6b95e9adf82fa"), 18)
                )
                else -> listOf()
            }
        }
}