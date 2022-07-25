package com.suki.wallet.app.loginRegister

import android.content.Intent
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import com.suki.wallet.R
import com.suki.wallet.app.loginRegister.createWallet.CreateWalletActivity
import com.suki.wallet.app.loginRegister.restoreWallet.RestoreWalletActivity
import com.suki.wallet.base.MyAppCompatActivity
import com.suki.wallet.databinding.ActivityLoginRegisterBinding

class LoginRegisterActivity : MyAppCompatActivity() {

    lateinit var binding: ActivityLoginRegisterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_login_register)

        binding.btnCreateWallet.setOnClickListener{
            goToCreateWallet()
        }

        binding.btnRestoreWallet.setOnClickListener{
            goToRestoreWallet()
        }
    }

    private fun goToCreateWallet(){
        val intent  = Intent(this, CreateWalletActivity::class.java)
        startActivity(intent)
    }

    private fun goToRestoreWallet(){
        val intent  = Intent(this, RestoreWalletActivity::class.java)
        startActivity(intent)
    }
}