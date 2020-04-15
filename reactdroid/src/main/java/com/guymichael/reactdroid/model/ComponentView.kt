package com.guymichael.reactdroid.model

import android.view.View
import com.guymichael.kotlinreact.model.Component
import com.guymichael.kotlinreact.model.OwnProps
import com.guymichael.kotlinreact.model.OwnState
import com.guymichael.reactdroid.ViewUtils

/**
 * To be used for xml layouts only
 * NOTICE: currently componentWillUnmount() is called AFTER the View had been detached from window (and not before)
 */
@Deprecated("use AComponent")
interface ComponentView<P : OwnProps, S : OwnState> : Component<P, S> {

    override fun isMounted(): Boolean = ViewUtils.isMounted(this as View)

    override fun listenOnMountStateChanges(consumer: (Boolean) -> Unit)
        = ViewUtils.listenOnMountStateChanges(this as View, consumer)
}