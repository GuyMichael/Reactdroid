package com.guymichael.reactdroid.extensions.components.text

import android.os.Build
import androidx.annotation.RequiresApi

//https://medium.com/over-engineering/drawing-multiline-text-to-canvas-on-android-9b98f0bfa16a


internal fun CTextCollapsing.setState(collapsed: Boolean) {
    setState(CollapsingTextState(collapsed))
}

internal fun CTextCollapsing.getAllowedMaxLines(collapsed: Boolean = this.ownState.isCollapsed): Int {
    return if (collapsed)
        props.collapsedLineCount
        else props.expandedMaxLines
}

/** @return true if full line count is greater than when collapsed */
@RequiresApi(Build.VERSION_CODES.M)
internal fun CTextCollapsing.isExpandable(text: CharSequence? = props.text): Boolean {
    //true if full line count is greater than when collapsed
    return calculateDesiredLineCount(text, false) >
                getAllowedMaxLines(collapsed = true)
}

@RequiresApi(Build.VERSION_CODES.M)
internal fun CTextCollapsing.buildCollapsedText(text: CharSequence? = props.text
                                                , expandBtnText: CharSequence? = props.expandBtnText): CharSequence? {

    return getAllowedMaxLines(true).let { lineCount ->
        TextUtils.ellipsizeText(mView, text
            , lineCount
            , expandBtnText
            , TextUtils.getHeightByLineCount(mView, lineCount)
        )
    }
}

@RequiresApi(Build.VERSION_CODES.M)
internal fun CTextCollapsing.calculateDesiredLineCount(text: CharSequence? = props.text
                                                       , collapsed: Boolean = this.ownState.isCollapsed): Int {

    return if (collapsed) {
        TextUtils.calculateDesiredLineCount(mView, text
            , TextUtils.getHeightByLineCount(mView, getAllowedMaxLines(collapsed = true)))
    } else {
        TextUtils.calculateDesiredLineCount(mView, text
            , Int.MAX_VALUE)
    }
}