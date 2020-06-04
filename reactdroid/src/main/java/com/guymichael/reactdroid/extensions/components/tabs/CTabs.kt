package com.guymichael.reactdroid.extensions.components.tabs

import androidx.annotation.IdRes
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.guymichael.kotlinreact.model.EmptyOwnState
import com.guymichael.reactdroid.core.activity.ComponentActivity
import com.guymichael.reactdroid.core.fragment.ComponentFragment
import com.guymichael.reactdroid.core.model.AComponent

/** [onRender] should be called after `pager`'s `onRender` (for the adapter to be initialized first).
 * but this `Component` will try to overcome a reverse `onRender` call */
class CTabs(
    v: TabLayout
    , pager: ViewPager2
    , binder: (tabLayout: TabLayout.Tab, position: Int) -> Unit
        = { tab, position -> tab.text = "Tab ${(position + 1)}" }
    , autoRefresh: Boolean = true
) : BaseTabsComponent<TabsProps, EmptyOwnState, TabLayout>(v, pager, binder, autoRefresh) {

    override fun createInitialState(props: TabsProps) = EmptyOwnState
}

fun AComponent<*, *, *>.withTabs(@IdRes id: Int, pager: ViewPager2
    , binder: (tabLayout: TabLayout.Tab, position: Int) -> Unit
    , autoRefresh: Boolean = true
) = CTabs(mView.findViewById(id), pager, binder, autoRefresh)

fun AComponent<*, *, *>.withTabs(@IdRes id: Int, @IdRes pager: Int
    , binder: (tabLayout: TabLayout.Tab, position: Int) -> Unit
    , autoRefresh: Boolean = true
) = CTabs(mView.findViewById(id), mView.findViewById(pager), binder, autoRefresh)

fun ComponentActivity<*>.withTabs(@IdRes id: Int, pager: ViewPager2
    , binder: (tabLayout: TabLayout.Tab, position: Int) -> Unit
    , autoRefresh: Boolean = true
) = CTabs(findViewById(id), pager, binder, autoRefresh)

fun ComponentActivity<*>.withTabs(@IdRes id: Int, @IdRes pager: Int
    , binder: (tabLayout: TabLayout.Tab, position: Int) -> Unit
    , autoRefresh: Boolean = true
) = CTabs(findViewById(id), findViewById(pager), binder, autoRefresh)

fun ComponentFragment<*>.withTabs(@IdRes id: Int, pager: ViewPager2
    , binder: (tabLayout: TabLayout.Tab, position: Int) -> Unit
    , autoRefresh: Boolean = true
) = CTabs(view!!.findViewById(id), pager, binder, autoRefresh)

fun ComponentFragment<*>.withTabs(@IdRes id: Int, @IdRes pager: Int
    , binder: (tabLayout: TabLayout.Tab, position: Int) -> Unit
    , autoRefresh: Boolean = true
) = CTabs(view!!.findViewById(id), view!!.findViewById(pager), binder, autoRefresh)