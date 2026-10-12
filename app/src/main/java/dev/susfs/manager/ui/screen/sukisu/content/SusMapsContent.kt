package dev.susfs.manager.ui.screen.sukisu.content

import androidx.compose.runtime.Composable
import dev.susfs.manager.ui.screen.sukisu.content.material.SusMapsContentMaterial

@Composable
fun SusMapsContent(
    susMaps: Set<String>,
    isLoading: Boolean,
    onAddSusMap: () -> Unit,
    onRemoveSusMap: (String) -> Unit,
    onEditSusMap: ((String) -> Unit)? = null,
    onReset: (() -> Unit)? = null
) {
    SusMapsContentMaterial(
            susMaps = susMaps,
            isLoading = isLoading,
            onAddSusMap = onAddSusMap,
            onRemoveSusMap = onRemoveSusMap,
            onEditSusMap = { onEditSusMap?.invoke(it) },
            onReset = { onReset?.invoke() }
        )
}
