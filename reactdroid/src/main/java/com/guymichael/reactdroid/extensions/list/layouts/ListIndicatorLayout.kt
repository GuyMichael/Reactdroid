package com.guymichael.reactdroid.extensions.list.layouts

import android.annotation.TargetApi
import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import androidx.recyclerview.widget.RecyclerView
import com.guymichael.reactdroid.extensions.list.ComponentListUtils
import com.guymichael.reactdroid.extensions.list.adapter.RecyclerComponentAdapter

class ListIndicatorLayout : LinearLayout {
    var adapter: RecyclerComponentAdapter? = null
        internal set
    private var tabViewResId = 0
    private var isIndicatorsClickable = false

    private val indexTabClickedListener = OnIndexTabClickedListener()
    private val onListScrollListener = OnListScrollListener()
    private val dataObserver = OnDataUpdatedObserver()

    val recyclerView: RecyclerView?
        get() = this.adapter?.recyclerView

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes)

    override fun onDetachedFromWindow() {
        if (this.adapter != null) {
            this.adapter!!.unregisterAdapterDataObserver(dataObserver)
        }
        super.onDetachedFromWindow()
    }

    /**
     * @param adapter
     * @param tabViewResId with a drawable background with a selected and normal states
     * @param indicatorsClickable
     */
    fun setup(adapter: RecyclerComponentAdapter, tabViewResId: Int, indicatorsClickable: Boolean) {
        this.adapter = adapter
        this.adapter!!.addOnListScrollListener(onListScrollListener)
        this.adapter!!.registerAdapterDataObserver(dataObserver)
        this.tabViewResId = tabViewResId
        this.isIndicatorsClickable = indicatorsClickable

        notifyDataSetChanged()
    }

    fun notifyDataSetChanged() {
        //refresh index tabs
        layoutIndexTabs()

        //set currently selected tab
        this.adapter?.let {
            if (it.getActualItemCount() > 0) {
                postDelayed({
                    var firstVisible = ComponentListUtils.getFirstVisiblePosition(it.layoutManager)
                    if (firstVisible < 0) {//cases when recycler is not yet visible (for example)
                        firstVisible = 0
                    }

                    val indexTab = getIndexTab(firstVisible)
                    if (indexTab != null) {
                        indexTab.isSelected = true
                    }
                }, 0)
            }
        }
    }

    fun setIndexBarVisible(visible: Boolean) {
        visibility = if (visible) View.VISIBLE else View.INVISIBLE
    }

    private fun layoutIndexTabs() {
        removeAllViews()

        if (this.tabViewResId == 0) {
            return
        }

        this.adapter?.let {
            for (item in it.getAllItems()) {
                val indexTab = View.inflate(context, tabViewResId, this)
                if (isIndicatorsClickable) {
                    indexTab.isClickable = true
                    indexTab.setOnClickListener(indexTabClickedListener)
                }

                //addView(indexTab);
            }
        }
    }

    private fun getIndexTab(position: Int): View? {
        return if (position in 0 until childCount) {
            getChildAt(position)
        } else null
    }

    fun selectTab(position: Int) {
        //for cyclic cases, these indexes might differ
        val actualCurrentPosition = adapter!!.getActualPosition(position)

        getIndexTab(actualCurrentPosition)?.let {
            selectTab(it)
        }
    }

    private fun selectTab(tab: View) {
        var index = 0

        //don't do anything if already "clicked"
        if (tab.isSelected) {
            return
        }

        /*find index and set selected state*/
        for (i in 0 until childCount) {
            val child = getChildAt(i)
            if (child == tab) {
                index = i
                child.isSelected = true
            } else {
                child.isSelected = false
            }
        }

        //for cyclic cases, these indexes might differ
        this.adapter?.let {
            val actualCurrentPosition = it.getActualFirstVisiblePosition()
            if (actualCurrentPosition != index) {
                it.smoothScroll(index)
            }
        }
    }

    private inner class OnIndexTabClickedListener : OnClickListener {
        override fun onClick(v: View) {
            selectTab(v)
        }
    }

    private inner class OnListScrollListener : RecyclerView.OnScrollListener() {
        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)

            this@ListIndicatorLayout.adapter?.let {
                when (newState) {
                    RecyclerView.SCROLL_STATE_IDLE -> selectTab(
                        ComponentListUtils.getFirstVisiblePosition(it.layoutManager))
                }
            }
        }
    }

    private inner class OnDataUpdatedObserver : RecyclerView.AdapterDataObserver() {
        override fun onChanged() {
            super.onChanged()

            notifyDataSetChanged()
        }
    }
}