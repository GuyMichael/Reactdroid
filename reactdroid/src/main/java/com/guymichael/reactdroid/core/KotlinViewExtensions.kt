package com.guymichael.reactdroid.core

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Build
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.*
import androidx.core.content.ContextCompat
import com.guymichael.apromise.APromise
import com.guymichael.kotlinreact.Logger
import com.guymichael.kotlinreact.model.OwnProps
import com.guymichael.reactdroid.core.model.AComponent
import io.reactivex.rxjava3.disposables.Disposable

inline fun <T, S> T.runNotNull(s: S?, crossinline block: T.(S) -> T): T {
    return if (s == null) this
        else block(this, s)
}

inline fun <T> T.letIf(condition: (T) -> Boolean, crossinline block: (T) -> T) :T {
    return this.takeIf(condition)?.let(block) ?: this
}

/** if input is not null, consumer will be called and this View's visibility will be set to visible.
 * Otherwise, View's visibility will be set to gone */
inline fun <V : View, T> V.applyOrElse(prop: T?
       , crossinline consumer: V.(T) -> Unit
       , crossinline orElse: V.() -> Unit): V {

    prop?.also {
        consumer(it)

    } ?: orElse()

    return this
}

/** if props are not null, consumer will be called and this View's visibility will be set to visible.
 * Otherwise - if props are null, View's visibility will be set to 'visibilityIfNull' */
inline fun <V : View, T> V.applyOrVisibility(props: T?, visibilityIfNull: Int
        , crossinline consumer: V.(T) -> Unit, vararg visibilityBoundViews: View): V {

    props?.let {
        consumer(it)
    }

    (if (props == null) visibilityIfNull else View.VISIBLE).let { visibility ->
        if (this.visibility != visibility) {
            this.visibility = visibility
        }

        visibilityBoundViews.forEach { v ->
            if (v.visibility != visibility) {
                v.visibility = visibility
            }
        }
    }

    return this
}

/** if props are not null, consumer will be called and this View's visibility will be set to visible.
 * Otherwise - if props are null, View's visibility will be set to gone */
inline fun <V : View, T> V.applyOrGone(props: T?, crossinline consumer: V.(T) -> Unit, vararg visibilityBoundViews: View): V {
    return applyOrVisibility(props, View.GONE, consumer, *visibilityBoundViews)
}

/** if props are not null, consumer will be called and this View's visibility will be set to visible.
 * Otherwise - if props are null, View's visibility will be set to invisible */
inline fun <V : View, T> V.applyOrInvisible(props: T?, crossinline consumer: V.(T) -> Unit, vararg visibilityBoundViews: View): V {
    return applyOrVisibility(props, View.INVISIBLE, consumer, *visibilityBoundViews)
}

/** if input is not null AND not empty (after trim),
 * text will be set and visibility will be set to visible.
 * Otherwise, View's visibility will be set to gone */
inline fun <V : TextView, T : CharSequence> V.applyTextOrGone(text: T?
        , crossinline consumer: V.(T) -> Unit): V {

    return applyOrGone(text?.takeIf { it.isNotBlank() }, consumer)
}

/** if input is not null AND not empty (after trim),
 * text will be set and visibility will be set to visible.
 * Otherwise, View's visibility will be set to invisible */
inline fun <V : TextView, T : CharSequence> V.applyTextOrInvisible(text: T?
        , crossinline consumer: V.(T) -> Unit): V {

    return applyOrInvisible(text?.takeIf { it.isNotBlank() }, consumer)
}

/** if input is not null AND not empty (after trim),
 * text will be set and visibility will be set to visible.
 * Otherwise, View's visibility will be set to gone */
fun <V : TextView, T : CharSequence> V.textOrGone(text: T?): V {
    return applyTextOrGone(text) {
        this.text = it
    }
}

/** if input is not null AND not empty (after trim),
 * text will be set and visibility will be set to visible.
 * Otherwise, View's visibility will be set to invisible */
fun <V : TextView, T : CharSequence> V.textOrInvisible(text: T?): V {
    return applyTextOrInvisible(text) {
        this.text = it
    }
}

/** sets StringRes text if res is not null and not 0, or null otherwise */
fun <V : TextView> V.textOrNull(@StringRes res: Int?): V {
    return applyOrElse(res?.takeIf { it != 0 }, {
        this.setText(it)
    }) { //or else
        this.text = null
    }
}

fun <P : OwnProps> AComponent<P, *, *>.renderOrGone(props: P?, vararg visibilityBoundViews: View) {
    mView.applyOrGone(props, {
        onRender(it)
    }, *visibilityBoundViews)
}

fun <P : OwnProps> AComponent<P, *, *>.renderOrInvisible(props: P?, vararg visibilityBoundViews: View) {
    mView.applyOrInvisible(props, {
        onRender(it)
    }, *visibilityBoundViews)
}

fun AComponent<*, *, *>.renderBackground(@DrawableRes res: Int?) {
    mView.renderBackground(res)
}

fun AComponent<*, *, *>.renderBackgroundColor(color: Int?) {
    mView.renderBackgroundColor(color)
}

fun AComponent<*, *, *>.renderActivated(active: Boolean) {
    mView.isActivated = active
}





fun View.renderBackground(@DrawableRes res: Int?) {
    if (res != null && res != 0) {
        setBackgroundResource(res) //checks for res != prevRes internally
    } else {
        background = null
    }
}

fun View.renderBackgroundColor(color: Int?) {
    if (color != null) {
        //TODO try to check if current color == 'color'
//        background?.colorFilter
        setBackgroundColor(color)
    } else {
        background = null //checks for drawable != prevDrawable internally
    }
}

@RequiresApi(Build.VERSION_CODES.M)
fun TextView.renderDrawableTint(@ColorRes res: Int?) {
    //THINK try to check if current res == 'res'
    compoundDrawableTintList = if (res == null) {
        null
    } else {
        ColorStateList.valueOf(getColor(res))
    }
}

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
fun View.renderBackgroundTint(@ColorRes res: Int?) {
    //THINK try to check if current res == 'res'
    if (res == null) {
        background?.setTintList(null)
    } else {
        background?.setTint(getColor(res))
    }
}

fun View.renderMarginsPx(top: Int? = null, start: Int? = null, bottom: Int? = null, end: Int? = null) {
    layoutParams?.apply { if (this is ViewGroup.MarginLayoutParams) {

        if (top != null && top != topMargin) {
            topMargin = top
        }

        if (start != null && start != marginStart) {
            marginStart = start
        }

        if (bottom != null && bottom != bottomMargin) {
            bottomMargin = bottom
        }

        if (end != null && end != marginEnd) {
            marginEnd = end
        }

    }} ?: run {
        Logger.w("React_KotlinViewExtensions", "setMargins() failed, view's layoutParams are null")
        Throwable("setMargins() failed, view's layoutParams are null").printStackTrace()
    }
}

fun View.renderMarginsRes(@DimenRes top: Int? = null, @DimenRes start: Int? = null
                          , @DimenRes bottom: Int? = null, @DimenRes end: Int? = null) {

    renderMarginsPx(top = top?.let { getDimenPx(it) }, start = start?.let { getDimenPx(it) }
        , bottom = bottom?.let { getDimenPx(it) }, end = end?.let { getDimenPx(it) }
    )
}

fun View.isVisible(): Boolean {
    return this.visibility == View.VISIBLE
}

fun View.getString(@StringRes res: Int, vararg format: Any): String? {
    return this.context?.getString(res, *format)
}

/** Uses `View`'s `context` to load text resource
 * @return text or null if `context` is null */
fun View.getText(@StringRes res: Int): CharSequence? {
    return this.context?.getText(res)
}

fun View.getColor(@ColorRes res: Int, orDefault: Int = Color.BLACK): Int {
    return context?.takeIf { res != 0 }?.let { ContextCompat.getColor(it, res) } ?: orDefault
}

fun View.getDimenPx(@DimenRes res: Int, orDefault: Int = 0): Int {
    return resources?.getDimensionPixelSize(res) ?: orDefault
}

/** applies `fitsSystemWindows(true)` behavior for top layouts (e.g. FrameLayout)
 * which aren't CoordinatorLayout
 *
 * @param excludeChildren child Views to apply `fitsSystemWindows(false)` to,
 * e.g. a Toolbar - as this method will apply `true` to all children by default
 * */
@RequiresApi(Build.VERSION_CODES.KITKAT_WATCH)
fun View.fitSystemWindowsNonCoordinator(vararg excludeChildren: View) {
    // set 'fitSystemWindows' = true, on non-CoordinatorLayout
    // https://proandroiddev.com/draw-under-status-bar-like-a-pro-db38cfff2870
    this.systemUiVisibility =
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN

    for (children in excludeChildren) {
        children.setOnApplyWindowInsetsListener { view, insets ->
            view?.renderMarginsPx(top = insets.systemWindowInsetTop)
            insets
        }
    }
}

fun androidx.recyclerview.widget.RecyclerView.children(): List<View> {
    return ArrayList<View>().apply {
        for (i in 0 until childCount) {
            add(getChildAt(i))
        }
    }
}

fun APromise.Companion.delayWhileAlive(component: AComponent<*, *, *>, ms: Long, consumer: () -> Unit)
        : Disposable {
    return delayWhileAlive(component.mView, ms, consumer)
}

fun APromise.Companion.delayWhileAlive(component: AComponent<*, *, *>, vararg delayMsToConsumer: Pair<Long, () -> Unit>)
        : Disposable {

    return delayMsToConsumer.map { delayWhileAlive(component.mView, it.first, it.second) }
        .let { disposables -> object : Disposable {
            override fun isDisposed() = disposables.all(Disposable::isDisposed)

            override fun dispose() {
                disposables.forEach {
                    if( !it.isDisposed) {
                        it.dispose()
                    }
                }
            }
        }}
}

fun <V : View> V.waitForMeasure(): APromise<V> {
    return ViewUtils.waitForViewMeasure(this)
}