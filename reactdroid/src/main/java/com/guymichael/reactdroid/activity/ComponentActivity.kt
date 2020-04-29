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
 * A reactdroid base-class for an `Activity` ([AppCompatActivity]), serving as a [Component].
 * It is encouraged to use it for your `Activity`s base-class, especially for it to be treated
 * as a component from outside (delivering `props` to open, instead of the Android way of Intents/extras).
 *
 * **Best practice** is **not** to use **any** `Activity` as the actual UI component, but use a single
 * `AComponent` as the top "page" component, and use an `Activity` just to wrap it for Android-related
 * usages (e.g. opening an `Activity`...).
 *
 * **Note** that while this `Activity`'s lifecycle is converted to a reactdroid lifecycle just like any
 * other `AComponent`, this is in fact a [Component] (`AComponent`'s pure kotlin interface),
 * so it is best to avoid using it as a component or for UI rendering at all.
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




    /* API */

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
        //already mounted (Activity after (see) onPostCreate).
        // onDestroy will call back this consumer for un-mounts.
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
    final override fun UNSAFE_componentDidMountHint() {
        super.UNSAFE_componentDidMountHint()
    }
    final override fun UNSAFE_componentDidUpdateHint(prevProps: P, prevState: EmptyOwnState, snapshot: Any?) {
        super.UNSAFE_componentDidUpdateHint(prevProps, prevState, snapshot)
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