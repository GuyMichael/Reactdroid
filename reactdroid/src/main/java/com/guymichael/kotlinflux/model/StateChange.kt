package com.guymichael.kotlinflux.model

data class StateChange(internal val prevState : GlobalState, internal val nextState : GlobalState)