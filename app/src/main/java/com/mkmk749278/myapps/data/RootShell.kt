package com.mkmk749278.myapps.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

object RootShell {
    data class ShellResult(
        val exitCode: Int,
        val stdout: String,
        val stderr: String,
    ) {
        val isSuccess: Boolean get() = exitCode == 0
    }

    suspend fun execute(command: String): ShellResult = withContext(Dispatchers.IO) {
        runCatching {
            val process = ProcessBuilder("su", "-c", command)
                .redirectErrorStream(false)
                .start()
            val stdout = process.inputStream.bufferedReader().readText().trim()
            val stderr = process.errorStream.bufferedReader().readText().trim()
            val exitCode = process.waitFor()
            ShellResult(exitCode = exitCode, stdout = stdout, stderr = stderr)
        }.getOrElse { error ->
            val message = if (error is IOException) {
                "Root shell not available on this device."
            } else {
                error.message ?: "Unknown shell failure"
            }
            ShellResult(exitCode = -1, stdout = "", stderr = message)
        }
    }

    fun isRootAvailable(): Boolean {
        return runCatching {
            val process = ProcessBuilder("su", "-c", "id")
                .redirectErrorStream(true)
                .start()
            process.waitFor() == 0
        }.getOrDefault(false)
    }
}
