package com.suki.wallet.base

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import com.innopage.core.activity.MyContextWrapper
import com.innopage.core.base.BaseAppCompatActivity
import com.suki.wallet.MyApplication

open class MyAppCompatActivity : BaseAppCompatActivity() {

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
}