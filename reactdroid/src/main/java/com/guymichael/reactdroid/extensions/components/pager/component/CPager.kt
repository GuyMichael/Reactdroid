package com.guymichael.reactdroid.extensions.components.pager.component

import android.view.View
import androidx.annotation.IdRes
import androidx.viewpager2.widget.ViewPager2
import com.guymichael.kotlinreact.model.EmptyOwnProps
import com.guymichael.kotlinreact.model.EmptyOwnState
import com.guymichael.reactdroid.extensions.components.pager.BasePager
import com.guymichael.reactdroid.extensions.components.pager.component.adapter.PagerAdapter
import com.guymichael.reactdroid.extensions.components.pager.component.adapter.PagerItemProps
import com.guymichael.reactdroid.core.activity.ComponentActivity
import com.guymichael.reactdroid.core.fragment.ComponentFragment
import com.guymichael.reactdroid.core.model.AComponent

class CPager(v: ViewPager2)
: BasePager<PagerProps, EmptyOwnState, PagerAdapter>(v) {

    override fun createInitialState(props: PagerProps) = EmptyOwnState

    override fun render_createAdapter() = PagerAdapter().also {
        it.notifyDataSetChanged(this.props.pages)
    }

    override fun render_notifyAdapterOnDataSetChanged(adapter: PagerAdapter) {
        adapter.notifyDataSetChanged(this.props.pages)
    }



    /* API */
    /** To render components with custom props, use the default `onRender` method -
     * the one that accepts [PagerProps] */
    fun onRender(vararg items: Pair<Int, (View) -> AComponent<EmptyOwnProps, *, *>>) {
        super.onRender(PagerProps(
            items.map { (layoutRes, componentCreator) ->
                PagerItemProps("$layoutRes", layoutRes, EmptyOwnProps, componentCreator)
            }
        ))
    }
}




fun AComponent<*, *, *>.withPager(@IdRes id: Int)
= CPager(mView.findViewById(id))

fun ComponentFragment<*>.withPager(@IdRes id: Int)
= CPager(view!!.findViewById(id))

fun ComponentActivity<*>.withPager(@IdRes id: Int)
= CPager(findViewById(id))