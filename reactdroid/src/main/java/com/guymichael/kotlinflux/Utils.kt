package com.guymichael.kotlinflux

import com.guymichael.kotlinflux.model.GlobalState
import com.guymichael.reactdroid.BuildConfig

object Utils {
    private val strictMode = BuildConfig.DEBUG

    /**
     * @param first
     * @param second
     * @return *true* if both *first* and *second* are null, same object/reference or
     * equals for rendering (hashCode).
     */
    fun shallowEquality(first: GlobalState?, second: GlobalState?): Boolean {
        return if (first == null || second == null) {
            first == null && second == null
        } else if (first === second) { //by reference
            true
        } else {
            //in order to allow complex state (not just primitives), we must do actual equality.
            //for primitives, equality is natural. For any other Object, equals&hashCode have to be implemented correctly
            first.map.hashCode() == second.map.hashCode()
                && ( !strictMode || first.map == second.map)
            //note: normally hashCode equality takes 20ms and actual equality 200ms
        }
    }
}