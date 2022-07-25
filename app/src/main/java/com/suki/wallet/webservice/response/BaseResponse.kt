package com.suki.wallet.webservice.response

import com.google.gson.annotations.SerializedName
import com.innopage.core.webservice.model.ErrorField

data class BaseResponse<T>(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("error_code")
    val errorCode: Int?,

    val data: T?,

    @SerializedName("error_message")
    val errorMessage: String?,
    @SerializedName("error_fields")
    val errorFields: List<ErrorField>?,
    @SerializedName("access_token")
    val accessToken: String?
)