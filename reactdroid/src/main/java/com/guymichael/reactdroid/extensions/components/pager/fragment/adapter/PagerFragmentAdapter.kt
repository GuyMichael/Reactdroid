package com.guymichael.reactdroid.extensions.components.pager.fragment.adapter

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.guymichael.kotlinreact.model.EmptyOwnProps
import com.guymichael.kotlinreact.model.OwnProps
import com.guymichael.reactdroid.core.fragment.ComponentFragment

abstract class PagerFragmentAdapter<F : ComponentFragment<*>>(
        activity: FragmentActivity
    ) : FragmentStateAdapter(activity) {

    final override fun createFragment(position: Int) = createComponentFragment(position).also {
        it.arguments = Bundle().apply {
            putSerializable(ComponentFragment.ARGS_KEY_PROPS, EmptyOwnProps)
        }
    }

    abstract fun createComponentFragment(position: Int): F
    abstract fun createFragmentProps(position: Int): OwnProps
}