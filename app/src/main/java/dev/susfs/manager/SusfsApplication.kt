package dev.susfs.manager

import android.app.Application
import com.topjohnwu.superuser.Shell
import dev.susfs.manager.ui.util.RootShell
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * The running application, published globally so the SUSFS data layer can reach the APK's own
 * files.
 *
 * Upstream also used this global to find `libksud.so`; the native library directory is only
 * reachable through a [android.content.Context], so the repository needs some handle on the
 * process. Keeping the same name and shape as upstream means the ported data layer is byte-for-byte
 * identical to the original.
 */
lateinit var ksuApp: SusfsApplication

class SusfsApplication : Application() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        ksuApp = this

        // `su` is the only shell entry point this app needs: KernelSU, KernelSU-Next, APatch and
        // Magisk all install one. stderr is deliberately left un-redirected so the repository can
        // keep it out of the JSON it parses.
        Shell.setDefaultBuilder(
            Shell.Builder.create().setTimeout(SHELL_TIMEOUT_SECONDS)
        )

        // One probe at startup so the screen can leave its "probing" placeholder. RootShell owns
        // every later retry; a denied prompt leaves the status Unavailable until the user returns.
        scope.launch { runCatching { RootShell.awaitRoot() } }
    }

    private companion object {
        const val SHELL_TIMEOUT_SECONDS = 20L
    }
}
