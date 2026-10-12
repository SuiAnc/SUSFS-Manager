package dev.susfs.manager.ui.util

import android.util.Log
import com.topjohnwu.superuser.Shell
import dev.susfs.manager.ksuApp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Helpers for the SukiSU-Ultra side of the app.
 *
 * SukiSU-Ultra ships its own `ksud` build, which is what understands the
 * `susfs` subcommand tree (status / version / features / config ...). We bundle
 * it as `libksud2.so` so it can live next to FolkSU's `libksud.so` in the same
 * APK, and we always invoke that one here.
 */
private const val TAG = "SukiSuCli"

/** Name of the SukiSU-Ultra daemon payload inside `nativeLibraryDir`. */
const val SUKISU_DAEMON_NAME = "libksud2.so"

/** Absolute path of the SukiSU-Ultra `ksud` payload. */
fun getKsuDaemonPath(): String =
    ksuApp.applicationInfo.nativeLibraryDir + File.separator + SUKISU_DAEMON_NAME

private val shellLock = Any()

@Volatile
private var cachedShell: Shell? = null

/**
 * Root shell used by every SukiSU command. Mirrors upstream `getRootShell()`:
 * the shell is created once and reused. Throws when root is unavailable, which
 * the callers already guard with `runCatching` / `try`.
 */
fun getRootShell(@Suppress("UNUSED_PARAMETER") globalMnt: Boolean = false): Shell {
    cachedShell?.let { if (it.isAlive && it.isRoot) return it }
    synchronized(shellLock) {
        cachedShell?.let { if (it.isAlive && it.isRoot) return it }
        val built = Shell.getShell()
        check(built.isRoot && built.isAlive) { "Root shell is not available" }
        cachedShell = built
        return built
    }
}

private fun fastCmd(shell: Shell, command: String): String =
    shell.newJob().add(command).exec().out.joinToString("\n")

/** `${ksud2} susfs features` — the raw enabled-feature string. */
fun getSuSFSFeatures(): String = runCatching {
    fastCmd(getRootShell(), "${getKsuDaemonPath()} susfs features")
}.onFailure { Log.w(TAG, "susfs features failed", it) }.getOrDefault("")

/** `${ksud2} susfs version` — e.g. `v2.0.0`, or `unsupport`. */
fun getSuSFSVersion(): String = runCatching {
    fastCmd(getRootShell(), "${getKsuDaemonPath()} susfs version").trim()
}.onFailure { Log.w(TAG, "susfs version failed", it) }.getOrDefault("")

/** `${ksud2} susfs status` — `true` / `false`. */
fun getSuSFSStatus(): String = runCatching {
    fastCmd(getRootShell(), "${getKsuDaemonPath()} susfs status").trim()
}.onFailure { Log.w(TAG, "susfs status failed", it) }.getOrDefault("false")

private fun shellQuote(value: String): String = "'${value.replace("'", "'\\''")}'"

/** `${ksud2} kernel spoof-uname --release <r> --version <v>`. */
fun spoofKernelUname(release: String, version: String): Boolean = runCatching {
    val command = "${getKsuDaemonPath()} kernel spoof-uname " +
        "--release ${shellQuote(release)} --version ${shellQuote(version)}"
    val result = getRootShell().newJob().add(command).exec()
    Log.i(TAG, "kernel spoof-uname success=${result.isSuccess}")
    result.isSuccess
}.onFailure { Log.w(TAG, "kernel spoof-uname failed", it) }.getOrDefault(false)

/** `${ksud2} boot-info is-ab-device`. */
suspend fun isAbDevice(): Boolean = withContext(Dispatchers.IO) {
    runCatching {
        fastCmd(getRootShell(), "${getKsuDaemonPath()} boot-info is-ab-device")
            .trim()
            .toBoolean()
    }.getOrDefault(false)
}
