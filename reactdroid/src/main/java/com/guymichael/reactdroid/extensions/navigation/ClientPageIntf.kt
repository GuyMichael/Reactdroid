package com.guymichael.reactdroid.extensions.navigation

import android.view.View
import androidx.core.util.Pair
import com.guymichael.apromise.APromise
import com.guymichael.kotlinreact.model.OwnProps
import com.guymichael.reactdroid.activity.ComponentActivity

interface ClientPageIntf {
    val key: String
    val allowToOpen: () -> Boolean
    /** Note that the page might already ben open (and not necessarily foreground),
     * in which case we still want to call it for the new props to take affect,
     * but in case you have a long chain of screens, you might call the top one only in this case.
     * See [NavigationLogic.isPageOpen]*/
    val openPage: (context: ComponentActivity<*>, OwnProps, animations: Pair<Int?, Int?>?
                   , transitions: Array<Pair<View, String>>?
                   , forResult_requestCode: Int?)
            -> APromise<out ComponentActivity<*>>

    fun getPageName(): String

    /** @param extras may be from PUSH or Deep Link, or any other source.. */
    fun mapExtrasToPropsOrNull(extras: Map<String, String>): OwnProps?

    fun openOrReject(context: ComponentActivity<*>, props: OwnProps
            , animations: Pair<Int?, Int?>? = null
            , transitions: Array<Pair<View, String>>? = null
            , forResult_requestCode: Int? = null
        ) : APromise<out ComponentActivity<*>> {

        return if (allowToOpen()) {
                openPage(context, props, animations, transitions, forResult_requestCode)
            } else {
                APromise.ofReject("${getPageName()} requires login or not allowed for some other reason")
            }
    }

    fun openOrReject(context: ComponentActivity<*>
            , extras: Map<String, String>
            , animations: Pair<Int?, Int?>? = null
            , transitions: Array<Pair<View, String>>? = null
            , forResult_requestCode: Int? = null
        ) : APromise<out ComponentActivity<*>> {

        return mapExtrasToPropsOrNull(extras)
            ?.let { openOrReject(context, it, animations, transitions, forResult_requestCode) }
            ?: return APromise.ofReject("${getPageName()} requires non-null props built from Map<String, String> extras")
    }

    fun isPageOpen(): Boolean {
        return AppForegroundLogic.isPageAlive(this)
    }
}