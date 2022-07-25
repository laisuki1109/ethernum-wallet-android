package com.innopage.core.base

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.innopage.core.webservice.config.Constants
import com.innopage.core.webview.WebViewActivity
import timber.log.Timber

abstract class BaseFragment<T : BaseViewModel> : Fragment() {

    protected abstract val viewModel: T

    inline fun <reified T : BaseViewModel> setUpViewModel(application: Application): T {
        val viewModel = ViewModelProvider.AndroidViewModelFactory.getInstance(application)
            .create(T::class.java)

        viewModel.status.observe(viewLifecycleOwner, Observer {
            when (it) {
                Constants.Status.LOADING -> showProgressDialog()
                Constants.Status.DONE -> hideProgressDialog()
                Constants.Status.ERROR -> hideProgressDialog()
                else -> hideProgressDialog()
            }
        })

        viewModel.error.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                showErrorMessage(it.responseCode, it.errorTitle, it.errorMessage)
                viewModel.resetError()
            }
        })

        return viewModel
    }

    fun <T : BaseViewModel> setUpViewModel(viewModel: T): T {
        viewModel.status.observe(viewLifecycleOwner, Observer {
            when (it) {
                Constants.Status.LOADING -> showProgressDialog()
                Constants.Status.DONE -> hideProgressDialog()
                Constants.Status.ERROR -> hideProgressDialog()
                else -> hideProgressDialog()
            }
        })

        viewModel.error.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                showErrorMessage(it.responseCode, it.errorTitle, it.errorMessage)
                viewModel.resetError()
            }
        })

        return viewModel
    }

    override fun onSaveInstanceState(outState: Bundle) {
        Timber.d("onSaveInstanceState called")
        super.onSaveInstanceState(outState)
    }

    override fun onAttach(context: Context) {
        Timber.d("onAttach called")
        super.onAttach(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Timber.d("onCreate called")

        // Set menu for fragment
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        Timber.d("onCreateView called")
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Timber.d("onViewCreated called")
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        Timber.d("onActivityCreated called")
        super.onActivityCreated(savedInstanceState)
    }

    override fun onStart() {
        Timber.d("onStart called")
        super.onStart()
    }

    override fun onResume() {
        Timber.d("onResume called")
        super.onResume()
    }

    override fun onPause() {
        Timber.d("onPause called")
        super.onPause()
    }

    override fun onStop() {
        Timber.d("onStop called")
        super.onStop()
    }

    override fun onDestroyView() {
        Timber.d("onDestroyView called")
        super.onDestroyView()
    }

    override fun onDestroy() {
        Timber.d("onDestroy called")
        super.onDestroy()
    }

    override fun onDetach() {
        Timber.d("onDetach called")
        super.onDetach()
    }

    // Show loading dialog
    fun showProgressDialog() {
        (activity as? BaseAppCompatActivity)?.showProgressDialog()
    }

    // Dismiss loading dialog
    fun hideProgressDialog() {
        (activity as? BaseAppCompatActivity)?.hideProgressDialog()
    }

    // Show error message from api
    fun showErrorMessage(
        responseCode: Int,
        errorTitle: String?,
        errorMessage: String?,
        dismissCallback: (() -> Unit)? = null
    ) {
        (activity as? BaseAppCompatActivity)?.showErrorMessage(
            responseCode,
            errorTitle,
            errorMessage,
            dismissCallback
        )
    }

    // Hide keyboard form view
    fun hideKeyboard(view: View) {
        val imm =
            requireContext().getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    // Hide keyboard form fragment
    fun hideKeyboard(currentFragment: Fragment) {
        val imm =
            requireContext().getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(currentFragment.view?.rootView?.windowToken, 0)
    }

    // Show keyboard form view
    fun showKeyboard(view: View) {
        val imm =
            requireContext().getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
    }

    // Show keyboard form fragment
    fun showKeyboard(currentFragment: Fragment) {
        val imm =
            requireContext().getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(currentFragment.view?.rootView, InputMethodManager.SHOW_IMPLICIT)
    }

    open fun openWebView(url: String, isHtmlData:Boolean){
        val intent = Intent(requireContext(), WebViewActivity::class.java)
        if (isHtmlData){
            intent.putExtra("htmlData", url)
        } else {
            intent.putExtra("url", url)
        }
        startActivity(intent)
    }
}