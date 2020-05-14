package com.guymichael.reactdroid.extensions.components.compoundbtn

import com.guymichael.kotlinreact.model.OwnProps

/**
 * @param checked first param is the 'checked' status, second ("dispatcher") is the callback for when user changes,
 * where you should update your local state (`setState`) or some global state (e.g. a `Store`) to immediately
 * reflect that change (and cause re-render for this checkbox component).
 *
 * Note: `checked`'s second argument ("dispatcher") doesn't affect re-renders
 */
abstract class BaseCompoundBtnProps(
        open val checked: Pair<Boolean, (Boolean) -> Unit>,
        open val text: CharSequence? = null
    ) : OwnProps() {

    final override fun getAllMembers() = listOf(checked.first, text).plus(getExtraMembers())

    /** No need to pass original props (e.g. `checked`, `text`) */
    abstract fun getExtraMembers(): List<*>
}