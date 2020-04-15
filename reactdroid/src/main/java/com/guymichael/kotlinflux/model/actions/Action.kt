package com.guymichael.kotlinflux.model.actions

import com.guymichael.kotlinflux.model.GlobalState
import com.guymichael.kotlinflux.model.StoreKey
import com.guymichael.kotlinflux.toTimedReducerMap
import com.guymichael.kotlinreact.Utils
import io.reactivex.rxjava3.schedulers.Timed

/** This is an abstract class in order for each `Reducer` to have its own action type,
 * and that is in order to 'bind' (store) keys with their relevant reducer */
open class Action(open val key: StoreKey, open val value: Any?) {

    override fun toString(): String {
        return "Action(key=${key.getName()}, value=$value)"
    }

    override fun hashCode(): Int {
        return Utils.computeHashCode(this, key, value)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Action

        return key == other.key
            && value == other.value
    }


    companion object {

        /**
         * A helper method to put collections in Reducers. They are put as `Map<Long, T>`, mapping
         * timestamp to actual value.
         */
        fun <T> putList(key: StoreKey, value: List<Timed<T>>
                        , mergeWithState: GlobalState? = null
                        , actionCreator: (key: StoreKey, Map<Long, T>?) -> Action = ::Action)
            : Action {

            return actionCreator(key
                , when (mergeWithState) {
                    null -> value.toTimedReducerMap()
                    else -> mergeWithCurrentState(value, key, mergeWithState)
                }
            )
        }

        /** See [putList] */
        fun <T> addToList(key: StoreKey, value: Timed<T>
                          , mergeWithState: GlobalState? = null
                          , actionCreator: (key: StoreKey, Map<Long, T>?) -> Action = ::Action)
            : Action {

            return putList(key, listOf(value), mergeWithState, actionCreator)
        }
    }
}







/**
 * A helper method to put/append collections in Reducers.
 */
private fun <T> mergeWithCurrentState(value: Collection<Timed<T>>, key: StoreKey, state: GlobalState)
        : Map<Long, T>? {
    return value.toTimedReducerMap().let {
        (key.getCurrentValue(state) as? Map<Long, T>?)?.plus(it) ?: it //THINK cast
    }
}