package com.suki.wallet.app.walletAbstract

import com.suki.wallet.base.MyViewModelFragmentActivity

class WalletAbstractActivity : MyViewModelFragmentActivity<WalletAbstractFragment>() {
    override val fragment: WalletAbstractFragment
        get() = WalletAbstractFragment()
}