package com.guymichael.kotlinflux.model

import com.guymichael.kotlinflux.fromTimedReducerMap
import com.guymichael.kotlinflux.model.reducers.Reducer
import com.guymichael.reactdroid.BuildConfig
import com.guymichael.reactdroid.Logger
import com.guymichael.reactdroid.Utils
import io.reactivex.rxjava3.schedulers.Timed
import kotlin.reflect.KClass

class GlobalState {//TODO make immutable

    internal val map: HashMap<String, Any?>

    constructor() {
        map = HashMap()
    }

    private constructor(other : GlobalState) {
        //TODO THINK deep cloning ?
        map = HashMap(other.map)
    }

    constructor(defaultState: List<Pair<StoreKey, Any?>>) : this() {
        for (pair in defaultState) {
            map[pair.first.getName()] = pair.second
        }
    }

    constructor(vararg defaultState: Pair<StoreKey, Any?>) : this(defaultState.toList())

    fun get(key: StoreKey) : Any? {
        return map[key.getName()]
    }

    fun get(key: String) : Any? {
        return map[key]
    }

    @Deprecated("deprecated usage of javaClass", ReplaceWith("get(String, KClass)"), DeprecationLevel.WARNING)
    fun <T> get(key: String, cls: Class<T>) : T? {
        return castOrNull(map[key], cls)
    }

    @Deprecated("deprecated usage of javaClass", ReplaceWith("get(StoreKey, KClass)"), DeprecationLevel.WARNING)
    fun <T> get(key: StoreKey, cls: Class<T>) : T? {
        return get(key.getName(), cls)
    }

    fun <T : Any> get(key: String, cls: KClass<T>) : T? {
        return castOrNull(map[key], cls)
    }

    fun <T : Any> get(key: StoreKey, cls: KClass<T>) : T? {
        return get(key.getName(), cls)
    }

    /**
     * @return a Map<Long, T>, originally dispatched using [Store.dispatchListAsMap],
     * as Collections in state are forbidden
     * @see getTimedList
     */
    fun <T : Any> getTimedListRawMap(key: StoreKey, cls: KClass<T>) : Map<Long, T>? {
        return get(key, Map::class) as? Map<Long, T>? //THINK cast
    }

    /**
     * Get a list (sorted), originally dispatched using [Store.dispatchAppendToList], meaning:
     * get a [Timed] list, which is saved to state as a Map<Long, T>, as Collections in
     * state are forbidden
     */
    fun <T : Any> getTimedList(key: StoreKey, cls: KClass<T>) : List<Timed<T>>? {
        return getTimedListRawMap(key, cls)?.fromTimedReducerMap()
    }

    /**
     * Use to get a (first) single value from a key pointing to a [Map].
     * Used mainly for storing whole Objects with timestamp (for shallow equality to work)
     */
    fun <T> getSingleMapValue(key: StoreKey, cls:Class<T>) : T? {
        return get(key.getName(), Map::class.java)?.values?.firstOrNull()?.let {
            castOrNull(it, cls)
        }
    }

    fun <T : Reducer> get(firstLevelReducer: Class<T>) : GlobalState? {
        return castOrNull(map[firstLevelReducer.simpleName], GlobalState::class.java)
    }

    fun <T : Reducer> get(firstLevelReducer: T) : GlobalState? {
        return get(firstLevelReducer::class.java)
    }

    fun clone() = GlobalState(this)

    fun cloneAndSetValues(vararg values: Pair<StoreKey, Any?>) : GlobalState {
        /* THINK resume. Otherwise equality checks on store keys may be heavy
        if ( !Utils.isEnum(key)) {
            throw IllegalArgumentException("Action must be an enum")
        }*/

        //immutable - clone state
        val nextState = GlobalState(this)
        //set values to new state
        values.forEach {
            nextState.map[it.first.getName()] = it.second
        }

        return nextState
    }

    fun cloneAndSetValue(key : StoreKey, value : Any?) : GlobalState {
        return cloneAndSetValues(Pair(key, value))
    }






    private fun <T> castOrNull(value: Any?, cls: Class<T>) : T? {
        return value?.let {
            try {
                it as T //THINK
            } catch (e: ClassCastException) {
                e.printStackTrace()
                if(BuildConfig.DEBUG) {
                    Logger.e(javaClass, "GlobalState get() found different"
                        + "class(${it.javaClass.simpleName}) than expected(${cls.simpleName})")
                }
                null
            }
        }
    }

    private fun <T : Any> castOrNull(value: Any?, cls: KClass<T>) : T? {
        return value?.let {
            try {
                it as T //THINK
            } catch (e: ClassCastException) {
                e.printStackTrace()
                if(BuildConfig.DEBUG) {
                    Logger.e(javaClass, "GlobalState get() found different"
                        + "class(${it.javaClass.simpleName}) than expected(${cls.simpleName})")
                }
                null
            }
        }
    }

    override fun hashCode(): Int {
        return Utils.computeHashCode(this, map)
    }

    override fun equals(other: Any?): Boolean {
        if (other == null) { return false }
        if (other.javaClass != this.javaClass) { return false }

        other as GlobalState

        return equals(other)
    }

    fun equals(other: GlobalState): Boolean {
        return this === other   //by reference
            || this.map == other.map
    }



    override fun toString(): String {
        return map.toString()
    }
}