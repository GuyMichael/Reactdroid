package com.guymichael.kotlinflux.extensions.data.model

import com.guymichael.apromise.APromise
import com.guymichael.kotlinflux.model.GlobalState
import com.guymichael.kotlinflux.model.Store
import com.guymichael.kotlinflux.model.StoreKey
import com.guymichael.kotlinflux.model.actions.DataAction

abstract class StoreDataType<DATA_MODEL : Any> : StoreKey {

    open fun getSchemaName() = javaClass.simpleName.replace("DataType", "")
    override fun getName() = getSchemaName()

    /*prefixes*/
    protected open val storeKeyPrefix by lazy { getSchemaName() }
    protected open val loadingPostfix by lazy { "Loading" }
    protected open val loadingErrorPostfix by lazy { "LoadingError" }
    protected open val loadedCountPostfix by lazy { "LoadedCount" }

    /*key names*/
    private val storeKeyLoadedCount by lazy { this.storeKeyPrefix + this.loadedCountPostfix }
    private val storeKeyLoading by lazy { this.storeKeyPrefix + this.loadingPostfix }
    private val storeKeyLoadingError by lazy { this.storeKeyPrefix + this.loadingErrorPostfix }

    /*store keys*/
    val loadingStoreKey by lazy { object : StoreKey {
        override fun getName() = storeKeyLoading
        override fun getReducer() = this@StoreDataType.getReducer()
    }}
    val loadingErrorStoreKey by lazy { object : StoreKey {
        override fun getName() = storeKeyLoadingError
        override fun getReducer() = this@StoreDataType.getReducer()
    }}
    val loadedCountStoreKey by lazy { object : StoreKey {
        override fun getName(): String = storeKeyLoadedCount
        override fun getReducer() = this@StoreDataType.getReducer()
    }}

    @Throws(Throwable::class)
    fun UNSAFE_persistOrThrow(dataModelList: Any?, merge: Boolean) {
        //if no-merge, start by clearing all current records
        if( !merge) {
            clearPersistOrThrow()
        }

        //cast value to list and persist
        if ( dataModelList != null) {
            (dataModelList as? List<DATA_MODEL>)?.also {
                if (it.isNotEmpty()) {
                    persistOrThrow(it)
                }

                //THINK log error if cast failed
            } ?: throw RuntimeException("failed to persist - failed casting " +
                "${dataModelList?.javaClass?.simpleName} to List<type of ${getName()}>")
        }
    }

    @Throws(Throwable::class)
    fun UNSAFE_removeFromPersistOrThrow(dataModelList: Any?) {
        if ( dataModelList != null) {
            (dataModelList as? List<DATA_MODEL>)?.also {
                if (it.isNotEmpty()) {
                    removeFromPersistOrThrow(it)
                }

                //THINK log error if cast failed
            } ?: throw RuntimeException(
                "failed to remove from persist - failed casting " +
                "${dataModelList?.javaClass?.simpleName} to List<type of ${getName()}>")
        }
    }

    fun asCollectionOrNull(t: Any?): List<DATA_MODEL>? {
        return if (t == null) {
            null
        } else {
            (t as? List<DATA_MODEL>)//THINK casting
            //THINK log error if cast failed
        }
    }

    fun getDataModelSchemaIdIfMatches(d: Any) : String? {
        return (d as? DATA_MODEL)?.let {//THINK casting
            getSchemaId(it)
        }
    }



    fun getValueAsModelMap(state: GlobalState = getStore().state): Map<String, DATA_MODEL>? {
        return getCurrentValue(state) as? Map<String, DATA_MODEL>? // THINK casting
    }

    fun getAll(state: GlobalState = getStore().state): List<DATA_MODEL>? {
        return getValueAsModelMap(state)?.mapNotNull { it.value }
    }

    fun get(ids: List<String>, state: GlobalState = getStore().state): List<DATA_MODEL>? {
        return getAll(state)?.filter { getSchemaId(it) in ids }
    }

    fun get(id: String, state: GlobalState = getStore().state): DATA_MODEL? {
        return getAll(state)?.find { getSchemaId(it) == id }
    }

    /** @return `true` if the parent [data reducer][getReducer] contains any mapping for this type
     * that is not null, according to [current state][getStore] */
    fun existsInCurrentState(): Boolean = exists(getStore().state)

    //make final
    final override fun exists(state: GlobalState): Boolean {
        return super.exists(state)
    }

    fun exists(id: String, state: GlobalState = getStore().state): Boolean {
        return get(id, state) != null
    }

    fun isDataLoading(state: GlobalState = getStore().state) : Boolean {
        return getReducerState(state)?.get(loadingStoreKey) as? Boolean? ?: false
    }

    fun getDataLoadingError(state: GlobalState = getStore().state) : String? {
        return getReducerState(state)?.get(loadingErrorStoreKey) as? String?
    }

    fun getDataLoadedCount(state: GlobalState = getStore().state) : Int {
        return getReducerState(state)?.get(loadedCountStoreKey) as? Int? ?: 0
    }

    fun wasDataEverLoaded(state: GlobalState = getStore().state) : Boolean {
        return getDataLoadedCount(state) > 0
    }


    override fun toString() = getSchemaName()


    abstract fun getStore(): Store
    abstract fun getPersistedData() : List<DATA_MODEL>?
    abstract fun getSchemaId(d: DATA_MODEL) : String

    /** A convenience method. Uses [getStore] to dispatch [DataAction.setDataLoaded]*/
    open fun dispatchLoaded(data: List<DATA_MODEL>, merge: Boolean, shouldPersist: Boolean = true) {
        getStore().dispatch(DataAction.setDataLoaded(this, data, merge, shouldPersist))
    }

    /**
     * @param data never empty. Should be added (replace on duplicate) to any previously persisted data
     * @throws Throwable if persist failed.
     */
    @Throws(Throwable::class)
    protected abstract fun persistOrThrow(data: List<DATA_MODEL>)

    /**
     * Remove requested records from persist, if found.
     *
     * @param data never empty
     * @throws Throwable if remove failed
     */
    @Throws(Throwable::class)
    protected abstract fun removeFromPersistOrThrow(data: List<DATA_MODEL>)

    /**
     * Remove ALL records of this data from persist
     * @throws Throwable if clear failed
     */
    @Throws(Throwable::class)
    protected abstract fun clearPersistOrThrow()
}