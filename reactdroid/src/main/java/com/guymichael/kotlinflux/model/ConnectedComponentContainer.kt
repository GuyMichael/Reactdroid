package com.guymichael.kotlinflux.model

import com.guymichael.reactdroid.Logger
import com.guymichael.reactdroid.model.OwnProps
import io.reactivex.rxjava3.core.Observer
import io.reactivex.rxjava3.disposables.Disposable
import java.lang.ref.WeakReference

class ConnectedComponentContainer<MERGED_PROPS : OwnProps>(
        component: ConnectedHOC<*, MERGED_PROPS, *>
)
    : Observer<MERGED_PROPS> {

    private val componentRef : WeakReference<ConnectedHOC<*, MERGED_PROPS, *>> = WeakReference(component)

    override fun onSubscribe(d: Disposable) {
        componentRef.get()?.apply {
            storeDisposable?.takeIf { !it.isDisposed }?.dispose()//in any case of re-subscribes (shouldn't happen)
            storeDisposable = d
        }
    }

    override fun onComplete() {}
    override fun onError(t: Throwable) {
        Logger.e(javaClass, "onError() ${t.message}")
    }

    internal fun shouldComponentReceiveStateChanges(): Boolean {
//        Logger(ConnectedComponentContainer::class.java).w("shouldComponentReceiveStateChanges(${this.componentRef.get()?.javaClass?.simpleName}) - ${this.componentRef.get()?.shouldComponentReceiveStateChanges() == true}")
        return this.componentRef.get()?.shouldComponentReceiveStateChanges() == true
    }

    internal fun mapStateToProps(state: GlobalState): MERGED_PROPS? {
//        Logger(ConnectedComponentContainer::class.java).w("mapStateToProps(${this.componentRef.get()?.javaClass?.simpleName}) - ${this.componentRef.get()?.run { mapStateToProps(state, apiProps)}}")
        return try {
            this.componentRef.get()?.mapStateToProps(state)
        } catch (e: UninitializedPropertyAccessException) {
            null//should be prevented by shouldComponentReceiveStateChanges(), but just in case (can happen before first render)
        }
    }

    internal fun shouldComponentUpdate(nextProps: MERGED_PROPS): Boolean {
//        Logger(ConnectedComponentContainer::class.java).w("shouldComponentUpdate(${this.componentRef.get()?.javaClass?.simpleName})")

        //the reason we prefer doing it here instead of just letting the component itself do it,
        //is mainly for thread reasons - we're currently off-main-thread, which is very important for speed
        return this.componentRef.get()?.shouldInnerComponentUpdate(nextProps) == true
    }

    override fun onNext(nextProps: MERGED_PROPS) {
//        Logger(ConnectedComponentContainer::class.java).w("onNext(${this.componentRef.get()?.javaClass?.simpleName}) - $nextProps")
        //global state changed and shouldComponentUpdate returned true, re-render
        componentRef.get()?.UNSAFE_onControlledRender(nextProps)
    }
}