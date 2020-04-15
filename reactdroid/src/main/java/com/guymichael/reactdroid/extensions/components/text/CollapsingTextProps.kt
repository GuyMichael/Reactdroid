package com.guymichael.reactdroid.extensions.components.text

data class CollapsingTextProps(
    override val text: CharSequence?
    , val collapsedLineCount: Int = 2
    , val expandedMaxLines: Int = 1000 //THINK ellipsis as well
    , val animateChanges: Boolean = true
    , val startCollapsed: Boolean = true
    , val expandBtnText: CharSequence = TextUtils.colorize("more", TextUtils.parseColor("#2196f3"))
    , val scrollParentOnExpand: Boolean = true

    ) : BaseATextProps(text) {

    override fun getAllMembers(): List<*> = listOf(
        text, collapsedLineCount, expandedMaxLines
        , expandBtnText //THINK to String
        , scrollParentOnExpand
        //note: we don't care about startCollapsed, as it affects only the first render
        //      animateChanges also doesn't affect us render-wise
    )
}