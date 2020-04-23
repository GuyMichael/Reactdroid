package com.guymichael.kotlinflux.extensions.data.model

import com.guymichael.kotlinflux.extensions.network.model.StoreAPIController
import com.guymichael.kotlinflux.model.actions.DataAction
import com.guymichael.promise.Promise

/**
 * A binding point between [StoreAPIController] and [StoreDataType], to easily connect API's
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

    /**
     * Chains the given API `call` with dispatch and persist management logic, as per given `dataTypes` to
     * handle the actual logic (of persisting and dispatching to a Store).
     *
     * @param call The API call presented as a [Promise]
     * @param dataTypes to process the response - dispatch and persist.
     * Each pair is a [StoreDataType] and a mapper to extract the relevant data from the actual response
     * Normally you will use only one pair.
     * @param persistSideEffects with persisting the data (types) done for you,
     * here you can add any extra persist logic, such as some flag that came with the response
     * @param dispatchSideEffects with dispatching the data (types) done for you,
     * here you can add any extra dispatch logic, such as some flag that came with the response
     * @param logErrors if true, logs errors and stack trace
     */
    fun <API_RESPONSE : Any?, P : Promise<API_RESPONSE>> prepare(
            call: P
            , dataTypes: List<Pair<StoreDataType<*>, (API_RESPONSE) -> List<Any>?>>
            , persistSideEffects: ((API_RESPONSE) -> Unit) = {}
            , dispatchSideEffects: ((API_RESPONSE) -> Unit) = {}
            , logErrors: Boolean = true
        ): P {

        return super.prepare(
            //dispatch data loading
            call.doOnExecution { dataTypes.forEach { (type, _) ->
                type.getStore().dispatch(DataAction.setDataLoading(type))
            }} as P
            //dispatch the data and side effects
            , {
                dispatch(it, dataTypes)
                dispatchSideEffects.invoke(it)
            }
            , persistSideEffects
            , logErrors
        )
    }

    /** see multi-dataTypes method for docs */
    fun <API_RESPONSE : Any?, P : Promise<API_RESPONSE>, D : Any> prepare(
        call: P
        , dataType: StoreDataType<D>
        , mapResponseToData: (API_RESPONSE) -> List<D>?
        , persistSideEffects: ((API_RESPONSE) -> Unit) = {}
        , dispatchSideEffects: ((API_RESPONSE) -> Unit) = {}
        , logErrors: Boolean = true
    ): P {
        return prepare(call
            , listOf(dataType to mapResponseToData)
            , persistSideEffects, dispatchSideEffects, logErrors
        )
    }

    /** see multi-dataTypes method for docs */
    fun <API_RESPONSE : Any?, P : Promise<API_RESPONSE>, D : Any> prepare(
        call: P
        , dataType: StoreDataTypeSingleModel<D>
        , mapResponseToData: (API_RESPONSE) -> D?
        , persistSideEffects: ((API_RESPONSE) -> Unit) = {}
        , dispatchSideEffects: ((API_RESPONSE) -> Unit) = {}
        , logErrors: Boolean = true
    ): P {
        return prepare(call
            , listOf(dataType to { r -> mapResponseToData(r)?.let(::listOf) })
            , persistSideEffects, dispatchSideEffects, logErrors
        )
    }

    private fun <API_RESPONSE: Any?> dispatch(response: API_RESPONSE
            , dataTypes: List<Pair<StoreDataType<*>, (API_RESPONSE) -> List<Any>?>>
        ) {

        dataTypes.forEach { (type, mapResponseToData) ->
            //THINK allow custom merge & persist (see UNSAFE_setDataLoaded arguments)
            type.getStore().dispatch(DataAction.UNSAFE_setDataLoaded(type, mapResponseToData(response)))
        }
    }

    private fun <API_RESPONSE: Any?> onApiError(e: Throwable
            , dataTypes: List<Pair<StoreDataType<*>, (API_RESPONSE) -> List<Any>?>>) {

        dataTypes.forEach { (type, _) ->
            type.getStore().dispatch(DataAction.setDataLoadingError(type, e))
        }
    }
}