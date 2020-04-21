package com.guymichael.reactdroid.activity

import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import com.guymichael.reactdroid.Utils
import com.guymichael.kotlinreact.BuildConfig
import com.guymichael.kotlinreact.Logger
import com.guymichael.reactdroid.activity.ComponentActivity.Companion.INTENT_KEY_API_PROPS
import com.guymichael.kotlinreact.model.Component
import com.guymichael.kotlinreact.model.EmptyOwnState
import com.guymichael.kotlinreact.model.OwnProps

/**
 * An Activity which is also a Component, which mainly helps in two ways:
 * 1. Converts original lifecycle to Component-like lifecycle
 * 2. As an extension to #1, converts [Intent] to [OwnProps] and handles the `Intent` flow
 * from both `onCreate()` and `onNewIntent()`
 *
 * note: default implementation of [mapIntentToProps] simply casts an extra with the key [INTENT_KEY_API_PROPS]
 * to the props type (`P`). If that is not the desired behavior, override [mapIntentToProps]
 *
 * @param P
 */
abstract class ComponentActivity<P : OwnProps> : AppCompatActivity(), Component<P, EmptyOwnState> {

    /** **DO NOT set yourself.** */
    final override lateinit var ownState: EmptyOwnState
//        private set THINK

    /** **DO NOT set yourself.** */
    final override lateinit var props: P
//        private set THINK

    final override val forceReRenderOnRemount: Boolean = true

    /** **DO NOT set yourself.** */
    final override var reRenderOnRemountDueToNewProps: Boolean = false
//        private set THINK




    /** @return apiProps from Intent, or null if Intent doesn't have enough information to create props.
     * If null is returned, the activity will not remain open, and close immediately */
    protected open fun mapIntentToProps(intent: Intent): P? {
        return (intent.getSerializableExtra(INTENT_KEY_API_PROPS) as? P?)
    }

    /**
     * Notifies you that a new `Intent` has been receieved, either by [onCreate] or [onNewIntent].
     * Default implementation is a no-op so no need to call super
     */
    protected open fun onIntentChanged(newIntent: Intent) {}

    /** @return a layout resource id. If you prefer to manually inflate a layout, override [inflateLayout],
     * in which case you may return 0 here */
    @LayoutRes
    protected abstract fun getLayoutRes(): Int
    protected open fun inflateLayout() { setContentView(getLayoutRes()) }
    protected abstract fun onBindViews(activityView: ViewGroup)
    protected abstract fun onBindViewListeners()




        //**** Android hooks *****/

    final override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        intent?.let(::mapIntentToProps) ?: run {
            //no apiProps -> we can't continue
            Logger.e(this::class, "missing apiProps in Intent")
            finish()
            return
        }

        notifyComponentWillMount()

        inflateLayout()

        //TODO QA for not null
        Utils.getActivityView(this)?.also {
            onBindViews(it)
            onBindViewListeners()

        } ?: run {
            if (BuildConfig.DEBUG) {
                throw IllegalStateException("ComponentActivity failed to get activity top view after inflateLayout() has been called")
            } else {
                Logger.e(this::class, "ComponentActivity failed to get activity top view after inflateLayout() has been called")
            }
        }
    }

    final override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)

        //THINK move to onCreate to avoid calling mapIntentToProps() twice
        //first render. As Activities don't have parents, we have to force the first render ourselves
        intent?.let { newIntent ->
            newIntent.let(::mapIntentToProps)?.also {
                onRender(it)
                onIntentChanged(newIntent)

            } ?: finish() //no apiProps -> we can't continue
        }
    }

    final override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)

        intent?.let { newIntent ->
            newIntent.let(::mapIntentToProps)?.also {
                onRender(it)
                onIntentChanged(newIntent)

            } ?: finish() //no apiProps -> we can't continue
        }
    }

    final override fun onDestroy() {
        waitForMountStateChangeConsumer?.invoke(false)
        super.onDestroy()
    }





        //*** empty (final) android lifecycle callbacks ***/

    final override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState, persistentState)
    }

    final override fun onStart() {
        super.onStart()
    }

    final override fun onResume() {
        super.onResume()
    }

    final override fun onPause() {
        super.onPause()
    }

    final override fun onStop() {
        super.onStop()
    }

    final override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
    }





        //*** react hooks ***/

    final override fun isMounted(): Boolean {
        return !this.isDestroyed && !this.isFinishing && Utils.getActivityView(this) != null
    }

    private var waitForMountStateChangeConsumer: ((Boolean) -> Unit)? = null
    final override fun listenOnMountStateChanges(consumer: (Boolean) -> Unit) {
        waitForMountStateChangeConsumer = consumer
        //already mounted (Activity). onDestroy will call back this consumer for un-mounts.
        //note: we don't have to call on mounts, because this is an Activity:
        //      In short, Activities can't *re*mount, only recreate from scratch, and the first render is always on a mounted Activity.

        //      As an Activity, it calls the first onRender by itself (onPostCreate), and as an Activity, it will
        //      always be mounted at this point (setContentView inside onCreate).
        //      Also, beside the fact that it will always be mounted during the first render,
        //      Activities can't re-mount - if they get destroyed (unmount), they have to be recreated from scratch to be shown again,
        //      meaning their props, state and everything gets re-created.
        //      So -> this callback will never be called for the first mount (already mounted on first render),
        //            and can never be called for re-mounts (there are none for an Activity). So no need to call it back for mounts, only unmounts
    }

    //make final and better logic
    final override fun isPropsInitialized(): Boolean {
        return this::props.isInitialized
    }

    //make final and better logic
    final override fun isStateInitialized(): Boolean {
        return this::ownState.isInitialized
    }

    //make final and implement
    final override fun createInitialState(props: P) = EmptyOwnState

    //make final
    final override fun setState(nextState: EmptyOwnState) {
        super.setState(nextState)
    }
    final override fun updateProps(nextProps: P) {
        super.updateProps(nextProps)
    }
    final override fun notifyComponentWillMount() {
        super.notifyComponentWillMount()
    }
    final override fun UNSAFE_componentWillUnmountHint() {
        super.UNSAFE_componentWillUnmountHint()
    }
    final override fun onRender(nextProps: P) {
        super.onRender(nextProps)
    }
    final override fun UNSAFE_forceRender(nextProps: P) {
        super.UNSAFE_forceRender(nextProps)
    }







    companion object {
        const val INTENT_KEY_API_PROPS = "apiProps"
    }
}