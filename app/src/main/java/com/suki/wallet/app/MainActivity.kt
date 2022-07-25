package com.suki.wallet.app

import android.content.Intent
import android.os.Bundle
import com.innopage.core.base.BaseAppCompatActivity
import com.suki.wallet.R
import timber.log.Timber

class MainActivity : BaseAppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Handle intent
        handleIntent(intent)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)

        // Handle intent
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        // TODO: Handle intent from notification or app link
        intent?.extras?.apply {
            for (key in keySet()) {
                if (get(key) is String) {
                    Timber.d("$key: ${get(key)}")
                }
            }
        }
    }
}