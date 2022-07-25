package com.innopage.core.webservice.retrofit.intercepter

import okhttp3.FormBody
import okhttp3.Interceptor
import okhttp3.RequestBody
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import okhttp3.internal.http2.Http2Reader.Companion.logger
import okio.Buffer

class LoggingInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val req = chain.request()
        val resp = chain.proceed(req)
        val reqBody = req.body
        val respStr: String? = resp.body?.string()

        if (resp.isSuccessful) {
            logger.info(
                """
        ============= Retrofit Request ==============
        Method: ${req.method}
        URL: ${req.url}
        Request Headers: ${req.headers.toMultimap()}
        Request Raw Body: ${tryToSerializeRequestBody(reqBody)}
        Request Body: ${(reqBody as? FormBody)?.toDebugString()}
        Response Headers: ${resp.headers.toMultimap()}
        Response Code: ${resp.code}
        Response: $respStr
        ========== End of Retrofit Request ==========
      """
            )
        } else {
            logger.warning(
                """
        ============= Error Retrofit Request ==============
        Method: ${req.method}
        URL: ${req.url}
        Request Headers: ${req.headers.toMultimap()}
        Request Raw Body: ${tryToSerializeRequestBody(reqBody)}
        Request Body: ${(reqBody as? FormBody)?.toDebugString()}
        Response Headers: ${resp.headers.toMultimap()}
        Response Code: ${resp.code}
        Error: $respStr
        ========== End of Error Retrofit Request ==========
      """
            )
        }

        // Because Response bodies can only be read once, re-construct a new response
        val content = respStr ?: ""
        return resp
            .newBuilder()
            .body(content.toResponseBody(resp.body?.contentType()))
            .build()
    }

    private fun tryToSerializeRequestBody(reqBody: RequestBody?): String {
        val buf = Buffer()
        return try {
            if (reqBody != null) {
                reqBody.writeTo(buf)
                buf.readUtf8()
            } else {
                "null"
            }
        } catch (e: Exception) {
            "Request Raw Body: Exist, but fail to decode"
        }
    }

    private fun FormBody.toDebugString(): String {
        return (0 until size).map { i ->
            "${this.name(i)}=${this.value(i)}"
        }.toString()
    }
}