package com.suki.wallet.base

import android.os.Bundle
import com.innopage.core.base.BaseFragment
import com.suki.wallet.R

abstract class MyViewModelFragmentActivity<T : BaseFragment<*>> : MyAppCompatActivity() {

    protected abstract val fragment: T

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fragment_view_model)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, fragment)
                .commit()
        }
    }
}