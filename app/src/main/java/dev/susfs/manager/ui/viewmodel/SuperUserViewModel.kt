package dev.susfs.manager.ui.viewmodel

import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.util.Log
import dev.susfs.manager.ksuApp

/** One installed app, as the SUSFS path pickers need it. */
data class SuperUserApp(
    val packageName: String,
    val label: String,
    val packageInfo: PackageInfo,
)

/**
 * Installed-app list for the SUSFS path / map pickers.
 *
 * The upstream app asks its own root manager for the list of apps that have been
 * granted root; a standalone app has no such list, so this enumerates installed
 * packages instead. That is what the pickers actually need — a package name, a
 * label and a `PackageInfo` to derive the uid and icon from.
 */
object SuperUserViewModel {

    private const val TAG = "SuperUserViewModel"

    private val lock = Any()

    @Volatile
    private var apps: List<SuperUserApp> = emptyList()

    @Volatile
    private var loaded = false

    /** Never throws; returns an empty list if the package manager refuses. */
    fun getAppsSafely(): List<SuperUserApp> {
        if (!loaded) loadAppList()
        return apps
    }

    fun loadAppList() {
        synchronized(lock) {
            if (loaded) return
            val pm = ksuApp.packageManager
            val result = mutableListOf<SuperUserApp>()
            runCatching {
                @Suppress("DEPRECATION", "QueryPermissionsNeeded")
                val packages = pm.getInstalledPackages(0)
                for (info in packages) {
                    val applicationInfo: ApplicationInfo = info.applicationInfo ?: continue
                    if (applicationInfo.packageName == ksuApp.packageName) continue
                    val label = runCatching {
                        pm.getApplicationLabel(applicationInfo).toString()
                    }.getOrDefault(applicationInfo.packageName)
                    result += SuperUserApp(
                        packageName = applicationInfo.packageName,
                        label = label,
                        packageInfo = info,
                    )
                }
            }.onFailure { Log.w(TAG, "cannot enumerate packages", it) }
            apps = result.sortedBy { it.label.lowercase() }
            loaded = true
        }
    }

    /** Kept for API parity with upstream callers. */
    @Suppress("UNUSED_PARAMETER")
    fun findPackage(packageName: String): SuperUserApp? =
        getAppsSafely().firstOrNull { it.packageName == packageName }

    internal fun invalidate() {
        synchronized(lock) {
            loaded = false
            apps = emptyList()
        }
    }

    @Suppress("unused")
    private fun systemFlag(info: PackageInfo): Boolean =
        (info.applicationInfo?.flags ?: 0) and ApplicationInfo.FLAG_SYSTEM != 0
}
