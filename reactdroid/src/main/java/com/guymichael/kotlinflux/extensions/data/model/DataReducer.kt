package com.guymichael.kotlinflux.extensions.data.model

import com.guymichael.kotlinflux.model.GlobalState
import com.guymichael.kotlinflux.model.StoreKey
import com.guymichael.kotlinflux.model.actions.Action
import com.guymichael.kotlinflux.model.actions.DataAction
import com.guymichael.kotlinflux.model.reducers.SideEffectReducer

abstract class DataReducer(
        childReducers: List<DataReducer> = emptyList()
    ) : SideEffectReducer(childReducers) {



    abstract fun getAllTypes(): List<StoreDataType<*>>?


    final override fun shouldApplySideEffect(action: Action)
        = (action as? DataAction)?.shouldPersist == true //THINK cast

    @Throws(Throwable::class)
    final override fun applySideEffectOrThrow(action: Action, state: GlobalState) {
        (action as? DataAction)?.actionType?.persistOrThrow?.invoke(action) //THINK cast
    }

    final override fun getSelfDefaultState(): GlobalState {
        val dataTypes = getAllTypes()
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
    return map?.takeIf { it.isNotEmpty() } //NOCOMMIT ?.toImmutableMap()
}