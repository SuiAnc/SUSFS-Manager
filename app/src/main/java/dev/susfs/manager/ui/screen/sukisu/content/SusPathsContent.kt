package dev.susfs.manager.ui.screen.sukisu.content

import androidx.compose.runtime.Composable
import dev.susfs.manager.ui.screen.sukisu.content.material.SusPathsContentMaterial

@Composable
fun SusPathsContent(
    susPaths: Set<String>,
    isLoading: Boolean,
    onAddPath: () -> Unit,
    onAddAppPath: () -> Unit,
    onRemovePath: (String) -> Unit,
    onEditPath: ((String) -> Unit)? = null,
    forceRefreshApps: Boolean = false,
    onReset: (() -> Unit)? = null
) {
    SusPathsContentMaterial(
            susPaths = susPaths,
            isLoading = isLoading,
            onAddPath = onAddPath,
            onAddAppPath = onAddAppPath,
            onRemovePath = onRemovePath,
            onEditPath = { onEditPath?.invoke(it) },
            onReset = { onReset?.invoke() }
        )
}
