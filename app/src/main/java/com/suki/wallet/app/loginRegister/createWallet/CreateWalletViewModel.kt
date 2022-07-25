package com.suki.wallet.app.loginRegister.createWallet

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.innopage.core.base.BaseViewModel
import com.innopage.core.webservice.config.Constants
import com.innopage.core.webservice.model.Error
import com.innopage.core.webservice.retrofit.exception.ErrorException
import com.suki.wallet.Configuration
import com.suki.wallet.MyApplication
import io.horizontalsystems.ethereumkit.core.signer.Signer
import io.horizontalsystems.ethereumkit.crypto.InternalBouncyCastleProvider
import io.horizontalsystems.hdwalletkit.Mnemonic
import org.bouncycastle.jce.provider.BouncyCastleProvider
import timber.log.Timber
import java.security.Security


class CreateWalletViewModel(application: Application) : BaseViewModel(application) {

    private val _wordList = MutableLiveData<List<String>>()
    val wordList: MutableLiveData<List<String>>
        get() = _wordList

    private val _addressEip55 = MutableLiveData<String>()
    val addressEip55: MutableLiveData<String>
        get() = _addressEip55

    fun createWallet() {
        ioLaunch {
            try {
                Security.removeProvider(BouncyCastleProvider.PROVIDER_NAME)
                Security.addProvider(InternalBouncyCastleProvider.getInstance())
                val words = Mnemonic().generate()
                val seed = Mnemonic().toSeed(words)
                _wordList.postValue(words)
                _addressEip55.postValue(Signer.address(seed, Configuration.chain).eip55)
                MyApplication.INSTANCE.addressWords = words.joinToString(" ")
                Timber.i(MyApplication.INSTANCE.addressWords)
            } catch (e: Exception) {
                throw ErrorException(Error(Constants.RESPONSE_WALLET_ERROR, "Invalid words Exception", e.message), null)
            }
        }
    }
}