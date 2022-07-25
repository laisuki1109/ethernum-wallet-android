package com.suki.wallet.repository

import com.innopage.core.webservice.config.Constants
import com.innopage.core.webservice.model.Error
import com.innopage.core.webservice.retrofit.exception.ErrorException
import com.suki.wallet.MyApplication
import com.suki.wallet.webservice.api.ApiService
import com.suki.wallet.webservice.response.BaseResponse

open class BaseRepository {

    protected val apiService: ApiService
        get() = MyApplication.INSTANCE.apiService

    suspend fun <T> apiRequest(call: suspend () -> BaseResponse<T>): T {
        try {
            call.invoke().let { response ->
                if (response.success) {
                    response.accessToken.takeIf { !it.isNullOrBlank() }?.let {
                        MyApplication.INSTANCE.accessToken = it
                    }
                    response.data?.let {
                        return it
                    } ?: throw Exception()
                } else {
                    throw ErrorException(
                        Error(
                            response.errorCode ?: Constants.RESPONSE_SERVER_ERROR,
                            null,
                            response.errorMessage
                        ),
                        response.errorFields
                    )
                }
            }
        } catch (e: Exception) {
            when (e) {
                is ErrorException -> throw e
                else -> throw ErrorException(
                    Error(
                        Constants.RESPONSE_SERVER_ERROR,
                        null,
                        null
                    ),
                    null
                )
            }
        }
    }
}
