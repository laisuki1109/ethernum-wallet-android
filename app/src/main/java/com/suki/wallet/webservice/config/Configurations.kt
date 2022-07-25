package com.suki.wallet.webservice.config

import com.suki.wallet.BuildConfig

object Configurations {
    private const val HOST = BuildConfig.BASE_URL
    private const val API = "api/"
    const val DOMAIN = "$HOST$API"
}