package com.guymichael.reactdroid.extensions.router.model

import com.guymichael.apromise.APromise
import com.guymichael.reactdroid.activity.ComponentActivity
import java.net.URI

interface CustomDeepLinkAction {
    fun executeOrReject(context: ComponentActivity<*>, uri: URI): APromise<Unit>
}