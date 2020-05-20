package com.guymichael.reactdroid.core.activity.model

import android.content.Intent
import java.io.Serializable

data class ActivityResult(val requestCode: Int, val resultCode: Int, val data: Intent?) : Serializable