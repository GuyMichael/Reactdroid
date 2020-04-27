package com.guymichael.kotlinflux.extensions.data.model

import com.guymichael.kotlinflux.model.GlobalState
import com.guymichael.kotlinflux.model.Store
import com.guymichael.kotlinflux.model.StoreKey

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
    fun UNSAFE_persistOrThrow(t: Any?, merge: Boolean) {
        if (t == null) {
            persistOrThrow(t, merge)
        } else {
            (t as? List<DATA_MODEL>)?.let {
                persistOrThrow(it, merge)
                //THINK log error if cast failed
            } ?: throw RuntimeException("failed to persist - failed casting ${t.javaClass.simpleName} " +
                    "to List<type of ${getName()}>")
        }
    }

    @Throws(Throwable::class)
    fun UNSAFE_removeFromPersistOrThrow(t: Any?) {
        if (t == null) {
            //no op
        } else {
            (t as? List<DATA_MODEL>)?.also {
                if (it.isNotEmpty()) {
                    removeFromPersistOrThrow(it)
                }
                //THINK log error if cast failed
            } ?: throw RuntimeException("failed to remove from persist - failed casting ${t.javaClass.simpleName} " +
                    "to List<type of ${getName()}>")
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
    abstract fun shouldMergeWithCurrentData(): Boolean//THINK remove entirely and let the CustomDataAction decide alone
    abstract fun getPersistedData() : List<DATA_MODEL>?
    abstract fun getSchemaId(d: DATA_MODEL) : String

    /**
     * @param data
     * @param merge when true, `data` should be added to any previously persisted data.
     * When false, the given `data` should be (looked up and) removed from persist.
     * That also mean that when false and `data` is `null`, the entire value/collection should be removed
     * from persist
     * @throws Throwable if persist failed. Note that null `data` should be allowed and normally
     * delete the (entire) persisted value (provided that `merge` is `false`
     */
    @Throws(Throwable::class)
    protected abstract fun persistOrThrow(data: List<DATA_MODEL>?, merge: Boolean)

    @Throws(Throwable::class)
    protected abstract fun removeFromPersistOrThrow(data: List<DATA_MODEL>)
}