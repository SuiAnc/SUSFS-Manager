package dev.susfs.manager.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import dev.susfs.manager.data.settings.ManagerMode
import dev.susfs.manager.data.settings.ManagerSettings
import dev.susfs.manager.ui.navigation3.LocalNavigator
import dev.susfs.manager.ui.navigation3.Navigator
import dev.susfs.manager.ui.screen.SusfsScreen
import dev.susfs.manager.ui.screen.sukisu.SukiSuScreen
import dev.susfs.manager.ui.settings.ManagerSwitcher
import dev.susfs.manager.ui.theme.SusfsTheme

/**
 * The whole application: one SUSFS management page at a time.
 *
 * Which one is decided by the persisted [ManagerMode] — FolkSU's page (driving
 * `libksud.so`) or SukiSU-Ultra's page (driving `libksud2.so`). Both payloads
 * ship in this APK, so switching does not require reinstalling.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val context = LocalContext.current
            var mode by remember { mutableStateOf(ManagerSettings.get(context)) }
            val navigator = remember { object : Navigator { override fun pop() = finish() } }
            SusfsTheme {
                CompositionLocalProvider(LocalNavigator provides navigator) {
                    Box(Modifier.fillMaxSize()) {
                        when (mode) {
                            ManagerMode.FolkSU -> SusfsScreen()
                            ManagerMode.SukiSU -> SukiSuScreen()
                        }
                        ManagerSwitcher(
                            mode = mode,
                            onModeChange = {
                                ManagerSettings.set(context, it)
                                mode = it
                            },
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = 88.dp),
                        )
                    }
                }
            }
        }
    }
}
