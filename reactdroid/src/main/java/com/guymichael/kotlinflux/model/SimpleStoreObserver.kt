package com.guymichael.kotlinflux.model

import com.guymichael.reactdroid.model.OwnProps
import io.reactivex.rxjava3.disposables.Disposable

class SimpleStoreObserver<P : OwnProps>(
        private val mapStateToProps: (GlobalState) -> P
        , private val onChange: (P) -> Unit
    ) : StoreObserver<P> {

    override var storeDisposable: Disposable? = null

    override fun mapStateToProps(state: GlobalState) = mapStateToProps.invoke(state)
    override fun onStoreStateChanged(nextProps: P) = onChange.invoke(nextProps)
}