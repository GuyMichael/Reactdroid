package com.guymichael.kotlinflux.model.reducers

import com.guymichael.kotlinflux.model.GlobalState
import com.guymichael.kotlinflux.model.actions.Action
import com.guymichael.kotlinflux.model.actions.GenericAction
import com.guymichael.kotlinreact.Utils

/**
 * should be extended as an 'object' (Kotlin's singleton)
 */
abstract class Reducer(private val childReducers: List<Reducer> = emptyList()) {

    internal fun onNewAction_mapToNextState(state: GlobalState, action: Action) : GlobalState {

        val parentNextState = when {

            action is GenericAction<*> //why first? -> its getReducer() throws..
            -> onGenericAction_mapToNextState(state, action)   //THINK casting

            //this is where we normally go, and this is how we 'bind' actions to their relevant reducer
            action.key.getReducer() === this
            -> onAction_mapToNextState(state, action)      //THINK casting

            else
            -> onOtherTypeAction_mapToNextState(state, action)
        }

        return if(state === parentNextState) {//referential equality (same ref)
            //no change, try child reducers
            notifyChildReducers_mapToNextState(state, action)
        } else {
            //different new state. no further op
            //THINK pass to child reducers anyways
            parentNextState
        }
    }

    internal fun getDefaultState() : GlobalState {
        val defaultState = getSelfDefaultState()
        for(reducer in childReducers) {
            defaultState.map[reducer::class.java.simpleName] = reducer.getSelfDefaultState()
        }

        return defaultState
    }

    private fun notifyChildReducers_mapToNextState(state: GlobalState, action: Action) : GlobalState {
        var nextState: GlobalState? = null

        for(reducer in childReducers) {
            val childState : GlobalState = state.get(reducer) ?: GlobalState()//should never actually be null (all reducers return a default non-null state)
            val childNextState = reducer.onNewAction_mapToNextState(childState, action)
            if(childState !== childNextState) {
                //state change, update nextState

                if(nextState == null) {
                    //first actual change
                    nextState = state.clone()
                }

                nextState.map[reducer::class.java.simpleName] = childNextState

                continue
            }
        }

        return nextState ?: state
    }









    /** Called when initializing or resetting the global state
     * @return the default state of your `Reducer` */
    abstract fun getSelfDefaultState() : GlobalState

    /**
     * May be overridden, as long as you accept the contract:
     * @return same state reference if nothing changed, cloned state if action was handled
     */
    protected open fun onAction_mapToNextState(state: GlobalState, action: Action) : GlobalState {
        return state.cloneAndSetValue(action.key, action.value)
    }

    protected open fun onGenericAction_mapToNextState(state: GlobalState, action: GenericAction<*>)
        : GlobalState = state //no op

    protected open fun onOtherTypeAction_mapToNextState(state: GlobalState, action: Action)
        : GlobalState = state //no op






    override fun hashCode(): Int {
        return Utils.computeHashCode(childReducers)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Reducer

        return childReducers == other.childReducers
    }

    override fun toString(): String {
        return javaClass.simpleName
    }
}