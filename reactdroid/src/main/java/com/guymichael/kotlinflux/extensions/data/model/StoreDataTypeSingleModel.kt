package com.guymichael.kotlinflux.extensions.data.model

import com.guymichael.kotlinflux.model.GlobalState

/** for types that hold only one model by definition */
abstract class StoreDataTypeSingleModel<T : Any> : StoreDataType<T>() {

    fun get(state: GlobalState) : T? = getAll(state)?.firstOrNull()

    final override fun getPersistedData(): List<T>? = getPersisted()?.let(::listOf)

    @Throws(Throwable::class)
    final override fun persistOrThrow(data: List<T>) {
//        clearPersistOrThrow()//allows only 1 at a time
        //THINK resume to guard.
        // Shouldn't happen as DataType.setDataLoaded(StoreDataTypeSingleModel) is hardcoded
        // with merge = false (see StoreDataType.UNSAFE_persistOrThrow())
        // * we want to avoid redundant (double) access to the db

        persistOrThrow(data.first())
    }



    /**
     * @param t value to persist
     * @throws Throwable if persist failed.
     */
    @Throws(Throwable::class)
    abstract fun persistOrThrow(t: T)
    abstract fun getPersisted(): T?
}