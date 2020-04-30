package com.guymichael.reactdroid.extensions.router.model

import com.guymichael.apromise.APromise
import com.guymichael.reactdroid.core.activity.ComponentActivity

/** An custom, non-openPage action to be executed for deep links */
interface CustomDeepLinkActionIntf {
    /** @param extras the uri query params as a [Map]. If duplicate keys exist, first one is taken */
    fun executeOrReject(context: ComponentActivity<*>, extras: Map<String, String>): APromise<Unit>
}