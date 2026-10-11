package dev.susfs.manager.ui.util

import com.topjohnwu.superuser.Shell
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import dev.susfs.manager.Natives

/**
 * State of the shared root shell. [Probing] means a build is running or scheduled; it is
 * emphatically not a failure, so the UI shows a placeholder instead of a warning while it is set.
 */
enum class RootShellStatus { Probing, Ready, Unavailable }

/**
 * Owns the single root shell every SUSFS command runs on.
 *
 * This mirrors the contract the SUSFS view model was written against: a [status] flow that reaches
 * [RootShellStatus.Ready] once `su` is granted, a [generation] counter that changes whenever a new
 * shell is published, and [awaitRoot] for callers that must not run on a non-root fallback.
 *
 * Unlike upstream this is deliberately small. Upstream also maintains a global-mount shell and a
 * bounded backoff retry loop because its whole app depends on root; here the only consumer is the
 * SUSFS screen, which re-probes on every resume.
 */
object RootShell {
    private val mutableStatus = MutableStateFlow(RootShellStatus.Probing)
    val status: StateFlow<RootShellStatus> = mutableStatus.asStateFlow()

    private val mutableGeneration = MutableStateFlow(0L)
    val generation: StateFlow<Long> = mutableGeneration.asStateFlow()

    private val mutex = Mutex()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    @Volatile
    private var shell: Shell? = null

    /**
     * Returns a ready root shell, building one if necessary.
     *
     * Never reports a plain `sh` fallback as ready: a shell that is not root is treated as a failed
     * probe and an [IllegalStateException] is thrown so privileged work cannot silently degrade.
     */
    suspend fun awaitRoot(): Shell = mutex.withLock {
        ready()?.let { return it }

        mutableStatus.value = RootShellStatus.Probing
        val built = withContext(Dispatchers.IO) {
            runCatching { Shell.getShell() }.getOrNull()
        }
        if (built != null && built.isRoot && built.isAlive) {
            shell = built
            Natives.refresh(built)
            mutableGeneration.value += 1
            mutableStatus.value = RootShellStatus.Ready
            built
        } else {
            shell = null
            mutableStatus.value = RootShellStatus.Unavailable
            error("Root access was not granted")
        }
    }

    /** Kicks a probe without waiting for it. */
    fun probe() {
        // Deliberately fire-and-forget: the caller is usually the UI, which must not block on the
        // superuser prompt. The status flow reports the outcome.
        scope.launch { runCatching { awaitRoot() } }
    }

    private fun ready(): Shell? = shell?.takeIf { it.isAlive && it.isRoot }
}
