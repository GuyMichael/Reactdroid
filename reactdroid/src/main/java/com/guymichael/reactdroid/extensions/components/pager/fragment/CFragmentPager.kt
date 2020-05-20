package com.guymichael.reactdroid.extensions.components.pager.fragment

import androidx.annotation.IdRes
import androidx.viewpager2.widget.ViewPager2
import com.guymichael.kotlinreact.model.EmptyOwnState
import com.guymichael.reactdroid.extensions.components.pager.BasePager
import com.guymichael.reactdroid.extensions.components.pager.fragment.adapter.PagerFragmentAdapter
import com.guymichael.reactdroid.core.Utils
import com.guymichael.reactdroid.core.activity.ComponentActivity
import com.guymichael.reactdroid.core.fragment.ComponentFragment
import com.guymichael.reactdroid.core.model.AComponent

class CFragmentPager<F : ComponentFragment<*>>(
        v: ViewPager2
    ) : BasePager<FragmentPagerProps<F>, EmptyOwnState, PagerFragmentAdapter<F>>(v) {
    override fun createInitialState(props: FragmentPagerProps<F>) = EmptyOwnState

    override fun render_createAdapter() = object : PagerFragmentAdapter<F>(
        Utils.getActivity(mView.context, ComponentActivity::class.java)
            ?: throw IllegalArgumentException(
                "${CFragmentPager::class.simpleName}: must be inside a Reactdroid ${ComponentActivity::class.simpleName}"
            )
        ) {

        override fun createComponentFragment(position: Int) = props.initial_pageCreator(position)
        override fun createFragmentProps(position: Int) = props.initial_propsSupplier(position)
        override fun getItemCount() = props.itemCount
    }

    override fun render_notifyAdapterOnDataSetChanged(adapter: PagerFragmentAdapter<F>) {
        adapter.notifyDataSetChanged()
    }
}



fun AComponent<*, *, *>.withFragmentPager(@IdRes id: Int)
= CFragmentPager<ComponentFragment<*>>(mView.findViewById(id))

fun ComponentFragment<*>.withFragmentPager(@IdRes id: Int)
= CFragmentPager<ComponentFragment<*>>(view!!.findViewById(id))

fun ComponentActivity<*>.withFragmentPager(@IdRes id: Int)
= CFragmentPager<ComponentFragment<*>>(findViewById(id))