package com.innopage.core.webservice.config

import androidx.annotation.Keep

object Constants {
    // State of response
    const val RESPONSE_SUCCESS = 0
    const val RESPONSE_NETWORK_ERROR = 9999
    const val RESPONSE_SERVER_ERROR = 9998
    const val RESPONSE_LOGIN_ERROR = 9997
    const val RESPONSE_LINK_ERROR = 9996
    const val RESPONSE_FACEBOOK_ERROR = 9995
    const val RESPONSE_GOOGLE_ERROR = 9994
    const val RESPONSE_EMAIL_PASSWORD_ERROR = 9993
    const val RESPONSE_PHONE_NUMBER_ERROR = 9992
    const val RESPONSE_WALLET_ERROR = 9992

    //  Type of start activity for result
    const val ACCESS_CAMERA_REQUEST_CODE = 991
    const val ACCESS_WRITE_EXTERNAL_STORAGE = 992

    //   Extra
    const val EXTRA_WALLET_ADDRESS = "address"

    //   Request
    const val REQUEST_SCAN_QR_CODE = 1001
    const val REQUEST_IMAGE_PICKER = 1002

    // Status of webservice
    @Keep
    enum class Status {
        DONE, LOADING, ERROR
    }
}