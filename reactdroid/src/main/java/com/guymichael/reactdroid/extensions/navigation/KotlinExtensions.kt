package com.guymichael.reactdroid.extensions.navigation

import com.guymichael.reactdroid.core.activity.ComponentActivity

/** Let this activity be handled (open/close) by a Store (NavigationReducer).
 *
 *  To be run on the ui thread, normally from [ComponentActivity.componentWillMount] */
fun ComponentActivity<*>.withStoreNavigation(page: ClientPageIntf) {
    AppForegroundLogic.bindActivityToClientPage(this, page)
}