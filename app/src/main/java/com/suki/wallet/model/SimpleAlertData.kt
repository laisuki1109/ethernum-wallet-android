package com.suki.wallet.model

import com.trustwallet.walletconnect.models.ethereum.WCEthereumSignMessage

data class SimpleAlertData(
    val type: AlertType,
    val title: String,
    val message: String,
    val signType: WCEthereumSignMessage.WCSignType? = null,
    val name: String? = null,
    val toAddress: String? = null,
    val amount: String? = null,
    val gas: String? = null,
    val positiveOption: AlertOption,
    val negativeOption: AlertOption
) {
    data class AlertOption(
        val text: String,
        val action: (() -> Unit)? = null
    )

    enum class AlertType {
        SESSION_REQUEST,
        SIGN,
        SIGN_TRANSACTION,
        SEND
    }
}