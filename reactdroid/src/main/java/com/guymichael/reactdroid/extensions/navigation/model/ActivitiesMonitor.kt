package com.guymichael.reactdroid.extensions.navigation.model

import android.app.Activity
import android.app.Application
import android.content.ComponentName
import android.os.Bundle
import com.guymichael.apromise.APromise
import com.guymichael.kotlinreact.Logger
import com.guymichael.reactdroid.extensions.navigation.ClientPageIntf
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.subjects.PublishSubject

internal class ActivitiesMonitor(internal var logLifecycle: Boolean = false)
        : Application.ActivityLifecycleCallbacks {

    private val activityStateRecords = HashMap<ComponentName, ActivityStateRecord?>()
    private val appForegroundStateSubject = PublishSubject.create<Boolean>()
    private val activitiesStateSubject = PublishSubject.create<Pair<Activity, @ActivityForegroundState Int>>()


    private val loggerDisposable = if (logLifecycle) {
        observeAppForegroundState().subscribe {
            Logger.d(ActivitiesMonitor::class,
                "app is now ${(if (it) "foreground" else "background")}"
            )
        }
    } else null



    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        addActivityRecord(activity, ActivityForegroundState.CREATED)
    }
    
    override fun onActivityStarted(activity: Activity) {
        addActivityRecord(activity, ActivityForegroundState.STARTED)
    }

    override fun onActivityResumed(activity: Activity) {
        addActivityRecord(activity, ActivityForegroundState.RESUMED)
    }

    override fun onActivityPaused(activity: Activity) {
        addActivityRecord(activity, ActivityForegroundState.PAUSED)
    }

    override fun onActivityStopped(activity: Activity) {
        addActivityRecord(activity, ActivityForegroundState.STOPPED)
    }

    override fun onActivityDestroyed(activity: Activity) {
        addActivityRecord(activity, ActivityForegroundState.DESTROYED)
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}





    /* API */

    /** To be run on the ui thread */
    fun bindActivityToClientPage(activity: Activity, page: ClientPageIntf) {
        addActivityRecord(activity, page)
    }

    fun isActivityForeground(activity: Activity): Boolean {
        return isActivityForegroundIntl(activity, allowPaused = true)
    }

    fun isActivityForeground(cls: Class<out Activity>): Boolean {
        return isActivityForegroundIntl(cls, allowPaused = true)
    }

    fun getActivityState(cls: Class<out Activity>): @ActivityForegroundState Int? {
        return getActivityRecord(cls)?.state
    }

    fun getActivityState(activity: Activity): @ActivityForegroundState Int? {
        return getActivityRecord(activity)?.state
    }

    /** Is any activity in foreground */
    fun isAppForeground(): Boolean { //THINK sync
        return activityStateRecords.values.any { record -> record != null &&
            isStateForeground(record.state)
        }
    }

    fun observeAppForegroundState(): Observable<Boolean> {
        return appForegroundStateSubject
    }

    fun observeActivitiesState(): Observable<Pair<Activity, @ActivityForegroundState Int>> {
        return activitiesStateSubject
    }

    fun waitForAppForeground(): APromise<Unit> {
        return if (isAppForeground()) {
            APromise.of()
        } else {
            APromise.ofCallback<Unit, Disposable>({ promiseCallback ->
                observeAppForegroundState()
                    .doOnError { promiseCallback.onFailure(it) }
                    .filter { it == true } //foreground only
                    .subscribe {
                        promiseCallback.onSuccess(Unit)
                    }

            //finally dispose the observer
            }) { d -> d.takeIf { !it.isDisposed }?.dispose() }
        }
    }

    fun waitForAppBackground(): APromise<Unit> {
        return if (isAppForeground()) {
            APromise.ofCallback<Unit, Disposable>({ promiseCallback ->
                observeAppForegroundState()
                    .doOnError { promiseCallback.onFailure(it) }
                    .filter { it == false } //background only
                    .subscribe {
                        promiseCallback.onSuccess(Unit)
                    }

                //finally dispose the observer
            }) { d -> d.takeIf { !it.isDisposed }?.dispose() }

        } else {
            APromise.of()
        }
    }






    /* privates */

    private fun componentNameFor(cls: Class<out Activity>): ComponentName? {
        return cls.`package`?.name?.let{
            ComponentName(it, cls.simpleName)
        }
    }

    private fun isStateForeground(state: @ActivityForegroundState Int): Boolean {
        return when(state) {
            ActivityForegroundState.CREATED, ActivityForegroundState.STARTED
                , ActivityForegroundState.RESUMED, ActivityForegroundState.PAUSED
            -> true

            else -> false
        }
    }

    private fun isActivityResumed(record: ActivityStateRecord, allowPaused: Boolean)
        : Boolean {

        return record.state == ActivityForegroundState.RESUMED
            || (allowPaused && record.state == ActivityForegroundState.PAUSED)
    }

    private fun isActivityForegroundIntl(activity: Activity, allowPaused: Boolean): Boolean {
        return getActivityRecord(activity)?.let {
            isActivityResumed(it, allowPaused)
        } ?: false
    }

    private fun isActivityForegroundIntl(cls: Class<out Activity>, allowPaused: Boolean): Boolean {
        return getActivityRecord(cls)?.let {
            isActivityResumed(it, allowPaused)
        } ?: false
    }

    //THINK cache as local variable for speed
    /**
     * @return topmost [Activity], if not null, guaranteed to be 'alive' - not finishing/destroyed
     */
    internal fun getForegroundRecordIntl(): ActivityStateRecord? {
        return ( //THINK sort
            activityStateRecords.values.firstOrNull { record ->
                record?.state == ActivityForegroundState.RESUMED
                && record.isActivityAlive()
            }

            //if not found, allow created or started
            ?: activityStateRecords.values.firstOrNull { record ->
                (record?.state == ActivityForegroundState.CREATED
                    || record?.state == ActivityForegroundState.STARTED)
                && record.isActivityAlive()
            }

            //if not found, allow paused
            ?: activityStateRecords.values.firstOrNull { record ->
                record?.state == ActivityForegroundState.PAUSED
                && record.isActivityAlive()
            }
        )
    }

    /**
     * @return a record matching the given `page` with a state `CREATED` to `STOPPED`, inclusive,
     * and its activity alive (not finished / destroyed)
     */
    internal fun getTopLiveActivityRecord(page: ClientPageIntf): ActivityStateRecord? { //THINK sync
        return getActivityRecords(page).let { records -> //THINK sort
            records.firstOrNull { record ->
                record.state == ActivityForegroundState.RESUMED
                && record.isActivityAlive()
            }

            //if not found, allow created or started
            ?: records.firstOrNull { record ->
                (record.state == ActivityForegroundState.CREATED
                    || record.state == ActivityForegroundState.STARTED
                )
                && record.isActivityAlive()
            }

            //if not found, allow paused
            ?: records.firstOrNull { record ->
                record.state == ActivityForegroundState.PAUSED
                && record.isActivityAlive()
            }

            //if not found, allow stopped
            ?: records.firstOrNull { record ->
                record.state == ActivityForegroundState.STOPPED
                && record.isActivityAlive()
            }
        }
    }

    /** To be run on the ui thread */
    private fun getActivityRecord(activity: Activity): ActivityStateRecord? {
        return activityStateRecords[activity.componentName] //THINK sync
    }

    /** To be run on the ui thread */
    private fun getActivityRecord(cls: Class<out Activity>) : ActivityStateRecord? { //THINK sync
        return componentNameFor(cls)?.let(activityStateRecords::get)
    }

    /** To be run on the ui thread */
    internal fun getActivityRecords(page: ClientPageIntf
            , predicate: ((ActivityStateRecord) -> Boolean)? = null
        ): List<ActivityStateRecord> { //THINK sync

        @Suppress("UNCHECKED_CAST")
        return activityStateRecords.values.filter {record ->
            record?.page == page && predicate?.invoke(record) != false
        } as List<ActivityStateRecord> //already checked for null but the compiler doesn't know
    }

    /**
     * To be run on the ui thread
     * @param activity will override any previously set record's activity, if exists
     */
    private fun addActivityRecord(activity: Activity, page: ClientPageIntf) { //THINK sync
        activityStateRecords[activity.componentName] = ActivityStateRecord.from(
            //take new reference
            activity
            //copy prev state
            , getActivityState(activity) ?: ActivityForegroundState.NEVER_CREATED
            , page
        )

        /*Logger.w(ActivitiesMonitor::class
            , "${activity.javaClass.simpleName} record initialized"
        )*/
    }

    /**
     * To be run on the ui thread
     * @param activity will override any previously set record's activity, if exists
     */
    private fun addActivityRecord(activity: Activity
            , state: @ActivityForegroundState Int
        ) {

        val wasAppForeground = isAppForeground()

        //add record
        activityStateRecords[activity.componentName] = ActivityStateRecord.from(
            //keep reference only if/while activity is alive
            activity.takeIf {
                state != ActivityForegroundState.DESTROYED
                        && !activity.isDestroyed
                        && !activity.isFinishing
            }
            , state
            //copy prev page
            , getActivityRecord(activity)?.page
        )

        if (logLifecycle) {
            Logger.d(
                ActivitiesMonitor::class.java
                , "${activity.javaClass.simpleName} state: "
                    + nameFor(state)
            )
        }

        //update app foreground state
        val isAppForeground = isAppForeground()
        if (isAppForeground != wasAppForeground) {
            appForegroundStateSubject.onNext(isAppForeground)
        }

        //notify state observers
        activitiesStateSubject.onNext(activity to state)
    }
}