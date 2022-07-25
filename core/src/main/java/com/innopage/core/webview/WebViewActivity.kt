package com.innopage.core.webview

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.innopage.core.R
import timber.log.Timber

class WebViewActivity : AppCompatActivity() {
    private val mFragment = WebViewFragment()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_web_view)

        val url = intent.extras?.getString("url")
        val htmlData = intent.extras?.getString("htmlData")
        Timber.i("recieved url:" + url)
        Timber.i("recieved htmlData:" + htmlData)

        val bundle = Bundle()
        if (url != null) {
            bundle.putString("url", url)
        }
        if (htmlData != null){
            bundle.putString("htmlData", htmlData)
        }

        mFragment.arguments = bundle
        supportFragmentManager.beginTransaction()
            .replace(R.id.container, mFragment)
            .commit()
    }
}
