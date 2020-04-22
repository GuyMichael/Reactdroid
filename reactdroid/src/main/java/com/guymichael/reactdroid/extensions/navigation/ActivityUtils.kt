package com.guymichael.reactdroid.extensions.navigation

import android.app.Activity
import android.app.Application
import android.app.Application.ActivityLifecycleCallbacks
import android.os.Bundle
import com.guymichael.apromise.APromise
import java.lang.ref.WeakReference

object ActivityUtils {

    fun <A : Activity> waitForResume(application: Application, cls: Class<A>): APromise<A> {
        val appRef = WeakReference(application)

        return APromise.ofWeakRefOrCancel(appRef)
            .thenAwait { app ->
                APromise.ofCallback<A, OnActivityResumedListener<A>>({ promiseCallback ->
                    //create an Android type callback
                    (object: OnActivityResumedListener<A>(cls) {
                        override fun onResumed(activity: A) {
                            //release callback now, as cancel() takes time
                            activity.application?.unregisterActivityLifecycleCallbacks(this)
                            promiseCallback.onSuccess(activity)
                        }

                    //register the callback
                    }).also { app.registerActivityLifecycleCallbacks(it) }

                    //unregister (on errors or cancel, normally)
                }) {
                    appRef.get()?.unregisterActivityLifecycleCallbacks(it)
                }
            }
    }
}










private abstract class OnActivityResumedListener<A: Activity>(private val cls: Class<A>)
    : ActivityLifecycleCallbacks {

    final override fun onActivityPaused(activity: Activity?) {}
    final override fun onActivityResumed(activity: Activity?) {
        if (activity?.javaClass == cls) {
            onResumed(activity as A) //THINK
        }
    }
    final override fun onActivityStarted(activity: Activity?) {}
    final override fun onActivitySaveInstanceState(activity: Activity?, outState: Bundle?) {}
    final override fun onActivityStopped(activity: Activity?) {}
    final override fun onActivityCreated(activity: Activity?, savedInstanceState: Bundle?) {}
    final override fun onActivityDestroyed(activity: Activity?) {}

    abstract fun onResumed(activity: A)
}