package com.mkmk749278.myapps.data

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import com.mkmk749278.myapps.model.ManagedApp
import com.mkmk749278.myapps.model.OperationBackend
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ManagedAppRepository(
    private val context: Context,
    private val selectionStore: SelectionStore,
) {
    private val packageManager: PackageManager = context.packageManager

    suspend fun loadApps(): List<ManagedApp> = withContext(Dispatchers.IO) {
        val selectionState = selectionStore.readState()
        packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
            .asSequence()
            .filterNot { it.packageName == context.packageName }
            .map { info ->
                val launchIntent = packageManager.getLaunchIntentForPackage(info.packageName)
                ManagedApp(
                    packageName = info.packageName,
                    label = packageManager.getApplicationLabel(info).toString(),
                    isSystemApp = info.flags and ApplicationInfo.FLAG_SYSTEM != 0,
                    isSelected = info.packageName in selectionState.selectedPackages,
                    isFavorite = info.packageName in selectionState.favoritePackages,
                    isFrozen = isAppFrozen(info),
                    isLaunchable = launchIntent != null,
                )
            }
            .sortedWith(compareBy<ManagedApp>({ !it.isSelected }, { it.label.lowercase() }))
            .toList()
    }

    fun resolveBackend(): AccessStatus {
        val rootAvailable = RootShell.isRootAvailable()
        val shizukuAvailability = ShizukuBridge.availability()
        val backend = when {
            rootAvailable -> OperationBackend.Root
            shizukuAvailability.isReady -> OperationBackend.Shizuku
            else -> OperationBackend.Unavailable
        }
        return AccessStatus(
            backend = backend,
            rootAvailable = rootAvailable,
            shizukuAvailable = shizukuAvailability.installedAndRunning,
            shizukuPermissionGranted = shizukuAvailability.permissionGranted,
        )
    }

    suspend fun freezeApp(packageName: String): Result<Unit> = changeFrozenState(packageName, frozen = true)

    suspend fun unfreezeApp(packageName: String): Result<Unit> = changeFrozenState(packageName, frozen = false)

    suspend fun freezeApps(packageNames: List<String>): Result<Unit> = batchChange(packageNames, frozen = true)

    suspend fun unfreezeApps(packageNames: List<String>): Result<Unit> = batchChange(packageNames, frozen = false)

    suspend fun launchApp(packageName: String): String = withContext(Dispatchers.IO) {
        val launchIntent = packageManager.getLaunchIntentForPackage(packageName)
            ?: return@withContext "No launch activity is available for this app."
        val unfreezeResult = unfreezeApp(packageName)
        if (unfreezeResult.isFailure) {
            return@withContext unfreezeResult.exceptionOrNull()?.message ?: "Unable to unfreeze app before launch."
        }
        launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
        context.startActivity(launchIntent)
        "App launched."
    }

    private suspend fun batchChange(packageNames: List<String>, frozen: Boolean): Result<Unit> {
        if (packageNames.isEmpty()) {
            return Result.failure(IllegalArgumentException("Select at least one app first."))
        }
        packageNames.forEach { packageName ->
            val result = changeFrozenState(packageName, frozen)
            if (result.isFailure) {
                return result
            }
        }
        return Result.success(Unit)
    }

    private suspend fun changeFrozenState(packageName: String, frozen: Boolean): Result<Unit> {
        val command = if (frozen) freezeCommand(packageName) else unfreezeCommand(packageName)
        val accessStatus = resolveBackend()
        val result = when (accessStatus.backend) {
            OperationBackend.Root -> RootShell.execute(command)
            OperationBackend.Shizuku -> ShizukuBridge.execute(command)
            OperationBackend.Unavailable -> RootShell.ShellResult(
                exitCode = -1,
                stdout = "",
                stderr = "Root or Shizuku access is required for this action.",
            )
        }
        return if (result.isSuccess) {
            Result.success(Unit)
        } else {
            Result.failure(IllegalStateException(result.stderr.ifBlank { result.stdout.ifBlank { "Command failed." } }))
        }
    }

    private fun isAppFrozen(info: ApplicationInfo): Boolean {
        return when (packageManager.getApplicationEnabledSetting(info.packageName)) {
            PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
            PackageManager.COMPONENT_ENABLED_STATE_DISABLED_USER,
            PackageManager.COMPONENT_ENABLED_STATE_DISABLED_UNTIL_USED -> true
            else -> !info.enabled
        }
    }

    private fun freezeCommand(packageName: String): String {
        val escaped = packageName.shellEscape()
        return "pm disable-user --user 0 $escaped || pm hide --user 0 $escaped || cmd package suspend --user 0 $escaped"
    }

    private fun unfreezeCommand(packageName: String): String {
        val escaped = packageName.shellEscape()
        return "pm enable $escaped || pm unhide --user 0 $escaped || cmd package unsuspend --user 0 $escaped"
    }

    private fun String.shellEscape(): String = "'" + replace("'", "'\"'\"'") + "'"
}

data class AccessStatus(
    val backend: OperationBackend,
    val rootAvailable: Boolean,
    val shizukuAvailable: Boolean,
    val shizukuPermissionGranted: Boolean,
)
