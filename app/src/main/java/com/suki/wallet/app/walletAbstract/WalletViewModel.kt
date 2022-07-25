package com.suki.wallet.app.walletAbstract

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.innopage.core.base.BaseViewModel
import com.innopage.core.webservice.config.Constants
import com.innopage.core.webservice.model.Error
import com.innopage.core.webservice.retrofit.exception.ErrorException
import com.suki.wallet.Configuration
import com.suki.wallet.MyApplication
import com.suki.wallet.app.ethereumCore.EthereumAdapter
import com.suki.wallet.base.SingleLiveEvent
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

class WalletViewModel(application: Application) : BaseViewModel(application) {

    private val disposables = CompositeDisposable()

    private val _addressEip55 = MutableLiveData<String>()
    val addressEip55: MutableLiveData<String>
        get() = _addressEip55

    private lateinit var ethereumKit: EthereumKit
    private lateinit var ethereumAdapter: EthereumAdapter
    private lateinit var signer: Signer

    val balance = MutableLiveData<BigDecimal>()
    val syncState = MutableLiveData<EthereumKit.SyncState>()
    val transactionsSyncState = MutableLiveData<EthereumKit.SyncState>()

    val sendStatus = SingleLiveEvent<Throwable?>()
    val estimatedGas = SingleLiveEvent<String>()
    private var gasPrice: GasPrice = GasPrice.Legacy(20_000_000_000)

    fun init() {
        ioLaunch {
            try {
                Security.removeProvider(BouncyCastleProvider.PROVIDER_NAME)
                Security.addProvider(InternalBouncyCastleProvider.getInstance())
                val seed = Mnemonic().toSeed(MyApplication.INSTANCE.addressWords.split(" "))
                _addressEip55.postValue(Signer.address(seed, Configuration.chain).eip55)
                signer = Signer.getInstance(seed, Configuration.chain)
                ethereumKit = createKit()
                ethereumAdapter = EthereumAdapter(ethereumKit, signer)
                updateBalance()
                updateState()
                updateTransactionsSyncState()

                // Ethereum
                ethereumAdapter.balanceFlowable.subscribe {
                    updateBalance()
                }.let {
                    disposables.add(it)
                }

                ethereumAdapter.syncStateFlowable.subscribe {
                    updateState()
                }.let {
                    disposables.add(it)
                }

                ethereumAdapter.transactionsSyncStateFlowable.subscribe {
                    updateTransactionsSyncState()
                }.let {
                    disposables.add(it)
                }

                ethereumAdapter.start()
            } catch (e: Exception) {
                throw ErrorException(Error(Constants.RESPONSE_WALLET_ERROR, "Ethereum Kit Exception", e.message), null)
            }
        }
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
                    RpcSource.ethereumInfuraWebSocket(Configuration.infuraProjectId, Configuration.infuraSecret)
                else
                    RpcSource.ethereumInfuraHttp(Configuration.infuraProjectId, Configuration.infuraSecret)
            }
            Chain.EthereumRopsten -> {
                transactionSource = TransactionSource.ropstenEtherscan(Configuration.etherscanKey)
                rpcSource = if (Configuration.webSocket)
                    RpcSource.ropstenInfuraWebSocket(Configuration.infuraProjectId, Configuration.infuraSecret)
                else
                    RpcSource.ropstenInfuraHttp(Configuration.infuraProjectId, Configuration.infuraSecret)
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

    private fun updateBalance() {
        balance.postValue(ethereumAdapter.balance)
    }

    private fun updateState() {
        syncState.postValue(ethereumAdapter.syncState)
    }

    private fun updateTransactionsSyncState() {
        transactionsSyncState.postValue(ethereumAdapter.transactionsSyncState)
    }

    fun refresh() {
        Timber.i("refresh is clicked")
        ethereumAdapter.refresh()
    }

    fun clear() {
        Timber.i("clear is called")
        EthereumKit.clear(MyApplication.INSTANCE, Configuration.chain, Configuration.walletId)
    }

    fun estimateGas(toAddress: String?, value: BigDecimal) {
        estimatedGas.postValue(null)

        if (toAddress == null) return

        val estimateSingle = ethereumAdapter.estimatedGasLimit(Address(toAddress), value, gasPrice)

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

    fun send(toAddress: String, amount: BigDecimal) {
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
}