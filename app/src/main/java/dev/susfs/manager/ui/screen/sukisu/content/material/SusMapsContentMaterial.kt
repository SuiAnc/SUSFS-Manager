package dev.susfs.manager.ui.screen.sukisu.content.material

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import dev.susfs.manager.R
import dev.susfs.manager.ui.screen.sukisu.component.material.BottomActionButtonsMaterial
import dev.susfs.manager.ui.screen.sukisu.component.material.DescriptionCardMaterial
import dev.susfs.manager.ui.screen.sukisu.component.material.EmptyStateCardMaterial
import dev.susfs.manager.ui.screen.sukisu.component.material.PathItemCardMaterial
import dev.susfs.manager.ui.screen.sukisu.component.material.ResetButtonMaterial
import dev.susfs.manager.ui.screen.sukisu.component.material.SectionHeaderMaterial

@Composable
fun SusMapsContentMaterial(
    susMaps: Set<String>,
    isLoading: Boolean,
    onAddSusMap: () -> Unit,
    onRemoveSusMap: (String) -> Unit,
    onEditSusMap: (String) -> Unit,
    onReset: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        DescriptionCardMaterial(
            title = stringResource(R.string.sus_maps_description_title),
            description = stringResource(R.string.sus_maps_description_text),
            warning = stringResource(R.string.sus_maps_warning),
            additionalInfo = stringResource(R.string.sus_maps_debug_info)
        )

        if (susMaps.isEmpty()) {
            EmptyStateCardMaterial(message = stringResource(R.string.susfs_no_sus_maps_configured))
        } else {
            SectionHeaderMaterial(
                title = stringResource(R.string.sus_maps_section),
                subtitle = null,
                icon = Icons.Default.Security,
                count = susMaps.size
            )
            susMaps.toList().forEach { map ->
                PathItemCardMaterial(
                    path = map,
                    icon = Icons.Default.Security,
                    onDelete = { onRemoveSusMap(map) },
                    onEdit = { onEditSusMap(map) },
                    isLoading = isLoading
                )
            }
        }
    }

    BottomActionButtonsMaterial(
        primaryButtonText = stringResource(R.string.add),
        onPrimaryClick = onAddSusMap,
        isLoading = isLoading
    )

    if (susMaps.isNotEmpty()) {
        ResetButtonMaterial(
            title = stringResource(R.string.susfs_reset_sus_maps_title),
            onClick = onReset
        )
    }
}
