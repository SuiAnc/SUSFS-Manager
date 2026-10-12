package dev.susfs.manager.ui.screen.sukisu.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.susfs.manager.ui.screen.sukisu.component.material.FeatureStatusCardMaterial
import dev.susfs.manager.ui.screen.sukisu.util.EnabledFeature

@Composable
fun FeatureStatusCard(
    feature: EnabledFeature,
    onRefresh: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    FeatureStatusCardMaterial(
            feature = feature,
            onRefresh = onRefresh,
            modifier = modifier
        )
}
