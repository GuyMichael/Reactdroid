package com.guymichael.kotlinreact.model

import com.guymichael.kotlinreact.Utils


abstract class OwnState {

    //NOTICE: this hashCode does NOT win over an extending kotlin 'data' class
    override fun hashCode(): Int {
        return Utils.computeHashCode(this, getAllMembers())
    }

    //NOTICE: this equals does NOT win over an extending kotlin 'data' class
    override fun equals(other: Any?): Boolean {
        if (other == null) { return false }
        if (other.javaClass != this.javaClass) { return false }

        other as OwnState

        return equals(other)
    }

    /** Use this method for equality checks, as the standard `equals(Any?)` one may be overridden by
     * an extending kotlin data class, if used */
    fun equals(other: OwnState): Boolean {
        return this === other   //by reference
            || this.getAllMembers() == other.getAllMembers()
    }

    //THINK reflection/annotation
    /** @return all state-relevant props, meaning props that if they change, you'd expect a re-render */
    abstract fun getAllMembers() : List<*>
}