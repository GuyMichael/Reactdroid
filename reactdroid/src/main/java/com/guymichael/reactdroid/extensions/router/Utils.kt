package com.guymichael.reactdroid.extensions.router

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import com.guymichael.reactdroid.core.Utils
import java.io.UnsupportedEncodingException
import java.net.URI
import java.net.URLDecoder
import java.net.URLEncoder
import java.util.*

object Utils {
    @JvmStatic
    fun openLink(context: Context, url: String?): Boolean {
        return if (url != null) {
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            try {
                startExternalActivity_notPure(context, browserIntent)
                true
            } catch (e: ActivityNotFoundException) {
                //broken link
                false
            }
        } else {
            false
        }
    }

    /**
     * @param queryString without '?'
     * @return the parsed data. You can be sure that if a key exists, it has a non-null AND non-empty list of values (at least 1 value)
     * @throws UnsupportedEncodingException
     */
    @Throws(UnsupportedEncodingException::class)
    @JvmStatic
    private fun parseUrlQueryString(queryString: String): Map<String, List<String>> {
        val queryParams = LinkedHashMap<String, MutableList<String>>()
        val pairs = queryString.split("&").toTypedArray()

        for (pair in pairs) {
            val separatorIndex = pair.indexOf("=")

            val key = separatorIndex.takeIf { it > 0 }?.let {
                URLDecoder.decode(pair.substring(0, it), "UTF-8")
            }

            val value = separatorIndex.takeIf { it > 0 && pair.length > it + 1 }?.let {
                URLDecoder.decode(pair.substring(it + 1), "UTF-8")
            }

            //add key&value to map, if not empty
            if( !key.isNullOrBlank() && !value.isNullOrBlank()) {
                if( !queryParams.containsKey(key)) {
                    queryParams[key] = LinkedList()
                }
                queryParams[key]!!.add(value)
            }
        }
        return queryParams
    }

    /** @return encoded query params, no "?" */
    @Throws(UnsupportedEncodingException::class)
    fun encodeQueryString(params: Map<String, Any?>, paramsEncoding: String): String {
        return StringBuilder().also {
            for ((key, value) in params) {
                it.append(URLEncoder.encode(key, paramsEncoding))
                it.append('=')
                it.append(URLEncoder.encode(value.toString(), paramsEncoding))
                it.append('&')
            }
            val length = it.length
            if (length > 0 && it.lastIndexOf("&") == length - 1) {
                it.delete(length - 1, length) //THINK efficiency - don't append last one instead of delete
            }
        }.toString()
    }

    /**
     * Encodes the url and query parameters using [URLEncoder].
     * If `url` is null (and `params` is not), a valid query string starting with "?" will be returned.
     * If `url` already contains a query string, `params` will be added to it, duplicate keys allowed.
     * If both are `null`, an empty String will be returned
     *
     * @return an [encoded url][URLEncoder.encode] with given query-params (also encoded)
     */
    @Throws(UnsupportedEncodingException::class)
    fun encodeUrl(url: String?, params: Map<String, Any?>? = null, encoding: String = "UTF-8"): String {
        val encodedUrl = url?.let {
            StringBuilder(URLEncoder.encode(it, encoding))
        } ?: StringBuilder()

        if (params?.isNotEmpty() == true) {
            encodeQueryString(params, encoding).takeIf { it.isNotBlank() }?.also { query ->
                if (encodedUrl.indexOf("?") < 0) {
                    encodedUrl.append("?")
                } else {
                    encodedUrl.append("&")
                }

                encodedUrl.append(query)
            }
        }

        return encodedUrl.toString()
    }

    @Throws(UnsupportedEncodingException::class)
    fun encodeUrl(url: String?, vararg params: Pair<String, Any?>, encoding: String = "UTF-8"): String? {
        return encodeUrl(url, mapOf(*params), encoding)
    }

    @Throws(UnsupportedEncodingException::class)
    @JvmStatic
    fun parseUrlQueryParams(url: URI): Map<String, List<String>> {
        val queryStr = url.query
        return parseUrlQueryString(queryStr ?: "")
    }

    @Throws(UnsupportedEncodingException::class)
    @JvmStatic
    fun parseUrlQueryParamsSimple(url: URI): Map<String, String> {
        return parseUrlQueryParams(url).mapValuesNotNull { it.value.firstOrNull() }
    }
}








/** NOT pure - may affect `intent` */
private fun startExternalActivity_notPure(context: Context, intent: Intent) {
    val callingActivity: Activity? = Utils.getActivity(context)

    if (callingActivity == null) {
        //note: FLAG_ACTIVITY_NEW_TASK *must* be present or the app will crash,
        // as we call startActivity from a non-Activity context:
        // see: https://developer.android.com/about/versions/pie/android-9.0-changes-all#fant-required

        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)//THINK not pure!!
        context.startActivity(intent)
    } else {
        callingActivity.startActivity(intent)
    }
}