package dev.susfs.manager.ui.screen.sukisu.repository

import android.content.Context
import dev.susfs.manager.ui.screen.sukisu.util.AppInfo
import dev.susfs.manager.ui.screen.sukisu.util.EnabledFeature
import dev.susfs.manager.ui.screen.sukisu.util.ModuleConfig
import dev.susfs.manager.ui.screen.sukisu.util.SlotInfo

interface SuSFSRepositoryInterface {
    suspend fun loadInitialConfig(context: Context): Result<ModuleConfig>
    suspend fun getEnabledFeatures(context: Context): Result<List<EnabledFeature>>
    suspend fun getInstalledApps(): Result<List<AppInfo>>
    suspend fun getSlotInfo(context: Context): Result<Pair<List<SlotInfo>, String>>
}
