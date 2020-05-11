package com.guymichael.reactdroid.core

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.ViewGroup
import androidx.annotation.DrawableRes

open class Utils {
    companion object {
        private val mainHandler by lazy {
            Handler(Looper.getMainLooper()) //uses synchronized! //THINK GC
        }

        private fun getMainLooper(): Looper {
            return mainHandler.looper
        }

        fun runOnUiThread(delay: Long = 0, runnable: () -> Unit) {
            if (delay <= 0L && isOnUiThread()) {
                runnable()
            } else {
                mainHandler.postDelayed(runnable, delay)
            }
        }

        /**
         * @param version as in "android.os.Build.VERSION_CODES"
         * @return True if the current API version is >= 'version'
         */
        fun isNewAPI(version: Int) = Build.VERSION.SDK_INT >= version

        @SuppressLint("NewApi")
        fun getDrawable(context: Context, @DrawableRes resId: Int, clone: Boolean = false): Drawable? {
            if (resId == 0) {
                return null
            }

            val d: Drawable? = if (isNewAPI(Build.VERSION_CODES.LOLLIPOP)) {
                context.getDrawable(resId)
            } else {
                @Suppress("DEPRECATION")
                context.resources.getDrawable(resId)
            }

            return if (d != null && clone) {
                    d.constantState?.newDrawable()?.mutate() ?: d
                } else { d }
        }

        /**
         * For cases when 'cont' can be also [ContextWrapper] and not an Activity.
         * @param context
         * @return
         */
        fun getActivity(context: Context): Activity? {
            return when (context) {
                is Activity -> context
                is ContextWrapper -> context.baseContext?.let(Companion::getActivity)
                else -> null
            }
        }

        fun getActivity(view: View): Activity? {
            return view.context?.let(Companion::getActivity)
        }

        fun <T> getActivity(context: Context, ifOfClass: Class<T>) : T? {
            @Suppress("UNCHECKED_CAST")
            return getActivity(context)?.takeIf { ifOfClass.isInstance(it) }?.let { it as? T }
        }

        fun getActivityView(activity: Activity): ViewGroup? {
            return (activity.window?.decorView?.findViewById<View>(android.R.id.content) as? ViewGroup?)?.getChildAt(0) as? ViewGroup?
                // THINK use getRootView() instead (or in addition, as a fallback) to getChildAt(0)
                // THINK use findViewById() directly (or in addition, as a fallback), instead of window.decorView
        }

        fun getActivityView(activityContext: Context): ViewGroup? {
            return getActivity(
                activityContext
            )?.let(Companion::getActivityView)
        }

        fun getActivityView(childView: View): ViewGroup? {
            return getActivity(
                childView
            )?.let(Companion::getActivityView)
        }

        @SuppressLint("NewApi")
        fun isOnUiThread(): Boolean {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                getMainLooper().isCurrentThread
            } else {
                Looper.myLooper() == getMainLooper()//also for null myLooper
            }
        }

        /*fun <T> mainThreadPublishSubject(observer: (T) -> Unit): Pair<Observable<T>, Disposable> {
            val publisher = PublishSubject.create<T>()
            val observable = publisher.observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(AndroidSchedulers.mainThread())
                    .share() //auto dispose on no subscribers

            return Pair(observable, observable.subscribe(observer))
        }*/
    }
}