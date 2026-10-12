package dev.susfs.manager.ui.screen.sukisu.content

import androidx.compose.runtime.Composable
import dev.susfs.manager.ui.screen.sukisu.content.material.KstatConfigContentMaterial

@Composable
fun KstatConfigContent(
    kstatConfigs: Set<String>,
    addKstatPaths: Set<String>,
    isLoading: Boolean,
    onAddKstatStatically: () -> Unit,
    onAddKstat: () -> Unit,
    onRemoveKstatConfig: (String) -> Unit,
    onEditKstatConfig: ((String) -> Unit)? = null,
    onRemoveAddKstat: (String) -> Unit,
    onEditAddKstat: ((String) -> Unit)? = null,
    onUpdateKstat: (String) -> Unit,
    onUpdateKstatFullClone: (String) -> Unit
) {
    KstatConfigContentMaterial(
            kstatConfigs = kstatConfigs,
            addKstatPaths = addKstatPaths,
            isLoading = isLoading,
            onAddKstatStatically = onAddKstatStatically,
            onAddKstat = onAddKstat,
            onRemoveKstatConfig = onRemoveKstatConfig,
            onEditKstatConfig = { onEditKstatConfig?.invoke(it) },
            onRemoveAddKstat = onRemoveAddKstat,
            onEditAddKstat = { onEditAddKstat?.invoke(it) },
            onUpdateKstat = onUpdateKstat,
            onUpdateKstatFullClone = onUpdateKstatFullClone
        )
}
