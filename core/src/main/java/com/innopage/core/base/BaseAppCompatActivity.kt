package com.innopage.core.base

import android.app.AlertDialog
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import com.innopage.core.R
import com.innopage.core.webservice.config.Constants

abstract class BaseAppCompatActivity : AppCompatActivity() {

    private var mProgressDialog: AlertDialog? = null
    private var mAlertDialog: AlertDialog? = null

    override fun onDestroy() {
        mProgressDialog?.dismiss()
        mAlertDialog?.dismiss()
        super.onDestroy()
    }

    fun showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog =
                AlertDialog.Builder(this, R.style.MyProgressDialogTheme)
                    .setView(ProgressBar(this))
                    .setCancelable(false)
                    .create()
        }
        if (mProgressDialog?.isShowing == false) {
            mProgressDialog?.show()
        }
    }

    fun hideProgressDialog() {
        mProgressDialog?.dismiss()
    }

    fun showErrorMessage(
        responseCode: Int,
        errorTitle: String?,
        errorMessage: String?,
        dismissCallback: (() -> Unit)? = null
    ) {
        when (responseCode) {
            Constants.RESPONSE_SUCCESS -> {

            }
            Constants.RESPONSE_NETWORK_ERROR -> {
                // Pop up alert dialog
                popUpAlertDialog(
                    title = null,
                    message = getString(R.string.error_network),
                    dismissCallback = dismissCallback
                )
            }
            Constants.RESPONSE_SERVER_ERROR -> {
                // Pop up alert dialog
                popUpAlertDialog(
                    title = null,
                    message = getString(R.string.error_network),
                    dismissCallback = dismissCallback
                )
            }
            else -> {
                // Pop up alert dialog
                popUpAlertDialog(
                    title = errorTitle,
                    message = errorMessage,
                    dismissCallback = dismissCallback
                )
            }
        }
    }

    private fun popUpAlertDialog(
        title: String?,
        message: String?,
        dismissCallback: (() -> Unit)? = null
    ) {
        if (!title.isNullOrEmpty() || !message.isNullOrEmpty()) {
            mAlertDialog?.dismiss()
            mAlertDialog = AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(R.string.btn_ok, null)
                .setOnDismissListener {
                    dismissCallback?.invoke()
                }
                .create()
            mAlertDialog?.show()
        }
    }
}