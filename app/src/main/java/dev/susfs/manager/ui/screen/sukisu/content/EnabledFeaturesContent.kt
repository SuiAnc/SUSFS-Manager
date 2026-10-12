package dev.susfs.manager.ui.screen.sukisu.content

import androidx.compose.runtime.Composable
import dev.susfs.manager.ui.screen.sukisu.content.material.EnabledFeaturesContentMaterial
import dev.susfs.manager.ui.screen.sukisu.util.EnabledFeature

@Composable
fun EnabledFeaturesContent(
    enabledFeatures: List<EnabledFeature>,
    onRefresh: () -> Unit
) {
    EnabledFeaturesContentMaterial(
            enabledFeatures = enabledFeatures,
            onRefresh = onRefresh
        )
}
