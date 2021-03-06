package com.guymichael.reactdroid.extensions.components.compoundbtn

/**
 * @param checked first param is the 'checked' status, second is the callback for when user changes,
 * where you should update your local state (`setState`) or some global state (e.g. a `Store`) to immediately
 * reflect that change (and cause re-render for this checkbox component)
 */
data class CompoundBtnProps(
        override val checked: Pair<Boolean, (Boolean) -> Unit>,
        override val text: CharSequence? = null
    ) : BaseCompoundBtnProps(checked, text) {

    override fun getExtraMembers() = emptyList<Any?>()
}