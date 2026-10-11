package dev.susfs.manager.ui.navigation3

import androidx.compose.runtime.staticCompositionLocalOf

/**
 * The one navigation capability the SUSFS screen needs.
 *
 * Upstream the screen lives on a navigation3 back stack, so its top bar pops a route. The
 * standalone manager shows the SUSFS screen as its only destination, so popping means leaving the
 * activity; keeping the same interface leaves the screen source untouched.
 */
interface Navigator {
    fun pop()
}

val LocalNavigator = staticCompositionLocalOf<Navigator> {
    error("LocalNavigator not provided")
}
