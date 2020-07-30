package com.guymichael.reactdroid.core

import android.graphics.Point
import android.graphics.Rect
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.view.animation.Interpolator
import android.view.animation.LinearInterpolator
import android.widget.HorizontalScrollView
import android.widget.ScrollView
import android.widget.TextView
import androidx.core.view.ViewCompat
import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.RecyclerView
import com.guymichael.apromise.APromise
import com.guymichael.kotlinreact.Logger
import com.guymichael.promise.Promise
import com.guymichael.reactdroid.extensions.animation.AnimUtils
import com.guymichael.reactdroid.core.model.android.SimpleTextWatcher
import java.lang.ref.WeakReference
import kotlin.reflect.KClass

object ViewUtils {

    fun isFinishedInflate(view: View): Boolean {
        return ViewCompat.isLaidOut(view) || view.run { width > 0 && height > 0 }
        //THINK size check is good? Actually, the best would be isPassedFirstRender on a component.
        // Note: size check IS very important, or onAttach callbacks while isLaidOut is false will
        // fail to render otherwise (isMounted will return false inside onRender() ).
        // This typically happens for recycler items, after they are first recycled
    }

    /** True when attached to window AND laid out for the first time */
    fun isMounted(view: View): Boolean {
        return ViewCompat.isAttachedToWindow(view) && isFinishedInflate(
            view
        )
    }

    /**
     * Note that the consumer may be called back before
     * the View has been laid out for the first time */
    fun listenOnMountStateChanges(view: View, consumer: (Boolean) -> Unit) {
        waitForOnFinishInflate(view) //pre-condition for both mount states
            .then { v ->

                //if already mounted, notify. It is ok by contract - see Component's method with similar name
                if (ViewCompat.isAttachedToWindow(v)) {
                    //already attached by finish inflate time.
                    //most cases come here. Notable exception - recycler items
                    consumer(true)
                }

                view.addOnAttachStateChangeListener(object: View.OnAttachStateChangeListener {
                    override fun onViewAttachedToWindow(v: View) {
                        consumer(true)
                    }//note: isLaidOut() may still return 'false' at this point, specifically for recycled items

                    override fun onViewDetachedFromWindow(v: View?) {
                        consumer(false)
                    }//note: isMounted() may still return 'true' at this point on some occasions
                })
            }
            .catch {
                Logger.e("ViewUtils", "waitForOnFinishInflate error: $it")
                it.printStackTrace()
            }
            .execute()
    }

    /**
     * note: Promise calls back immediately if 'view' has already been inflated (laid out)
     * by the time of execution
     */
    fun <V : View> waitForOnFinishInflate(view: V, timeoutMs: Long? = null) : APromise<V> {
        val viewRef = WeakReference(view)

        return APromise.ofWeakRefOrCancel(viewRef).thenAwait { v ->
            //if already inflated, call back now
            if (isFinishedInflate(v)) {
                Promise.of(v)
            } else {
                waitForNextOnDraw(v) or waitForNextGlobalLayout(
                    v
                )
            }
        }
        .runNotNull(timeoutMs) { timeout(it) }
    }

    fun <V : View> waitForAttachToWindow(v: V): APromise<V> {
        val viewRef = WeakReference(v)

        return APromise.ofCallback { promiseCallback -> viewRef.get()?.also { view ->
            //if already mounted, notify immediately
            if (ViewCompat.isAttachedToWindow(view)) {
                //already attached by finish inflate time.
                //most cases come here. Notable exception - recycler items
                promiseCallback.onSuccess(view)
            } else {
                view.addOnAttachStateChangeListener(object: View.OnAttachStateChangeListener {
                    override fun onViewDetachedFromWindow(v: View) {}

                    override fun onViewAttachedToWindow(v: View) {
                        v.removeOnAttachStateChangeListener(this)
                        @Suppress("UNCHECKED_CAST")
                        promiseCallback.onSuccess(v as V)
                    }//note: isLaidOut() may still return 'false' at this point, specifically for recycled items
                })
            }


        } ?: promiseCallback.onCancel("view GC'd") }
    }

    /**
     * Wait for View's first measure which returns height **and** width > 0.
     * <br></br>If view size is already > 0, *listener* will be called immediately
     * @param view
     * @param listener
     */
    fun <T : View> waitForViewMeasure(view: T, listener: (view: T, width: Int, height: Int) -> Unit)
        : OnGlobalLayoutListener? {

        if (view.height > 0 && view.width > 0) {
            listener(view, view.width, view.height)
            return null
        }

        val viewWeakReference = WeakReference(view)

        return object : OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                viewWeakReference.get()?.let {
                    val width = it.width
                    val height = it.height

                    if (width > 0 && height > 0) {
                        removeViewGlobalLayoutListener(
                            it,
                            this
                        )

                        listener(it, width, height)
                    }
                }
            }
        }.also {
            view.viewTreeObserver.addOnGlobalLayoutListener(it)
        }
    }

    fun <T : View> waitForViewMeasure(view: T): APromise<T> {
        val viewRef: WeakReference<T> = WeakReference(view)

        return APromise.ofCallback<T, ViewTreeObserver.OnGlobalLayoutListener?>({ promiseCallback ->
            viewRef.get()?.let {
                waitForViewMeasure(it) { v, _, _ ->
                    promiseCallback.onSuccess(v)
                }
            } ?: run {
                promiseCallback.onFailure(Throwable("View ref released"))
                null
            }

        //finally, release listener if set
        }) { observer ->
            observer?.let { o -> viewRef.get()?.let { v ->
                removeViewGlobalLayoutListener(
                    v,
                    o
                )
            }}
        }
    }

    fun <V : View> waitForNextOnDraw(view: V) : Promise<V> {
        val viewRef = WeakReference(view)

        return APromise.ofCallback<V, ViewTreeObserver.OnDrawListener?>({ promiseCallback ->
            val v = viewRef.get()

            if (v == null) {
                promiseCallback.onCancel("waitForNextOnDraw: View was garbage collected")
                null
            } else {
                try {
                    createOnDrawListener(
                        viewRef,
                        promiseCallback
                    ).also {
                        v.viewTreeObserver.addOnDrawListener(it)
                    }
                } catch (e: IllegalStateException) {
                    if (e.message?.contains("Cannot call addOnDrawListener inside of onDraw") == true) {
                        //as the message suggests, we're current onDrawing. So we may wait for it to finish
                        //and then call back! no need to listen.. THINK better way?
                        v.post { //posting at end of execution queue should delay until after the onDraw finishes
                            promiseCallback.onSuccess(v)
                        }
                    } else {
                        //unknown reason
                        promiseCallback.onFailure(e)
                    }
                    null
                }
            }

            //finally - unregister
        }) { it?.let { listener -> viewRef.get()?.apply {
            //we need 'post' as removing the onDraw during its callback crashes
            post { viewTreeObserver.removeOnDrawListener(listener) }
        }}}
    }

    fun <V : View> waitForNextGlobalLayout(view: V) : APromise<V> {
        val viewRef = WeakReference(view)

        return APromise.ofCallback<V, ViewTreeObserver.OnGlobalLayoutListener>({ promiseCallback ->

            (object : ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() { viewRef.get()?.let {
                    //remove here as well (before finally) to prevent race conditions THINK needed?
                    removeViewGlobalLayoutListener(
                        it,
                        this
                    )

                    promiseCallback.onSuccess(it)
                }}

                //register
            }).apply { viewRef.get()?.viewTreeObserver?.addOnGlobalLayoutListener(this) }

            //finally - unregister
        }) {
            removeViewGlobalLayoutListener(
                viewRef.get(),
                it
            )
        }
    }








    /**
     * @param maxIterations maximum parent-hierarchy steps to search. Min value is `1`
     * @return found parent if inherits-from/with-same-class-as 'cls' T, or null if not-found/max-iterations-reached
     */
    fun <T : View> findParent(child: View, cls: KClass<T>, maxIterations: Int = Int.MAX_VALUE): T? {
        //loop through all parents
        var currentParent = child
        var i = 0
        do {
            if (currentParent.parent == null || currentParent.parent !is View) {
                return null
            }
            currentParent = currentParent.parent as View
            i+=1
        } while (i < maxIterations && !cls.isInstance(currentParent))

        @Suppress("UNCHECKED_CAST")
        return currentParent as T
    }

    fun isParent(child: View, possibleParent: View): Boolean {
        //loop through all parents
        var currentParent = child
        do {
            if (currentParent.parent == null || currentParent.parent !is View) {
                return false
            }
            currentParent = currentParent.parent as View
        } while (currentParent !== possibleParent)

        return true
    }

    /** @param childMightBeParent if true, and `child` is in `possibleParents`, it will be returned */
    fun findAnyParent(child: View, possibleParents: List<View>, childMightBeParent: Boolean): View? {
        return possibleParents.firstOrNull {
            (childMightBeParent && child === it) //check by object ref (not equals() )
                || isParent(child, it)
        }
    }

    fun findAnyScrollingParent(child: View): ViewGroup? {
        return findParent(
            child,
            NestedScrollView::class
        )
            ?: findParent(
                child,
                ScrollView::class
            )
            ?: findParent(
                child,
                RecyclerView::class
            )
    }












    /**
     * Should be called from the UI Thread.
     * @param parent
     * @param child doesn't have to be a direct child of 'parent'
     * @param gravity to locate the child according to. Correlates to [Gravity.TOP], [Gravity.BOTTOM] or [Gravity.CENTER]/[Gravity.CENTER_VERTICAL]
     */
    fun smoothScrollToChild(parent: ScrollView, child: View, gravity: Int) {
        parent.smoothScrollTo(0,
            getScrollChildInParentVerticalOffsetIntl(
                parent,
                child,
                gravity
            )
        )
    }

    /**
     * Should be called from the UI Thread.
     * @param parent
     * @param child doesn't have to be a direct child of 'parent'
     * @param gravity to locate the child according to. Correlates to [Gravity.TOP], [Gravity.BOTTOM] or [Gravity.CENTER]/[Gravity.CENTER_VERTICAL]
     */
    fun smoothScrollToChild(parent: NestedScrollView, child: View, gravity: Int) {
        parent.smoothScrollTo(0,
            getScrollChildInParentVerticalOffsetIntl(
                parent,
                child,
                gravity
            )
        )
    }

    /**
     * Should be called from the UI Thread.
     * @param parent
     * @param child doesn't have to be a direct child of 'parent'
     * @param gravity to locate the child according to. Correlates to [Gravity.LEFT], [Gravity.RIGHT] or [Gravity.CENTER]/[Gravity.CENTER_HORIZONTAL]
     */
    fun smoothScrollToChild(parent: HorizontalScrollView, child: View, gravity: Int) {
        parent.smoothScrollTo(
            getScrollChildInParentHorizontalOffsetIntl(parent, child, gravity)
            , 0
        )
    }

    /**
     * Should be called from the UI Thread.
     * @param parent
     * @param child doesn't have to be a direct child of 'parent'
     */
    fun smoothScrollToChild(parent: RecyclerView, child: View) {
        findAnyParent(child, parent.children(), true)?.let { directRecyclerChild ->
        parent.getChildAdapterPosition(directRecyclerChild)
        .takeIf { it != RecyclerView.NO_POSITION }?.let { directChildPosition ->

            parent.smoothScrollToPosition(directChildPosition)
        }}
    }

    /**
     * Smoothly scrolls to a child inside a parent view, provided that parent is one of
     * [NestedScrollView], [ScrollView] or [HorizontalScrollView]
     * @param parent
     * @param child
     * @param gravity
     */
    @JvmStatic
    fun smoothScrollToChild(parent: View, child: View, gravity: Int) {
        when (parent) {
            is NestedScrollView     -> smoothScrollToChild(parent, child, gravity)
            is ScrollView           -> smoothScrollToChild(parent, child, gravity)
            is HorizontalScrollView -> smoothScrollToChild(parent, child, gravity)
            is RecyclerView -> smoothScrollToChild(parent, child)
            else -> Logger.w("ViewUtils","smoothScrollToChild() : parent of class " + parent.javaClass.simpleName + " is not supported")
        }
    }

    fun smoothScrollAnyScrollingParentToChild(child: View, gravity: Int) {
        findAnyScrollingParent(child)?.let {
            smoothScrollToChild(
                it,
                child,
                gravity
            )
        }
    }






    fun waitForNextTextChange(textView: TextView): APromise<() -> String?> {
        val textRef = WeakReference(textView)

        return APromise.ofCallback<() -> String?, SimpleTextWatcher>({ promiseCallback ->
            (object : SimpleTextWatcher {
                override fun onTextChanged(text: String?) {
                    textRef.get()?.let {
                        //register now to be safe (also has in finally) THINK needed?
                        it.removeTextChangedListener(this)
                    }

                    promiseCallback.onSuccess { text }
                }

                //register
            }).apply { textView.addTextChangedListener(this) }

            //finally, unregister
        }) { textRef.get()?.removeTextChangedListener(it) }
    }

    /**
     * @param targetVisibility one of [View.GONE], [View.VISIBLE] and [View.INVISIBLE]
     * @param animStartVisibility is null by default to use the 'smart default', per given 'view'
     * @param animStartAlpha is null by default to use the 'smart default', per given 'view'
     */
    fun applyVisibility(targetVisibility: Int, vararg views: View
            , animate: Boolean = false
            , animDuration: Long = 0
            , animStartDelay: Long = 0
            , animStartVisibility: Int? = null
            , animStartAlpha: Float? = null
            , interpolator: Interpolator = LinearInterpolator()
        ) {

        views.forEach { it.takeIf { shouldVisibilityUpdate(it, targetVisibility) }?.let { v ->
            when {
                animate -> {
                    val startAlpha = animStartAlpha
                    //default: address all 3 method types' defaults (see method impl.)
                        ?: if (targetVisibility != View.VISIBLE || v.visibility == View.VISIBLE) v.alpha else 0F

                    val startVisibility = animStartVisibility
                    //default: address all 2-relevant method types' defaults (see method impl.)
                        ?: v.visibility

                    when (targetVisibility) {
                        //THINK prevent re-animation if already animating to same destination? using View TAG?
                        View.VISIBLE -> AnimUtils.animateFadeIn(v, animDuration, startAlpha
                            , animStartDelay, interpolator).execute()
                        View.GONE -> AnimUtils.animateFadeOutAndGone(v, animDuration, startAlpha
                            , animStartDelay, startVisibility, interpolator).execute()
                        View.INVISIBLE -> AnimUtils.animateFadeOutAndInvisible(v, animDuration, startAlpha
                            , animStartDelay, startVisibility, interpolator).execute()
                        else -> {}
                    }

                }

                v.visibility != targetVisibility -> {
                    v.visibility = targetVisibility

                    //note: we don't force alpha here. This is done only when animating,
                    //      in which case it's obviously needed
                }

                else -> {}
            }

        } ?: Unit}
    }





    /**
     * Waits until `view` [is visible][ViewUtils.isViewVisibleInParent].
     * If currently visible, listener will be invoked immediately
     * @param view
     * @param fullyVisible whether `view` should be fully visible or partly
     * @param atLeastMs the minimum required 'on-screen' time to wait before invoking `listener`
     */
    fun waitForViewOnScreen(view: View, fullyVisible: Boolean, atLeastMs: Int): APromise<View> {
        val screenView = Utils.getActivityView(view.context)
            ?: return APromise.ofReject("Activity not found for view $view") //THINK wait for it?

        return waitForViewOnScreenIntl(view, screenView, fullyVisible)
            .letIf({ atLeastMs > 0 }) { p ->
                p.thenAwait { v ->
                    APromise.ofDelayOrCancel(v, atLeastMs.toLong())
                        .thenAwait { waitForViewOnScreenIntl(it, screenView, fullyVisible) }
                }
            }
    }

    fun isViewOnScreen(view: View, fullyVisible: Boolean): Boolean {
        return isViewVisibleInParent(
            Utils.getActivityView(view.context) ?: return false,
            view,
            fullyVisible
        )
    }

    fun isViewVisibleInParent(parent: View, view: View, fullyVisible: Boolean): Boolean {
        //note: we're double checking lots of parameters. This is because of two things:
        //1. Some method behave differently with each Android version (e.g. return a value considering translation(x/y) or not
        //2. Related to #1, if a view is translated, e.g. in a Fragment, some methods may return values which represent as if they're on screen, but they're not.
        val parentBounds = Rect().also {
            parent.getHitRect(it)
        }
        val childBounds = Rect().also {
            view.getGlobalVisibleRect(it)
        }
        val locationOnWindow = IntArray(2).also {
            view.getLocationInWindow(it)
        }
        val viewX = locationOnWindow[0]
        val viewY = locationOnWindow[1]

        if (childBounds.isEmpty || parentBounds.isEmpty || viewX == 0 && viewY == 0) {
            return false
        }

        return if (fullyVisible) {
            if (parentBounds.contains(childBounds)) {
                childBounds.width() >= view.width
                        && childBounds.height() >= view.height
                        && viewX >= parentBounds.left
                        && viewX + childBounds.width() <= parentBounds.right
                        && viewY >= parentBounds.top
                        && viewY + childBounds.height() <= parentBounds.bottom
            } else false
        } else {
            parentBounds.intersect(childBounds)
                    && viewX >= parentBounds.left
                    && viewX <= parentBounds.right
                    && viewY >= parentBounds.top
                    && viewY <= parentBounds.bottom
        }
    }
}











fun viewVisibilityOf(bool: Boolean?, visibilityIfFalse: Int = View.GONE): Int {
    return if (bool == true) {
        View.VISIBLE
    } else {
        visibilityIfFalse
    }
}

internal fun shouldVisibilityUpdate(view: View, targetVisibility: Int): Boolean {
    return targetVisibility != view.visibility || run {
        //already in desired visibility. But are we visible (if that's what we want) ? check alpha
        when (targetVisibility) {
            View.VISIBLE -> view.alpha < 1F
            else -> false
        }
    }
}

private fun removeViewGlobalLayoutListener(view: View?, listener: ViewTreeObserver.OnGlobalLayoutListener?) {
    if (view != null && listener != null) {
        view.viewTreeObserver.removeOnGlobalLayoutListener(listener)
    }
}

/**
 * Used to get deep child offset.
 *
 *
 * 1. We need to scroll to child in scrollview, but the child may not the direct child to scrollview.
 * 2. So to get correct child position to scroll, we need to iterate through all of its parent views till the main parent.
 *
 * @param mainParent        Main Top parent.
 * @param child             Child.
 *
 * @return the accumulated offset
 */
private fun getDeepChildOffset(mainParent: View, child: View, gravityVertical: Int, gravityHorizontal: Int): Point {
    if (child == mainParent) {
        return Point()
    }

    val accumulatedOffset = Point()

    //for the original child, we should consider the Gravity requested. For all of it's parents, we take top & left.
    when (gravityVertical) {
        Gravity.TOP -> accumulatedOffset.y += child.top
        Gravity.CENTER, Gravity.CENTER_VERTICAL -> accumulatedOffset.y += (child.bottom + child.top) / 2
        Gravity.BOTTOM -> accumulatedOffset.y += child.bottom
        else -> {
        }
    }//do nothing
    when (gravityHorizontal) {
        Gravity.LEFT, Gravity.START -> accumulatedOffset.x += child.left
        Gravity.CENTER, Gravity.CENTER_HORIZONTAL -> accumulatedOffset.x += (child.left + child.right) / 2
        Gravity.RIGHT, Gravity.END -> accumulatedOffset.x += child.right
        else -> {
        }
    }//do nothing


    //loop through all parents
    if (child.parent == null || child.parent !is View) {
        return accumulatedOffset
    }
    var currentChild = child.parent as View
    while (currentChild != mainParent) {
        accumulatedOffset.y += currentChild.top
        accumulatedOffset.x += currentChild.left

        if (currentChild.parent == null || currentChild.parent !is View) {
            return accumulatedOffset
        }
        currentChild = currentChild.parent as View
    }

    return accumulatedOffset
}

private fun getScrollChildInParentVerticalOffsetIntl(parent: View, child: View, gravity: Int): Int {
    return getDeepChildOffset(
        parent,
        child,
        gravity,
        Gravity.NO_GRAVITY
    ).y
}

private fun getScrollChildInParentHorizontalOffsetIntl(parent: View, child: View, gravity: Int): Int {
    return getDeepChildOffset(
        parent,
        child,
        Gravity.NO_GRAVITY,
        gravity
    ).x
}

private fun <V : View> createOnDrawListener(viewRef: WeakReference<V>
        , promiseCallback: Promise.Companion.SimpleCallback<V>): ViewTreeObserver.OnDrawListener {

    return object : ViewTreeObserver.OnDrawListener {
        override fun onDraw() {
            viewRef.get()?.let {
                //remove here as well (before finally) to prevent race conditions THINK needed?
                it.post {
                    it.viewTreeObserver.removeOnDrawListener(this)
                    //we need 'post' as removing the onDraw during its callback crahes
                }

                promiseCallback.onSuccess(it)

            } ?: promiseCallback.onCancel("createOnDrawListener: View was garbage collected")
        }
    }
}

/** Calls back immediately if already on screen */
private fun waitForViewOnScreenIntl(view: View, screenView: ViewGroup, fullyVisible: Boolean)
        : APromise<View> {
    val viewRef = WeakReference(view)

    return APromise.ofCallback<View, ViewTreeObserver.OnPreDrawListener?>({ promiseCallback ->
        val onExecutionView = viewRef.get() ?: run {
            promiseCallback.onCancel("View became null")
            return@ofCallback null
        }

        //check if already visible to immediately call back
        if (ViewUtils.isViewVisibleInParent(screenView, onExecutionView, fullyVisible)) {
            promiseCallback.onSuccess(onExecutionView)
            null
        }

        //or register a preDraw listener to wait for it
        else {
            object : ViewTreeObserver.OnPreDrawListener {
                override fun onPreDraw(): Boolean {
                    //check if still on screen
                    val preDrawView = viewRef.get() ?: run {
                        promiseCallback.onCancel("View became null")
                        return true
                    }

                    if (ViewUtils.isViewVisibleInParent(screenView, preDrawView, fullyVisible)) {
                        promiseCallback.onSuccess(preDrawView)
                    }

                    return true
                }

                //register the listener
            }.also { view.viewTreeObserver.addOnPreDrawListener(it) }
        }
    })

    //finally, remove the listener
    { viewRef.get()?.viewTreeObserver?.removeOnPreDrawListener(it) }
}