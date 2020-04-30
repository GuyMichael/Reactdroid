package com.guymichael.reactdroid.extensions.components.text

import android.app.Activity
import android.os.Build
import android.view.Gravity
import android.view.View
import android.widget.TextView
import androidx.annotation.IdRes
import com.guymichael.reactdroid.core.ViewUtils
import com.guymichael.reactdroid.core.model.AComponent

//to understand how calculating line-count before rendering may be implemented,
//see here: https://stackoverflow.com/questions/15679147/how-to-get-line-count-of-textview-before-rendering
open class CTextCollapsing(v: TextView) : AComponent<CollapsingTextProps, CollapsingTextState, TextView>(v) {
    override fun createInitialState(props: CollapsingTextProps) = CollapsingTextState.from(props)

    init {
        //TODO understand why normal setOnClickListener calls-back two times on every click
        // currently using debounce
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            setOnClickListener(150) {
                if (isExpandable()) {
                    setState(collapsed = !ownState.isCollapsed)
                }
            }
        } //else - that's it, collapsing not supported
    }

    override fun componentDidUpdate(prevProps: CollapsingTextProps, prevState: CollapsingTextState, snapshot: Any?) {
        if (props.scrollParentOnExpand && prevState.isCollapsed && !ownState.isCollapsed) {
            //we've just expanded. Scroll a parent to reveal us
            mView.let { textView ->
            ViewUtils.findAnyScrollingParent(textView)?.let { parent ->

                if (parent is androidx.recyclerview.widget.RecyclerView) {//recyclers take longer to re-layout
                    textView.post {
                        ViewUtils.smoothScrollToChild(parent, textView)
                    }
                } else {
                    ViewUtils.smoothScrollToChild(parent, textView, Gravity.TOP)
                }
        }}}
    }

    override fun render() { mView.let { view ->
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            view.text = props.text
            return //that's it, collapsing not supported
        }

        val hasText = !props.text.isNullOrBlank()

        //update text
        view.text = if( hasText && isExpandable() && ownState.isCollapsed) {
            //expandable and collapsed, we want '...more' at the end
            buildCollapsedText()
        } else {
            props.text
        }
    }}

    /*

    /**
     * @param parent doesn't have to be a direct parent
     */
    fun setHasScrollingParent(parent: View?) {
        this.scrollingParent = if (parent !== this) parent else null
    }

    fun hasScrollingParent(): Boolean {
        return scrollingParent != null
    }

    private inner class OnExpandAnimFinishedListener : Animator.AnimatorListener {
        internal var heightBefore: Int = 0

        override fun onAnimationStart(animation: Animator) {
            heightBefore = height
        }

        override fun onAnimationEnd(animation: Animator) {
            postDelayed({ scrollParent() }, 50)
        }

        override fun onAnimationCancel(animation: Animator) {

        }

        override fun onAnimationRepeat(animation: Animator) {

        }

        private fun scrollParent() {
            if (scrollingParent != null) {
                if (scrollingParent is ScrollView) {
                    if (!Utilities.isChildCompletelyVisible(scrollingParent as ScrollView?, this@ExpandableTextView)) {
                        Utilities.smoothScrollToChild(scrollingParent as ScrollView?, this@ExpandableTextView, Gravity.TOP)
                    }
                } else if (scrollingParent is NestedScrollView) {
                    if (!Utilities.isChildCompletelyVisible(scrollingParent as NestedScrollView?, this@ExpandableTextView)) {
                        Utilities.smoothScrollToChild(scrollingParent as NestedScrollView?, this@ExpandableTextView, Gravity.TOP)
                    }
                } else if (scrollingParent is RecyclerView) {
                    val directChild = Utilities.findDirectChild(scrollingParent!!, this@ExpandableTextView)
                    if (directChild != null) {
                        (scrollingParent as RecyclerView).smoothScrollToPosition((scrollingParent as RecyclerView).getChildAdapterPosition(directChild))
                    }
                } else {
                    if (BuildConfig.DEBUG) {
                        Logger.e(this@CollapsingTextView::class.java, "scrollParent(): Unfamiliar scrolling parent class " + scrollingParent!!.javaClass)
                    }
                }
            }
        }
    }*/
}


//THINK as Annotations
fun withCollapsingText(view: TextView) = CTextCollapsing(view)
fun View.withCollapsingText(@IdRes id: Int) = CTextCollapsing(findViewById(id))
fun Activity.withCollapsingText(@IdRes id: Int) = CTextCollapsing(findViewById(id))