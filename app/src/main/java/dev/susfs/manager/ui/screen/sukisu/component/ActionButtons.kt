package dev.susfs.manager.ui.screen.sukisu.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.susfs.manager.ui.screen.sukisu.component.material.BottomActionButtonsMaterial

@Composable
fun BottomActionButtons(
    modifier: Modifier = Modifier,
    primaryButtonText: String,
    onPrimaryClick: () -> Unit,
    secondaryButtonText: String? = null,
    onSecondaryClick: (() -> Unit)? = null,
    isLoading: Boolean = false,
    enabled: Boolean = true
) {
    BottomActionButtonsMaterial(
            modifier = modifier,
            primaryButtonText = primaryButtonText,
            onPrimaryClick = onPrimaryClick,
            secondaryButtonText = secondaryButtonText,
            onSecondaryClick = onSecondaryClick,
            isLoading = isLoading,
            enabled = enabled
        )
}
