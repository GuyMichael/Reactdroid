package com.guymichael.reactdroid.core.activity.model

class PermissionsDeniedException(
    val requestedPermissions: List<String>
    , val deniedPermissions: List<String>
) : SecurityException("permissions denied by user - $deniedPermissions") {

    val grantedPermissions: List<String>
        get() = requestedPermissions.filter(deniedPermissions::contains)
}