package com.guymichael.reactdroid.extensions.navigation

import com.guymichael.apromise.APromise
import com.guymichael.kotlinreact.model.OwnProps
import com.guymichael.reactdroid.activity.ComponentActivity
import com.guymichael.reactdroid.extensions.navigation.model.NavigationAction

interface ClientPageIntf {
    val path: String
    val allowToOpen: () -> Boolean
    /** Note that the page might already ben open (and not necessarily foreground),
     * in which case we still want to call it for the new props to take affect,
     * but in case you have a long chain of screens, you might call the top one only in this case.
     * See [NavigationLogic.isPageOpen]*/
    val openPage: (context: ComponentActivity<*>, NavigationAction<*>)
            -> APromise<out ComponentActivity<*>>
    /** `extras` may be from PUSH or Deep Link, or any other source.. */
    val mapExtrasToPropsOrNull: (extras: Map<String, String>) -> OwnProps?

    fun getPageName(): String
}