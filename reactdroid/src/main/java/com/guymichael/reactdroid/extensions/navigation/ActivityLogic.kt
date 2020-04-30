package com.guymichael.reactdroid.extensions.navigation

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.core.app.ActivityOptionsCompat
import androidx.core.util.Pair
import com.guymichael.apromise.APromise
import com.guymichael.kotlinreact.model.OwnProps
import com.guymichael.reactdroid.core.activity.ComponentActivity
import com.guymichael.reactdroid.core.post
import java.io.Serializable

/** A logic/helper class for opening an [Activity], or better yet, a [ComponentActivity] */
object ActivityLogic {

    /**
     * Opens an [Activity], notifying when it's ready ('resumed').
     * If *context* is of same type as *cls*, the desired activity is already opened
     * and it's [Activity.onNewIntent] will be called (as well as the promise's resolve,
     * at end of execution queue)
     * @param context
     * @param cls
     * @param intentExtras
     */
    @JvmStatic
    fun <T : Activity> openActivity(
            context: Activity
            , cls: Class<T>
            , intentExtras: Bundle? = null
            , intentFlags: Int? = null
            , inOutAnimations: Pair<Int?, Int?>? = null
            , transitions: Array<Pair<View, String>>? = null
            , forResult_requestCode: Int? = null
        ) : APromise<T> {

        //transitions - append to intentExtras bundle or create new one if null
        val bundleExtras = intentExtras.and ( transitions?.let {
            ActivityOptionsCompat.makeSceneTransitionAnimation(context, *it).toBundle()
        })

        //init the Intent
        val intent = Intent(context, cls).also { i ->
            bundleExtras?.let(i::putExtras)
            intentFlags?.let(i::setFlags)
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }

        //prepare the Promise
        val promise = prepareActivityResumePromise(context, cls)

        //open activity.
        // If already open, activity.onNewIntent will be called
        // thanks to previously set flag FLAG_ACTIVITY_REORDER_TO_FRONT
        if (forResult_requestCode == null) {
            context.startActivity(intent)
        } else {
            context.startActivityForResult(intent, forResult_requestCode)
        }

        //animations
        inOutAnimations?.let { anims ->
            context.overridePendingTransition(anims.first?:0, anims.second?:0)
        }

        return promise
    }

    @JvmStatic
    fun <OWN_PROPS : OwnProps, T : ComponentActivity<OWN_PROPS>>
    openActivity(context: Activity
            , cls: Class<T>
            , props: OWN_PROPS
            , animations: Pair<Int?, Int?>? = null
            , transitions: Array<Pair<View, String>>? = null
            , forResult_requestCode: Int? = null
            , intentFlags: Int? = null
        ) : APromise<T> {


        //create Bundle from props
        val bundle = Bundle().also {
            it.putSerializable(ComponentActivity.INTENT_KEY_API_PROPS, props as Serializable)
        }

        return openActivity(context, cls, bundle, intentFlags, animations, transitions, forResult_requestCode)
    }
}








private fun <T : Activity> prepareActivityResumePromise(
        currentlyOpenedActivity: Activity, cls: Class<T>
    ) : APromise<T> {

    //decide how/when to notify promise
    return if (currentlyOpenedActivity.javaClass == cls) {
        APromise.post(currentlyOpenedActivity as T)
    } else {
        //wait
        ActivityUtils.waitForResume(currentlyOpenedActivity.application, cls)
    }
}



private fun Bundle.with(other: Bundle): Bundle {
    return Bundle(this).apply { putAll(other) }
}

private fun Bundle.withNotNull(other: Bundle?): Bundle {
    return other?.let(::with) ?: this
}

private infix fun Bundle?.and(other: Bundle?): Bundle? {
    return this?.withNotNull(other) ?: other
}