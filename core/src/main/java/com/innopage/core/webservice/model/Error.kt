package com.innopage.core.webservice.model

data class Error(
    val responseCode: Int,
    val errorTitle: String?,
    val errorMessage: String?
)