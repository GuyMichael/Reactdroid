package com.guymichael.reactdroid.extensions.navigation

import android.app.Activity
import android.app.ActivityManager
import android.app.Application
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import com.guymichael.apromise.APromise
import com.guymichael.reactdroid.extensions.navigation.model.ActivitiesMonitor
import com.guymichael.kotlinreact.Logger
import com.guymichael.reactdroid.extensions.navigation.model.ActivityStateRecord
import java.lang.ref.WeakReference
import kotlin.reflect.KClass


object AppForegroundLogic {

    private lateinit var monitor: ActivitiesMonitor
    private var logLifecycle: Boolean = false

    /** safe to call multiple times in which case nothing will change */
    internal fun init(app: Application) {
        startMonitoringActivities(app)
    }

    /** To be run on the ui thread */
    internal fun bindActivityToClientPage(activity: Activity, page: ClientPageIntf) {
        monitor.bindActivityToClientPage(activity, page)
    }

    internal fun isInitialized(): Boolean = this::monitor.isInitialized

    internal fun getTopActivityFromSystemService(context: Context): Activity? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getTopActivityApi23(context)
        } else {
            getTopActivityCompat(context)
        }
    }



    @JvmStatic
    fun logLifecycle(log: Boolean = true) {
        logLifecycle = true
        if (isInitialized()) {
            monitor.logLifecycle = log
        }
    }

    @JvmStatic
    fun isAppForeground(): Boolean {
        return monitor.isAppForeground()
    }

    @JvmStatic
    fun isActivityForeground(activity: Activity): Boolean {
        return monitor.isActivityForeground(activity)
    }

    @JvmStatic
    fun isActivityForeground(cls: Class<out Activity>): Boolean {
        return monitor.isActivityForeground(cls)
    }

    /**
     * @return [Activity], if not null, guaranteed to be 'alive' - not finishing/destroyed
     */
    @JvmStatic
    fun getForegroundActivity(): Activity? {
        return monitor.getForegroundRecordIntl()?.activity?.get()
    }

    /**
     * @return instance of [ClientPageIntf], activity guaranteed to be 'alive' - not finishing/destroyed
     */
    @JvmStatic
    fun getForegroundPage(): ClientPageIntf? {
        return monitor.getForegroundRecordIntl()?.page
    }

    @JvmStatic
    fun <T: Activity> getForegroundActivity(ifOfClass: Class<T>): T? {
        return getForegroundActivity()?.takeIf { ifOfClass.isInstance(it) }?.let { it as? T }
    }

    fun <T: Activity> getForegroundActivity(ifOfClass: KClass<T>): T? {
        return getForegroundActivity(ifOfClass.java)
    }

    @JvmStatic
    fun <T: Activity> waitForegroundActivity(app: Application, cls: Class<T>): APromise<T> {
        val appRef = WeakReference(app)

        return APromise.ofCallback { promiseCallback ->
            //already foreground
            getForegroundActivity(cls)?.also(promiseCallback::onSuccess)

            //wait
            ?: appRef.get()?.also {
                ActivityUtils.waitForResume(it, cls)
                    .then(promiseCallback::onSuccess)
                    .onError(promiseCallback::onFailure)
                    .execute()
            }

            //no app ref
            ?: promiseCallback.onCancel()
        }
    }

    fun <T: Activity> waitForegroundActivity(app: Application, cls: KClass<T>): APromise<T> {
        return waitForegroundActivity(app, cls.java)
    }



    /**
     * @return true if there is a record matching the given `page`
     * and the page is alive according to [Activity.isFinishing], [Activity.isDestroyed] and
     * the state (`CREATED` to `STOPPED`, inclusive)
     */
    internal fun isPageAlive(page: ClientPageIntf): Boolean {
        return getActivityRecords(page).any { it.isPageAlive() }
    }

    /**
     * @return a record matching the given `page` with a state `CREATED` to `STOPPED`, inclusive,
     * and its activity alive (not finished / destroyed)
     */
    internal fun getTopLiveActivityRecord(page: ClientPageIntf): ActivityStateRecord? {
        return monitor.getTopLiveActivityRecord(page)
    }

    internal fun getActivityRecords(page: ClientPageIntf): List<ActivityStateRecord> { //THINK sync
        return monitor.getActivityRecords(page)
    }

    private fun startMonitoringActivities(app: Application) {
        try {
            monitor = ActivitiesMonitor(logLifecycle).also {
                app.registerActivityLifecycleCallbacks(it)
            }
        } catch (e: Throwable) {
            //monitor already initialized
            Logger.w(AppForegroundLogic::class, "startMonitoringActivities() was called more than once")
        }
    }
}

@RequiresApi(Build.VERSION_CODES.M)
private fun getTopActivityApi23(context: Context): Activity? {
    (context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager?)
        ?.appTasks?.firstOrNull()?.taskInfo?.topActivity

    return null
}

private fun getTopActivityCompat(context: Context): Activity? {
    (context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager?)
        ?.getRunningTasks(1)?.firstOrNull()?.topActivity //NOCOMMIT QA on LOLLIPOP

    return null
}