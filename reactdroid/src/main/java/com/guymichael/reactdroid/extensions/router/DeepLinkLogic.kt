package com.guymichael.reactdroid.extensions.router

import com.guymichael.apromise.APromise
import com.guymichael.reactdroid.extensions.router.model.CustomDeepLinkActionIntf
import com.guymichael.reactdroid.activity.ComponentActivity
import com.guymichael.reactdroid.extensions.navigation.ClientPageIntf
import com.guymichael.reactdroid.extensions.navigation.NavigationLogic
import java.io.File
import java.io.UnsupportedEncodingException
import java.net.URI

/**
 * A logic class for opening a deep link. Default behavior opens a [ClientPageIntf] instance if found
 * according to uri. When custom actions are found ([customActionParser], [CustomDeepLinkActionIntf]),
 * they take precedence over opening a page
 */
object DeepLinkLogic {

    private lateinit var pageParser: (String) -> ClientPageIntf?
    private var customActionParser: ((String) -> CustomDeepLinkActionIntf?)? = null

    internal fun init(pageParser: (String) -> ClientPageIntf?
            , customActionParser: ((String) -> CustomDeepLinkActionIntf?)?) {
        DeepLinkLogic.pageParser = pageParser
        DeepLinkLogic.customActionParser = customActionParser
    }

    /**
     * Opens a DeepLink and, if not found, opens `url` as a normal link - on the default browser
     */
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
        val path = uri.getInnerPath()
        val extras = uri.parseQuery()

        //parse custom action first
        return customActionParser?.invoke(path)?.executeOrReject(context, extras)

        //or open a ClientPage (default)
        ?: pageParser.invoke(path)?.let { page ->

            NavigationLogic.open(page, context, extras
                , null, null, null, true
            ).thenMap { Unit } //THINK animations
        }

        ?: APromise.ofReject("error parsing page/uri: $uri\n")
    }
}






/** @return the uri path, without the hostname and protocol */
private fun URI.getInnerPath(): String {
    return File(this.path).name
}

/** @return query params as a [Map]. If duplicate keys exist, first key&value pair is used  */
private fun URI.parseQuery(): Map<String, String> {
    return try {
        Utils.parseUrlQueryParamsSimple(this)
    } catch (e: UnsupportedEncodingException) {
        e.printStackTrace()
        HashMap()
    }
}