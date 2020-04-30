package com.guymichael.reactdroid.core.model.android

import android.view.View

abstract class DebouncedClickListener(private val debounceMs: Int): View.OnClickListener {
    private var lastClick: Long? = null

    final override fun onClick(v: View) {
        lastClick                   .let { lastClicked ->
        System.currentTimeMillis()  .let { now ->

            lastClick = now
            if (lastClicked == null || now - lastClicked > debounceMs ) {
                onClicked(v)
            }
        }}
    }

    abstract fun onClicked(v: View)
}