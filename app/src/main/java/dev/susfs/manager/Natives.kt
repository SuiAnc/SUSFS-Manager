package dev.susfs.manager

import com.topjohnwu.superuser.Shell

/**
 * The runtime facts the SUSFS screen needs from the root implementation.
 *
 * Upstream read these over JNI from its bundled `libkernelsu.so`. A standalone manager has no such
 * library, so the one value the screen actually consults is probed through the root shell and
 * cached until the next probe.
 */
object Natives {
    /**
     * True while the device booted into a safe mode, where the kernel skips every operation.
     *
     * This mirrors `ksud`'s own property check (`persist.sys.safemode` / `ro.sys.safemode`). The
     * kernel's separate `check_safemode` ioctl is not reachable from a shell, so a device that set
     * only the kernel flag reads as false here; the worst case is an "Apply now" tap that the
     * kernel then ignores.
     */
    @Volatile
    var isSafeMode: Boolean = false
        private set

    internal fun refresh(shell: Shell) {
        val stdout = mutableListOf<String>()
        val stderr = mutableListOf<String>()
        val script = "getprop persist.sys.safemode; getprop ro.sys.safemode"
        runCatching { shell.newJob().add(script).to(stdout, stderr).exec() }
        isSafeMode = stdout.any { it.trim() == "1" }
    }
}
