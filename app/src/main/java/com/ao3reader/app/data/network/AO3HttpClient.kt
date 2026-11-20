package com.ao3reader.app.data.network

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import java.util.concurrent.TimeUnit

class AO3HttpClient(context: Context) {

    private val cookieJar = AO3CookieJar(context)

    private val client: OkHttpClient = OkHttpClient.Builder()
        .cookieJar(cookieJar)
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(35, TimeUnit.SECONDS)
        .writeTimeout(20, TimeUnit.SECONDS)
        .followRedirects(true)
        .followSslRedirects(true)
        .build()

    private val defaultUserAgent =
        "Mozilla/5.0 (Linux; Android 14; Mobile; rv:125.0) Gecko/125.0 Firefox/125.0 AO3Reader/1.0"

    suspend fun getHtml(url: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            // Append view_adult=true if accessing AO3 works to bypass adult content warning screen
            val finalUrl = if (url.contains("archiveofourown.org/works/") && !url.contains("view_adult=true")) {
                if (url.contains("?")) "$url&view_adult=true" else "$url?view_adult=true"
            } else {
                url
            }

            val request = Request.Builder()
                .url(finalUrl)
                .header("User-Agent", defaultUserAgent)
                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                .header("Accept-Language", "zh-CN,zh;q=0.9,en-US;q=0.8,en;q=0.7")
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    return@withContext Result.failure(
                        IOException("HTTP Error: ${response.code} ${response.message}")
                    )
                }
                val body = response.body?.string() ?: ""
                Result.success(body)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun fetchWorkHtml(workId: String, viewFullWork: Boolean = true): Result<String> {
        val baseUrl = "https://archiveofourown.org/works/$workId"
        val queryParams = if (viewFullWork) "?view_adult=true&view_full_work=true" else "?view_adult=true"
        return getHtml("$baseUrl$queryParams")
    }
}
