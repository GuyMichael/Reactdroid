package com.guymichael.kotlinflux.extensions.network.model

import com.guymichael.promise.Promise
import com.guymichael.kotlinreact.Logger
import io.reactivex.rxjava3.disposables.Disposable

/**
 * Contains simple [Promise] chaining to handle api response in a predefined order
 */
interface StoreAPIController {

    companion object {
        fun <API_RESPONSE, P : Promise<API_RESPONSE>> withStoreDispatch(
            call: P
            , dispatch: (API_RESPONSE) -> Unit
            , persistSideEffects: ((API_RESPONSE) -> Unit) = {}
            , catch: (Throwable) -> Unit
        ): P {

            @Suppress("UNCHECKED_CAST")
            return call
                .then(persistSideEffects)
                .then(dispatch) //dispatch after persisting so that the persist will reflect the changes
                // when store listeners receive the update
                .catch { catch(it) }
                as P
        }
    }

    /** See [withStoreDispatch] for docs */
    fun <API_RESPONSE, P : Promise<API_RESPONSE>> prepare(
            call: P
            , dispatch: (API_RESPONSE) -> Unit
            , persistSideEffects: ((API_RESPONSE) -> Unit) = {}
            , logErrors: Boolean = true
        ): P {

        return withStoreDispatch(call, dispatch, persistSideEffects) {
            if (logErrors) { logError(it) }
        }
    }

    /** for tests */
    fun <API_RESPONSE> inject(
            manualResponse: API_RESPONSE
            , dispatch: (API_RESPONSE) -> Unit
            , persistSideEffects: ((API_RESPONSE) -> Unit) = {}
            , logErrors: Boolean = true
        ): Disposable {

        return prepare(
            Promise.of { manualResponse }
            , dispatch
            , persistSideEffects
            , logErrors
        ).execute()
    }

    fun logError(e: Throwable) {
        Logger.e(this::class, "onApiError: ${e.message}")
        e.printStackTrace()
    }
}