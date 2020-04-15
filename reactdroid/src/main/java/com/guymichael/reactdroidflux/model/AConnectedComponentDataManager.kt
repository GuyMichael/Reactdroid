package com.guymichael.reactdroidflux.model

import android.view.View
import com.guymichael.reactdroid.Utils
import com.guymichael.reactdroid.model.AHOC
import com.guymichael.reactdroid.model.WithAComponentDataManager
import com.guymichael.kotlinflux.model.ConnectedComponentDataManager
import com.guymichael.kotlinreact.model.OwnProps
import com.guymichael.kotlinreact.model.OwnState
import com.guymichael.promise.Promise
import com.guymichael.reactdroid.model.AComponent

abstract class AConnectedComponentDataManager<API_PROPS : OwnProps, DATA_PROPS : OwnState>
    : ConnectedComponentDataManager<API_PROPS, DATA_PROPS>() {

    final override fun runOnUiThread(delay: Long, consumer: () -> Unit) {
        Utils.runOnUiThread(delay, consumer)
    }

    companion object {
        /**
         * Same as using [withSimpleDataManager] but dedicated for **connected** Components
         * @param shouldReloadData if left null, default impl. will take place ([shallow equality][AConnectedComponentDataManager.shouldReloadData])
         */
        fun <P : OwnProps> from(
            loader: (P) -> Promise<*>
            , shouldLoadDataOnMount: (P) -> Boolean = { true }
            , shouldReloadData: ((prevProps: P, nextProps: P) -> Boolean)? = null
            , listener: (() -> Unit)? = null
        ) : AConnectedComponentDataManager<P, P> {

            return object : AConnectedComponentDataManager<P, P>() {
                override fun mapPropsToDataProps(ownProps: P): P = ownProps
                override fun onComponentDidMount_isDataAlreadyLoaded(dataProps: P): Boolean {
                    return !shouldLoadDataOnMount(dataProps)
                }

                override fun loadAndCacheData(nextDataProps: P, isFromPageViewOrContextChange: Boolean): Promise<*> {
                    return loader(nextDataProps)
                }

                override fun shouldReloadData(prevDataProps: P, nextDataProps: P): Boolean {
                    //this is the main idea of this class - we override the default prop equality
                    //with a simple original-props shouldLoadData delegate
                    return shouldReloadData?.invoke(prevDataProps, nextDataProps)
                        ?: super.shouldReloadData(prevDataProps, nextDataProps)
                }
            }.apply {
                this.dataListener = listener
            }
        }
    }
}

//export as a method
fun <P : OwnProps, V : View>
withDataManager(
    component: AComponent<P, *, V>
    , loader: (P) -> Promise<*>
    , shouldLoadDataOnMount: (P) -> Boolean = { true }
    , shouldReloadData: ((prevProps: P, nextProps: P) -> Boolean)? = null
    , listener: (() -> Unit)? = null
    ) : AHOC<P, *, V, *> {

    return object : AConnectedComponentDataManager<P, P>() {
        override fun mapPropsToDataProps(ownProps: P): P = ownProps
        override fun onComponentDidMount_isDataAlreadyLoaded(dataProps: P): Boolean {
            return !shouldLoadDataOnMount(dataProps)
        }

        override fun loadAndCacheData(nextDataProps: P, isFromPageViewOrContextChange: Boolean): Promise<*> {
            return loader(nextDataProps)
        }

        override fun shouldReloadData(prevDataProps: P, nextDataProps: P): Boolean {
            //this is the main idea of this class - we override the default prop equality
            //with a simple original-props shouldLoadData delegate
            return shouldReloadData?.invoke(prevDataProps, nextDataProps)
                    ?: super.shouldReloadData(prevDataProps, nextDataProps)
        }
    }.let {
        it.dataListener = listener

        WithAComponentDataManager(component, it)
    }
}

/** Suitable for standard usage - load data if missing in a Store / DataReducer (cache)
 *
 * @param existsInCache if no way to get cache (e.g. missing model id in current apiProps), return null
 * */
fun <P : OwnProps, V : View>
withSimpleDataManager(
    component: AComponent<P, *, V>
    , loader: (P) -> Promise<*>
    , existsInCache: (P) -> Boolean?
) : AHOC<P, *, V, *> {

    return withDataManager(
        component
        , loader
        , shouldLoadDataOnMount = { existsInCache(it) == false }
        , shouldReloadData = { prevProps, nextProps ->
            !prevProps.equals(nextProps) && existsInCache(nextProps) == false
        }
    )
}