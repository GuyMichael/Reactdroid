package com.guymichael.reactdroid.extensions.components.tabs

import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.guymichael.kotlinreact.model.OwnState
import com.guymichael.reactdroid.core.model.AComponent
import java.lang.ref.WeakReference

abstract class BaseTabsComponent<P : BaseTabsProps, S : OwnState, V : TabLayout>(
        v: V
        , pager: ViewPager2
        , private val binder: (tabLayout: TabLayout.Tab, position: Int) -> Unit
        , private val autoRefresh: Boolean = true
    ) : AComponent<P, S, V>(v) {

    private val mPager: WeakReference<ViewPager2> = WeakReference(pager)


    private var isBound = false
    private fun bindToPager(pager: ViewPager2) {
        if( !isBound) {
            isBound = true
            TabLayoutMediator(mView, pager, autoRefresh, binder).attach()
        }
    }

    private fun renderPager(pager: ViewPager2) {
        if (pager.adapter == null) {                //first render
            //assume parent is calling tabs render before pager render,
            // so put at end of execution queue
            mView.post { mPager.get()?.also { if (it.adapter != null) {
                bindToPager(it)
            }}} //THINK wait for adapter

        } else {                                    //normal render
            bindToPager(pager)
        }
    }

    override fun render() {
        mPager.get()?.also(::renderPager)
    }
}