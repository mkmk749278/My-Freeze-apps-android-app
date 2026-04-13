package com.mkmk749278.myapps.data

import android.content.pm.PackageManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import rikka.shizuku.Shizuku
import kotlin.coroutines.resume

object ShizukuBridge {
    private const val REQUEST_CODE = 7492

    data class Availability(
        val installedAndRunning: Boolean,
        val permissionGranted: Boolean,
    ) {
        val isReady: Boolean = installedAndRunning && permissionGranted
    }

    fun availability(): Availability {
        val running = runCatching { Shizuku.pingBinder() }.getOrDefault(false)
        val granted = running && runCatching {
            Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED
        }.getOrDefault(false)
        return Availability(installedAndRunning = running, permissionGranted = granted)
    }

    suspend fun requestPermission(): Boolean = suspendCancellableCoroutine { continuation ->
        val availability = availability()
        if (!availability.installedAndRunning) {
            continuation.resume(false)
            return@suspendCancellableCoroutine
        }
        if (availability.permissionGranted) {
            continuation.resume(true)
            return@suspendCancellableCoroutine
        }

        lateinit var listener: Shizuku.OnRequestPermissionResultListener
        listener = Shizuku.OnRequestPermissionResultListener { requestCode, grantResult ->
            if (requestCode != REQUEST_CODE || !continuation.isActive) {
                return@OnRequestPermissionResultListener
            }
            Shizuku.removeRequestPermissionResultListener(listener)
            continuation.resume(grantResult == PackageManager.PERMISSION_GRANTED)
        }

        continuation.invokeOnCancellation {
            Shizuku.removeRequestPermissionResultListener(listener)
        }

        Shizuku.addRequestPermissionResultListener(listener)
        Shizuku.requestPermission(REQUEST_CODE)
    }

    suspend fun execute(command: String): RootShell.ShellResult = withContext(Dispatchers.IO) {
        if (!availability().isReady) {
            return@withContext RootShell.ShellResult(
                exitCode = -1,
                stdout = "",
                stderr = "Shizuku is unavailable or permission has not been granted.",
            )
        }

        runCatching {
            // Shizuku 13.x keeps shell process creation behind a non-public helper, so this bridge
            // intentionally pins behavior to that API surface until an equivalent public API exists.
            val method = Shizuku::class.java.getDeclaredMethod(
                "newProcess",
                Array<String>::class.java,
                Array<String>::class.java,
                String::class.java,
            ).apply { isAccessible = true }
            val process = method.invoke(null, arrayOf("sh", "-c", command), null, null) as Process
            val stdout = process.inputStream.bufferedReader().readText().trim()
            val stderr = process.errorStream.bufferedReader().readText().trim()
            val exitCode = process.waitFor()
            RootShell.ShellResult(exitCode = exitCode, stdout = stdout, stderr = stderr)
        }.getOrElse { error ->
            RootShell.ShellResult(
                exitCode = -1,
                stdout = "",
                stderr = error.message ?: "Shizuku command failed.",
            )
        }
    }
}
