package dev.susfs.manager.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

/**
 * Material 3 theme for the standalone manager.
 *
 * Upstream this came from a user-configurable palette- and wallpaper-driven theme. The SUSFS screen
 * reads the wallpaper tokens composition local directly, and every wallpaper branch short-circuits
 * to plain Material colours when that local is null, so the port supplies a dynamic-colour scheme
 * and leaves the local unset.
 */
@Composable
fun SusfsTheme(content: @Composable () -> Unit) {
    val dark = isSystemInDarkTheme()
    val context = LocalContext.current
    val colorScheme = when {
        android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S ->
            if (dark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)

        dark -> darkColorScheme()
        else -> lightColorScheme()
    }
    MaterialTheme(colorScheme = colorScheme, shapes = FolkShape.materialShapes, content = content)
}
