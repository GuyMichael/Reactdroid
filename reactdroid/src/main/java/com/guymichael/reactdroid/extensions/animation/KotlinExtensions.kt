package com.guymichael.reactdroid.extensions.animation

import android.view.View
import androidx.annotation.StringRes
import com.guymichael.kotlinreact.model.OwnProps
import com.guymichael.reactdroid.*
import com.guymichael.reactdroid.extensions.components.list.BaseListComponent
import com.guymichael.reactdroid.extensions.components.list.BaseListProps
import com.guymichael.reactdroid.extensions.components.text.TextProps
import com.guymichael.reactdroid.model.AComponent
import com.guymichael.reactdroid.shouldVisibilityUpdate

/**
 * Note: animation will not work if view is not attached to window
 *
 * @param targetVisibility once of [View.GONE], [View.VISIBLE] and [View.INVISIBLE]
 * @param animDuration leave null for default (150 ms)
 * @param animStartDelay leave null for default (0)
 * @param animStartVisibility is null by default to use the 'smart default', per given 'view'.
 *      note: not relevant for fade in (always start with INVISIBLE and alpha 0)
 *
 * @param animStartAlpha is null by default to use the 'smart default', per given 'view'
 *
 * @return true if 'this' view is changing its visibility
 */
fun View.renderVisibility(targetVisibility: Int
        , animate: Boolean
        , animDuration: Long? = null
        , animStartDelay: Long? = null
        , animStartVisibility: Int? = null
        , animStartAlpha: Float? = null
        , vararg visibilityBoundViews: View
    ): Boolean {

    return shouldVisibilityUpdate(this, targetVisibility).also { //THINK avoid this double call
        ViewUtils.applyVisibility(targetVisibility, this, *visibilityBoundViews
            , animate = animate
            , animDuration = animDuration ?: AnimUtils.defaultVisibilityAnimDuration
            , animStartDelay = animStartDelay ?: 0
            , animStartVisibility = animStartVisibility
            , animStartAlpha = animStartAlpha)
    }
}

/**
 * @param targetVisibility once of [View.GONE], [View.VISIBLE] and [View.INVISIBLE]
 * @param animDuration leave null for default (150 ms)
 * @param animStartDelay leave null for default (0)
 * @param animStartVisibility is null by default to use the 'smart default', per given 'view'.
 *      note: not relevant for fade in (always start with INVISIBLE and alpha 0)
 *
 * @param animStartAlpha is null by default to use the 'smart default', per given 'view'
 *
 * @return true if 'this' view is changing its visibility
 * */
fun AComponent<*, *, *>.animateVisibilityWithViews(targetVisibility: Int
        , vararg visibilityBoundViews: View
        , animDuration: Long? = null
        , animStartDelay: Long? = null
        , animStartVisibility: Int? = null
        , animStartAlpha: Float?= null
    ): Boolean {

    return mView.renderVisibility(
        targetVisibility = targetVisibility
        , animate = true
        , animDuration = animDuration
        , animStartDelay = animStartDelay
        , animStartVisibility = animStartVisibility
        , animStartAlpha = animStartAlpha
        , visibilityBoundViews = *visibilityBoundViews
    )
}

/**
 * @param targetVisibility once of [View.GONE], [View.VISIBLE] and [View.INVISIBLE]
 * @param animDuration leave null for default (150 ms)
 * @param animStartDelay leave null for default (0)
 * @param animStartVisibility is null by default to use the 'smart default', per given 'view'.
 *      note: not relevant for fade in (always start with INVISIBLE and alpha 0)
 *
 * @param animStartAlpha is null by default to use the 'smart default', per given 'view'
 *
 * NOTICE: if you use this to hide, you should only use this method to show, as hiding changes alpha
 *
 * @return true if 'this' view is changing its visibility
 * */
fun AComponent<*, *, *>.animateVisibility(targetVisibility: Int
        , vararg visibilityBoundComponents: AComponent<*, *, *>
        , animDuration: Long? = null
        , animStartDelay: Long? = null
        , animStartVisibility: Int? = null
        , animStartAlpha: Float?= null
    ): Boolean {

    return mView.renderVisibility(
        targetVisibility = targetVisibility
        , animate = true
        , animDuration = animDuration
        , animStartDelay = animStartDelay
        , animStartVisibility = animStartVisibility
        , animStartAlpha = animStartAlpha
        , visibilityBoundViews = *(visibilityBoundComponents.map { it.mView }.toTypedArray())
    )
}

/** if props are not null, consumer will be called and this View's visibility will be set to visible.
 * Otherwise - if props are null, View's visibility will be set to 'visibilityIfNull'
 *
 * @see [renderVisibility]
 *
 * @return true if 'this' visibility is changing to visible (View.VISIBLE) */
inline fun <V : View, T> V.applyOrVisibility(input: T?
        , visibilityIfNull: Int
        , crossinline consumer: V.(T) -> Unit
        , animateVisibility: Boolean = false
        , animDuration: Long? = null
        , animStartDelay: Long? = null
        , animStartVisibility: Int? = null
        , animStartAlpha: Float?= null
        , vararg visibilityBoundViews: View
    ): Boolean {

    input?.let {
        consumer.invoke(this, it)
    }

    val hasInput = input != null

    return renderVisibility(
        if (hasInput) View.VISIBLE else visibilityIfNull
        , animateVisibility, animDuration, animStartDelay, animStartVisibility, animStartAlpha
        , *visibilityBoundViews
    ) && hasInput
}

/** if input is not null, consumer will be called and this View's visibility will be set to visible.
 * Otherwise, View's visibility will be set to gone
 *
 * @see [applyOrVisibility] */
inline fun <V : View, T> V.applyOrGone(input: T?
        , crossinline consumer: V.(T) -> Unit
        , animateVisibility: Boolean = false
        , animDuration: Long? = null
        , animStartDelay: Long? = null
        , animStartVisibility: Int? = null
        , animStartAlpha: Float?= null
        , vararg visibilityBoundViews: View
    ): Boolean {

    return applyOrVisibility(input, View.GONE, consumer
        , animateVisibility
        , animDuration
        , animStartDelay
        , animStartVisibility
        , animStartAlpha
        , *visibilityBoundViews
    )
}

/** if input is not null, consumer will be called and this View's visibility will be set to visible.
 * Otherwise, View's visibility will be set to gone
 *
 * @see [applyOrVisibility] */
fun <P : BaseListProps, C : BaseListComponent<P, *, *>> C.renderOrGone(props: P?
        , vararg visibilityBoundViews: View
        , animateVisibility: Boolean = false
        , animDuration: Long? = null
        , animStartDelay: Long? = null
        , animStartVisibility: Int? = null
        , animStartAlpha: Float?= null
    ): Boolean {

    return mView.applyOrVisibility(props, View.GONE, { onRender(it) }
        , animateVisibility
        , animDuration
        , animStartDelay
        , animStartVisibility
        , animStartAlpha
    ).also {

        //note: the special case with RecyclerComponent, which differs it from just any View for
        //      'renderOrGone' is that, recyclers take time to render after data changes.
        //      So updating bound views before the data actually rendered looks weird.
        //      Basically, we just need to wait for the data observer to update the visibility changes
        if (visibilityBoundViews.isNotEmpty()) {
            /*adapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
                override fun onChanged() {
                    adapter.unregisterAdapterDataObserver(this) //THINK weak ref
                }
            })*/
            //THINK we use 'post' instead of waiting for data change, as we can't be sure there WILL
            // be a data change. Can we?
            mView.post {
                ViewUtils.applyVisibility(
                    viewVisibilityOf(props != null, View.GONE)
                    , *visibilityBoundViews
                    , animate = animateVisibility
                    , animDuration = animDuration ?: AnimUtils.defaultVisibilityAnimDuration
                    , animStartDelay = animStartDelay ?: 0
                    , animStartVisibility = animStartVisibility
                    , animStartAlpha = animStartAlpha)
            }
        }
    }
}

fun <P : OwnProps> AComponent<P, *, *>.renderOrGone(props: P?
        , vararg visibilityBoundComponents: AComponent<*, *, *>
        , animateVisibility: Boolean = false
        , animDuration: Long? = null
        , animStartDelay: Long? = null
        , animStartVisibility: Int? = null
        , animStartAlpha: Float? = null
    ) {

    mView.applyOrVisibility(props, View.GONE, { onRender(it) }
        , animateVisibility
        , animDuration
        , animStartDelay
        , animStartVisibility
        , animStartAlpha
        , *visibilityBoundComponents.map { it.mView }.toTypedArray()
    )
}

fun <P : OwnProps> AComponent<P, *, *>.renderOrGoneWithViews(props: P?
        , vararg visibilityBoundViews: View
        , animateVisibility: Boolean = false
        , animDuration: Long? = null
        , animStartDelay: Long? = null
        , animStartVisibility: Int? = null
        , animStartAlpha: Float? = null
    ) {

    mView.applyOrVisibility(props, View.GONE, { onRender(it) }
        , animateVisibility
        , animDuration
        , animStartDelay
        , animStartVisibility
        , animStartAlpha
        , *visibilityBoundViews
    )
}

fun AComponent<TextProps, *, *>.renderTextOrVisibility(text: CharSequence?
        , visibilityIfNull: Int
        , animateVisibility: Boolean = false
        , animDuration: Long? = null
        , animStartDelay: Long? = null
        , animStartVisibility: Int? = null
        , animStartAlpha: Float? = null
        , vararg visibilityBoundComponents: AComponent<*, *, *>
    ) {

    mView.applyOrVisibility(
        text?.takeIf { !it.isBlank() }?.let(::TextProps)
        , visibilityIfNull
        , { onRender(it) }
        , animateVisibility
        , animDuration
        , animStartDelay
        , animStartVisibility
        , animStartAlpha
        , *visibilityBoundComponents.map { it.mView }.toTypedArray()
    )
}

fun AComponent<TextProps, *, *>.renderTextOrGone(text: CharSequence?
        , vararg visibilityBoundComponents: AComponent<*, *, *>
        , animateVisibility: Boolean = false
        , animDuration: Long? = null
        , animStartDelay: Long? = null
        , animStartVisibility: Int? = null
        , animStartAlpha: Float? = null
    ) {

    renderTextOrVisibility(text, View.GONE
        , animateVisibility
        , animDuration
        , animStartDelay
        , animStartVisibility
        , animStartAlpha
        , *visibilityBoundComponents
    )
}

fun AComponent<TextProps, *, *>.renderTextRes(@StringRes res: Int
        , vararg visibilityBoundComponents: AComponent<*, *, *>
        , animateVisibility: Boolean = false
        , animDuration: Long? = null
        , animStartDelay: Long? = null
        , animStartVisibility: Int? = null
        , animStartAlpha: Float? = null) {

    renderTextOrVisibility(mView.getText(res), mView.visibility
        , animateVisibility
        , animDuration
        , animStartDelay
        , animStartVisibility
        , animStartAlpha
        , *visibilityBoundComponents
    )
}