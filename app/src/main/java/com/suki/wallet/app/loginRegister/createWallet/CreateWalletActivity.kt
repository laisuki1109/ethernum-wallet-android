package com.suki.wallet.app.loginRegister.createWallet

import com.suki.wallet.base.MyViewModelFragmentActivity

class CreateWalletActivity : MyViewModelFragmentActivity<CreateWalletFragment>() {
    override val fragment: CreateWalletFragment
        get() = CreateWalletFragment()
}