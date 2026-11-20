package com.ao3reader.app.data.network

import android.content.Context
import android.webkit.CookieManager
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl

class AO3CookieJar(private val context: Context) : CookieJar {

    private val cookieStore = mutableMapOf<String, MutableMap<String, Cookie>>()

    init {
        // Pre-set adult consent cookie for archiveofourown.org
        val adultConsentCookie = Cookie.Builder()
            .domain("archiveofourown.org")
            .path("/")
            .name("view_adult")
            .value("true")
            .build()
        saveCookie("archiveofourown.org", adultConsentCookie)
    }

    @Synchronized
    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        val host = url.host
        val hostCookies = cookieStore.getOrPut(host) { mutableMapOf() }
        for (cookie in cookies) {
            hostCookies[cookie.name] = cookie
            // Sync to WebView CookieManager
            try {
                CookieManager.getInstance().setCookie(url.toString(), "${cookie.name}=${cookie.value}")
            } catch (ignored: Exception) {}
        }
    }

    @Synchronized
    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        val host = url.host
        val result = mutableListOf<Cookie>()
        
        // Ensure view_adult is always present for AO3
        if (host.contains("archiveofourown.org")) {
            result.add(
                Cookie.Builder()
                    .domain("archiveofourown.org")
                    .path("/")
                    .name("view_adult")
                    .value("true")
                    .build()
            )
        }

        cookieStore[host]?.values?.let {
            result.addAll(it)
        }

        // Also sync from system CookieManager (if user logged in inside WebView)
        try {
            val webViewCookies = CookieManager.getInstance().getCookie(url.toString())
            if (!webViewCookies.isNullOrBlank()) {
                val split = webViewCookies.split(";")
                for (item in split) {
                    val parts = item.trim().split("=", limit = 2)
                    if (parts.size == 2) {
                        val name = parts[0].trim()
                        val value = parts[1].trim()
                        if (result.none { it.name == name }) {
                            result.add(
                                Cookie.Builder()
                                    .domain(host)
                                    .path("/")
                                    .name(name)
                                    .value(value)
                                    .build()
                            )
                        }
                    }
                }
            }
        } catch (ignored: Exception) {}

        return result
    }

    @Synchronized
    private fun saveCookie(domain: String, cookie: Cookie) {
        val domainCookies = cookieStore.getOrPut(domain) { mutableMapOf() }
        domainCookies[cookie.name] = cookie
    }
}
