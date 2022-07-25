package com.suki.wallet.app.scanQrCode

import com.suki.wallet.base.MyViewModelFragmentActivity

class ScanQrCodeActivity : MyViewModelFragmentActivity<ScanQrCodeFragment>() {
    override val fragment: ScanQrCodeFragment
        get() = ScanQrCodeFragment()
}