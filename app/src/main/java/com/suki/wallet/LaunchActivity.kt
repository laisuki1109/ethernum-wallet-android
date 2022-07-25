package com.suki.wallet

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import com.innopage.core.base.BaseAppCompatActivity
import com.innopage.core.utility.RootUtils
import com.suki.wallet.app.loginRegister.LoginRegisterActivity
import com.suki.wallet.app.walletAbstract.WalletAbstractActivity
import io.horizontalsystems.ethereumkit.crypto.InternalBouncyCastleProvider
import org.bouncycastle.jce.provider.BouncyCastleProvider
import timber.log.Timber
import java.security.Security

class LaunchActivity : BaseAppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_launch)

        // Check device rooted
        if (!BuildConfig.DEBUG && RootUtils().isDeviceRooted()) {
            popUpRootedDialog()
        } else {
            Security.removeProvider(BouncyCastleProvider.PROVIDER_NAME)
            Security.addProvider(InternalBouncyCastleProvider.getInstance())
            val intent = handleIntent(intent)
            if(MyApplication.INSTANCE.addressWords.isNullOrEmpty()){
                intent.setClass(this, LoginRegisterActivity::class.java)
            } else {
                intent.setClass(this, WalletAbstractActivity::class.java)
            }
            startActivity(intent)
            finish()
        }
    }

    private fun popUpRootedDialog() {
        val alertDialog = AlertDialog.Builder(this)
            .setMessage(R.string.dialog_title_rooted)
            .setPositiveButton(R.string.btn_ok) { dialog, _ ->
                dialog.dismiss()
                finish()
            }.create()
        alertDialog.show()
    }

    private fun handleIntent(intent: Intent?): Intent {
        val i = Intent()
        val action = intent?.action
        val uri = intent?.data
        if (action == Intent.ACTION_VIEW) {
            uri?.apply {
                queryParameterNames?.forEach {
                    Timber.d("$it: ${getQueryParameter(it)}")
                    i.putExtra(it, getQueryParameter(it))
                }
            }
        } else {
            intent?.extras?.apply {
                for (key in keySet()) {
                    if (get(key) is String) {
                        Timber.d("$key: ${get(key)}")
                        i.putExtra(key, getString(key))
                    }
                }
            }
        }

        return i
    }
}