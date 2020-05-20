package com.guymichael.reactdroid.core.activity.model

data class PermissionsResult(
    val requestCode: Int
    , val requestedPermissions: List<String>
    , val deniedPermissions: List<String>?
) {

    /** If `false`, [deniedPermissions] is not empty (nor null) */
    val isSuccess: Boolean
        get() = this.deniedPermissions.isNullOrEmpty()
}