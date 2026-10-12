package dev.susfs.manager.data.settings

import android.content.Context

/**
 * Which manager's SUSFS page (and therefore which bundled `ksud` payload) the
 * app is driving.
 *
 * The two are not interchangeable: FolkSU speaks the JSON
 * `export` / `import` / `apply` protocol, SukiSU-Ultra speaks its own
 * config-key protocol. Both payloads ship in this APK under different names so
 * one install can drive either.
 */
enum class ManagerMode(val value: String, val payloadName: String) {
    FolkSU("folksu", "libksud.so"),
    SukiSU("sukisu", "libksud2.so");

    companion object {
        val DEFAULT = FolkSU

        fun fromValue(value: String?): ManagerMode =
            entries.firstOrNull { it.value == value } ?: DEFAULT
    }
}

/** Persists [ManagerMode] in the same `settings` preferences file upstream uses. */
object ManagerSettings {

    private const val PREFS = "settings"
    private const val KEY = "manager_mode"

    fun get(context: Context): ManagerMode {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        return ManagerMode.fromValue(prefs.getString(KEY, null))
    }

    fun set(context: Context, mode: ManagerMode) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY, mode.value)
            .apply()
    }
}
