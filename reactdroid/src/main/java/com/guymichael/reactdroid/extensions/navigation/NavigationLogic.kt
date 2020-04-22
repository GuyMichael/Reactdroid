package com.guymichael.reactdroid.extensions.navigation

import android.app.Application
import com.guymichael.kotlinreact.Logger
import com.guymichael.kotlinreact.model.OwnProps
import com.guymichael.reactdroid.extensions.navigation.model.NavigationAction
import com.guymichael.reactdroid.Utils
import com.guymichael.reactdroid.activity.ComponentActivity

object NavigationLogic {

    fun initMonitoring(app: Application) {
        if( !AppForegroundLogic.isInitialized()) {
            AppForegroundLogic.init(app)
        }
    }

    fun dispatch(action: NavigationAction, context: ComponentActivity<*>) {
        Utils.runOnUiThread {
            handleAction(action, context)
        }
    }

    fun <P : OwnProps>openPage(context: ComponentActivity<P>, page: ClientPageIntf, props: P) {
        dispatch(NavigationAction.open(page, props), context)
    }

    fun openPageWithoutContext(page: ClientPageIntf, props: OwnProps) {
        Utils.runOnUiThread {
            handleAction(NavigationAction.open(page, props), null)
        }
    }

    /** Searches for any foreground activity to be used as context.
     * NOTICE: may fail if there is no foreground activity found to be used */
    fun dispatchWithoutContext(action: NavigationAction) {
        Utils.runOnUiThread {
            handleAction(action, null)
        }
    }

    fun closePage(page: ClientPageIntf) {
        Utils.runOnUiThread {
            handleAction(NavigationAction.close(page), null)//no need for context to close
        }
    }

    fun isPageOpen(page: ClientPageIntf): Boolean {
        return AppForegroundLogic.getActivityRecords(page).any {
            it.isPageAlive()
        }
    }

    fun getForegroundPage(): ClientPageIntf? {
        return AppForegroundLogic.getForegroundPage()
    }

    fun isPageForeground(page: ClientPageIntf): Boolean {
        return AppForegroundLogic.getTopLiveActivityRecord(page) != null //activity guaranteed to be alive
    }









    /** To be run on the ui thread */
    private fun handleAction(action: NavigationAction, context: ComponentActivity<*>?) {
        //init monitoring
        if( !AppForegroundLogic.isInitialized()) {
            if (context == null) {
                Logger.e(
                    NavigationLogic::class
                    , "monitoring is not initialized. Please call NavigationLogic.initMonitoring " +
                        "or be sure to call dispatch/open page with a context as first navigation " +
                        "in your app. Action aborted:\n$action"
                )
                return
            } else {
                AppForegroundLogic.init(context.application)
            }
        }

        if (action.opened) {
            if (action.props == null) {
                Logger.e(NavigationLogic::class, "NavigationAction given without page props. Page won't open")
            } else {
                (context ?: AppForegroundLogic.getForegroundActivity(ComponentActivity::class))?.also {
                    openPageIntl(it, action, action.props)
                }

                ?: Logger.e(
                    NavigationLogic::class, "NavigationAction (open) can't run as no " +
                    "foreground activity found to be used as context. Please provide a context " +
                    "to dispatch()/openPage(). Action aborted:\n$action")
            }

        } else {
            closePageIntl(action)
        }
    }

    /** To be run on the ui thread */
    private fun openPageIntl(context: ComponentActivity<*>, action: NavigationAction, props: OwnProps) {
            action.page.openOrReject(
                context, props, action.inOutAnimations, action.transitions
                , action.forResult_requestCode, action.showLoader
            )
            .catch {}
            .executeWhileAlive(context)
    }

    /** To be run on the ui thread */
    private fun closePageIntl(action: NavigationAction): Boolean {
        //close all activities which match this page (action.key)
        AppForegroundLogic.getActivityRecords(action.page).forEach { record ->
            if (record.isPageAlive()) {
                record.activity?.get()?.finish()
            }
        }

        return true //assume succeed
    }
}