package com.suki.wallet.utility

import com.suki.wallet.Configuration
import com.suki.wallet.MyApplication
import com.suki.wallet.app.ethereumCore.Erc20Adapter
import com.suki.wallet.app.ethereumCore.EthereumAdapter
import com.suki.wallet.base.SingleLiveEvent
import io.horizontalsystems.erc20kit.core.Erc20Kit
import io.horizontalsystems.ethereumkit.core.EthereumKit
import io.horizontalsystems.ethereumkit.core.signer.Signer
import io.horizontalsystems.ethereumkit.core.toHexString
import io.horizontalsystems.ethereumkit.crypto.InternalBouncyCastleProvider
import io.horizontalsystems.ethereumkit.models.Address
import io.horizontalsystems.ethereumkit.models.Chain
import io.horizontalsystems.ethereumkit.models.GasPrice
import io.horizontalsystems.ethereumkit.models.RpcSource
import io.horizontalsystems.ethereumkit.models.TransactionSource
import io.horizontalsystems.hdwalletkit.Mnemonic
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import org.bouncycastle.jce.provider.BouncyCastleProvider
import timber.log.Timber
import java.math.BigDecimal
import java.security.Security


object EthKit {
    private val disposables = CompositeDisposable()

    var ethereumKit: EthereumKit
    var ethereumAdapter: EthereumAdapter
    var signer: Signer

    // erc20
    var erc20Adapter: Erc20Adapter

    val seed: ByteArray
        get() = Mnemonic().toSeed(MyApplication.INSTANCE.addressWords.split(" "))

    val sendStatus = SingleLiveEvent<Throwable?>()
    val estimatedGas = SingleLiveEvent<String>()
    private var gasPrice: GasPrice = GasPrice.Legacy(20_000_000_000)

    init {
        Security.removeProvider(BouncyCastleProvider.PROVIDER_NAME)
        Security.addProvider(InternalBouncyCastleProvider.getInstance())
        signer = Signer.getInstance(seed, Configuration.chain)
        ethereumKit = createKit()
        ethereumAdapter = EthereumAdapter(ethereumKit, signer)
        erc20Adapter = Erc20Adapter(
            MyApplication.INSTANCE,
            Configuration.erc20Tokens.first(),
            ethereumKit,
            signer
        )

        Erc20Kit.addTransactionSyncer(ethereumKit)
        Erc20Kit.addDecorators(ethereumKit)
    }

    private fun createKit(): EthereumKit {
        val rpcSource: RpcSource?
        val transactionSource: TransactionSource?

        when (Configuration.chain) {
            Chain.BinanceSmartChain -> {
                transactionSource = TransactionSource.bscscan(Configuration.bscScanKey)
                rpcSource = if (Configuration.webSocket)
                    RpcSource.binanceSmartChainWebSocket()
                else
                    RpcSource.binanceSmartChainHttp()
            }
            Chain.Ethereum -> {
                transactionSource = TransactionSource.ethereumEtherscan(Configuration.etherscanKey)
                rpcSource = if (Configuration.webSocket)
                    RpcSource.ethereumInfuraWebSocket(
                        Configuration.infuraProjectId,
                        Configuration.infuraSecret
                    )
                else
                    RpcSource.ethereumInfuraHttp(
                        Configuration.infuraProjectId,
                        Configuration.infuraSecret
                    )
            }
            Chain.EthereumRopsten -> {
                transactionSource = TransactionSource.ropstenEtherscan(Configuration.etherscanKey)
                rpcSource = if (Configuration.webSocket)
                    RpcSource.ropstenInfuraWebSocket(
                        Configuration.infuraProjectId,
                        Configuration.infuraSecret
                    )
                else
                    RpcSource.ropstenInfuraHttp(
                        Configuration.infuraProjectId,
                        Configuration.infuraSecret
                    )
            }
            else -> {
                rpcSource = null
                transactionSource = null
            }
        }

        checkNotNull(rpcSource) {
            throw Exception("Could not get rpcSource!")
        }

        checkNotNull(transactionSource) {
            throw Exception("Could not get transactionSource!")
        }

        val words = MyApplication.INSTANCE.addressWords.split(" ")
        return EthereumKit.getInstance(
            MyApplication.INSTANCE, words, "",
            Configuration.chain, rpcSource, transactionSource,
            Configuration.walletId
        )
    }

    fun estimateGas(toAddress: String?, value: BigDecimal, isErc20: Boolean) {
        estimatedGas.postValue(null)

        if (toAddress == null) return

        val estimateSingle = if (isErc20)
            erc20Adapter.estimatedGasLimit(Address(toAddress), value, gasPrice)
        else
            ethereumAdapter.estimatedGasLimit(Address(toAddress), value, gasPrice)


        estimateSingle
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                //success
                estimatedGas.value = it.toString()
            }, {
                Timber.i("Gas estimate: ${it.message}")
                estimatedGas.value = it.message
            })
            .let { disposables.add(it) }
    }

    fun send(toAddress: String, amount: BigDecimal, isErcToken: Boolean) {
        if (isErcToken) sendERC20(toAddress, amount) else sendEthereum(toAddress, amount)
    }

    fun sendEthereum(toAddress: String, amount: BigDecimal) {
        val gasLimit = estimatedGas.value?.toLongOrNull() ?: kotlin.run {
            sendStatus.value = Exception("No gas limit!!")
            return
        }

        ethereumAdapter.send(Address(toAddress), amount, gasPrice, gasLimit)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ fullTransaction ->
                //success
                Timber.i("Successfully sent, hash: ${fullTransaction.transaction.hash.toHexString()}")

                sendStatus.value = null
            }, {
                Timber.i("Ether send failed: ${it.message}")
                sendStatus.value = it
            }).let { disposables.add(it) }
    }

    fun sendERC20(toAddress: String, amount: BigDecimal) {
        val gasLimit = estimatedGas.value?.toLongOrNull() ?: kotlin.run {
            sendStatus.value = Exception("No gas limit!!")
            return
        }

        erc20Adapter.send(Address(toAddress), amount, gasPrice, gasLimit)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ fullTransaction ->
                Timber.i("Successfully sent, hash: ${fullTransaction.transaction.hash.toHexString()}")
                //success
                sendStatus.value = null
            }, {
                Timber.i("Erc20 send failed: ${it.message}")
                sendStatus.value = it
            }).let { disposables.add(it) }
    }

    fun setEstimatedGas(gas: String?) {
        estimatedGas.value = gas
    }
}