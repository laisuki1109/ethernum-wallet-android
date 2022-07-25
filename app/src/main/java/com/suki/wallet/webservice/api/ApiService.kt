package com.suki.wallet.webservice.api

import com.suki.wallet.MyApplication
import com.suki.wallet.webservice.response.BaseResponse
import retrofit2.http.FieldMap
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface ApiService {

    companion object {
        // TODO: Define urls
        private const val CONFIG_URL = "config"
    }

    @FormUrlEncoded
    @POST(CONFIG_URL)
    suspend fun postConfig(
        @FieldMap params: Map<String, String> = getDefaultParams()
    ): BaseResponse<Any>

    private fun getDefaultParams(): Map<String, String> {
        return MyApplication.INSTANCE.run {
            HashMap<String, String>().also {
                it["device_type"] = "android"
                it["device_id"] = deviceId.orEmpty()
                it["language"] = appLanguage
                it["os_version"] = osVersion
                it["app_version"] = appVersion
                if (!accessToken.isBlank()) {
                    it["access_token"] = accessToken
                }
            }
        }
    }
}