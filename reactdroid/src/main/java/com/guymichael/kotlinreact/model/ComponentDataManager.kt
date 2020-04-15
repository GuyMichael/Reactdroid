package com.guymichael.kotlinreact.model

import com.guymichael.kotlinreact.Logger
import com.guymichael.promise.Promise

abstract class ComponentDataManager<P : OwnProps, DATA_PROPS : OwnState>
    : ComponentContextManager<P, DATA_PROPS> {

    var dataListener: (() -> Unit)? = null //THINK weak ref?

    var isDataLoading: Boolean = false
        private set

    /* to implement (or optional) */

    abstract fun mapPropsToDataProps(ownProps: P) : DATA_PROPS
    protected abstract fun onComponentDidMount_isDataAlreadyLoaded(dataProps: DATA_PROPS) : Boolean
    /**
     * @param isFromPageViewOrContextChange true if from didMount or [shouldReloadData_isDueToContextChange] returned 'true'
     */
    abstract fun loadAndCacheData(nextDataProps: DATA_PROPS, isFromPageViewOrContextChange: Boolean)
            : Promise<*>//THINK consider adding a failure callback

    abstract fun runOnUiThread(consumer: () -> Unit)
    protected open fun onDataLoadingFailed(isFromPageViewOrContextChange: Boolean, error: Throwable) {}

    /** will only be called if [shouldReloadData] returned 'true'.
     * Default impl. returns true, as on most cases, if you returned 'true' from shouldReloadData,
     * it means the whole context has changed.
     * Implement yourself for a more complex logic
     *
     * @return true if according to prev/next data props the reloading is somewhat equivalent to
     * a whole re-mount/re-pageView.
     * for example, if this is a restaurant-page, and the restaurant-id just changed,
     * meaning the whole page context is changing, and we can treat this change similar to page-view */
    protected open fun shouldReloadData_isDueToContextChange(prevDataProps: DATA_PROPS
        , nextDataProps: DATA_PROPS) : Boolean = true

    protected open fun shouldReloadData(prevDataProps: DATA_PROPS, nextDataProps: DATA_PROPS) : Boolean {
        return !prevDataProps.equals(nextDataProps)
    }





    /* privates / internal */

    fun onComponentDidMount(props: P) {
        val dataProps = mapPropsToDataProps(props)

        if (onComponentDidMount_isDataAlreadyLoaded(dataProps)) {
            onDataLoadedIntl(true)
        } else {
            loadDataIntl(dataProps, true)
        }
    }

    //make final and pass over to load data (comes from the Component impl. itself, if returned as a context manager for Component.getContextManagerOrNull())
    final override fun onComponentDidMount(props: P, isRemount: Boolean) {
        //THINK avoid two did mounts if somehow misused - both as getContextManagerOrNull() and as a HOC
        onComponentDidMount(props)
    }

    final override fun shouldNotifyComponent(prevDataProps: DATA_PROPS, nextDataProps: DATA_PROPS): Boolean {
        return shouldReloadData(prevDataProps, nextDataProps)
    }

    //make final and pass over to load data
    final override fun onContextPropsChanged(newProps: P, prevProps: P, nextDataProps: DATA_PROPS, prevDataProps: DATA_PROPS) {
        loadDataIntl(nextDataProps, shouldReloadData_isDueToContextChange(prevDataProps, nextDataProps))
    }

    //make final and pass over to map data props
    final override fun mapPropsToContextProps(ownProps: P): DATA_PROPS {
        return mapPropsToDataProps(ownProps)
    }

    private fun loadDataIntl(dataProps: DATA_PROPS, isFromPageViewOrContextChange: Boolean) {
        isDataLoading = true
        loadAndCacheData(dataProps, isFromPageViewOrContextChange)
            .then {
                isDataLoading = false
                onDataLoadedIntl(isFromPageViewOrContextChange)

            }.catch {error ->
                onDataLoadingFailedIntl(isFromPageViewOrContextChange, error)
            }.execute()
    }

    private fun onDataLoadedIntl(isFromPageViewOrContextChange: Boolean) {
        dataListener?.let {
            if (isFromPageViewOrContextChange) {
                runOnUiThread { it.invoke() }
            }
        }
    }

    private fun onDataLoadingFailedIntl(isFromPageViewOrContextChange: Boolean, error: Throwable) {
        Logger.e(javaClass, "loadAndDispatchData() failed: $error")

        this.isDataLoading = false

        onDataLoadingFailed(isFromPageViewOrContextChange, error)
    }

    //make final
    /** Consider as a private method */
    final override fun onComponentDidUpdate(newProps: P, prevProps: P) {
        super.onComponentDidUpdate(newProps, prevProps)
    }
}