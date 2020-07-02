package com.guymichael.kotlinflux.model

import com.guymichael.kotlinflux.fromTimedReducerMap
import com.guymichael.kotlinflux.model.actions.Action
import com.guymichael.kotlinflux.model.reducers.Reducer
import io.reactivex.rxjava3.schedulers.Timed
import kotlin.reflect.KClass

/**
 * Should be implemented by an enum
 */
interface StoreKey {

    /**
     * Defines the uniqueness of this key in the store
     */
    fun getName() : String

    /** remember, [Reducer]s should be Kotlin object(s). Do NOT create a new reducer each time! */
    fun getReducer(): Reducer




    @JvmSynthetic
    fun createAction(value: Any?): Action = Action(this, value)

    @JvmSynthetic
    fun getReducerState(state: GlobalState): GlobalState? {
        return state.get(getReducer())
    }

    /**
     * @return the value associated with this key.
     * Note that even though a key may have multiple affects on multiple `Reducer`s (which is discouraged),
     * there should be only one 'value' which it correlates to.
     *
     * **example** (default implementation): `return state.get(getReducer())?.get(this))`
     */
    @JvmSynthetic
    fun getCurrentValue(state: GlobalState): Any? {
        return getReducerState(state)?.get(this)
    }

    @JvmSynthetic
    fun exists(state: GlobalState): Boolean {
        return getCurrentValue(state) != null
    }

    @JvmSynthetic
    fun <T : Any> getValue(state: GlobalState, cls: KClass<T>): T? {
        @Suppress("UNCHECKED_CAST")
        return getCurrentValue(state) as? T? //THINK cast
    }

    /** [getValue] as `Double` */
    @JvmSynthetic
    fun getDouble(state: GlobalState): Double? {
        return getValue(state, Double::class)
    }

    /** [getValue] as `Long` */
    @JvmSynthetic
    fun getLong(state: GlobalState): Long? {
        return getValue(state, Long::class)
    }

    /** [getValue] as `Long` */
    @JvmSynthetic
    fun getInt(state: GlobalState): Int? {
        return getValue(state, Int::class)
    }

    /** [getValue] as `Boolean` */
    @JvmSynthetic
    fun getBoolean(state: GlobalState): Boolean? {
        return getValue(state, Boolean::class)
    }

    /** [getValue] as `String` */
    @JvmSynthetic
    fun getString(state: GlobalState): String? {
        return getValue(state, String::class)
    }

    /**
     * @return a sorted list, representing the current state (actual state holds `Map<Long, T>`, used by
     * `Action.putList()`
     */
    @JvmSynthetic
    fun <T : Any> getValueAsTimedList(state: GlobalState, itemCls: KClass<T>): List<Timed<T>>? {
        @Suppress("UNCHECKED_CAST")
        return (getCurrentValue(state) as? Map<Long, T>?)?.fromTimedReducerMap() //THINK cast
    }
}