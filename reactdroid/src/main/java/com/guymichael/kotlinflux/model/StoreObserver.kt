package com.guymichael.kotlinflux.model

import com.guymichael.reactdroid.Utils
import com.guymichael.reactdroid.model.OwnProps
import io.reactivex.rxjava3.disposables.Disposable


interface StoreObserver<P: OwnProps> {

    /** **CONTRACT:**
     * 1. DO NOT set yourself! Never!
     * 2. Just override and init. with null
     * */
    var storeDisposable: Disposable?

    /**
     * Pure and without any side effects or outside params THINK enforce
     *
     * This is where we connect to the global state (props-wise)
     *
     * Note: This is where we merge all 3 streams: (state, apiProps) -> componentProps
     *       this method will be called from two flows:
     *       1. api props changes (from parent Component)
     *       2. (global) state changes
     */
    fun mapStateToProps(state: GlobalState): P

    /** @return true if the given props-change justifies a call to [onStoreStateChanged].
     * Default behavior is [props (in)equality][OwnProps.equals] */
    fun shouldUpdate(prevProps: P, nextProps: P): Boolean {
        return !Utils.shallowEquality(prevProps, nextProps)
    }

    /** A break-point supplier. Once `true` is returned, this observer will not get ANY more updates,
     * similar to [Store.unsubscribe]
     * Default behavior returns constant `true`, as on normal cases, [Store.unsubscribe] should normally be used
     * to stop receive changes*/
    fun shouldReceiveStateChanges(): Boolean = true

    /** Called when the state changes, provided that [shouldReceiveStateChanges] and [shouldUpdate]
     * both returned `true` */
    fun onStoreStateChanged(nextProps: P)
}