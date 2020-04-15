package com.guymichael.reactdroid.model

import android.widget.RatingBar

interface ReactRatingBarWatcher: RatingBar.OnRatingBarChangeListener {
    override fun onRatingChanged(ratingBar: RatingBar, rating: Float, fromUser: Boolean) {
        if (fromUser) {
            onScoreChanged(ratingBar, rating)
        }
    }

    fun onScoreChanged(ratingBar: RatingBar, rating: Float)
}