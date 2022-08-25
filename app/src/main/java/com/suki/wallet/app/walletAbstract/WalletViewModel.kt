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
import com.suki.wallet.utility.EthKit
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

class WalletViewModel(application: Application) : BaseViewModel(application) {

    private val disposables = CompositeDisposable()

    private val _addressEip55 = MutableLiveData<String>()
    val addressEip55: MutableLiveData<String>
        get() = _addressEip55

    val balance = MutableLiveData<BigDecimal>()
    val syncState = MutableLiveData<EthereumKit.SyncState>()
    val transactionsSyncState = MutableLiveData<EthereumKit.SyncState>()

//    erc20

    val erc20SyncState = MutableLiveData<EthereumKit.SyncState>()
    val erc20TransactionsSyncState = MutableLiveData<EthereumKit.SyncState>()
    val erc20TokenBalance = MutableLiveData<BigDecimal>()
    val erc20TokenCoinType = MutableLiveData<String>()

    fun init() {
        ioLaunch {
            try {
                _addressEip55.postValue(Signer.address(EthKit.seed, Configuration.chain).eip55)
                erc20TokenCoinType.postValue(Configuration.erc20Tokens.first().code)

                updateBalance()
                updateState()
                updateTransactionsSyncState()

                updateErc20Balance()
                updateErc20State()
                updateErc20TransactionsSyncState()

                // Ethereum
                EthKit.ethereumAdapter.balanceFlowable.subscribe {
                    updateBalance()
                }.let {
                    disposables.add(it)
                }

                EthKit.ethereumAdapter.syncStateFlowable.subscribe {
                    updateState()
                }.let {
                    disposables.add(it)
                }

                EthKit.ethereumAdapter.transactionsSyncStateFlowable.subscribe {
                    updateTransactionsSyncState()
                }.let {
                    disposables.add(it)
                }

                EthKit.ethereumAdapter.start()
            } catch (e: Exception) {
                throw ErrorException(
                    Error(
                        Constants.RESPONSE_WALLET_ERROR,
                        "Ethereum Kit Exception",
                        e.message
                    ), null
                )
            }
        }
    }

    private fun updateBalance() {
        balance.postValue(EthKit.ethereumAdapter.balance)
    }

    private fun updateState() {
        syncState.postValue(EthKit.ethereumAdapter.syncState)
    }

    private fun updateTransactionsSyncState() {
        transactionsSyncState.postValue(EthKit.ethereumAdapter.transactionsSyncState)
    }

    private fun updateErc20State() {
        erc20SyncState.postValue(EthKit.erc20Adapter.syncState)
    }

    private fun updateErc20TransactionsSyncState() {
        erc20TransactionsSyncState.postValue(EthKit.erc20Adapter.transactionsSyncState)
    }

    private fun updateErc20Balance() {
        erc20TokenBalance.postValue(EthKit.erc20Adapter.balance)
    }

    fun refresh() {
        Timber.i("refresh is clicked")
        EthKit.ethereumAdapter.refresh()
        EthKit.erc20Adapter.refresh()
        updateBalance()
        updateErc20Balance()
    }

    fun clear() {
        Timber.i("clear is called")
        EthereumKit.clear(MyApplication.INSTANCE, Configuration.chain, Configuration.walletId)
        Erc20Kit.clear(MyApplication.INSTANCE, Configuration.chain, Configuration.walletId)
//        init()
    }

}