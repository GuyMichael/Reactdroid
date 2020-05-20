package com.guymichael.reactdroid.extensions.components.pager.fragment

import com.guymichael.kotlinreact.model.EmptyOwnProps
import com.guymichael.kotlinreact.model.OwnProps
import com.guymichael.reactdroid.core.fragment.ComponentFragment

/**
 * @param itemCount total fragments in pager
 * @param initial_pageCreator creates (new) fragment per call, for 'position'
 * @param initial_propsSupplier supply relevant props for fragment in 'position'.
 * Default supplies [empty props][EmptyOwnProps]
 */
data class FragmentPagerProps<F : ComponentFragment<*>>(
        val itemCount: Int,
        val initial_pageCreator: (position: Int) -> F,
        val initial_propsSupplier: (position: Int) -> OwnProps = { EmptyOwnProps }
    ) : OwnProps() {

    override fun getAllMembers() = listOf(
        itemCount
    )
}