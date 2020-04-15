package com.guymichael.kotlinflux.extensions.network.model

import com.guymichael.promise.Promise
import com.guymichael.kotlinreact.Logger
import io.reactivex.rxjava3.disposables.Disposable


interface StoreAPI<API_RESPONSE> {

    fun persistSideEffects(response: API_RESPONSE) {}
    fun dispatch(response: API_RESPONSE)



    fun <P : Promise<API_RESPONSE>> prepare(call: P): P {
        return call
            .then(::persistSideEffects)
            .then(::dispatch)
            .catch(::onApiError)
        as P //THINK better way?
    }

    fun onApiError(e: Throwable) {
        Logger.e(this::class, "onApiError: ${e.message}")
        e.printStackTrace()
    }

    fun inject(manualResponse: API_RESPONSE): Disposable {
        return prepare(Promise.of { manualResponse }).execute()
    }
}