package com.guymichael.reactdroid.extensions.components.pager.fragment

import com.guymichael.kotlinreact.model.EmptyOwnProps
import com.guymichael.kotlinreact.model.OwnProps
import com.guymichael.reactdroid.core.fragment.ComponentFragment

/**
 * @param fragments pairs of fragment-creator to props supplier
 */
data class FragmentPagerProps<F : ComponentFragment<*>>(
        val fragments: List<Pair<() -> F, () -> OwnProps>>
    ) : OwnProps() {

    private val itemCount: Int = fragments.size

    override fun getAllMembers() = listOf(
        itemCount
    )



    companion object {
        fun <T : ComponentFragment<*>> from(vararg fragments: () -> T
            , unifiedPropsSupplier: () -> OwnProps = { EmptyOwnProps }
        ): FragmentPagerProps<T> {

            return FragmentPagerProps(
                fragments.map { it to unifiedPropsSupplier }
            )
        }
    }
}