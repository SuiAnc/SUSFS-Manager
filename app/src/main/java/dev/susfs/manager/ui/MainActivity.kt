package dev.susfs.manager.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import dev.susfs.manager.ui.navigation3.LocalNavigator
import dev.susfs.manager.ui.navigation3.Navigator
import dev.susfs.manager.ui.screen.SusfsScreen
import dev.susfs.manager.ui.theme.SusfsTheme

/**
 * The whole application: the SUSFS management screen, shown as the only destination.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val navigator = remember { object : Navigator { override fun pop() = finish() } }
            SusfsTheme {
                CompositionLocalProvider(LocalNavigator provides navigator) {
                    SusfsScreen()
                }
            }
        }
    }
}
