package com.guymichael.kotlinflux.model.actions

import com.guymichael.kotlinflux.extensions.data.model.DataActionType
import com.guymichael.kotlinflux.extensions.data.model.StoreDataType
import com.guymichael.kotlinflux.extensions.data.model.StoreDataTypeSingleModel

/**
 * @param actionType e.g. [DataAction.setDataLoaded]
 * @param shouldPersist normally used for tests only
 * @param mergeWithCurrent If true, new dispatch data will
 * update current data in state, replacing duplicate keys, and in persist (if `shouldPersist` is `true`)
 * If false, this new data will replace the whole data schema
 */
class DataAction private constructor(
        val actionType: DataActionType
        , override val key: StoreDataType<*>
        , value: Any?
        , val mergeWithCurrent: Boolean
        , val shouldPersist: Boolean
    ) : Action(key, value) {




    companion object {
        fun <T : Any> setDataLoading(type: StoreDataType<T>)
            = DataAction(DataActionType.dataLoading, type,
                value = true,               //doesn't matter here
                mergeWithCurrent = true,    //doesn't matter here
                shouldPersist = true        //doesn't matter here
            )

        /**
         * @param mergeWithCurrent If true, `data` will be merged (override when exists) with current data
         * in store AND in persist. If false, `data` will replace the whole existing data/collection,
         * so when `data` is `null`, the entire data/collection will be cleared.
         * Note: data in state is replaced using equality of the store key alone. See [StoreDataType.getSchemaId]
         *
         * @param shouldPersist Leave `true` to let [`type`][StoreDataType] persist implementation decide.
         * `false` should be used for tests or unique scenarios.
         */
        @JvmStatic
        @JvmOverloads
        fun <T : Any> setDataLoaded(type: StoreDataType<T>, data: List<T>?, mergeWithCurrent: Boolean
            , shouldPersist: Boolean = true
        ) = DataAction(
            actionType = DataActionType.dataLoaded
            , key = type
            , value = data
            , mergeWithCurrent = mergeWithCurrent
            , shouldPersist = shouldPersist
        )

        @JvmStatic
        @JvmOverloads
        fun <T : Any> setDataLoaded(type: StoreDataTypeSingleModel<T>, data: T?
            , shouldPersist: Boolean = true)
        = DataAction(
            actionType = DataActionType.dataLoaded,
            key = type,
            value = data?.let(::listOf),
            mergeWithCurrent = false,       //single type, always replace (important!)
            shouldPersist = shouldPersist
        )

        fun <T : Any> setDataLoadingError(type: StoreDataType<T>, error: String)//note: null as value will be ignored and a default text will be used - this action always sets an error
            = DataAction(DataActionType.dataLoadingError, type, error
                , mergeWithCurrent = true       //doesn't matter here
                , shouldPersist = true          //doesn't matter here
            )

        fun <T : Any> setDataLoadingError(type: StoreDataType<T>, error: Throwable)
            = setDataLoadingError(type, error.message ?: "error")//note: null as value will be ignored and a default text will be used - this action always sets an error

        /** Removes data (by ids) from BOTH the Store and the persist */
        fun <T : Any> removeData(type: StoreDataType<T>, ids: List<String>)
            = DataAction(DataActionType.deleteData, type, ids.toList()
                , mergeWithCurrent = true       //doesn't matter here
                , shouldPersist = true          //doesn't matter here as deleting always deletes both
            )

        fun <T : Any> removeData(type: StoreDataType<T>, vararg ids: String)
                = removeData(type, ids.toList())

        /** Resets all `type` related keys. Data will be taken from persist again ([StoreDataType.getPersistedData]) */
        fun <T : Any> reset(type: StoreDataType<T>)
            = DataAction(DataActionType.reset, type, null
                , mergeWithCurrent = true       //doesn't matter here
                , shouldPersist = true          //doesn't matter here as we always take initial data from persist if exists
            )



        fun isDataLoaded(action: Action): Boolean {
            return action is DataAction && action.actionType == DataActionType.dataLoaded
        }
    }
}