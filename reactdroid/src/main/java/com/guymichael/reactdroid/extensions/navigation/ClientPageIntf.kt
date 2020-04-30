package com.guymichael.reactdroid.extensions.navigation

import com.guymichael.apromise.APromise
import com.guymichael.kotlinreact.model.OwnProps
import com.guymichael.reactdroid.core.activity.ComponentActivity
import com.guymichael.reactdroid.extensions.navigation.model.NavigationAction

/**
 * An interface (to be extended by an enum class), which serves as the binding point for
 * all the opening-a-page logic in the app (NavigationLogic, DeepLinkLogic, etc.)
 * by implementing the core logic of a virtual page:
 * 1. 'allowToOpen' (a page. Depends on the app state, e.g. isLoggedIn)
 * 2. 'openPage' (actual page-opening logic. May be a chain of pages, may depend on the app state.
 * 3. 'path' for DeepLinks
 * 4. 'extrasToProps' for DeepLinks and PUSH, for open-a-page notifications
 */
interface ClientPageIntf {
    /** DeepLink url */
    val path: String                                //THINK allow url params ( `/path/to/{param}` )
    val allowToOpen: () -> Boolean                  //THINK remove & use openPage-promise rejection
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