package com.guymichael.kotlinflux.extensions.data.model

import com.guymichael.apromise.APromise
import com.guymichael.kotlinflux.extensions.network.model.StoreAPIController
import com.guymichael.kotlinflux.model.GlobalState
import com.guymichael.kotlinflux.model.actions.DataAction
import com.guymichael.promise.Promise

/**
 * A binding point between [StoreAPIController] and [StoreDataType], to easily connect APIs
 * to relevant data schemas
 *
 * Contains simple [Promise] chaining to handle api response (dispatching and persisting)
 * in a predefined manner.
 *
 * Also, some dispatch logic is added to add 'loading' (boolean) and loading-error (`String`)
 * states to DataReducers in a given StoreDataTypes set, along with the logic of dispatching
 * the actual data to the given DataReducers
 */
abstract class StoreDataAPIController : StoreAPIController {

    companion object {
        /**
         * Chains the given API `call` with dispatch and persist management logic, as per given `dataTypes` to
         * handle the actual logic (of persisting and dispatching to a Store).
         *
         * @param call The API call presented as a [Promise]
         * @param dataTypesToDispatchers to process the response - dispatch and persist.
         * Each pair is a [StoreDataType] and a consumer to actually dispatch the relevant data-piece to Store,
         * e.g.`type.getStore().dispatch(DataAction.setDataLoaded)
         * @param persistSideEffects with persisting the data (types) done for you,
         * here you can add any extra persist logic, such as some flag that came with the response
         * @param dispatchSideEffects with dispatching the data (types) done for you,
         * here you can add any extra dispatch logic, such as some flag that came with the response
         * @param catch errors, optional. No advantage over using standard [APromise.catch].
         *
         * Also see the simpler single-dataType method
         */
        @JvmStatic
        fun <API_RESPONSE : Any, P : Promise<API_RESPONSE>> withDataDispatch(
            call: P
            , dataTypesToDispatchers: List<Pair<StoreDataType<*>, (API_RESPONSE) -> Unit>>
            , persistSideEffects: ((API_RESPONSE) -> Unit) = {}
            , dispatchSideEffects: ((API_RESPONSE) -> Unit) = {}
            , catch: ((Throwable) -> Unit)? = null
        ): P {

            @Suppress("UNCHECKED_CAST")
            return StoreAPIController.withStoreDispatch(
                //on api execution - dispatch data loading state
                call.doOnExecution {
                    dataTypesToDispatchers.forEach { (type, _) ->
                        type.getStore().dispatch(DataAction.setDataLoading(type))
                    }
                } as P

                //on response - dispatch the data and side effects
                , { response ->
                    //dispatch data to Store
                    dataTypesToDispatchers.forEach { (_, dispatcher) ->
                        dispatcher.invoke(response)
                    }

                    //dispatch side-effects to Store
                    dispatchSideEffects.invoke(response)
                }

                //side effects
                , persistSideEffects

                //catch & dispatch
                , { e ->
                    dataTypesToDispatchers.forEach { (type, _) ->
                        type.getStore().dispatch(DataAction.setDataLoadingError(type, e))
                    }

                    catch?.invoke(e)
                }
            )
        }
    }

    /**
     * See [withDataDispatch] for docs
     *
     * Also see the simpler single-dataType method
     */
    fun <API_RESPONSE : Any, P : Promise<API_RESPONSE>> prepare(
            call: P
            , dataTypes: List<Pair<StoreDataType<*>, (API_RESPONSE) -> Unit>>
            , persistSideEffects: ((API_RESPONSE) -> Unit) = {}
            , dispatchSideEffects: ((API_RESPONSE) -> Unit) = {}
            , logErrors: Boolean = true
        ): P {

        return withDataDispatch(call, dataTypes, persistSideEffects, dispatchSideEffects) {
            if (logErrors) { logError(it) }
            onApiError(it, dataTypes)
        }
    }

    /** see multi-dataTypes method for docs */
    fun <API_RESPONSE : Any, P : Promise<API_RESPONSE>, D : Any, TYPE : StoreDataType<D>>
    prepare(
        call: P
        , dataType: TYPE
        , dispatchDataLoaded: (API_RESPONSE, TYPE) -> Unit
        , persistSideEffects: ((API_RESPONSE) -> Unit) = {}
        , dispatchSideEffects: ((API_RESPONSE) -> Unit) = {}
        , logErrors: Boolean = true
    ): P {
        return prepare(call
            , listOf(dataType to { res -> dispatchDataLoaded.invoke(res, dataType) })
            , persistSideEffects, dispatchSideEffects, logErrors
        )
    }

    /** see multi-dataTypes method for docs */
    fun <API_RESPONSE : Any, P : Promise<API_RESPONSE>, D : Any, TYPE : StoreDataTypeSingleModel<D>>
    prepare(
        call: P
        , dataType: TYPE
        , mapResponseToData: (API_RESPONSE, TYPE) -> D?
        , persistSideEffects: ((API_RESPONSE) -> Unit) = {}
        , dispatchSideEffects: ((API_RESPONSE) -> Unit) = {}
        , logErrors: Boolean = true
    ): P {
        return prepare(call
            , listOf(dataType to { res -> mapResponseToData(res, dataType)?.let(::listOf) })
            , persistSideEffects, dispatchSideEffects, logErrors
        )
    }





    /**
     * A convenience method. Loads relevant records from db if missing in Store (cache).
     * If db table is empty as well, it uses a provided API: `fetch` and then
     * dispatches[StoreDataType.dispatchLoaded] to store using given `dataType`
     *
     * @param dataType used for the 3 steps: check if exists in Store (cache), load from db
     * and dispatch to store (whether from db or from `fetch`)
     * @param fetch APromise that fetches relevant data
     */
    fun <DATA_MODEL : Any> loadOrFetch(
            dataType: StoreDataType<DATA_MODEL>
            , fetch: () -> APromise<List<DATA_MODEL>>
            , state: GlobalState = dataType.getStore().state)
        : APromise<Unit> {

        return if (dataType.exists(state)) {
            //already in cache
            APromise.of()
        } else {
            //no records in cache (state), load them now

            //try from (local) db first
            dataType.getPersistedData()?.takeIf { it.isNotEmpty() }?.let { data ->
                APromise.of().then {
                    dataType.dispatchLoaded(data, merge = false, shouldPersist = false) //already persisted..
                }
            }

            //or from the API
            ?: fetch.invoke().thenMap { data ->
                //dispatch data loaded
                dataType.dispatchLoaded(data, merge = false, shouldPersist = true) //persist fresh response

                Unit
            }
        }
    }






    protected open fun <API_RESPONSE: Any?> onApiError(
        e: Throwable
        , dataTypes: List<Pair<StoreDataType<*>, (API_RESPONSE) -> Unit>>
    ) {}
}