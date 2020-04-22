package com.guymichael.reactdroid.extensions.router

import com.guymichael.apromise.APromise
import com.guymichael.reactdroid.extensions.router.model.CustomDeepLinkAction
import com.guymichael.reactdroid.activity.ComponentActivity
import com.guymichael.reactdroid.extensions.navigation.ClientPageIntf
import java.io.File
import java.io.UnsupportedEncodingException
import java.net.URI

object DeepLinkLogic {

    private lateinit var pageParser: (String) -> ClientPageIntf?
    private var customActionParser: ((String) -> CustomDeepLinkAction?)? = null

    internal fun init(pageParser: (String) -> ClientPageIntf?
            , customActionParser: ((String) -> CustomDeepLinkAction?)?) {
        DeepLinkLogic.pageParser = pageParser
        DeepLinkLogic.customActionParser = customActionParser
    }

    @JvmStatic
    fun openDeepLinkOrNormalLink(context: ComponentActivity<*>, url: String): APromise<Unit> {
        return openDeepLink(context, url)
            .catchResume {
                //not a deep link. Open url normally
                Utils.openLink(context, url)
                APromise.of()
            }
    }

    @JvmStatic
    fun openDeepLink(context: ComponentActivity<*>, url : String) : APromise<Unit> {
        return try {
            openDeepLink(context, URI.create(url))
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
            APromise.ofReject("error parsing uri: $url\n${e.message?:""}")
        }
    }

    fun openDeepLink(context: ComponentActivity<*>, uri : URI) : APromise<Unit> {
        val key = parseDeepLinkOrClientPageKey(uri)

        //custom action for this key
        return customActionParser?.invoke(key)?.executeOrReject(context, uri)

        //or open the new CLIENT_COMPONENT_PAGE model (per uri)
        ?: pageParser.invoke(key)?.let { componentPage ->
            parseUri(uri)?.let { extras ->

                componentPage.openOrReject(context, extras
                    , null, null, null, true
                ).thenMap { Unit } //THINK animations
            }
        }

        ?: APromise.ofReject("error parsing page/uri: $uri\n")
    }
}







private fun parseDeepLinkOrClientPageKey(uri: URI): String {
    return File(uri.path).name//use File to get last path segment;
}

private fun parseUri(uri: URI): Map<String, String>? {
    return try {
        Utils.parseUrlQueryParamsSimple(uri)
    } catch (e: UnsupportedEncodingException) {
        e.printStackTrace()
        null
    }
}