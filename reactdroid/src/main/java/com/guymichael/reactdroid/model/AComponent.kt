package com.guymichael.reactdroid.model

import android.view.View
import com.guymichael.reactdroid.ViewUtils
import com.guymichael.reactdroid.model.android.DebouncedClickListener
import com.guymichael.kotlinreact.BuildConfig
import com.guymichael.kotlinreact.model.Component
import com.guymichael.kotlinreact.model.EmptyOwnState
import com.guymichael.kotlinreact.model.OwnProps
import com.guymichael.kotlinreact.model.OwnState

/**
 * Android [Component], to be bound to an Android [View]
 *
 * NOTICE: currently componentWillUnmount() is called AFTER the View had been detached from window (and not before) */
abstract class AComponent<P : OwnProps, S : OwnState, V : View>(
        val mView: V
        , final override val forceReRenderOnRemount: Boolean = false
    ) : Component<P, S> {

    final override lateinit var ownState: S
    final override lateinit var props: P

    /** **DO NOT set yourself.** */
    final override var reRenderOnRemountDueToNewProps: Boolean = false
//        private set THINK

    init {
        super.notifyComponentWillMount()//THINK it's logical, as we already have an inflated view. But is there a better place?
    }


    /* APIs */

    /**
     * @param debounceGuardMs if > 0, re-clicks faster than [debounceGuardMs] will be ignored
     * @param passClicksWhenDisabled if false and the View is disabled or not-clickable, clicks won't pass
     * (normal Android behavior is to pass clicks even when disabled or isClickable is false)
     * @param listener listener
     */
    @JvmOverloads
    fun setOnClickListener(debounceGuardMs: Int? = null
           , passClicksWhenDisabled: Boolean = false
           , listener: ((V) -> Unit)?
        ) {

        if (listener == null) {
            mView.setOnClickListener(null)
        } else {
            val listenerOrWrapper = if (passClicksWhenDisabled) listener else { v ->
                if (v.isEnabled && v.isClickable) {
                    listener(v)
                }
            }

            debounceGuardMs?.takeIf { it > 0 }?.also { debounce ->
                mView.setOnClickListener(object : DebouncedClickListener(debounce) {
                    override fun onClicked(v: View) {
                        listenerOrWrapper(v as V)
                    }
                })
            } ?: mView.setOnClickListener { listenerOrWrapper(it as V) }
        }
    }

    /**
     * @param debounceMs if > 0, re-clicks faster than [debounceMs] will be ignored
     */
    @JvmOverloads
    fun setOnDebouncedClickListener(debounceMs: Int = 100
            , passClicksWhenDisabled: Boolean = false
            , listener: ((V) -> Unit)
        ) {

        setOnClickListener(debounceMs, passClicksWhenDisabled, listener)
    }

    fun clearClickListener() {
        mView.setOnClickListener(null)
    }

    fun setVisibility(viewVisibility: Int) {
        if (mView.visibility != viewVisibility) {
            mView.visibility = viewVisibility
        }
    }



    /* final overrides (Component)*/

    //make final and better logic
    final override fun isPropsInitialized(): Boolean {
        return this::props.isInitialized
    }

    //make final and better logic
    final override fun isStateInitialized(): Boolean {
        return this::ownState.isInitialized
    }

    //make final and better logic
    final override fun notifyComponentWillMount() {
        if (isPassedOrDuringFirstRender()) {
            //remount call from Component. Will be removed once Component handles the first willMount as well
           super.notifyComponentWillMount()
        } else {
            check(!BuildConfig.DEBUG) { "${javaClass.simpleName} - notifyComponentWillMount is private" }
        }
    }

    //make final and implement
    /** NOTICE: override at your own risk */
    override fun isMounted(): Boolean = ViewUtils.isMounted(mView)
    /** NOTICE: override at your own risk */
    override fun listenOnMountStateChanges(consumer: (Boolean) -> Unit) {
        ViewUtils.listenOnMountStateChanges(mView, consumer)
    }

    //make final
    final override fun onRender(nextProps: P) {
        super.onRender(nextProps)
    }
    final override fun onRenderOrThrow(nextProps: OwnProps) {
        super.onRenderOrThrow(nextProps)
    }
    final override fun UNSAFE_forceRender(nextProps: P) {
        super.UNSAFE_forceRender(nextProps)
    }
}

/**
 * Android [Component], to be bound to an Android [View]
 *
 * NOTICE: currently componentWillUnmount() is called AFTER the View had been detached from window (and not before) */
abstract class AViewComponent<P : OwnProps, S : OwnState>(v: View) : AComponent<P, S, View>(v)

/**
 * Android [Component], to be bound to an Android [View]
 *
 * NOTICE: currently componentWillUnmount() is called AFTER the View had been detached from window (and not before) */
abstract class ASimpleComponent<P : OwnProps>(v: View) : AComponent<P, EmptyOwnState, View>(v) {
    override fun createInitialState(props: P) = EmptyOwnState
}

/**
 * Android [Component], to be bound to an Android [View]
 *
 * NOTICE: currently componentWillUnmount() is called AFTER the View had been detached from window (and not before) */
abstract class ASimpleViewComponent<P : OwnProps>(v: View) : AComponent<P, EmptyOwnState, View>(v) {
    override fun createInitialState(props: P) = EmptyOwnState
}