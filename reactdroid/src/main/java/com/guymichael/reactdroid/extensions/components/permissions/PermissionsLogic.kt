package com.guymichael.reactdroid.extensions.components.permissions

import android.annotation.TargetApi
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.guymichael.apromise.APromise
import com.guymichael.kotlinreact.Logger
import com.guymichael.reactdroid.core.activity.ComponentActivity
import com.guymichael.reactdroid.core.activity.model.PermissionsDeniedException
import com.guymichael.reactdroid.core.letIf
import io.reactivex.rxjava3.disposables.Disposable
import java.lang.ref.WeakReference

object PermissionsLogic {
    private lateinit var mPersistDeniedPermissions: (Set<String>) -> Unit
    private lateinit var mGetPersistedDeniedPermissions: () -> Set<String>?


    fun init(persistDeniedPermissions: (Set<String>) -> Unit
            , getPersistedDeniedPermissions: () -> Set<String>?
        ) {

        this.mPersistDeniedPermissions = persistDeniedPermissions
        this.mGetPersistedDeniedPermissions = getPersistedDeniedPermissions
    }

    /**
     * Not pure! Checks for permissions and, for those which are granted, saves their
     * 'shouldShowRationale' state
     * @param permissions e.g. [android.Manifest.permission.ACCESS_FINE_LOCATION]
     * @return true if **ALL** permissions are granted (@link [PackageManager.PERMISSION_GRANTED]), false otherwise.
     */
    fun checkPermissions(context: Context, permissions: Array<String>): Boolean {
        var ans = true
        for (permission in permissions) {
            if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                ans = false
            } else {
                //granted, persist shouldShowRationale state (side-effect)
                setRequestPermissionShouldNotShowRationale(permission, false)
            }
        }
        return ans
    }

    fun getDeniedPermissions(context: Context, permissions: Array<String>): List<String> {
        return permissions.filter {
            ContextCompat.checkSelfPermission(context, it) != PackageManager.PERMISSION_GRANTED
        }
    }

    fun getAlwaysDenyPermissions(context: Activity, permissions: Array<String>): List<String> {
        return permissions.filter { isPermissionOnAlwaysDeny(context, it) }
    }

    /**
     * @param permission
     * @return true if the **previous** time *permission* was asked from the user,
     * [ActivityCompat.shouldShowRequestPermissionRationale] returned *false*
     */
    @TargetApi(Build.VERSION_CODES.M)
    fun isPreviousRequestPermissionShouldNotShowRationale(permission: String): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) { return false }

        return getPersistedDeniedPermissions().contains(permission)
    }

    /**
     * @param permissions
     * @param grantResults
     * @return true if all granted, false otherwise
     */
    fun areAllPermissionsGranted(permissions: Array<String>, grantResults: IntArray): Boolean {
        for (i in permissions.indices) {
            if (grantResults.getOrNull(i) != PackageManager.PERMISSION_GRANTED) {
                //NOT granted
                return false
            }
        }
        return true
    }

    /**
     * @param permissions
     * @param grantResults
     * @return true if all granted, false otherwise
     */
    fun filterGrantedPermissions(permissions: Array<String>, grantResults: IntArray): List<String> {
        return permissions.filterIndexed { i, _ ->
            grantResults.getOrNull(i) == PackageManager.PERMISSION_GRANTED
        }
    }

    /**
     * @param permissions
     * @param grantResults
     * @return true if all granted, false otherwise
     */
    fun filterDeniedPermissions(permissions: Array<String>, grantResults: IntArray): List<String> {
        return permissions.filterIndexed { i, _ ->
            grantResults.getOrNull(i) != PackageManager.PERMISSION_GRANTED
        }
    }

    /**
     * @param permissions
     * @param grantResults
     * @return true if all granted, false otherwise
     */
    fun isAtLeastOnePermissionGranted(permissions: Array<String>, grantResults: IntArray): Boolean {
        for (i in permissions.indices) {
            if (i < grantResults.size) {
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    return true
                }
            }
        }
        return false
    }

    @TargetApi(Build.VERSION_CODES.M)
    fun isPermissionOnAlwaysDeny(context: Activity, permission: String): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) { return false }

        return isPreviousRequestPermissionShouldNotShowRationale(permission)
            && !context.shouldShowRequestPermissionRationale(permission) //what is this method?? See here: https://stackoverflow.com/questions/30719047/android-m-check-runtime-permission-how-to-determine-if-the-user-checked-nev#comment-53800740
    }

    fun requestPermissions(context: ComponentActivity<*>, permissions: List<String>
            , requestThroughSettingsIfAlwaysDeny: Boolean = false
        ): APromise<Unit> {

        val permissionsArr = permissions.toTypedArray()

        val alwaysDenyPermissions = if (requestThroughSettingsIfAlwaysDeny) {
            getAlwaysDenyPermissions(context, permissionsArr).toTypedArray()
        } else null

        return if (alwaysDenyPermissions.isNullOrEmpty()) {
            requestPermissionsImpl(context, permissionsArr)
        } else {
            requestPermissionsThroughPhoneSettings(context, alwaysDenyPermissions)
                .letIf({ alwaysDenyPermissions.size < permissionsArr.size }) { promise ->
                    //some permissions were not 'alwaysDeny', we should now check for them as well
                    promise.thenAwaitWithContextOrCancel(context) { c, _ ->
                        //all 'always denied' permissions granted!
                        // check if there are other, non-always-deny permissions pending
                        requestPermissionsImpl(c
                            , permissions.filter { !alwaysDenyPermissions.contains(it) }.toTypedArray()
                        )
                    }
                }
        }
    }

    fun requestPermissions(context: ComponentActivity<*>, vararg permissions: String
            , requestThroughSettingsIfAlwaysDeny: Boolean = false
        ): APromise<Unit> {

        return requestPermissions(context, permissions.toList(), requestThroughSettingsIfAlwaysDeny)
    }







    internal fun onPermissionResult(context: Activity, permissions: Array<String>, grantResults: IntArray) {
        for (i in permissions.indices) {
            setRequestPermissionShouldNotShowRationale(
                permissions[i]
                ,grantResults.getOrNull(i) != PackageManager.PERMISSION_GRANTED
                        && !ActivityCompat.shouldShowRequestPermissionRationale(context, permissions[i]) //THINK can remove?
            ) //(false) happens when:  1. First perm. request.   2. 'always denied' checked (just now or previously)   3. device policy (irrelevant)
        }
    }

    /** @return a promise that resolves if ALL requested permissions are granted,
     * or rejects with a [PermissionsDeniedException] */
    private fun requestPermissionsImpl(context: ComponentActivity<*>, permissions: Array<String>): APromise<Unit> {
        val contextRef = WeakReference(context)

        return APromise.ofCallback<Unit, Disposable?>({ promiseCallback ->
            contextRef.get()?.takeIf { !it.isDestroyed && !it.isFinishing }?.let {
                it.observeOnPermissionResults()
                .filter { result -> result.requestCode == ComponentActivity.REQUEST_CODE_PERMISSIONS }
                .subscribe(
                    { promiseCallback.onSuccess(Unit) }           //result -> all permissions granted
                    , { e -> promiseCallback.onFailure(e) }       //error in observer
                ).also { _ ->
                    ActivityCompat.requestPermissions(it, permissions, ComponentActivity.REQUEST_CODE_PERMISSIONS)
                }
            } ?: let {
                promiseCallback.onCancel("context became null")
                null
            }
        })

        //finally, dispose the activityResult observer
        { it?.takeIf { !it.isDisposed }?.dispose() }
    }

    /** @return a promise that resolves if ALL requested permissions are granted,
     * or rejects with a [PermissionsDeniedException] */
    private fun requestPermissionsThroughPhoneSettings(context: ComponentActivity<*>
        , permissions: Array<String>
    ): APromise<Unit> {

        val intent: Intent = createPermissionsAppSettingsIntent(context)
        val contextRef = WeakReference(context)

        return (
            APromise.ofCallback<Unit, Disposable?>({ promiseCallback ->
                contextRef.get()?.takeIf { !it.isDestroyed && !it.isFinishing }?.let {
                    it.observeOnActivityResults()
                        .doOnError { e ->
                            promiseCallback.onFailure(e)
                        }.subscribe { result ->
                            if (result.requestCode == ComponentActivity.REQUEST_CODE_SETTINGS_PERMISSIONS) {
                                promiseCallback.onSuccess(Unit)
                            }
                        }.also { _ ->
                            it.startActivityForResult(intent,
                                ComponentActivity.REQUEST_CODE_SETTINGS_PERMISSIONS
                            )
                        }
                } ?: let {
                    promiseCallback.onCancel("context became null")
                    null
                }
            })
            //finally, dispose the activityResult observer
            { it?.takeIf { !it.isDisposed }?.dispose() }

        ).thenWithContext(context) { c, _ ->
            val deniedPermissions = getDeniedPermissions(c, permissions)
            val isSuccess = deniedPermissions.isEmpty()

            //refresh 'shouldShowRationale' states
            onSettingsPermissionResult(c, permissions)

            //log & reject if not all permissions are granted
            if( !isSuccess) {
                Logger.w(this::class, "Permissions not granted by the user through settings - $deniedPermissions")
                throw PermissionsDeniedException(permissions.toList(), deniedPermissions)
            }
        }
    }

    private fun onSettingsPermissionResult(context: Activity, permissions: Array<String>) {
        for (i in permissions.indices) {
            setRequestPermissionShouldNotShowRationale(
                permissions[i]
                , !ActivityCompat.shouldShowRequestPermissionRationale(context, permissions[i])
            )
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    private fun setRequestPermissionShouldNotShowRationale(permission: String, shouldNotShow: Boolean) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) { return }

        val deniedPermissions = getPersistedDeniedPermissions()

        if (shouldNotShow && deniedPermissions.add(permission)
            || ( !shouldNotShow && deniedPermissions.remove(permission))
        ) {
            //added/removed. Persist
            persistDeniedPermissions(deniedPermissions)
        }
    }

    /**
     * **Note: called startActivity with this Intent from a non-Activity context requires to add a flag:
     * FLAG_ACTIVITY_NEW_TASK. That's why currently 'context' must be an Activity**
     * https://developer.android.com/about/versions/pie/android-9.0-changes-all#fant-required
     * @param context
     */
    private fun createPermissionsAppSettingsIntent(context: Activity): Intent {
        return Intent().also {
            it.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
            it.addCategory(Intent.CATEGORY_DEFAULT)
            it.data = Uri.parse("package:" + context.packageName)
            it.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP) //cannot be 'singleTask', or using this intent with startActivityForResult will resume immediately (onActivityResult() ) with result = CANCELLED
            it.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                it.addFlags(Intent.FLAG_ACTIVITY_LAUNCH_ADJACENT)
            }

            it.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
        }
    }

    private fun persistDeniedPermissions(permissions: Set<String>) {
        permissions.takeIf { it.isNotEmpty() }?.also {
            mPersistDeniedPermissions.invoke(it)
        }
    }

    private fun getPersistedDeniedPermissions(): MutableSet<String> {
        return mGetPersistedDeniedPermissions.invoke()?.toMutableSet() ?: mutableSetOf()
    }
}