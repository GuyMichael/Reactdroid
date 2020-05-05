package com.guymichael.reactdroid.extensions.components.list

import android.app.Activity
import android.view.View
import androidx.annotation.IdRes
import androidx.recyclerview.widget.RecyclerView
import com.guymichael.reactdroid.extensions.components.list.adapter.model.RecyclerComponentViewHolder
import com.guymichael.reactdroid.extensions.components.list.adapter.RecyclerComponentAdapter
import com.guymichael.reactdroid.extensions.components.list.model.ListItemProps
import com.guymichael.reactdroid.core.model.AComponent


class CList(
        v: RecyclerView
        , override val adapter: RecyclerComponentAdapter
    ) : BaseListComponent<ListProps, ListState, RecyclerView>(v) {

    @JvmOverloads
    constructor(v: RecyclerView, orientation: Int = RecyclerView.VERTICAL)
        : this(v, createAdapter(v, orientation))

    init {
        //THINK only if props have uncontrolled_initialScrollIndex (but we can't use props now!)
        adapter.addOnListScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    onListScrollIndexChanged(adapter.getActualLastVisiblePosition())
                }
            }
        })
    }

    override fun createInitialState(props: ListProps) = ListState(props.uncontrolled_initialScrollIndex)

    private fun getScrollIndex(): Int {
        //controlled scroll mode
        return this.props.controlledScroll?.first

        //or uncontrolled
        ?: this.ownState.uncontrolledIndex
    }




    fun onListScrollIndexChanged(newIndex: Int) {
        //controlled scroll mode
        this.props.controlledScroll?.second?.also {
            it.invoke(newIndex)
        }

        //or uncontrolled
        ?: this.setState(ListState(newIndex))
    }



    fun onRender(items: List<ListItemProps>) {
        onRender(ListProps(items))
    }




    private var didFirstRender = false
    override fun render() {
        if( !didFirstRender || adapter.getAllItems() != props.items) { //THINK efficiency
            super.render()
        }

        val scrollIndex = getScrollIndex()
        if (adapter.getActualLastVisiblePosition() != scrollIndex) {
            if (didFirstRender) {
                adapter.smoothScroll(scrollIndex)
            } else {
                adapter.scrollImmediately(scrollIndex)
            }
        }

        didFirstRender = true
    }
}



private fun createAdapter(v: RecyclerView, orientation: Int = RecyclerView.VERTICAL): RecyclerComponentAdapter {
    return RecyclerComponentAdapter(v
        , viewHolderSupplier = ::RecyclerComponentViewHolder
        , orientation = orientation)
}

//THINK as Annotations
//export as a method
fun withList(recycler: RecyclerView, orientation: Int = RecyclerView.VERTICAL)
    = CList(recycler, orientation)

fun View.withList(@IdRes id: Int, orientation: Int = RecyclerView.VERTICAL)
    = CList(findViewById(id), orientation)

fun AComponent<*, *, *>.withList(@IdRes id: Int, orientation: Int = RecyclerView.VERTICAL)
    = CList(mView.findViewById(id), orientation)

fun Activity.withList(@IdRes id: Int, orientation: Int = RecyclerView.VERTICAL)
    = CList(findViewById(id), orientation)