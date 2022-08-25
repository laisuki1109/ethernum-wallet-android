package com.suki.wallet.app.walletConnect

import com.suki.wallet.base.MyViewModelFragmentActivity

class WalletConnectTestingActivity : MyViewModelFragmentActivity<WalletConnectTestingFragment>() {
    override val fragment: WalletConnectTestingFragment
        get() = WalletConnectTestingFragment()
}