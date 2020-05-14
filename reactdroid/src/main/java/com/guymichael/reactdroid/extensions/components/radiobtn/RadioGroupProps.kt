package com.guymichael.reactdroid.extensions.components.radiobtn

import androidx.annotation.IdRes
import com.guymichael.kotlinreact.model.OwnProps

/**
 * @param checkedBtnId
 * @param initial_onChecked a callback for when a `RadioButton` (with `xmlId`) state has changed
 * to 'checked'. If `xmlId` is null, the whole group got cleared.
 * This callback is guaranteed to be called once per change and never with '-1' or '0'.
 * Keep in mind that 'null', '-1' and '0' are all treated the same - but you should only always 'null'
 * to clear the id, to have a consistent state. This prop doesn't affect re-renders
 */
data class RadioGroupProps(
        @IdRes val checkedBtnId: Int? = null,
        val initial_onChecked: (xmlId: Int?) -> Unit
    ) : OwnProps() {

    override fun getAllMembers() = listOf(
        checkedBtnId
    )
}