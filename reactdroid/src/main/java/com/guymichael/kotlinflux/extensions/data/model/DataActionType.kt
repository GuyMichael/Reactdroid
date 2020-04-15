package com.guymichael.kotlinflux.extensions.data.model

import com.guymichael.kotlinflux.model.GlobalState
import com.guymichael.kotlinflux.model.actions.DataAction
import com.guymichael.reactdroid.Logger

enum class DataActionType(
        val persistOrThrow: ((action: DataAction) -> Unit)? = null
        , val mapToNextState: (state: GlobalState, action: DataAction) -> GlobalState
) {

    dataLoading(mapToNextState = { state, action ->
        state.cloneAndSetValues(
            action.key.loadingStoreKey to true           //set loading
            , action.key.loadingErrorStoreKey to null    //reset error state
        )
    })

    , dataLoadingError(mapToNextState = { state, action ->
        state.cloneAndSetValues(
            action.key.loadingStoreKey to false          //reset loading state
            , action.key.loadingErrorStoreKey to ((action.value as? String?) ?: "error") //set error
        )
    })

    , dataLoaded(::persistDataLoadedOrThrow, ::mapDataLoadedToState)

    , reset(mapToNextState = { state, action ->
        state.cloneAndSetValues(//THINK don't reset counter and maybe others
            action.key.loadingStoreKey to false //reset loading state
            , action.key.loadingErrorStoreKey to null //reset error state
            , action.key.loadedCountStoreKey to 0 //reset loaded count
            , action.key to emptyDataStateMap().apply { //reset actual data
                putAll(action.key.getPersistedData(), action.key::getDataModelSchemaIdIfMatches)
            }
        )
    })

    , deleteData(::persistDeleteOrThrow, ::mapDeleteDataToState)
}











@Throws(Throwable::class)
private fun persistDataLoadedOrThrow(action: DataAction) {
    action.key.UNSAFE_persistOrThrow(
        action.value
        , action.mergeWithCurrent ?: action.key.shouldMergeWithCurrentData()
    )
}

@Throws(Throwable::class)
private fun persistDeleteOrThrow(action: DataAction) {
    action.key.UNSAFE_removeFromPersistOrThrow(action.value)
}

private fun mapDataLoadedToState(state: GlobalState, action: DataAction): GlobalState {
    val loadedCountKey = action.key.loadedCountStoreKey

    //THINK no need to use StoreKey, we could just use String(s)   (?)
    return state.cloneAndSetValues(
        //reset loading state
        action.key.loadingStoreKey to false

        //reset error state
        , action.key.loadingErrorStoreKey to null

        //increment loaded count
        , loadedCountKey    to (1 + (state.get(loadedCountKey, Int::class) ?: 0))

        //update actual data
        , action.key        to mapDataLoadedToValue(state, action)
    )
}

private fun mapDataLoadedToValue(state: GlobalState, action: DataAction): Map<String, Any?>? {
    val nextValue = emptyDataStateMap()//empty

    //merge (start) with current data (?)
    if (action.mergeWithCurrent ?: action.key.shouldMergeWithCurrentData()) {
        nextValue.putAllNotNull(state.get(action.key.getSchemaName()) as? Map<String, *>?)//THINK casting
    }

    //append new data
    nextValue.putAll(action.key.asCollectionOrNull(action.value), action.key::getDataModelSchemaIdIfMatches)

    return asImmutableStateMapOrNull(nextValue)
}

private fun mapDeleteDataToState(state: GlobalState, action: DataAction): GlobalState {
    return try {
        (action.value as List<String>).let { dataToRemove ->
            val nextValue = emptyDataStateMap()

            //merge with current data - add all data, except if its id is contained in the given list
            val currentValue = state.get(action.key.getSchemaName()) as? Map<String, *>? //THINK casting
            currentValue?.keys?.forEach { id ->
                if( !dataToRemove.contains(id)) {
                    currentValue[id]?.let {
                        nextValue[id] = it
                    }
                }
            }

            return state.cloneAndSetValue(action.key, asImmutableStateMapOrNull(nextValue))
        }
    } catch (e: Exception) { //e.g. NullPointerException, ClassCastException
        Logger.e("DataReducer", "delete data action called with bad arguments. Should be List<String> (ids)")
        e.printStackTrace()

        state //no-op
    }
}