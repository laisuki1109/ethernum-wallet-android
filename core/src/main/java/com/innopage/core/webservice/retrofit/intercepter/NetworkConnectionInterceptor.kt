package com.innopage.core.webservice.retrofit.intercepter

import android.annotation.SuppressLint
import android.content.Context
import android.content.Context.CONNECTIVITY_SERVICE
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import com.innopage.core.webservice.retrofit.exception.NoConnectivityException
import okhttp3.Interceptor
import okhttp3.Response

class NetworkConnectionInterceptor(private val mContext: Context) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {

        if (!isConnected()) {
            throw NoConnectivityException()
        }

        return chain.proceed(chain.request())
    }

    @Suppress("DEPRECATION")
    @SuppressLint("MissingPermission")
    private fun isConnected(): Boolean {
        val connectivityManager =
            mContext.getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)?.apply {
                return when {
                    hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                    hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                    hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                    else -> false
                }
            }
        } else {
            return when (connectivityManager.activeNetworkInfo?.type) {
                ConnectivityManager.TYPE_WIFI -> true
                ConnectivityManager.TYPE_MOBILE -> true
                else -> false
            }
        }

        return false
    }
}