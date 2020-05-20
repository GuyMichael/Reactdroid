package com.guymichael.reactdroid.extensions.components.permissions

import com.guymichael.kotlinreact.model.OwnProps
import com.guymichael.reactdroid.core.Utils

data class PermissionProps<COMPONENT_PROPS : OwnProps>(
        /** Requested permissions to grant. Until ALL are granted, the inner component will
         * be 'GONE' - never rendered.
         * Permissions may be changed later on, in which case same logic applies, except that
         * the inner component may already be rendered from previous permissions grant,
         * so keep that in mind (but it WILL become 'GONE' if new permissions aren't granted yet) */
        val permissions: List<String>,

        /** props for your (inner) component */
        val componentProps: COMPONENT_PROPS,

        //doesn't affect re-renders
        /** Action to execute if user denied the requested permissions, normally close the page.
         * Doesn't affect re-renders
         * @see `PermissionsDeniedException` regarding the [Throwable] argument `e`, which *might*,
         * and probably will be, of this type*/
        val initial_actionOnDenied: (e: Throwable, WithPermissions<COMPONENT_PROPS, *, *>) -> Unit
            = { _, c -> Utils.getActivity(c.mView)?.onBackPressed() },

        /** If `true`, and user has `denied always` at least one of the given permissions,
         * the app settings will open to let the user change there.
         * If the user grants permissions, and there are some permissions still missing
         * (weren't on 'always deny'), they will be automatically requested when app resumes.
         * Doesn't affect re-renders */
        val initial_goToAppSettingsIfAlwaysDeny: Boolean = false,

        /** If `true`, fades the component in and out when permission state
         * changes (between granted and denied). See `ViewUtils.applyVisibility` for more.
         * Doesn't affect re-renders */
        val initial_animateVisibility: Boolean = true
    ) : OwnProps() {

    override fun getAllMembers() = listOf(
        componentProps, permissions
    )
}