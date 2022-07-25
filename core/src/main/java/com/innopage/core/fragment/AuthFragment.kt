package com.innopage.core.fragment

import android.content.Intent
import com.innopage.core.base.BaseFragment

abstract class AuthFragment<T : AuthViewModel> : BaseFragment<T>() {

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        viewModel.handleCallback(requestCode, resultCode, data)
    }
}