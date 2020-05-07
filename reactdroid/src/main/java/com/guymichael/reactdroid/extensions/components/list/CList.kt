package com.guymichael.reactdroid.extensions.components.list

import android.app.Activity
import android.view.View
import androidx.annotation.IdRes
import androidx.recyclerview.widget.RecyclerView
import com.guymichael.kotlinreact.model.EmptyOwnState
import com.guymichael.reactdroid.extensions.components.list.adapter.model.RecyclerComponentViewHolder
import com.guymichael.reactdroid.extensions.components.list.adapter.RecyclerComponentAdapter
import com.guymichael.reactdroid.extensions.components.list.model.ListItemProps
import com.guymichael.reactdroid.core.model.AComponent
import com.guymichael.reactdroid.extensions.components.list.model.ListProps

class CList(
        v: RecyclerView
        , override val adapter: RecyclerComponentAdapter
    ) : BaseListComponent<ListProps, EmptyOwnState, RecyclerView>(v) {

    @JvmOverloads
    constructor(v: RecyclerView, orientation: Int = RecyclerView.VERTICAL)
    : this(v, createAdapter(v, orientation))


    /* lifecycle */

    override fun createInitialState(props: ListProps) = EmptyOwnState

    override fun componentDidMount() {
        //controlled scroll from props
        if (getScrollIndex() != null) {
            // listen on user scroll changes to update parent (props' callback)
            listenOnScrollStateChanges()
        }

        initialScrollIfNeeded()
    }

    override fun componentDidUpdate(prevProps: ListProps, prevState: EmptyOwnState, snapshot: Any?) {
        initialScrollIfNeeded()
    }




    /* API */
    fun onRender(items: List<ListItemProps>) {
        onRender(ListProps(items))
    }




    /* privates */

    private fun initialScrollIfNeeded() {
        props.initialScrollIndex
            ?.takeIf { it > 0 && !didFirstScrollAdapter && props.items.isNotEmpty() }?.also {
                scrollAdapter(it, false)
            }
    }

    private fun getScrollIndex(props: ListProps = this.props): Int? {
        //controlled scroll mode
        return props.controlledScroll?.first
    }

    private fun listenOnScrollStateChanges() {
        adapter.addOnListScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    props.controlledScroll?.second?.invoke(adapter.getActualFirstVisiblePosition())
                }
            }
        })
    }





    /* render */

    private var didFirstScrollAdapter = false
    private fun scrollAdapter(index: Int, smoothScroll: Boolean) {
        if (smoothScroll) {
            adapter.smoothScroll(index)
        } else {
            adapter.scrollImmediately(index)
        }
        didFirstScrollAdapter = true
    }

    private fun renderScrollPosition(scrollIndex: Int) {
        if (adapter.itemCount > 0) {
            if (adapter.getActualFirstVisiblePosition() != scrollIndex) {
                scrollAdapter(scrollIndex, true)
            }
        }
    }

    private var didFirstRender = false
    override fun render() {

        if( !didFirstRender || adapter.getAllItems() != props.items) { //THINK efficiency
            //notify data set changed
            super.render()
        }

        //update adapter's scroll position
        getScrollIndex()?.also {
            //end of execution queue to let recycler itself update with new data
            mView.post { renderScrollPosition(it) }
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