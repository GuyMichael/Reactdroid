package com.guymichael.kotlinflux.model.actions

import com.guymichael.kotlinflux.extensions.data.model.DataActionType
import com.guymichael.kotlinflux.extensions.data.model.StoreDataType
import com.guymichael.kotlinflux.extensions.data.model.StoreDataTypeSingleModel

/**
 * @param actionType e.g. [DataAction.setDataLoaded]
 * @param shouldPersist normally used for tests only
 * @param mergeWithCurrent if null, default [StoreDataType] behavior. If true, new dispatch data will
 * update current data in state, replacing duplicate keys. If false, this new data will replace the whole
 * data schema
 */
class DataAction(
        val actionType: DataActionType
        , override val key: StoreDataType<*>
        , value: Any?
        , val shouldPersist: Boolean = true
        , val mergeWithCurrent: Boolean? = null)
    : Action(key, value) {




    companion object {
        fun <T : Any> setDataLoading(type: StoreDataType<T>)
                = DataAction(DataActionType.dataLoading, type, true)//note: true (value) is ignored - this action always sets to 'true'

        /**
         * @param shouldPersist normally used (false) for tests only
         * @param mergeWithCurrent if null, defaults to [StoreDataType.shouldMergeWithCurrentData].
         * If true, `data` will be merged (override when exists) with current data.
         * If false, `data` will replace the whole existing data/collection, so when `data` is `null`,
         * the entire data/collection will be cleared.
         * NOTICE: Same goes for persistence!! (provided that `should persist` is `true`).
         * Note: data in state is replaced using equality of the store key alone! see [StoreDataType.getSchemaId]
         */
        fun <T : Any> setDataLoaded(type: StoreDataType<T>, data: List<T>?
                                    , shouldPersist: Boolean = true, mergeWithCurrent: Boolean? = null)
            = DataAction(
            DataActionType.dataLoaded, type, data
                , shouldPersist = shouldPersist, mergeWithCurrent = mergeWithCurrent
            )

        @JvmStatic
        @JvmOverloads
        fun <T : Any> setDataLoaded(type: StoreDataTypeSingleModel<T>, data: T?
                                    , shouldPersist: Boolean = true)
            = DataAction(
            DataActionType.dataLoaded, type, data?.let(::listOf)
                , shouldPersist = shouldPersist, mergeWithCurrent = false//single type, always replace
            )

        fun UNSAFE_setDataLoaded(type: StoreDataType<*>, data: List<Any>?
                                 , shouldPersist: Boolean = true, mergeWithCurrent: Boolean? = null)
            = DataAction(
            DataActionType.dataLoaded, type, data
                , shouldPersist = shouldPersist, mergeWithCurrent = mergeWithCurrent
            )

        fun <T : Any> setDataLoadingError(type: StoreDataType<T>, error: String)//note: null as value will be ignored and a default text will be used - this action always sets an error
                = DataAction(DataActionType.dataLoadingError, type, error)

        fun <T : Any> setDataLoadingError(type: StoreDataType<T>, error: Throwable)
                = setDataLoadingError(type, error.message ?: "error")//note: null as value will be ignored and a default text will be used - this action always sets an error

        fun <T : Any> removeData(type: StoreDataType<T>, ids: List<String>)
                = DataAction(DataActionType.deleteData, type, ids.toList())

        fun <T : Any> removeData(type: StoreDataType<T>, vararg ids: String)
                = removeData(type, ids.toList())

        /** Resets all `type` related keys. Data will be taken from persist again ([StoreDataType.getPersistedData]) */
        fun <T : Any> reset(type: StoreDataType<T>)
                = DataAction(DataActionType.reset, type, null)



        fun isDataLoaded(action: Action): Boolean {
            return action is DataAction && action.actionType == DataActionType.dataLoaded
        }
    }
}