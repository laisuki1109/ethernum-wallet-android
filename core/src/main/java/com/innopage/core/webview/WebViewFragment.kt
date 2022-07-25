package com.innopage.core.webview

import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.google.android.material.appbar.MaterialToolbar
import com.innopage.core.R
import com.innopage.core.base.BaseFragment
import com.innopage.core.databinding.FragmentWebViewBinding
import com.suki.wallet.webview.WebViewViewModel

class WebViewFragment : BaseFragment<WebViewViewModel>() {

    companion object {
        fun newInstance() = WebViewFragment()
    }

    override val viewModel: WebViewViewModel by lazy {
        setUpViewModel(requireActivity().application) as WebViewViewModel
    }

    lateinit var binding: FragmentWebViewBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_web_view, container, false)
        binding.lifecycleOwner = this

        setupToolbar(binding.toolbar)

        val htmlData = arguments?.getString("htmlData")
        var url = arguments?.getString("url")

        if (htmlData != null){
            binding.webView.loadData(htmlData,"text/html", "UTF-8")
        } else if (url != null){
            binding.progressBar.progress = 1
            binding.progressBar.max = 100
            binding.webView.apply {
                settings.javaScriptEnabled = true
                webChromeClient = object : WebChromeClient(){
                    override fun onProgressChanged(view: WebView?, newProgress: Int) {
                        binding.progressBar.progress = newProgress
                    }
                }
                webViewClient = object : WebViewClient(){
                    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                        super.onPageStarted(view, url, favicon)
                        binding.progressBar.visibility = View.VISIBLE
                    }

                    override fun onPageFinished(view: WebView?, url: String?) {
                        binding.progressBar.visibility = View.GONE
                        super.onPageFinished(view, url)
                    }
                }
            }
            if (!url.startsWith("http://") && !url.startsWith("https://")){
                url = "http://$url"
            }
            binding.webView.loadUrl(url)
        }



        return binding.root
    }

    fun setupToolbar(toolbar: MaterialToolbar) {
        (activity as AppCompatActivity).run {
            setSupportActionBar(toolbar)
//            supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_navbar_back_light)
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            supportActionBar?.title = null
            toolbar.setNavigationOnClickListener {
                finish()
            }
        }
    }



    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
    }
}