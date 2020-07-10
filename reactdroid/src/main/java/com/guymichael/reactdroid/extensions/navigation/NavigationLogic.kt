package com.guymichael.reactdroid.extensions.navigation

import android.app.Application
import android.view.View
import androidx.annotation.UiThread
import androidx.core.util.Pair
import com.guymichael.apromise.APromise
import com.guymichael.kotlinreact.BuildConfig
import com.guymichael.kotlinreact.Logger
import com.guymichael.kotlinreact.model.OwnProps
import com.guymichael.reactdroid.core.Utils
import com.guymichael.reactdroid.extensions.navigation.model.NavigationAction
import com.guymichael.reactdroid.core.activity.ComponentActivity
import com.guymichael.reactdroid.core.model.AComponent

object NavigationLogic {

    fun init(app: Application) {
        if( !AppForegroundLogic.isInitialized()) {
            AppForegroundLogic.init(app)
        }
    }

    @UiThread
    fun open(page: ClientPageIntf, context: ComponentActivity<*>
        , props: OwnProps
        , animations: Pair<Int?, Int?>? = null
        , transitions: Array<Pair<View, String>>? = null
        , forResult_requestCode: Int? = null
        , showLoader: Boolean = false
        , intentFlags: Int? = null
    ) : APromise<out ComponentActivity<*>> {

        return if (page.allowToOpen()) {
            page.openPage(context
                , NavigationAction(props, animations, transitions, forResult_requestCode, showLoader, intentFlags)
            )
        } else {
            APromise.ofReject("${page.getPageName()} is not allowed to be open")
        }
    }

    @UiThread
    fun open(page: ClientPageIntf, context: ComponentActivity<*>
        , extras: Map<String, String>
        , animations: Pair<Int?, Int?>? = null
        , transitions: Array<Pair<View, String>>? = null
        , forResult_requestCode: Int? = null
        , showLoader: Boolean = false
        , intentFlags: Int? = null
    ) : APromise<out ComponentActivity<*>> {

        return page.mapExtrasToPropsOrNull(extras)
            ?.let { open(page, context, it, animations, transitions, forResult_requestCode, showLoader, intentFlags) }
            ?: return APromise.ofReject("${page.getPageName()} requires non-null props built from Map<String, String> extras")
    }

    /** Opens a page from a component context. 'component' must have a ComponentActivity parent! */
    fun open(page: ClientPageIntf, component: AComponent<*, *, *>
        , props: OwnProps
        , animations: kotlin.Pair<Int, Int>? = null
        , transitions: Array<kotlin.Pair<View, String>>? = null
        , forResult_requestCode: Int? = null
        , showLoader: Boolean = false
        , intentFlags: Int? = null
    ) : APromise<out ComponentActivity<*>> {

        return component.mView.context?.let { context ->
            Utils.getActivity(context, ComponentActivity::class.java)?.let {

                open(page, it, props
                    , animations?.let { anims -> androidx.core.util.Pair<Int?, Int?>(anims.first, anims.second) }
                    , transitions?.map { trans -> androidx.core.util.Pair(trans.first, trans.second) }?.toTypedArray()
                    , forResult_requestCode
                    , showLoader
                    , intentFlags
                )

            }} ?: run {
                Logger.e(NavigationLogic::class, "open(...component) must receive a component with " +
                    "a ComponentActivity context"
                )

                return APromise.ofReject("NavigationLogic.open(...component) must receive a" +
                    "component with a ComponentActivity context)"
                )
            }
    }

    @UiThread
    @JvmStatic
    fun closePage(page: ClientPageIntf, animations: androidx.core.util.Pair<Int?, Int?>?) {
        checkInitOrThrow()
        //close all activities which match this page
        AppForegroundLogic.getActivityRecords(page).forEach { record ->
            if (record.isPageAlive()) {
                record.activity?.get()?.also {
                    it.finish()
                    it.overridePendingTransition(animations?.first?:0, animations?.second?:0)
                }
            }
        }
    }

    @UiThread
    fun closePage(page: ClientPageIntf, animations: kotlin.Pair<Int, Int>? = null) {
        closePage(page, animations?.let { androidx.core.util.Pair<Int?, Int?>(it.first, it.second) })
    }

    fun isPageOpen(page: ClientPageIntf): Boolean {
        checkInitOrThrow()
        return AppForegroundLogic.getActivityRecords(page).any {
            it.isPageAlive()
        }
    }

    fun getForegroundPage(): ClientPageIntf? {
        checkInitOrThrow()
        return AppForegroundLogic.getForegroundPage()
    }

    fun isPageForeground(page: ClientPageIntf): Boolean {
        checkInitOrThrow()
        return AppForegroundLogic.getTopLiveActivityRecord(page) != null //activity guaranteed to be alive
    }

    fun getForegroundActivity(): ComponentActivity<*>? {
        checkInitOrThrow()
        return AppForegroundLogic.getForegroundActivity(ComponentActivity::class)
    }

    /** @throws IllegalStateException if [init] wasn't called */
    @Throws(IllegalStateException::class)
    private fun checkInitOrThrow() {
        if( !AppForegroundLogic.isInitialized()) {
            Logger.e(
                NavigationLogic::class
                , "monitoring is not initialized. Please call NavigationLogic.init(app)"
            )

            throw IllegalStateException("monitoring is not initialized")
        }
    }
}