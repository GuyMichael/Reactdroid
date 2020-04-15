package com.guymichael.kotlinflux.extensions.data.model

import com.guymichael.kotlinflux.model.GlobalState

/** for types that hold only one model by definition */
abstract class StoreDataTypeSingleModel<T : Any> : StoreDataType<T>() {

    fun get(state: GlobalState) : T? = getAll(state)?.firstOrNull()

    final override fun getPersistedData(): List<T>? = getPersisted()?.let(::listOf)

    @Throws(Throwable::class)
    final override fun persistOrThrow(data: List<T>?, merge: Boolean) {
        persistOrThrow(data?.firstOrNull())
    }

    @Throws(Throwable::class)
    override fun removeFromPersistOrThrow(data: List<T>) {
        throw RuntimeException("${StoreDataTypeSingleModel::class.java.simpleName}.removeFromPersistOrThrow(data)" +
                "should not be used. To remove current data, use DataAction.setDataLoaded(null)")
    }


    final override fun shouldMergeWithCurrentData(): Boolean = false//single model, always replace

    abstract fun getPersisted(): T?
    @Throws(Throwable::class)
    abstract fun persistOrThrow(t: T?)
}