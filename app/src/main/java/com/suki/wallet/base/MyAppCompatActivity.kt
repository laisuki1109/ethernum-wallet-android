package com.suki.wallet.base

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.innopage.core.activity.MyContextWrapper
import com.innopage.core.base.BaseAppCompatActivity
import com.suki.wallet.MyApplication
import com.suki.wallet.R
import com.suki.wallet.databinding.DialogWalletConnectSessionRequestBinding
import com.suki.wallet.databinding.DialogWalletConnectSignBinding
import com.suki.wallet.databinding.DialogWalletConnectSignTransactionBinding
import com.suki.wallet.model.SimpleAlertData
import com.suki.wallet.utility.WalletConnect

open class MyAppCompatActivity : BaseAppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!MyApplication.INSTANCE.addressWords.isNullOrEmpty()) {
            WalletConnect.walletConnectRequest.observe(this) {
                showAlert(it)
            }
        }
    }

    override fun attachBaseContext(newBase: Context?) {
        super.attachBaseContext(newBase?.let {
            MyContextWrapper.wrap(
                it,
                MyApplication.INSTANCE.appLocale,
                MyApplication.INSTANCE.fontScale
            )
        })
    }

    override fun applyOverrideConfiguration(overrideConfiguration: Configuration?) {
        if (Build.VERSION.SDK_INT in Build.VERSION_CODES.LOLLIPOP..Build.VERSION_CODES.N_MR1) {
            overrideConfiguration?.setLocale(MyApplication.INSTANCE.appLocale)
            overrideConfiguration?.fontScale = MyApplication.INSTANCE.fontScale
        }
        super.applyOverrideConfiguration(overrideConfiguration)
    }

    private fun showAlert(data: SimpleAlertData) {
        runOnUiThread {
            when(data.type){
                SimpleAlertData.AlertType.SESSION_REQUEST -> popupWalletConnectSessionRequest(data)
                SimpleAlertData.AlertType.SIGN -> popupWalletConnectSign(data)
                SimpleAlertData.AlertType.SIGN_TRANSACTION -> popupWalletConnectSignTransaction(data, send = false)
                SimpleAlertData.AlertType.SEND -> popupWalletConnectSignTransaction(data, send = true)
            }
        }
    }

    private fun popupWalletConnectSessionRequest(data: SimpleAlertData){
        val dialog = BottomSheetDialog(this)
        val dialogBinding = DataBindingUtil.inflate<DialogWalletConnectSessionRequestBinding>(
            layoutInflater, R.layout.dialog_wallet_connect_session_request, null, false
        )
        dialogBinding.btnCancel.setOnClickListener {
            data.negativeOption.action?.let { it() }
            dialog.dismiss()
        }
        dialogBinding.btnConnect.setOnClickListener {
            data.positiveOption.action?.let { it() }
            dialog.dismiss()
        }
        dialogBinding.textWcName.text = data.name
        dialogBinding.textWcUrl.text = data.message
        dialogBinding.textWcDesc.text = resources.getString(R.string.wallet_connect_chain) + com.suki.wallet.Configuration.chain.id

        dialog.setContentView(dialogBinding.root)
        dialog.show()
    }

    private fun popupWalletConnectSign(data: SimpleAlertData){
        val dialog = BottomSheetDialog(this)
        val dialogBinding = DataBindingUtil.inflate<DialogWalletConnectSignBinding>(
            layoutInflater, R.layout.dialog_wallet_connect_sign, null, false
        )
        dialogBinding.btnCancel.setOnClickListener {
            data.negativeOption.action?.let { it() }
            dialog.dismiss()
        }
        dialogBinding.btnSign.setOnClickListener {
            data.positiveOption.action?.let { it() }
            dialog.dismiss()
        }
        dialogBinding.textWcType.text = data.signType?.name
        dialogBinding.textWcMsg.text = data.message

        dialog.setContentView(dialogBinding.root)
        dialog.show()
    }

    private fun popupWalletConnectSignTransaction(data: SimpleAlertData, send: Boolean = false){
        val dialog = BottomSheetDialog(this)
        val dialogBinding = DataBindingUtil.inflate<DialogWalletConnectSignTransactionBinding>(
            layoutInflater, R.layout.dialog_wallet_connect_sign_transaction, null, false
        )
        dialogBinding.btnCancel.setOnClickListener {
            data.negativeOption.action?.let { it() }
            dialog.dismiss()
        }
        dialogBinding.btnSign.setOnClickListener {
            data.positiveOption.action?.let { it() }
            dialog.dismiss()
        }
        dialogBinding.textWcTitle.text = if(send) resources.getString(R.string.wallet_connect_send_transaction) else resources.getString(R.string.wallet_connect_sign_transaction)
        dialogBinding.textWcToAddress.text = data.toAddress
        dialogBinding.textWcAmount.text = data.amount
        dialogBinding.textWcGas.text = data.gas

        dialog.setContentView(dialogBinding.root)
        dialog.show()
    }
}