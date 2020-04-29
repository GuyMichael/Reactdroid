package com.guymichael.reactdroid.extensions.router

import com.guymichael.reactdroid.extensions.navigation.ClientPageIntf
import com.guymichael.reactdroid.extensions.router.model.CustomDeepLinkActionIntf

object Router {

    /**
     * To enable deep linking:
     *
     * 1. Call this method with a page parser
     * 2. Override your `ComponentActivity`'s onIntentChanged() to call the
     *    `ComponentActivity` extension method `checkForDeepLinkAndExecute()`
     * 3. Add this following to your deep-link entry Activity in manifest, normally your splash screen:
     *
     * `<intent-filter>`
     *
     * `<action android:name="android.intent.action.VIEW"/>`
     *
     * `<category android:name="android.intent.category.DEFAULT"/>`
     *
     * `<category android:name="android.intent.category.BROWSABLE"/>`
     *
     * `<data android:scheme="http"  android:host="@string/deepLinkBaseUrl" />`
     *
     * `<data android:scheme="https" android:host="@string/deepLinkBaseUrl" />`
     *
     * `</intent-filter>`
     */
    @JvmStatic
    fun init(deepLinkPageParser: (String) -> ClientPageIntf?
         , deepLinkCustomActionParser: ((String) -> CustomDeepLinkActionIntf?)? = null) {

        DeepLinkLogic.init(deepLinkPageParser, deepLinkCustomActionParser)
    }
}