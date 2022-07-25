package com.innopage.core.webservice.retrofit

import android.content.Context
import com.innopage.core.webservice.retrofit.intercepter.CommonInterceptor
import com.innopage.core.webservice.retrofit.intercepter.LoggingInterceptor
import com.innopage.core.webservice.retrofit.intercepter.NetworkConnectionInterceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.*

interface RetrofitProvider {
    fun createOkHttpClient(): OkHttpClient
}

inline fun <reified T> createRetrofit(client: OkHttpClient, baseUrl: String): T {
    return Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
        .build()
        .create(T::class.java)
}

inline fun <reified T> RetrofitProvider.createApiService(baseUrl: String): T {
    return createRetrofit(createOkHttpClient(), baseUrl)
}

class RetrofitProviderImp(private val mContext: Context) : RetrofitProvider {

    private val networkConnectionInterceptor: NetworkConnectionInterceptor by lazy {
        NetworkConnectionInterceptor(
            mContext
        )
    }
    private val commonInterceptor: CommonInterceptor by lazy { CommonInterceptor() }
    private val loggingInterceptor: LoggingInterceptor by lazy { LoggingInterceptor() }

    override fun createOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .addInterceptor(networkConnectionInterceptor)
            .addInterceptor(commonInterceptor)
            .addInterceptor(loggingInterceptor)
            .build()
    }
}