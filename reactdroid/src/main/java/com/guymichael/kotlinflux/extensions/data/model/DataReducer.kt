package com.guymichael.kotlinflux.extensions.data.model

import com.guymichael.kotlinflux.extensions.data.putAll
import com.guymichael.kotlinflux.extensions.data.toImmutableMap
import com.guymichael.kotlinflux.model.GlobalState
import com.guymichael.kotlinflux.model.StoreKey
import com.guymichael.kotlinflux.model.actions.Action
import com.guymichael.kotlinflux.model.actions.DataAction
import com.guymichael.kotlinflux.model.reducers.SideEffectReducer

abstract class DataReducer(
        childReducers: List<DataReducer> = emptyList()
    ) : SideEffectReducer(childReducers) {


    /**
     * Provides a list of [dataTypes][StoreDataType] which are known to have persistence,
     * to load persistence into the global state when app starts (or when state [resets][DataActionType.reset])
     *
     * @return dataTypes with persistence to be used when initializing (or resetting) the global state
     *
     * It is OK to omit certain types if you'd like the data to be loaded lazy, in which case
     * you are in charge of doing so ([dispatch db data to store][DataAction.setDataLoaded]).
     * Just be aware that if the data isn't loaded to Store at startup, some components which rely
     * on the data-in-state might not work properly - so be sure to load the data into state
     * before the first component which needs it is shown (e.g. some drawer Fragment)
     *
     * @see getSelfDefaultState
     * @see StoreDataAPIController.loadOrFetch
     */
    abstract fun getDefaultStatePersistenceTypes(): List<StoreDataType<*>>?


    final override fun shouldApplySideEffect(action: Action)
        = (action as? DataAction)?.shouldPersist == true //THINK cast

    @Throws(Throwable::class)
    final override fun applySideEffectOrThrow(action: Action, state: GlobalState) {
        (action as? DataAction)?.actionType?.persistOrThrow?.invoke(action) //THINK cast
    }

    final override fun getSelfDefaultState(): GlobalState {
        val dataTypes = getDefaultStatePersistenceTypes()
        val dataTypeValues = ArrayList<Pair<StoreKey, Any?>>()//actually key to Map<String, Any>

        dataTypes?.forEach {
            //for each data type, we're gonna have a map with it's schema name to it's values (by ids)
            val dataTypeMap = emptyDataStateMap()
            dataTypeMap.putAll(it.getPersistedData(), it::getDataModelSchemaIdIfMatches)
            dataTypeValues += it to asImmutableStateMapOrNull(dataTypeMap)//THINK use getImmutableStateMapOrNull()
        }

        return GlobalState(dataTypeValues)
    }



    override fun mapToNextState(state: GlobalState, action: Action) : GlobalState {
        if (action !is DataAction) { return state } //THINK cast

        return action.actionType.mapToNextState(state, action)
    }

}










internal fun emptyDataStateMap(): MutableMap<String, Any> {
    return HashMap()
}

internal fun asImmutableStateMapOrNull(map: MutableMap<String, Any>?): Map<String, Any>? {
    return map?.takeIf { it.isNotEmpty() }?.toImmutableMap()
}