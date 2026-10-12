package dev.susfs.manager.ui.screen.sukisu.content

import androidx.compose.runtime.Composable
import dev.susfs.manager.ui.screen.sukisu.content.material.BasicSettingsContentMaterial

@Composable
fun BasicSettingsContent(
    unameValue: String,
    onUnameValueChange: (String) -> Unit,
    buildTimeValue: String,
    onBuildTimeValueChange: (String) -> Unit,
    executeInPostFsData: Boolean,
    onExecuteInPostFsDataChange: (Boolean) -> Unit,
    autoStartEnabled: Boolean,
    canEnableAutoStart: Boolean,
    isLoading: Boolean,
    onAutoStartToggle: (Boolean) -> Unit,
    onShowSlotInfo: () -> Unit,
    enableHideBl: Boolean,
    onEnableHideBlChange: (Boolean) -> Unit,
    enableCleanupResidue: Boolean,
    onEnableCleanupResidueChange: (Boolean) -> Unit,
    enableAvcLogSpoofing: Boolean,
    onEnableAvcLogSpoofingChange: (Boolean) -> Unit,
    hideSusMountsForAllProcs: Boolean,
    onHideSusMountsForAllProcsChange: (Boolean) -> Unit,
    cmdlineOrBootconfigPath: String = "",
    onCmdlineOrBootconfigApply: (String) -> Unit = {},
    onReset: (() -> Unit)? = null,
    onApply: (() -> Unit)? = null,
    onConfigReload: () -> Unit
) {
    BasicSettingsContentMaterial(
            unameValue = unameValue,
            onUnameValueChange = onUnameValueChange,
            buildTimeValue = buildTimeValue,
            onBuildTimeValueChange = onBuildTimeValueChange,
            executeInPostFsData = executeInPostFsData,
            onExecuteInPostFsDataChange = onExecuteInPostFsDataChange,
            autoStartEnabled = autoStartEnabled,
            canEnableAutoStart = canEnableAutoStart,
            isLoading = isLoading,
            onAutoStartToggle = onAutoStartToggle,
            onShowSlotInfo = onShowSlotInfo,
            enableHideBl = enableHideBl,
            onEnableHideBlChange = onEnableHideBlChange,
            enableCleanupResidue = enableCleanupResidue,
            onEnableCleanupResidueChange = onEnableCleanupResidueChange,
            enableAvcLogSpoofing = enableAvcLogSpoofing,
            onEnableAvcLogSpoofingChange = onEnableAvcLogSpoofingChange,
            hideSusMountsForAllProcs = hideSusMountsForAllProcs,
            onHideSusMountsForAllProcsChange = onHideSusMountsForAllProcsChange,
            cmdlineOrBootconfigPath = cmdlineOrBootconfigPath,
            onCmdlineOrBootconfigApply = onCmdlineOrBootconfigApply,
            onReset = onReset,
            onApply = onApply,
            onConfigReload = onConfigReload
        )
}
