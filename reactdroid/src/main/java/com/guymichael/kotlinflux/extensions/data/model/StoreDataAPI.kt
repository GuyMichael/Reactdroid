package com.guymichael.kotlinflux.extensions.data.model

import com.guymichael.kotlinflux.extensions.network.model.StoreAPI
import com.guymichael.kotlinflux.model.actions.DataAction
import com.guymichael.promise.Promise

/**
 * A binding point between [StoreAPI] and [StoreDataType], to easily connect API's to relevant data schemas
 *
 * @param dataTypes list of [StoreDataType] and `mapResponseToData` for each type
 */
abstract class StoreDataAPI<API_RESPONSE : Any?>(
        private val dataTypes: List<Pair<StoreDataType<*>, (r: API_RESPONSE) -> List<Any>?>>
    ) : StoreAPI<API_RESPONSE> {

    //THINK enforce same type for both arguments
    constructor(dataType: StoreDataType<*>, mapResponseToData: (API_RESPONSE) -> List<Any>?)
        : this(listOf(dataType to mapResponseToData))

    //THINK enforce same type for both arguments
    constructor(dataType: StoreDataTypeSingleModel<*>, mapResponseToData: (API_RESPONSE) -> Any?)
        : this(listOf(dataType to { r -> mapResponseToData(r)?.let(::listOf) }))

    override fun <P : Promise<API_RESPONSE>> prepare(call: P): P {
        return super.prepare(call.doOnExecution {
            dataTypes.forEach { (type, _) ->
                type.getStore().dispatch(DataAction.setDataLoading(type))
            }
        } as P) //THINK better way?
    }

    final override fun dispatch(response: API_RESPONSE) {//THINK computation thread
        dataTypes.forEach { (type, mapResponseToData) ->
            //THINK allow custom merge & persist (see UNSAFE_setDataLoaded arguments)
            type.getStore().dispatch(DataAction.UNSAFE_setDataLoaded(type, mapResponseToData(response)))
        }

        dispatchSideEffects(response)
    }

    final override fun onApiError(e: Throwable) {
        super.onApiError(e)

        dataTypes.forEach { (type, _) ->
            type.getStore().dispatch(DataAction.setDataLoadingError(type, e))
        }
    }



    open fun dispatchSideEffects(response: API_RESPONSE) {}
}