package com.guymichael.reactdroid.extensions.components.ratingbar

import android.widget.RatingBar

interface ReactdroidRatingBarWatcher: RatingBar.OnRatingBarChangeListener {
    override fun onRatingChanged(ratingBar: RatingBar, rating: Float, fromUser: Boolean) {
        if (fromUser) {
            onScoreChanged(ratingBar, rating)
        }
    }

    fun onScoreChanged(ratingBar: RatingBar, rating: Float)
}