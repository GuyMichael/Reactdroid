package com.guymichael.kotlinflux.model

import com.guymichael.kotlinreact.Logger
import com.guymichael.kotlinreact.model.OwnProps
import io.reactivex.rxjava3.core.Observer
import io.reactivex.rxjava3.disposables.Disposable
import java.lang.ref.WeakReference

class StoreObserverContainer<P : OwnProps>(
        component: StoreObserver<P>
)
    : Observer<P> {

    private val mRef : WeakReference<StoreObserver<P>> = WeakReference(component)
    private var prevProps: P? = null

    override fun onSubscribe(d: Disposable) {
        mRef.get()?.apply {
            storeDisposable?.takeIf { !it.isDisposed }?.dispose()//in any case of re-subscribes (shouldn't happen)
            storeDisposable = d
        }
    }
    override fun onComplete() {}

    override fun onError(t: Throwable) {
        Logger.e(javaClass, "onError() ${t.message}")
    }

    internal fun shouldReceiveStateChanges(): Boolean {
//        Logger(ConnectedComponentContainer::class.java).w("shouldComponentReceiveStateChanges(${this.mRef.get()?.javaClass?.simpleName}) - ${this.mRef.get()?.shouldComponentReceiveStateChanges() == true}")
        return this.mRef.get()?.shouldReceiveStateChanges() == true
    }

    internal fun mapStateToProps(state: GlobalState): P? {
//        Logger(ConnectedComponentContainer::class.java).w("mapStateToProps(${this.mRef.get()?.javaClass?.simpleName}) - ${this.mRef.get()?.run { mapStateToProps(state, apiProps)}}")
        return try {
            this.mRef.get()?.mapStateToProps(state)
        } catch (e: UninitializedPropertyAccessException) {
            null//should be prevented by shouldComponentReceiveStateChanges(), but just in case (can happen before first render)
        }
    }

    internal fun shouldUpdate(nextProps: P): Boolean {
//        Logger(ConnectedComponentContainer::class.java).w("shouldComponentUpdate(${this.mRef.get()?.javaClass?.simpleName})")

        //the reason we prefer doing it here instead of just letting the component itself do it,
        //is mainly for thread reasons - we're currently off-main-thread, which is very important for speed
        return prevProps?.let {
            this.mRef.get()?.shouldUpdate(it, nextProps) == true
        } ?: true //if prevProps are null
    }

    override fun onNext(nextProps: P) {
//        Logger(ConnectedComponentContainer::class.java).w("onNext(${this.mRef.get()?.javaClass?.simpleName}) - $nextProps")
        //global state changed and shouldComponentUpdate returned true, re-render
        prevProps = nextProps

        mRef.get()?.onStoreStateChanged(nextProps)
    }
}