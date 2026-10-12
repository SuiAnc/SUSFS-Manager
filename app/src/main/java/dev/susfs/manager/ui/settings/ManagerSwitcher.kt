package dev.susfs.manager.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import dev.susfs.manager.R
import dev.susfs.manager.data.settings.ManagerMode

/** Display name of a mode, from resources. */
@Composable
fun managerModeLabel(mode: ManagerMode): String = stringResource(
    when (mode) {
        ManagerMode.FolkSU -> R.string.manager_mode_folksu
        ManagerMode.SukiSU -> R.string.manager_mode_sukisu
    }
)

/**
 * The only "settings" this app has: which manager's SUSFS page to show.
 *
 * Rendered as a small pill so it can float above either manager's screen without
 * editing either of them. Tapping it opens the chooser.
 */
@Composable
fun ManagerSwitcher(
    mode: ManagerMode,
    onModeChange: (ManagerMode) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showDialog by remember { mutableStateOf(false) }

    Surface(
        modifier = modifier.clickable { showDialog = true },
        shape = RoundedCornerShape(50),
        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.94f),
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        tonalElevation = 3.dp,
        shadowElevation = 3.dp,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Rounded.Settings,
                contentDescription = null,
                modifier = Modifier.size(15.dp),
            )
            Spacer(Modifier.width(7.dp))
            Text(
                text = "${stringResource(R.string.manager_mode_label)} · ${managerModeLabel(mode)}",
                style = MaterialTheme.typography.labelMedium,
            )
        }
    }

    if (showDialog) {
        ManagerModeDialog(
            current = mode,
            onDismiss = { showDialog = false },
            onPick = {
                showDialog = false
                if (it != mode) onModeChange(it)
            },
        )
    }
}

@Composable
private fun ManagerModeDialog(
    current: ManagerMode,
    onDismiss: () -> Unit,
    onPick: (ManagerMode) -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.manager_mode_title)) },
        text = {
            androidx.compose.foundation.layout.Column {
                ManagerMode.entries.forEach { mode ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = mode == current,
                                role = Role.RadioButton,
                                onClick = { onPick(mode) },
                            )
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(selected = mode == current, onClick = null)
                        Spacer(Modifier.width(12.dp))
                        androidx.compose.foundation.layout.Column(
                            verticalArrangement = Arrangement.spacedBy(2.dp),
                        ) {
                            Text(
                                text = managerModeLabel(mode),
                                style = MaterialTheme.typography.titleSmall,
                            )
                            Text(
                                text = stringResource(
                                    when (mode) {
                                        ManagerMode.FolkSU -> R.string.manager_mode_folksu_desc
                                        ManagerMode.SukiSU -> R.string.manager_mode_sukisu_desc
                                    }
                                ),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
                Spacer(Modifier.size(8.dp))
                Text(
                    text = stringResource(R.string.manager_mode_note),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.susfs_cancel)) }
        },
    )
}
