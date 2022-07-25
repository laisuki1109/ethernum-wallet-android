package com.innopage.core.webservice.retrofit.exception

import java.io.IOException

class NoConnectivityException : IOException() {

    override val message: String?
        get() = "No Internet Connection";
}