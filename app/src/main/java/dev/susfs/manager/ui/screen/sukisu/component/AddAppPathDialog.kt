package dev.susfs.manager.ui.screen.sukisu.component

import androidx.compose.runtime.Composable
import dev.susfs.manager.ui.screen.sukisu.component.material.AddAppPathDialogMaterial
import dev.susfs.manager.ui.screen.sukisu.util.AppInfo

@Composable
fun AddAppPathDialog(
    showDialog: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (List<String>) -> Unit,
    isLoading: Boolean,
    apps: List<AppInfo> = emptyList(),
    onLoadApps: () -> Unit,
    existingSusPaths: Set<String> = emptySet()
) {
    AddAppPathDialogMaterial(
            showDialog = showDialog,
            onDismiss = onDismiss,
            onConfirm = onConfirm,
            isLoading = isLoading,
            apps = apps,
            onLoadApps = onLoadApps,
            existingSusPaths = existingSusPaths
        )
}
