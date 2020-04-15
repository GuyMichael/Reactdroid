package com.guymichael.kotlinflux.model.reducers

import com.guymichael.kotlinflux.model.GlobalState
import com.guymichael.kotlinflux.model.actions.Action
import com.guymichael.reactdroid.Logger

//THINK make internal
abstract class SideEffectReducer(
        childReducers: List<Reducer> = emptyList()
    ) : Reducer(childReducers) {



    /** @return true if this `action` should apply a side effect, false otherwise */
    abstract fun shouldApplySideEffect(action: Action): Boolean

    /** Contract: try-to-apply or no-op. If (tried and) failed, throw a [RuntimeException] */
    @Throws(RuntimeException::class)
    abstract fun applySideEffectOrThrow(action: Action, state: GlobalState)




    final override fun onAction_mapToNextState(state: GlobalState, action: Action): GlobalState {
        if (shouldApplySideEffect(action)) {
            //THINK consider applying to state, then persisting(thread?), then on error resuming prev values, so not to block the ui
            try {
                applySideEffectOrThrow(action, state)
            } catch (e: Throwable) {
                Logger.e(this::class, e.message ?: "no message")
                e.printStackTrace()

                //persist failed, assume we should cancel the state change as well
                return state
            }
        }

        return mapToNextState(state, action)
    }



    /**
     * Replaces [onAction_mapToNextState] with same (default) implementation
     */
    protected open fun mapToNextState(state: GlobalState, action: Action): GlobalState {
        return super.onAction_mapToNextState(state, action)
    }
}