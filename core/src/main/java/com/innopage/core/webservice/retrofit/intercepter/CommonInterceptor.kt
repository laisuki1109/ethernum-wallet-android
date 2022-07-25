package com.innopage.core.webservice.retrofit.intercepter

import okhttp3.FormBody
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okio.Buffer
import org.json.JSONArray
import org.json.JSONObject
import java.net.URLDecoder

class CommonInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val req = chain.request()
        val reqBody = req.body

        val builder = FormBody.Builder()
        val jsonObject = JSONObject()

        tryToSerializeRequestBody(reqBody).apply {
            split("&")
                .filter { it.isNotEmpty() }
                .map {
                    it.split("=")
                }
                .filter { it.size > 1 }
                .map {
                    builder.add(it[0], URLDecoder.decode(it[1], "utf-8"))
                    val value = URLDecoder.decode(it[1], "utf-8")
                    if (value.startsWith("[") && value.endsWith("]")) {
                        jsonObject.put(it[0], JSONArray(value))
                    } else {
                        jsonObject.put(it[0], value)
                    }
                }
        }

        // Set form body
        val formBody = builder
            .build()

        // Set json
        val requestBody = jsonObject.toString()
            .toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())

        val request = when (req.method) {
            "GET" ->
                req.newBuilder()
                    .url("${req.url}")
                    .build()
            "POST" ->
                req.newBuilder()
                    .post(requestBody)
                    .build()
            else -> req
        }

        return chain.proceed(request)
    }

    private fun tryToSerializeRequestBody(reqBody: RequestBody?): String {
        val buf = Buffer()
        return try {
            if (reqBody != null) {
                reqBody.writeTo(buf)
                buf.readUtf8()
            } else {
                ""
            }
        } catch (e: Exception) {
            "Request Raw Body: Exist, but fail to decode"
        }
    }
}