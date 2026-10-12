package dev.susfs.manager.ui.screen.sukisu.repository

import android.content.Context
import dev.susfs.manager.ui.screen.sukisu.util.AppInfo
import dev.susfs.manager.ui.screen.sukisu.util.EnabledFeature
import dev.susfs.manager.ui.screen.sukisu.util.ModuleConfig
import dev.susfs.manager.ui.screen.sukisu.util.SlotInfo
import dev.susfs.manager.ui.screen.sukisu.util.SuSFSManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SuSFSRepositoryImpl : SuSFSRepositoryInterface {

    override suspend fun loadInitialConfig(context: Context): Result<ModuleConfig> =
        withContext(Dispatchers.IO) {
            runCatching { SuSFSManager.getCurrentModuleConfig() }
        }

    override suspend fun getEnabledFeatures(context: Context): Result<List<EnabledFeature>> =
        withContext(Dispatchers.IO) {
            runCatching { SuSFSManager.getEnabledFeatures(context) }
        }

    override suspend fun getInstalledApps(): Result<List<AppInfo>> =
        withContext(Dispatchers.IO) {
            runCatching { SuSFSManager.getInstalledApps() }
        }

    override suspend fun getSlotInfo(
        context: Context
    ): Result<Pair<List<SlotInfo>, String>> = withContext(Dispatchers.IO) {
        runCatching {
            val slotInfo = SuSFSManager.getCurrentSlotInfo()
            val currentActive = SuSFSManager.getCurrentActiveSlot()
            slotInfo to currentActive
        }
    }
}
