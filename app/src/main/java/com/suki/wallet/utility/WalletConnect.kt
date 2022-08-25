package com.suki.wallet.utility

import com.google.gson.GsonBuilder
import com.suki.wallet.Configuration
import com.suki.wallet.base.SingleLiveEvent
import com.suki.wallet.model.SimpleAlertData
import com.trustwallet.walletconnect.WCClient
import com.trustwallet.walletconnect.exceptions.InvalidSessionException
import com.trustwallet.walletconnect.models.WCAccount
import com.trustwallet.walletconnect.models.WCPeerMeta
import com.trustwallet.walletconnect.models.WCSignTransaction
import com.trustwallet.walletconnect.models.ethereum.WCEthereumSignMessage
import com.trustwallet.walletconnect.models.ethereum.WCEthereumTransaction
import com.trustwallet.walletconnect.models.session.WCSession
import io.horizontalsystems.ethereumkit.core.hexStringToByteArray
import io.horizontalsystems.ethereumkit.core.signer.Signer
import io.horizontalsystems.ethereumkit.models.Address
import io.horizontalsystems.ethereumkit.models.GasPrice
import io.horizontalsystems.ethereumkit.spv.core.toBigInteger
import io.horizontalsystems.ethereumkit.spv.core.toLong
import okhttp3.OkHttpClient
import timber.log.Timber
import java.math.RoundingMode

object WalletConnect {

    private val wcClient by lazy {
        WCClient(GsonBuilder(), OkHttpClient())
    }

    private val peerMeta = WCPeerMeta(
        name = "Wallet",
        url = "https://walletConnect.com"
    ) // all example is hardcode this place

    val walletConnectRequest = SingleLiveEvent<SimpleAlertData>()
    val isConnected = SingleLiveEvent<Boolean>()
    val connectedSession = SingleLiveEvent<String?>()

    private lateinit var wcSession: WCSession
    private val signer = EthKit.signer
    private var remotePeerMeta: WCPeerMeta? = null
    private val addressEip55 = Signer.address(EthKit.seed, Configuration.chain).eip55

    init {
        wcClient.onDisconnect = { _, _ -> disconnect() }
        wcClient.onFailure = { t -> onFailure(t) }
        wcClient.onSessionRequest = { _, peer -> onSessionRequest(peer) }
        wcClient.onGetAccounts = { id -> onGetAccounts(id) }

        wcClient.onEthSign = { id, message -> onEthSign(id, message) }
        wcClient.onEthSignTransaction = { id, transaction -> onEthTransaction(id, transaction) }
        wcClient.onEthSendTransaction = { id, transaction -> onEthTransaction(id, transaction, send = true) }

        wcClient.onSignTransaction = { id, transaction -> onSignTransaction(id, transaction) }

        wcClient.onWalletChangeNetwork = { id, chainId -> /* TODO: change chain */}
    }

    fun connect(uri: String?) {
        uri ?: return
        disconnect()
        Timber.i("uri = $uri")
        wcSession = WCSession.from(uri) ?: throw InvalidSessionException()
        wcClient.connect(wcSession, peerMeta)
    }

    fun disconnect() {
        isConnected.postValue(false)
        connectedSession.postValue(null)
        if (wcClient.session != null) {
            wcClient.killSession()
        } else {
            wcClient.disconnect()
        }
    }

    fun approveSession() {
        wcClient.approveSession(listOf(addressEip55), Configuration.chain.id)
        isConnected.postValue(true)
        connectedSession.postValue(wcClient.session?.toUri())
    }

    fun rejectSession() {
        wcClient.rejectSession()
        wcClient.disconnect()
    }

    fun rejectRequest(id: Long) {
        wcClient.rejectRequest(id, "User canceled")
    }

    private fun onFailure(throwable: Throwable) {
        throwable.printStackTrace()
    }

    private fun onSessionRequest(peer: WCPeerMeta) {
        remotePeerMeta = peer
        wcClient.remotePeerId ?: run {
            println("remotePeerId can't be null")
            return
        }
        val meta = remotePeerMeta ?: return

        walletConnectRequest.postValue(SimpleAlertData(
            type = SimpleAlertData.AlertType.SESSION_REQUEST,
            title = meta.name,
            name = meta.name,
            message = "${meta.description}\n${meta.url}",
            positiveOption = SimpleAlertData.AlertOption("Approve") { approveSession() },
            negativeOption = SimpleAlertData.AlertOption("Reject") { rejectSession() }
        ))
    }

    private fun onEthSign(id: Long, message: WCEthereumSignMessage) {
        walletConnectRequest.postValue(SimpleAlertData(
            type = SimpleAlertData.AlertType.SIGN,
            title = message.type.name,
            message = message.data,
            signType = message.type,
            positiveOption = SimpleAlertData.AlertOption("Sign") {
                if (message.type == WCEthereumSignMessage.WCSignType.TYPED_MESSAGE) {
                    wcClient.approveRequest(id, signTypedData(message.data))
                } else {
                    wcClient.approveRequest(id, signByteArray(message.data.decodeHex()))
                }
            },
            negativeOption = SimpleAlertData.AlertOption("Cancel") { rejectRequest(id) }
        ))
    }

    private fun onEthTransaction(id: Long, payload: WCEthereumTransaction, send: Boolean = false) {
        walletConnectRequest.postValue(SimpleAlertData(
            type = if(send) SimpleAlertData.AlertType.SEND else SimpleAlertData.AlertType.SIGN_TRANSACTION,
            title = "onEthTransaction",
            message = payload.toString(),
            toAddress = payload.to,
            amount = payload.value?.hexStringToByteArray().toBigInteger().toBigDecimal().movePointLeft(18).setScale(5, RoundingMode.HALF_EVEN).toString(),
            gas = payload.gas?.hexStringToByteArray().toLong().toString(),
            positiveOption = SimpleAlertData.AlertOption("Sign") {
                val toAddress = payload.to?.let { Address(it) }
                if (toAddress != null) {
                    if(send){
                        EthKit.setEstimatedGas(payload.gas?.hexStringToByteArray().toLong().toString())
                        EthKit.send(payload.to ?: "",
                            payload.value?.hexStringToByteArray().toBigInteger().toBigDecimal().movePointLeft(18), false )
                    }
                    wcClient.approveRequest(
                        id,
                        signedTransaction(payload)
                    )
                }
            },
            negativeOption = SimpleAlertData.AlertOption("Cancel") { rejectRequest(id) }
        ))
    }

    private fun onGetAccounts(id: Long) {
        val account = WCAccount(Configuration.chain.id, addressEip55)
        wcClient.approveRequest(id, account)
    }

    private fun onSignTransaction(id: Long, payload: WCSignTransaction) {
        //  TODO: wallet connect testing cannot test
        walletConnectRequest.postValue(SimpleAlertData(
            type = SimpleAlertData.AlertType.SIGN_TRANSACTION,
            title = "onSignTransaction",
            message = payload.toString(),
            positiveOption = SimpleAlertData.AlertOption("Sign") { Timber.i("payload $payload") },
            negativeOption = SimpleAlertData.AlertOption("Cancel") { rejectRequest(id) }
        ))
    }

    private fun signTypedData(data: String): ByteArray = signer.signTypedData(data)

    private fun signByteArray(data: ByteArray): ByteArray = signer.signByteArray(data)

    private fun signedTransaction(payload: WCEthereumTransaction): ByteArray {
        val toAddress = payload.to?.let { Address(it) }
        if (toAddress != null){
            return signer.signedTransaction(
                toAddress,
                payload.value?.hexStringToByteArray().toBigInteger(),
                payload.data.hexStringToByteArray(),
                GasPrice.Legacy(payload.gasPrice?.hexStringToByteArray().toLong()),
                payload.gas?.hexStringToByteArray().toLong(),
                payload.nonce?.hexStringToByteArray().toLong()
            )
        }
        return byteArrayOf()
    }

    private fun String.decodeHex(): ByteArray {
        Timber.i("string = ${this}")
        check(length % 2 == 0) { "Must have an even length" }

        return removePrefix("0x")
            .chunked(2)
            .map { it.toInt(16).toByte() }
            .toByteArray()
    }


}